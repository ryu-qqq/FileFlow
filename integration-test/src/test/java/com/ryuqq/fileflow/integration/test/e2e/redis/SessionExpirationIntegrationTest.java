package com.ryuqq.fileflow.integration.test.e2e.redis;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.MultipartUploadSessionJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.SingleUploadSessionJpaRepository;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.vo.MultipartSessionStatus;
import com.ryuqq.fileflow.domain.session.vo.SingleSessionStatus;
import com.ryuqq.fileflow.integration.test.common.base.IntegrationTestBase;
import com.ryuqq.fileflow.integration.test.common.container.TestContainerConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;

/**
 * 세션 만료 통합 테스트.
 *
 * <p>Redis keyspace notification → Consumer → Distributed Lock → DB 상태 변경 전체 흐름을 검증합니다.
 *
 * <p>TestContainers Redis에 keyspace notification이 활성화되어 있어야 합니다. ({@code --notify-keyspace-events
 * Ex})
 */
@DisplayName("세션 만료 통합 테스트 (Redis Keyspace Notification + Distributed Lock)")
class SessionExpirationIntegrationTest extends IntegrationTestBase {

    @Autowired private SingleUploadSessionJpaRepository singleUploadSessionJpaRepository;

    @Autowired private MultipartUploadSessionJpaRepository multipartUploadSessionJpaRepository;

    @Autowired private StringRedisTemplate stringRedisTemplate;

    @Autowired private S3Client s3Client;

    @BeforeEach
    void setUp() {
        singleUploadSessionJpaRepository.deleteAllInBatch();
        multipartUploadSessionJpaRepository.deleteAllInBatch();
    }

    private String newSessionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * LocalStack S3에 실제 멀티파트 업로드를 생성하여 유효한 uploadId를 반환합니다.
     *
     * <p>멀티파트 세션 만료 시 S3 abortMultipartUpload가 호출되므로, 실제 S3에 멀티파트 업로드가 존재해야 합니다.
     */
    private String createRealMultipartUpload(String s3Key) {
        return s3Client.createMultipartUpload(
                        CreateMultipartUploadRequest.builder()
                                .bucket(TestContainerConfig.BUCKET_NAME)
                                .key(s3Key)
                                .contentType("image/jpeg")
                                .build())
                .uploadId();
    }

    @Nested
    @DisplayName("단건 업로드 세션 만료")
    class SingleSessionExpirationTest {

        @Test
        @DisplayName("CREATED 상태 세션이 Redis TTL 만료 후 EXPIRED로 변경된다")
        void shouldExpireCreatedSessionViaRedisKeyspaceNotification() {
            // given: CREATED 상태 세션 DB 저장
            Instant now = Instant.now();
            String sessionId = newSessionId();

            singleUploadSessionJpaRepository.save(
                    SingleUploadSessionJpaEntity.create(
                            sessionId,
                            "public/2026/01/test-expire.jpg",
                            "fileflow-test-bucket",
                            AccessType.PUBLIC,
                            "test-expire.jpg",
                            "image/jpeg",
                            "https://s3.presigned-url.com/test",
                            "product-image",
                            "test-service",
                            SingleSessionStatus.CREATED,
                            now.plus(Duration.ofHours(1)),
                            now,
                            now));

            // when: Redis에 짧은 TTL로 만료 키 등록 (2초 후 만료)
            String redisKey = "session:expiration:SINGLE:" + sessionId;
            stringRedisTemplate.opsForValue().set(redisKey, "SINGLE", Duration.ofSeconds(2));

            // then: TTL 만료 후 keyspace notification → Consumer → DB 상태 변경 확인
            Awaitility.await()
                    .atMost(Duration.ofSeconds(15))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(
                            () -> {
                                var updated =
                                        singleUploadSessionJpaRepository
                                                .findById(sessionId)
                                                .orElseThrow();
                                assertThat(updated.getStatus())
                                        .isEqualTo(SingleSessionStatus.EXPIRED);
                            });
        }

        @Test
        @DisplayName("이미 EXPIRED 상태인 세션의 만료 이벤트는 에러 없이 처리된다")
        void shouldHandleAlreadyExpiredSessionGracefully() {
            // given: EXPIRED 상태 세션 DB 저장
            Instant now = Instant.now();
            String sessionId = newSessionId();

            singleUploadSessionJpaRepository.save(
                    SingleUploadSessionJpaEntity.create(
                            sessionId,
                            "public/2026/01/already-expired.jpg",
                            "fileflow-test-bucket",
                            AccessType.PUBLIC,
                            "already-expired.jpg",
                            "image/jpeg",
                            "https://s3.presigned-url.com/expired",
                            "product-image",
                            "test-service",
                            SingleSessionStatus.EXPIRED,
                            now.minus(Duration.ofHours(1)),
                            now.minus(Duration.ofHours(2)),
                            now.minus(Duration.ofHours(1))));

            // when: Redis 만료 이벤트 발생
            String redisKey = "session:expiration:SINGLE:" + sessionId;
            stringRedisTemplate.opsForValue().set(redisKey, "SINGLE", Duration.ofSeconds(2));

            // then: 에러 없이 처리되고, 상태는 여전히 EXPIRED
            Awaitility.await()
                    .during(Duration.ofSeconds(5))
                    .atMost(Duration.ofSeconds(10))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(
                            () -> {
                                var updated =
                                        singleUploadSessionJpaRepository
                                                .findById(sessionId)
                                                .orElseThrow();
                                assertThat(updated.getStatus())
                                        .isEqualTo(SingleSessionStatus.EXPIRED);
                            });
        }

        @Test
        @DisplayName("관련 없는 Redis 키 만료는 무시된다")
        void shouldIgnoreUnrelatedKeyExpiration() {
            // given: CREATED 상태 세션 DB 저장
            Instant now = Instant.now();
            String sessionId = newSessionId();

            singleUploadSessionJpaRepository.save(
                    SingleUploadSessionJpaEntity.create(
                            sessionId,
                            "public/2026/01/ignore-test.jpg",
                            "fileflow-test-bucket",
                            AccessType.PUBLIC,
                            "ignore-test.jpg",
                            "image/jpeg",
                            "https://s3.presigned-url.com/ignore",
                            "product-image",
                            "test-service",
                            SingleSessionStatus.CREATED,
                            now.plus(Duration.ofHours(1)),
                            now,
                            now));

            // when: 관련 없는 Redis 키가 만료됨
            stringRedisTemplate
                    .opsForValue()
                    .set("unrelated:key:" + sessionId, "value", Duration.ofSeconds(2));

            // then: 세션 상태는 CREATED 유지
            Awaitility.await()
                    .during(Duration.ofSeconds(5))
                    .atMost(Duration.ofSeconds(10))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(
                            () -> {
                                var entity =
                                        singleUploadSessionJpaRepository
                                                .findById(sessionId)
                                                .orElseThrow();
                                assertThat(entity.getStatus())
                                        .isEqualTo(SingleSessionStatus.CREATED);
                            });
        }
    }

    @Nested
    @DisplayName("멀티파트 업로드 세션 만료")
    class MultipartSessionExpirationTest {

        @Test
        @DisplayName("INITIATED 상태 세션이 Redis TTL 만료 후 EXPIRED로 변경된다")
        void shouldExpireInitiatedSessionViaRedisKeyspaceNotification() {
            // given: INITIATED 상태 세션 DB 저장 + S3에 실제 멀티파트 업로드 생성
            Instant now = Instant.now();
            String sessionId = newSessionId();
            String s3Key = "public/2026/01/test-multipart-" + sessionId + ".jpg";
            String realUploadId = createRealMultipartUpload(s3Key);

            multipartUploadSessionJpaRepository.save(
                    MultipartUploadSessionJpaEntity.create(
                            sessionId,
                            s3Key,
                            TestContainerConfig.BUCKET_NAME,
                            AccessType.PUBLIC,
                            "test-multipart.jpg",
                            "image/jpeg",
                            realUploadId,
                            5_242_880L,
                            "product-image",
                            "test-service",
                            MultipartSessionStatus.INITIATED,
                            now.plus(Duration.ofHours(1)),
                            now,
                            now));

            // when: Redis에 짧은 TTL로 만료 키 등록
            String redisKey = "session:expiration:MULTIPART:" + sessionId;
            stringRedisTemplate.opsForValue().set(redisKey, "MULTIPART", Duration.ofSeconds(2));

            // then: TTL 만료 후 EXPIRED 상태로 변경
            Awaitility.await()
                    .atMost(Duration.ofSeconds(15))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(
                            () -> {
                                var updated =
                                        multipartUploadSessionJpaRepository
                                                .findById(sessionId)
                                                .orElseThrow();
                                assertThat(updated.getStatus())
                                        .isEqualTo(MultipartSessionStatus.EXPIRED);
                            });
        }

        @Test
        @DisplayName("UPLOADING 상태 세션이 Redis TTL 만료 후 EXPIRED로 변경된다")
        void shouldExpireUploadingSessionViaRedisKeyspaceNotification() {
            // given: UPLOADING 상태 세션 DB 저장 + S3에 실제 멀티파트 업로드 생성
            Instant now = Instant.now();
            String sessionId = newSessionId();
            String s3Key = "public/2026/01/test-uploading-" + sessionId + ".jpg";
            String realUploadId = createRealMultipartUpload(s3Key);

            multipartUploadSessionJpaRepository.save(
                    MultipartUploadSessionJpaEntity.create(
                            sessionId,
                            s3Key,
                            TestContainerConfig.BUCKET_NAME,
                            AccessType.PUBLIC,
                            "test-uploading.jpg",
                            "image/jpeg",
                            realUploadId,
                            5_242_880L,
                            "product-image",
                            "test-service",
                            MultipartSessionStatus.UPLOADING,
                            now.plus(Duration.ofHours(1)),
                            now,
                            now));

            // when: Redis에 짧은 TTL로 만료 키 등록
            String redisKey = "session:expiration:MULTIPART:" + sessionId;
            stringRedisTemplate.opsForValue().set(redisKey, "MULTIPART", Duration.ofSeconds(2));

            // then: TTL 만료 후 EXPIRED 상태로 변경
            Awaitility.await()
                    .atMost(Duration.ofSeconds(15))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(
                            () -> {
                                var updated =
                                        multipartUploadSessionJpaRepository
                                                .findById(sessionId)
                                                .orElseThrow();
                                assertThat(updated.getStatus())
                                        .isEqualTo(MultipartSessionStatus.EXPIRED);
                            });
        }
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.redis.session.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.redis.common.CacheTestSupport;
import com.ryuqq.fileflow.adapter.out.persistence.redis.common.UploadSessionTestFixture;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * UploadSessionCacheAdapter 통합 테스트
 *
 * <p>Redis를 사용한 업로드 세션 캐시 기능을 검증합니다.
 */
@DisplayName("UploadSessionCacheAdapter 통합 테스트")
class UploadSessionCacheAdapterTest extends CacheTestSupport {

    private static final String SINGLE_UPLOAD_KEY_PREFIX = "cache::single-upload::";
    private static final String MULTIPART_UPLOAD_KEY_PREFIX = "cache::multipart-upload::";

    @Autowired private UploadSessionCacheAdapter cacheAdapter;

    @Nested
    @DisplayName("persist(SingleUploadSession) 메서드")
    class PersistSingleUploadSession {

        @Test
        @DisplayName("성공 - 단일 업로드 세션 캐시 저장")
        void persist_singleUploadSession_success() {
            // Given
            SingleUploadSession session =
                    UploadSessionTestFixture.createSingleUploadSession("single-1");
            Duration ttl = Duration.ofMinutes(15);

            // When
            cacheAdapter.persist(session, ttl);

            // Then
            String expectedKey = SINGLE_UPLOAD_KEY_PREFIX + session.getIdValue();
            assertCacheExists(expectedKey);
            assertTtlSet(expectedKey, 900, 10); // 15분 TTL
        }

        @Test
        @DisplayName("성공 - 다른 TTL로 세션 캐시 저장")
        void persist_singleUploadSession_customTtl_success() {
            // Given
            SingleUploadSession session =
                    UploadSessionTestFixture.createSingleUploadSession("single-2");
            Duration ttl = Duration.ofMinutes(30);

            // When
            cacheAdapter.persist(session, ttl);

            // Then
            String expectedKey = SINGLE_UPLOAD_KEY_PREFIX + session.getIdValue();
            assertCacheExists(expectedKey);
            assertTtlSet(expectedKey, 1800, 10); // 30분 TTL
        }

        @Test
        @DisplayName("성공 - 동일 키로 덮어쓰기")
        void persist_singleUploadSession_overwrite_success() {
            // Given
            SingleUploadSession session1 =
                    UploadSessionTestFixture.createSingleUploadSession("single-overwrite");
            cacheAdapter.persist(session1, Duration.ofMinutes(5));

            SingleUploadSession session2 =
                    UploadSessionTestFixture.createSingleUploadSession("single-overwrite-2");
            // 동일 키로 덮어쓰기 위해 직접 키 생성
            String key = SINGLE_UPLOAD_KEY_PREFIX + session1.getIdValue();

            // When - 직접 동일한 키에 저장 (덮어쓰기 테스트)
            setDirectly(key, session2);

            // Then
            assertCacheExists(key);
        }
    }

    @Nested
    @DisplayName("persist(MultipartUploadSession) 메서드")
    class PersistMultipartUploadSession {

        @Test
        @DisplayName("성공 - 멀티파트 업로드 세션 캐시 저장")
        void persist_multipartUploadSession_success() {
            // Given
            MultipartUploadSession session =
                    UploadSessionTestFixture.createMultipartUploadSession("multipart-1");
            Duration ttl = Duration.ofHours(24);

            // When
            cacheAdapter.persist(session, ttl);

            // Then
            String expectedKey = MULTIPART_UPLOAD_KEY_PREFIX + session.getId().value().toString();
            assertCacheExists(expectedKey);
            assertTtlSet(expectedKey, 86400, 60); // 24시간 TTL
        }

        @Test
        @DisplayName("성공 - 짧은 TTL로 멀티파트 세션 캐시 저장")
        void persist_multipartUploadSession_shortTtl_success() {
            // Given
            MultipartUploadSession session =
                    UploadSessionTestFixture.createMultipartUploadSession("multipart-2");
            Duration ttl = Duration.ofMinutes(5);

            // When
            cacheAdapter.persist(session, ttl);

            // Then
            String expectedKey = MULTIPART_UPLOAD_KEY_PREFIX + session.getId().value().toString();
            assertCacheExists(expectedKey);
            assertTtlSet(expectedKey, 300, 10); // 5분 TTL
        }
    }

    @Nested
    @DisplayName("deleteSingleUploadSession 메서드")
    class DeleteSingleUploadSession {

        @Test
        @DisplayName("성공 - 단일 업로드 세션 캐시 삭제")
        void delete_singleUploadSession_success() {
            // Given
            SingleUploadSession session =
                    UploadSessionTestFixture.createSingleUploadSession("delete-single");
            cacheAdapter.persist(session, Duration.ofMinutes(15));

            String key = SINGLE_UPLOAD_KEY_PREFIX + session.getIdValue();
            assertCacheExists(key);

            // When
            cacheAdapter.deleteSingleUploadSession(session.getId());

            // Then
            assertCacheNotExists(key);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 세션 삭제 시 예외 없음")
        void delete_nonExistingSession_noException() {
            // Given
            UploadSessionId nonExistingId = UploadSessionId.of(UUID.randomUUID());

            // When & Then - 예외 발생하지 않음
            cacheAdapter.deleteSingleUploadSession(nonExistingId);
        }
    }

    @Nested
    @DisplayName("deleteMultipartUploadSession 메서드")
    class DeleteMultipartUploadSession {

        @Test
        @DisplayName("성공 - 멀티파트 업로드 세션 캐시 삭제")
        void delete_multipartUploadSession_success() {
            // Given
            MultipartUploadSession session =
                    UploadSessionTestFixture.createMultipartUploadSession("delete-multipart");
            cacheAdapter.persist(session, Duration.ofHours(24));

            String key = MULTIPART_UPLOAD_KEY_PREFIX + session.getId().value().toString();
            assertCacheExists(key);

            // When
            cacheAdapter.deleteMultipartUploadSession(session.getId());

            // Then
            assertCacheNotExists(key);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 멀티파트 세션 삭제 시 예외 없음")
        void delete_nonExistingMultipartSession_noException() {
            // Given
            UploadSessionId nonExistingId = UploadSessionId.of(UUID.randomUUID());

            // When & Then - 예외 발생하지 않음
            cacheAdapter.deleteMultipartUploadSession(nonExistingId);
        }
    }

    @Nested
    @DisplayName("키 생성 규칙 검증")
    class KeyGenerationTests {

        @Test
        @DisplayName("단일 업로드 키 형식: cache::single-upload::{sessionId}")
        void singleUploadKey_format_correct() {
            // Given
            SingleUploadSession session =
                    UploadSessionTestFixture.createSingleUploadSession("key-format-single");
            cacheAdapter.persist(session, Duration.ofMinutes(5));

            // When
            String expectedKey = SINGLE_UPLOAD_KEY_PREFIX + session.getIdValue();

            // Then
            assertCacheExists(expectedKey);
            assertKeyHasPrefix(expectedKey, "cache::single-upload::");
        }

        @Test
        @DisplayName("멀티파트 업로드 키 형식: cache::multipart-upload::{sessionId}")
        void multipartUploadKey_format_correct() {
            // Given
            MultipartUploadSession session =
                    UploadSessionTestFixture.createMultipartUploadSession("key-format-multipart");
            cacheAdapter.persist(session, Duration.ofMinutes(5));

            // When
            String expectedKey = MULTIPART_UPLOAD_KEY_PREFIX + session.getId().value().toString();

            // Then
            assertCacheExists(expectedKey);
            assertKeyHasPrefix(expectedKey, "cache::multipart-upload::");
        }
    }

    @Nested
    @DisplayName("TTL 만료 검증")
    class TtlExpirationTests {

        @Test
        @DisplayName("성공 - 짧은 TTL 후 캐시 만료 확인")
        void shortTtl_expiration_success() throws InterruptedException {
            // Given
            SingleUploadSession session =
                    UploadSessionTestFixture.createSingleUploadSession("ttl-expire");
            Duration shortTtl = Duration.ofSeconds(2);

            // When
            cacheAdapter.persist(session, shortTtl);
            String key = SINGLE_UPLOAD_KEY_PREFIX + session.getIdValue();

            // Then - 즉시 존재 확인
            assertCacheExists(key);

            // 3초 대기 후 만료 확인
            assertCacheExpiredAfter(key, 3);
        }
    }
}

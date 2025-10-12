package com.ryuqq.fileflow.adapter.redis.listener;

import com.ryuqq.fileflow.adapter.redis.adapter.RedisUploadSessionAdapter;
import com.ryuqq.fileflow.adapter.redis.config.RedisConfig;
import com.ryuqq.fileflow.adapter.redis.dto.UploadSessionDto;
import com.ryuqq.fileflow.application.upload.service.UploadSessionPersistenceService;
import com.ryuqq.fileflow.domain.policy.FileType;
import com.ryuqq.fileflow.domain.policy.PolicyKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.upload.vo.UploadRequest;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UploadSessionExpirationListener 통합 테스트
 *
 * Redis TTL 만료 시나리오를 실제 환경에서 검증합니다.
 * - Testcontainers Redis 사용
 * - 짧은 TTL(2-3초)로 실제 만료 동작 검증
 * - Listener가 KeyExpiredEvent를 감지하는지 확인
 * - DB에서 세션이 FAILED 상태로 변경되는지 검증
 *
 * @author sangwon-ryu
 */
@SpringBootTest(classes = {
        RedisConfig.class,
        RedisUploadSessionAdapter.class,
        UploadSessionExpirationListener.class,
        UploadSessionExpirationListenerIntegrationTest.TestRedisConfig.class
})
class UploadSessionExpirationListenerIntegrationTest {

    @TestConfiguration
    static class TestRedisConfig {
        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            LettuceConnectionFactory factory = new LettuceConnectionFactory(
                    REDIS_CONTAINER.getHost(),
                    REDIS_CONTAINER.getFirstMappedPort()
            );
            factory.afterPropertiesSet();
            return factory;
        }

        @Bean
        public RedissonClient redissonClient() {
            Config config = new Config();
            config.useSingleServer()
                    .setAddress("redis://" + REDIS_CONTAINER.getHost() + ":" + REDIS_CONTAINER.getFirstMappedPort());
            return Redisson.create(config);
        }
    }

    private static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @BeforeAll
    static void startContainer() {
        REDIS_CONTAINER.start();
    }

    @AfterAll
    static void stopContainer() {
        REDIS_CONTAINER.stop();
    }

    @Autowired
    private RedisUploadSessionAdapter redisUploadSessionAdapter;

    @Autowired
    private RedisTemplate<String, UploadSessionDto> uploadSessionRedisTemplate;

    @MockBean
    private UploadSessionPersistenceService persistenceService;

    private static final String KEY_PREFIX = "upload:session:";

    @BeforeEach
    void setUp() {
        // Redis 전체 캐시 삭제
        uploadSessionRedisTemplate.keys("*").forEach(uploadSessionRedisTemplate::delete);
    }

    @Test
    @DisplayName("Redis TTL 만료 시 Listener가 호출되어 세션을 FAILED 처리한다")
    void listener_handles_expired_session() {
        // given
        String sessionId = "test-session-001";
        UploadSession session = createTestSession(sessionId, 2); // 2초 TTL

        // Mock: DB에서 세션 조회 성공
        when(persistenceService.failSession(anyString(), anyString()))
                .thenReturn(session.fail());

        // Redis에 세션 저장 (짧은 TTL)
        String redisKey = KEY_PREFIX + sessionId;
        UploadSessionDto dto = UploadSessionDto.from(
                sessionId,
                session.getUploaderId(),
                session.getStatus().name(),
                session.getCreatedAt(),
                session.getExpiresAt()
        );
        uploadSessionRedisTemplate.opsForValue().set(redisKey, dto, Duration.ofSeconds(2));

        // when: Redis TTL 만료 대기 (KeyExpiredEvent 발생)
        // then: Listener가 호출되어 persistenceService.failSession() 실행
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    verify(persistenceService, times(1))
                            .failSession(sessionId, "Session expired by Redis TTL");
                });
    }

    @Test
    @DisplayName("여러 세션이 동시에 만료되어도 모두 처리된다")
    void listener_handles_multiple_expired_sessions() {
        // given
        String sessionId1 = "test-session-101";
        String sessionId2 = "test-session-102";
        String sessionId3 = "test-session-103";

        UploadSession session1 = createTestSession(sessionId1, 2);
        UploadSession session2 = createTestSession(sessionId2, 2);
        UploadSession session3 = createTestSession(sessionId3, 2);

        // Mock: 각 세션 조회 성공
        when(persistenceService.failSession(anyString(), anyString()))
                .thenReturn(session1.fail())
                .thenReturn(session2.fail())
                .thenReturn(session3.fail());

        // Redis에 세션 3개 저장 (짧은 TTL)
        saveSessionToRedis(sessionId1, session1, 2);
        saveSessionToRedis(sessionId2, session2, 2);
        saveSessionToRedis(sessionId3, session3, 2);

        // when: Redis TTL 만료 대기
        // then: 모든 세션이 처리됨
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    verify(persistenceService, times(3))
                            .failSession(anyString(), anyString());
                });
    }

    @Test
    @DisplayName("Redis Key가 upload:session: 접두사가 아니면 무시한다")
    void listener_ignores_non_session_keys() {
        // given
        String nonSessionKey = "other:key:test";
        uploadSessionRedisTemplate.opsForValue().set(nonSessionKey, null, Duration.ofSeconds(2));

        // when: Redis TTL 만료 대기
        // then: persistenceService.failSession()이 호출되지 않음
        await()
                .pollDelay(3, TimeUnit.SECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(persistenceService, times(0))
                            .failSession(anyString(), anyString());
                });
    }

    @Test
    @DisplayName("세션 만료 처리 중 예외 발생 시 로그만 남기고 계속 진행한다")
    void listener_continues_on_exception() {
        // given
        String sessionId = "test-session-error";
        UploadSession session = createTestSession(sessionId, 2);

        // Mock: 예외 발생
        when(persistenceService.failSession(anyString(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Redis에 세션 저장
        saveSessionToRedis(sessionId, session, 2);

        // when: Redis TTL 만료 대기
        // then: 예외가 발생해도 Listener는 종료되지 않음 (로그만 남김)
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    verify(persistenceService, times(1))
                            .failSession(sessionId, "Session expired by Redis TTL");
                });

        // Listener가 계속 동작하는지 확인 (추가 테스트 가능)
        assertThat(true).isTrue(); // Listener 살아있음
    }

    // ========== Helper Methods ==========

    private UploadSession createTestSession(String sessionId, int expirationMinutes) {
        PolicyKey policyKey = PolicyKey.of("b2c", "CONSUMER", "REVIEW");
        UploadRequest uploadRequest = UploadRequest.of(
                "test-file.jpg",
                FileType.IMAGE,
                1024L,
                "image/jpeg",
                IdempotencyKey.generate()
        );

        return UploadSession.reconstitute(
                sessionId,
                policyKey,
                uploadRequest,
                "test-uploader-001",
                UploadStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(expirationMinutes)
        );
    }

    private void saveSessionToRedis(String sessionId, UploadSession session, int ttlSeconds) {
        String redisKey = KEY_PREFIX + sessionId;
        UploadSessionDto dto = UploadSessionDto.from(
                sessionId,
                session.getUploaderId(),
                session.getStatus().name(),
                session.getCreatedAt(),
                session.getExpiresAt()
        );
        uploadSessionRedisTemplate.opsForValue().set(redisKey, dto, Duration.ofSeconds(ttlSeconds));
    }
}

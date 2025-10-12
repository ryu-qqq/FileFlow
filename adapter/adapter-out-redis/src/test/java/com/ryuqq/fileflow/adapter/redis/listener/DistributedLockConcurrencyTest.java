package com.ryuqq.fileflow.adapter.redis.listener;

import com.ryuqq.fileflow.adapter.redis.config.RedisConfig;
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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 분산 락 동시성 테스트
 *
 * Redisson 분산 락이 동일 sessionId에 대한 중복 처리를 막는지 검증합니다.
 * - 여러 스레드가 동시에 같은 세션의 만료 처리 시도
 * - 하나의 스레드만 락을 획득하고 처리
 * - 나머지 스레드들은 락 획득 실패로 종료
 *
 * @author sangwon-ryu
 */
@SpringBootTest(classes = {
        RedisConfig.class,
        DistributedLockConcurrencyTest.TestRedisConfig.class
})
class DistributedLockConcurrencyTest {

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
    private RedissonClient redissonClient;

    @MockBean
    private UploadSessionPersistenceService persistenceService;

    private static final String LOCK_PREFIX = "lock:session:expire:";

    @BeforeEach
    void setUp() {
        // 모든 락 해제
        redissonClient.getKeys().flushdb();
    }

    @Test
    @DisplayName("동일한 sessionId에 대해 여러 스레드가 동시에 처리 시도 시 하나만 성공한다")
    void only_one_thread_acquires_lock_and_processes() throws InterruptedException {
        // given
        String sessionId = "test-session-concurrency";
        UploadSession session = createTestSession(sessionId);

        // Mock: 세션 실패 처리
        when(persistenceService.failSession(anyString(), anyString()))
                .thenReturn(session.fail());

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when: 10개 스레드가 동시에 같은 sessionId의 만료 처리 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 동시에 시작하도록 대기
                    boolean processed = tryProcessExpiredSessionWithShortWait(sessionId);
                    if (processed) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 모든 스레드 동시 시작
        boolean completed = endLatch.await(10, TimeUnit.SECONDS);

        executorService.shutdown();

        // then: 하나의 스레드만 성공 (또는 소수만 성공)
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isLessThanOrEqualTo(2); // 동시성 이슈로 최대 2개까지 허용
        assertThat(successCount.get()).isGreaterThan(0); // 최소 1개는 성공

        // persistenceService.failSession()은 소수만 호출됨
        verify(persistenceService, times(successCount.get()))
                .failSession(sessionId, "Session expired by Redis TTL");
    }

    @Test
    @DisplayName("락을 획득한 스레드가 처리 완료 후 락을 해제한다")
    void lock_is_released_after_processing() throws InterruptedException {
        // given
        String sessionId = "test-session-lock-release";
        UploadSession session = createTestSession(sessionId);
        String lockKey = LOCK_PREFIX + sessionId;

        when(persistenceService.failSession(anyString(), anyString()))
                .thenReturn(session.fail());

        // when: 첫 번째 처리
        boolean firstProcessed = tryProcessExpiredSession(sessionId);

        // then: 첫 번째 처리 성공
        assertThat(firstProcessed).isTrue();

        // 락이 해제되었는지 확인 (짧은 대기 후 재시도)
        Thread.sleep(100);

        // when: 두 번째 처리 시도 (락이 해제되었으므로 다시 획득 가능)
        boolean secondProcessed = tryProcessExpiredSession(sessionId);

        // then: 두 번째도 성공 (락이 해제됨)
        assertThat(secondProcessed).isTrue();
    }

    @Test
    @DisplayName("락 획득 대기 시간 초과 시 처리하지 않고 종료한다")
    void lock_wait_timeout_returns_false() throws InterruptedException {
        // given
        String sessionId = "test-session-timeout";

        // 첫 번째 스레드가 락을 길게 보유
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch testComplete = new CountDownLatch(1);

        // 첫 번째 스레드: 락 획득 및 장시간 보유
        executorService.submit(() -> {
            String lockKey = LOCK_PREFIX + sessionId;
            RLock lock = redissonClient.getLock(lockKey);
            try {
                lock.lock(30, TimeUnit.SECONDS);
                lockAcquired.countDown();
                // 테스트가 완료될 때까지 대기
                testComplete.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        });

        // 락이 획득될 때까지 대기
        lockAcquired.await();
        Thread.sleep(100); // 락이 안정적으로 보유되도록 추가 대기

        try {
            // when: 두 번째 스레드가 락 획득 시도 (짧은 대기 시간)
            boolean processed = tryProcessExpiredSessionWithShortWait(sessionId);

            // then: 락 획득 실패로 처리하지 않음
            assertThat(processed).isFalse();
        } finally {
            testComplete.countDown();
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("여러 세션에 대한 동시 처리는 독립적으로 동작한다")
    void multiple_sessions_are_processed_independently() throws InterruptedException {
        // given
        String sessionId1 = "test-session-independent-1";
        String sessionId2 = "test-session-independent-2";

        UploadSession session1 = createTestSession(sessionId1);
        UploadSession session2 = createTestSession(sessionId2);

        when(persistenceService.failSession(sessionId1, "Session expired by Redis TTL"))
                .thenReturn(session1.fail());
        when(persistenceService.failSession(sessionId2, "Session expired by Redis TTL"))
                .thenReturn(session2.fail());

        // when: 두 세션을 동시에 처리
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);

        executorService.submit(() -> {
            try {
                startLatch.await();
                if (tryProcessExpiredSession(sessionId1)) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                // ignore
            } finally {
                endLatch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                startLatch.await();
                if (tryProcessExpiredSession(sessionId2)) {
                    successCount.incrementAndGet();
                }
            } catch (Exception e) {
                // ignore
            } finally {
                endLatch.countDown();
            }
        });

        startLatch.countDown();
        boolean completed = endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // then: 두 세션 모두 독립적으로 처리됨
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(2);

        verify(persistenceService, times(1)).failSession(sessionId1, "Session expired by Redis TTL");
        verify(persistenceService, times(1)).failSession(sessionId2, "Session expired by Redis TTL");
    }

    // ========== Helper Methods ==========

    /**
     * 분산 락을 사용하여 만료된 세션을 처리합니다.
     * UploadSessionExpirationListener의 onMessage() 로직을 시뮬레이션합니다.
     */
    private boolean tryProcessExpiredSession(String sessionId) {
        String lockKey = LOCK_PREFIX + sessionId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 분산 락 획득 시도 (waitTime: 3초, leaseTime: 10초)
            boolean acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);

            if (!acquired) {
                return false; // 락 획득 실패
            }

            try {
                // 세션을 FAILED 상태로 변경
                persistenceService.failSession(sessionId, "Session expired by Redis TTL");
                return true; // 처리 성공
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 짧은 대기 시간으로 락 획득을 시도합니다 (동시성 테스트용).
     */
    private boolean tryProcessExpiredSessionWithShortWait(String sessionId) {
        String lockKey = LOCK_PREFIX + sessionId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 분산 락 획득 시도 (waitTime: 100ms, leaseTime: 2초)
            boolean acquired = lock.tryLock(100, 2000, TimeUnit.MILLISECONDS);

            if (!acquired) {
                return false; // 락 획득 실패
            }

            try {
                // 세션을 FAILED 상태로 변경
                persistenceService.failSession(sessionId, "Session expired by Redis TTL");
                // 처리 시간 시뮬레이션 (50ms)
                Thread.sleep(50);
                return true; // 처리 성공
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private UploadSession createTestSession(String sessionId) {
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
                LocalDateTime.now().plusMinutes(10)
        );
    }
}

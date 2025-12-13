package com.ryuqq.fileflow.adapter.out.persistence.redis.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.vo.LockKey;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Lock Adapter 테스트 지원 추상 클래스
 *
 * <p>분산락 테스트에 특화된 유틸리티를 제공합니다. 동시성 테스트, Lock 상태 검증 등의 기능을 포함합니다.
 *
 * <p>제공 기능:
 *
 * <ul>
 *   <li>RedissonClient 자동 주입
 *   <li>동시성 테스트 유틸리티
 *   <li>Lock 상태 검증 메서드
 *   <li>테스트 후 Lock 자동 해제
 * </ul>
 *
 * <h2>사용 예시:</h2>
 *
 * <pre>{@code
 * @DisplayName("DistributedLockAdapter 통합 테스트")
 * class DistributedLockAdapterTest extends LockTestSupport {
 *
 *     @Autowired
 *     private DistributedLockAdapter lockAdapter;
 *
 *     @Test
 *     @DisplayName("성공 - 동시에 하나만 Lock 획득")
 *     void concurrentLock() throws InterruptedException {
 *         // Given
 *         OrderLockKey lockKey = new OrderLockKey(100L);
 *
 *         // When
 *         int successCount = runConcurrently(10, () -> {
 *             if (lockAdapter.tryLock(lockKey, 5, 30, TimeUnit.SECONDS)) {
 *                 try {
 *                     Thread.sleep(100);
 *                     return true;
 *                 } finally {
 *                     lockAdapter.unlock(lockKey);
 *                 }
 *             }
 *             return false;
 *         });
 *
 *         // Then
 *         assertThat(successCount).isGreaterThanOrEqualTo(1);
 *     }
 * }
 * }</pre>
 *
 * @author Development Team
 * @since 1.0.0
 * @see RedisTestSupport 기본 Redis 테스트 지원
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class LockTestSupport {

    /**
     * Redis TestContainer
     *
     * <p>모든 테스트에서 공유되는 단일 컨테이너입니다.
     */
    @Container
    protected static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    /**
     * RedissonClient - 분산락용
     *
     * <p>Redisson 기반의 분산락 기능을 사용합니다.
     */
    @Autowired protected RedissonClient redissonClient;

    /**
     * TestContainers 동적 프로퍼티 설정
     *
     * @param registry 동적 프로퍼티 레지스트리
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    /**
     * 테스트 후 Redis 데이터 정리
     *
     * <p>모든 Lock을 해제하고 데이터를 삭제합니다.
     */
    @AfterEach
    void tearDown() {
        redissonClient.getKeys().flushdb();
    }

    /**
     * Lock이 획득되어 있는지 검증
     *
     * @param lockKey Lock 키
     */
    protected void assertLocked(LockKey lockKey) {
        RLock lock = redissonClient.getLock(lockKey.value());
        assertThat(lock.isLocked()).as("Lock should be held for key: %s", lockKey.value()).isTrue();
    }

    /**
     * Lock이 획득되어 있는지 검증 (String 키 버전)
     *
     * @param lockKey Lock 키 문자열
     */
    protected void assertLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        assertThat(lock.isLocked()).as("Lock should be held for key: %s", lockKey).isTrue();
    }

    /**
     * Lock이 해제되어 있는지 검증
     *
     * @param lockKey Lock 키
     */
    protected void assertUnlocked(LockKey lockKey) {
        RLock lock = redissonClient.getLock(lockKey.value());
        assertThat(lock.isLocked())
                .as("Lock should be released for key: %s", lockKey.value())
                .isFalse();
    }

    /**
     * Lock이 해제되어 있는지 검증 (String 키 버전)
     *
     * @param lockKey Lock 키 문자열
     */
    protected void assertUnlocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        assertThat(lock.isLocked()).as("Lock should be released for key: %s", lockKey).isFalse();
    }

    /**
     * 동시성 테스트 실행 유틸리티
     *
     * <p>여러 스레드를 동시에 시작하여 작업을 실행하고, 성공한 스레드 수를 반환합니다.
     *
     * <h3>사용 예시:</h3>
     *
     * <pre>{@code
     * int successCount = runConcurrently(10, () -> {
     *     if (lockAdapter.tryLock(key, 5, 30, TimeUnit.SECONDS)) {
     *         try {
     *             // 작업 수행
     *             return true;
     *         } finally {
     *             lockAdapter.unlock(key);
     *         }
     *     }
     *     return false;
     * });
     * }</pre>
     *
     * @param threadCount 동시 실행 스레드 수
     * @param task 각 스레드에서 실행할 작업 (true 반환 시 성공으로 카운트)
     * @return 성공한 스레드 수
     * @throws InterruptedException 대기 중 인터럽트 발생 시
     */
    protected int runConcurrently(int threadCount, Callable<Boolean> task)
            throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(
                    () -> {
                        try {
                            startLatch.await(); // 모든 스레드 동시 시작 대기
                            Boolean result = task.call();
                            if (Boolean.TRUE.equals(result)) {
                                successCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            // 예외 발생 시 실패로 처리
                        } finally {
                            endLatch.countDown();
                        }
                    });
        }

        startLatch.countDown(); // 모든 스레드 동시 시작
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        if (!completed) {
            throw new RuntimeException("Concurrent test timed out");
        }

        return successCount.get();
    }

    /**
     * 동시성 테스트 실행 (최대 동시 실행 수 추적)
     *
     * <p>동시에 실행된 최대 스레드 수를 추적합니다. 분산락이 제대로 동작하면 최대 동시 실행 수는 1이어야 합니다.
     *
     * @param threadCount 동시 실행 스레드 수
     * @param task 각 스레드에서 실행할 작업
     * @return ConcurrencyResult (성공 수, 최대 동시 실행 수)
     * @throws InterruptedException 대기 중 인터럽트 발생 시
     */
    protected ConcurrencyResult runConcurrentlyWithTracking(int threadCount, Callable<Boolean> task)
            throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger currentConcurrent = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(
                    () -> {
                        try {
                            startLatch.await();

                            // 동시 실행 수 추적 시작
                            int current = currentConcurrent.incrementAndGet();
                            maxConcurrent.updateAndGet(max -> Math.max(max, current));

                            Boolean result = task.call();

                            // 동시 실행 수 추적 종료
                            currentConcurrent.decrementAndGet();

                            if (Boolean.TRUE.equals(result)) {
                                successCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            currentConcurrent.decrementAndGet();
                        } finally {
                            endLatch.countDown();
                        }
                    });
        }

        startLatch.countDown();
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        if (!completed) {
            throw new RuntimeException("Concurrent test timed out");
        }

        return new ConcurrencyResult(successCount.get(), maxConcurrent.get());
    }

    /**
     * Lock 획득 직접 테스트 (Redisson 직접 사용)
     *
     * @param lockKey Lock 키
     * @param waitTime 대기 시간
     * @param leaseTime 유지 시간
     * @param unit 시간 단위
     * @return Lock 획득 성공 여부
     */
    protected boolean tryLockDirectly(
            String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Lock 해제 직접 테스트 (Redisson 직접 사용)
     *
     * @param lockKey Lock 키
     */
    protected void unlockDirectly(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 동시성 테스트 결과
     *
     * @param successCount 성공한 스레드 수
     * @param maxConcurrent 최대 동시 실행 수
     */
    public record ConcurrencyResult(int successCount, int maxConcurrent) {

        /**
         * 최대 동시 실행 수가 1인지 검증
         *
         * <p>분산락이 제대로 동작하면 동시에 실행되는 스레드는 1개뿐이어야 합니다.
         */
        public void assertSingleConcurrent() {
            assertThat(maxConcurrent)
                    .as("Max concurrent executions should be 1 (lock working correctly)")
                    .isEqualTo(1);
        }

        /**
         * 모든 스레드가 순차적으로 성공했는지 검증
         *
         * @param expectedCount 예상 성공 수
         */
        public void assertAllSucceeded(int expectedCount) {
            assertThat(successCount)
                    .as("All threads should succeed sequentially")
                    .isEqualTo(expectedCount);
        }
    }
}

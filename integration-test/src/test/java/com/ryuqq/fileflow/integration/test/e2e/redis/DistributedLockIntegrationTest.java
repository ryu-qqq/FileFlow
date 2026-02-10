package com.ryuqq.fileflow.integration.test.e2e.redis;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.domain.session.vo.SessionLockKey;
import com.ryuqq.fileflow.integration.test.common.base.IntegrationTestBase;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 분산 락 통합 테스트.
 *
 * <p>Redisson + TestContainers Redis 환경에서 분산 락의 획득, 해제, 동시성 제어를 검증합니다.
 */
@DisplayName("분산 락 통합 테스트 (Redisson + TestContainers Redis)")
class DistributedLockIntegrationTest extends IntegrationTestBase {

    @Autowired private DistributedLockPort distributedLockAdapter;

    @Nested
    @DisplayName("락 기본 동작")
    class BasicLockOperationTest {

        @Test
        @DisplayName("락 획득 및 해제가 정상 동작한다")
        void shouldAcquireAndReleaseLock() {
            // given
            SessionLockKey key = new SessionLockKey("basic-test-" + UUID.randomUUID());

            // when: 락 획득
            boolean acquired = distributedLockAdapter.tryLock(key, 0, 30, TimeUnit.SECONDS);

            // then: 락 상태 확인
            assertThat(acquired).isTrue();
            assertThat(distributedLockAdapter.isLocked(key)).isTrue();
            assertThat(distributedLockAdapter.isHeldByCurrentThread(key)).isTrue();

            // when: 락 해제
            distributedLockAdapter.unlock(key);

            // then: 락 해제 확인
            assertThat(distributedLockAdapter.isLocked(key)).isFalse();
        }

        @Test
        @DisplayName("동일 스레드에서 락을 재획득할 수 있다 (reentrant)")
        void shouldSupportReentrantLock() {
            // given
            SessionLockKey key = new SessionLockKey("reentrant-test-" + UUID.randomUUID());

            // when: 동일 스레드에서 두 번 락 획득
            boolean first = distributedLockAdapter.tryLock(key, 0, 30, TimeUnit.SECONDS);
            boolean second = distributedLockAdapter.tryLock(key, 0, 30, TimeUnit.SECONDS);

            // then: 둘 다 성공
            assertThat(first).isTrue();
            assertThat(second).isTrue();

            // cleanup: reentrant lock이므로 두 번 unlock
            distributedLockAdapter.unlock(key);
            distributedLockAdapter.unlock(key);
        }
    }

    @Nested
    @DisplayName("동시성 제어")
    class ConcurrencyControlTest {

        @Test
        @DisplayName("다른 스레드가 락을 보유 중이면 waitTime=0으로 즉시 실패한다")
        void shouldFailFastWhenLockHeldByAnotherThread() throws Exception {
            // given
            SessionLockKey key = new SessionLockKey("concurrent-test-" + UUID.randomUUID());
            CountDownLatch lockAcquired = new CountDownLatch(1);
            CountDownLatch testDone = new CountDownLatch(1);
            AtomicInteger otherResult = new AtomicInteger(-1); // -1: not set, 0: false, 1: true

            // when: 별도 스레드에서 락 획득 후 대기
            Thread holder =
                    new Thread(
                            () -> {
                                boolean acquired =
                                        distributedLockAdapter.tryLock(
                                                key, 0, 30, TimeUnit.SECONDS);
                                if (acquired) {
                                    lockAcquired.countDown();
                                    try {
                                        testDone.await(10, TimeUnit.SECONDS);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    } finally {
                                        distributedLockAdapter.unlock(key);
                                    }
                                }
                            });
            holder.start();

            // 락 획득 대기
            assertThat(lockAcquired.await(5, TimeUnit.SECONDS)).isTrue();

            // then: 현재 스레드에서 락 획득 시도 → 실패 (waitTime=0)
            boolean acquired = distributedLockAdapter.tryLock(key, 0, 30, TimeUnit.SECONDS);
            assertThat(acquired).isFalse();

            // cleanup
            testDone.countDown();
            holder.join(5000);
        }

        @Test
        @DisplayName("여러 스레드가 동시에 락을 시도하면 하나만 성공한다")
        void shouldAllowOnlyOneThreadToAcquireLock() throws Exception {
            // given
            SessionLockKey key = new SessionLockKey("multi-thread-" + UUID.randomUUID());
            int threadCount = 5;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);

            // when: 5개 스레드가 동시에 락 획득 시도 (waitTime=0)
            for (int i = 0; i < threadCount; i++) {
                new Thread(
                                () -> {
                                    try {
                                        startLatch.await();
                                        boolean acquired =
                                                distributedLockAdapter.tryLock(
                                                        key, 0, 10, TimeUnit.SECONDS);
                                        if (acquired) {
                                            successCount.incrementAndGet();
                                            // 잠시 대기 후 해제
                                            Thread.sleep(100);
                                            distributedLockAdapter.unlock(key);
                                        }
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    } finally {
                                        doneLatch.countDown();
                                    }
                                })
                        .start();
            }

            // 모든 스레드 동시 시작
            startLatch.countDown();

            // then: 첫 번째 스레드만 성공
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
            assertThat(successCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("락 해제 후 다른 스레드가 획득할 수 있다")
        void shouldAllowAcquisitionAfterRelease() throws Exception {
            // given
            SessionLockKey key = new SessionLockKey("release-test-" + UUID.randomUUID());

            // when: 락 획득 후 해제
            boolean firstAcquired = distributedLockAdapter.tryLock(key, 0, 30, TimeUnit.SECONDS);
            assertThat(firstAcquired).isTrue();
            distributedLockAdapter.unlock(key);

            // then: 다른 스레드에서 락 획득 성공
            CountDownLatch done = new CountDownLatch(1);
            AtomicInteger result = new AtomicInteger(0);

            new Thread(
                            () -> {
                                boolean acquired =
                                        distributedLockAdapter.tryLock(
                                                key, 0, 30, TimeUnit.SECONDS);
                                result.set(acquired ? 1 : 0);
                                if (acquired) {
                                    distributedLockAdapter.unlock(key);
                                }
                                done.countDown();
                            })
                    .start();

            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(result.get()).isEqualTo(1);
        }
    }
}

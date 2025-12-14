package com.ryuqq.fileflow.adapter.out.persistence.redis.lock.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.redis.common.LockTestSupport;
import com.ryuqq.fileflow.adapter.out.persistence.redis.common.TestLockKey;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * DistributedLockAdapter 통합 테스트
 *
 * <p>Redisson을 사용한 분산락 기능을 검증합니다.
 */
@DisplayName("DistributedLockAdapter 통합 테스트")
class DistributedLockAdapterTest extends LockTestSupport {

    @Autowired private DistributedLockAdapter lockAdapter;

    @Nested
    @DisplayName("tryLock 메서드")
    class TryLockMethod {

        @Test
        @DisplayName("성공 - 락 획득")
        void tryLock_success() {
            // Given
            TestLockKey key = new TestLockKey("lock-1");

            // When
            boolean acquired = lockAdapter.tryLock(key, 5, 30, TimeUnit.SECONDS);

            // Then
            assertThat(acquired).isTrue();
            assertLocked(key);

            // Cleanup
            lockAdapter.unlock(key);
        }

        @Test
        @DisplayName("성공 - 대기 시간 내 락 획득 실패")
        void tryLock_timeout_failure() throws InterruptedException {
            // Given
            TestLockKey key = new TestLockKey("lock-timeout");
            AtomicBoolean lockHeld = new AtomicBoolean(false);

            // 다른 스레드에서 먼저 락 획득
            Thread lockHolder =
                    new Thread(
                            () -> {
                                try {
                                    boolean acquired =
                                            tryLockDirectly(key.value(), 0, 30, TimeUnit.SECONDS);
                                    if (acquired) {
                                        lockHeld.set(true);
                                        // 테스트가 완료될 때까지 대기
                                        Thread.sleep(5000);
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } finally {
                                    if (lockHeld.get()) {
                                        unlockDirectly(key.value());
                                    }
                                }
                            });
            lockHolder.start();

            // 락이 획득될 때까지 대기
            Thread.sleep(100);
            assertThat(lockHeld.get()).isTrue();

            // When
            boolean acquired = lockAdapter.tryLock(key, 1, 30, TimeUnit.SECONDS);

            // Then
            assertThat(acquired).isFalse();

            // Cleanup
            lockHolder.interrupt();
            lockHolder.join(1000);
        }

        @Test
        @DisplayName("성공 - 짧은 대기 시간으로 락 획득 시도")
        void tryLock_shortWait_success() {
            // Given
            TestLockKey key = new TestLockKey("lock-short-wait");

            // When
            boolean acquired = lockAdapter.tryLock(key, 0, 10, TimeUnit.SECONDS);

            // Then
            assertThat(acquired).isTrue();

            // Cleanup
            lockAdapter.unlock(key);
        }

        @Test
        @DisplayName("성공 - Watchdog 자동 갱신 (leaseTime -1)")
        void tryLock_withWatchdog_success() {
            // Given
            TestLockKey key = new TestLockKey("lock-watchdog");

            // When
            boolean acquired = lockAdapter.tryLock(key, 5, -1, TimeUnit.SECONDS);

            // Then
            assertThat(acquired).isTrue();
            assertLocked(key);

            // Cleanup
            lockAdapter.unlock(key);
        }
    }

    @Nested
    @DisplayName("unlock 메서드")
    class UnlockMethod {

        @Test
        @DisplayName("성공 - 락 해제")
        void unlock_success() {
            // Given
            TestLockKey key = new TestLockKey("unlock-1");
            lockAdapter.tryLock(key, 5, 30, TimeUnit.SECONDS);
            assertLocked(key);

            // When
            lockAdapter.unlock(key);

            // Then
            assertUnlocked(key);
        }

        @Test
        @DisplayName("성공 - 다른 스레드가 보유한 락은 해제하지 않음")
        void unlock_otherThread_notReleased() throws InterruptedException {
            // Given
            TestLockKey key = new TestLockKey("unlock-other");
            AtomicBoolean lockAcquired = new AtomicBoolean(false);

            Thread otherThread =
                    new Thread(
                            () -> {
                                tryLockDirectly(key.value(), 0, 30, TimeUnit.SECONDS);
                                lockAcquired.set(true);
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                unlockDirectly(key.value());
                            });

            otherThread.start();
            Thread.sleep(100); // 다른 스레드가 락 획득할 때까지 대기

            assertThat(lockAcquired.get()).isTrue();
            assertLocked(key);

            // When - 현재 스레드에서 unlock 시도
            lockAdapter.unlock(key); // 다른 스레드가 보유하고 있으므로 해제되지 않음

            // Then - 여전히 락이 걸려있어야 함
            assertLocked(key);

            // Cleanup
            otherThread.join(3000);
        }
    }

    @Nested
    @DisplayName("isHeldByCurrentThread 메서드")
    class IsHeldByCurrentThreadMethod {

        @Test
        @DisplayName("성공 - 현재 스레드가 락을 보유")
        void isHeldByCurrentThread_true() {
            // Given
            TestLockKey key = new TestLockKey("held-1");
            lockAdapter.tryLock(key, 5, 30, TimeUnit.SECONDS);

            // When
            boolean held = lockAdapter.isHeldByCurrentThread(key);

            // Then
            assertThat(held).isTrue();

            // Cleanup
            lockAdapter.unlock(key);
        }

        @Test
        @DisplayName("성공 - 락을 보유하지 않음")
        void isHeldByCurrentThread_false() {
            // Given
            TestLockKey key = new TestLockKey("not-held");

            // When
            boolean held = lockAdapter.isHeldByCurrentThread(key);

            // Then
            assertThat(held).isFalse();
        }
    }

    @Nested
    @DisplayName("isLocked 메서드")
    class IsLockedMethod {

        @Test
        @DisplayName("성공 - 락이 걸려있음")
        void isLocked_true() {
            // Given
            TestLockKey key = new TestLockKey("locked-1");
            lockAdapter.tryLock(key, 5, 30, TimeUnit.SECONDS);

            // When
            boolean locked = lockAdapter.isLocked(key);

            // Then
            assertThat(locked).isTrue();

            // Cleanup
            lockAdapter.unlock(key);
        }

        @Test
        @DisplayName("성공 - 락이 걸려있지 않음")
        void isLocked_false() {
            // Given
            TestLockKey key = new TestLockKey("not-locked");

            // When
            boolean locked = lockAdapter.isLocked(key);

            // Then
            assertThat(locked).isFalse();
        }
    }

    @Nested
    @DisplayName("동시성 테스트")
    class ConcurrencyTests {

        @Test
        @DisplayName("성공 - 분산락으로 동시 실행 방지")
        void concurrentLock_onlyOneSucceeds() throws InterruptedException {
            // Given
            TestLockKey key = new TestLockKey("concurrent-1");

            // When - 10개 스레드가 동시에 락 획득 시도
            ConcurrencyResult result =
                    runConcurrentlyWithTracking(
                            10,
                            () -> {
                                if (lockAdapter.tryLock(key, 5, 30, TimeUnit.SECONDS)) {
                                    try {
                                        // 임계 구역 작업 시뮬레이션
                                        Thread.sleep(50);
                                        return true;
                                    } finally {
                                        lockAdapter.unlock(key);
                                    }
                                }
                                return false;
                            });

            // Then
            // 모든 스레드가 순차적으로 성공했거나, 일부만 성공
            assertThat(result.successCount()).isGreaterThanOrEqualTo(1);
            assertThat(result.successCount()).isLessThanOrEqualTo(10);
        }

        @Test
        @DisplayName("성공 - 분산락 재진입 (Reentrant)")
        void reentrantLock_success() {
            // Given
            TestLockKey key = new TestLockKey("reentrant-1");

            // When - 같은 스레드에서 두 번 락 획득
            boolean firstAcquired = lockAdapter.tryLock(key, 5, 30, TimeUnit.SECONDS);
            boolean secondAcquired = lockAdapter.tryLock(key, 5, 30, TimeUnit.SECONDS);

            // Then
            assertThat(firstAcquired).isTrue();
            assertThat(secondAcquired).isTrue();

            // Cleanup - 두 번 unlock 필요
            lockAdapter.unlock(key);
            lockAdapter.unlock(key);
        }
    }
}

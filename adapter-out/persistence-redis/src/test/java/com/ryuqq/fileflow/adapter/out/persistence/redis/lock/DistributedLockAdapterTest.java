package com.ryuqq.fileflow.adapter.out.persistence.redis.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.domain.common.vo.LockKey;
import com.ryuqq.fileflow.domain.session.vo.SessionLockKey;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Tag("unit")
@DisplayName("DistributedLockAdapter 단위 테스트")
class DistributedLockAdapterTest {

    private RedissonClient redissonClient;
    private RLock rLock;
    private DistributedLockAdapter sut;

    private final LockKey lockKey = new SessionLockKey("session-001");

    @BeforeEach
    void setUp() {
        redissonClient = mock(RedissonClient.class);
        rLock = mock(RLock.class);
        given(redissonClient.getLock(lockKey.value())).willReturn(rLock);
        sut = new DistributedLockAdapter(redissonClient);
    }

    @Nested
    @DisplayName("tryLock 메서드")
    class TryLockTest {

        @Test
        @DisplayName("성공: RLock.tryLock()에 위임하여 true를 반환한다")
        void shouldReturnTrueWhenLockAcquired() throws InterruptedException {
            // given
            long waitTime = 5L;
            long leaseTime = 30L;
            TimeUnit unit = TimeUnit.SECONDS;

            given(rLock.tryLock(waitTime, leaseTime, unit)).willReturn(true);

            // when
            boolean result = sut.tryLock(lockKey, waitTime, leaseTime, unit);

            // then
            assertThat(result).isTrue();
            then(redissonClient).should().getLock(lockKey.value());
            then(rLock).should().tryLock(waitTime, leaseTime, unit);
        }

        @Test
        @DisplayName("실패: RLock.tryLock()이 false를 반환하면 false를 반환한다")
        void shouldReturnFalseWhenLockNotAcquired() throws InterruptedException {
            // given
            long waitTime = 5L;
            long leaseTime = 30L;
            TimeUnit unit = TimeUnit.SECONDS;

            given(rLock.tryLock(waitTime, leaseTime, unit)).willReturn(false);

            // when
            boolean result = sut.tryLock(lockKey, waitTime, leaseTime, unit);

            // then
            assertThat(result).isFalse();
            then(rLock).should().tryLock(waitTime, leaseTime, unit);
        }

        @Test
        @DisplayName("실패: InterruptedException 발생 시 false를 반환하고 인터럽트 상태를 복원한다")
        void shouldReturnFalseAndRestoreInterruptWhenInterrupted() throws InterruptedException {
            // given
            long waitTime = 5L;
            long leaseTime = 30L;
            TimeUnit unit = TimeUnit.SECONDS;

            given(rLock.tryLock(waitTime, leaseTime, unit))
                    .willThrow(new InterruptedException("interrupted"));

            // when
            boolean result = sut.tryLock(lockKey, waitTime, leaseTime, unit);

            // then
            assertThat(result).isFalse();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();

            // 다른 테스트에 영향을 주지 않도록 인터럽트 상태 클리어
            Thread.interrupted();
        }
    }

    @Nested
    @DisplayName("unlock 메서드")
    class UnlockTest {

        @Test
        @DisplayName("성공: 현재 스레드가 Lock을 보유 중이면 unlock을 호출한다")
        void shouldUnlockWhenHeldByCurrentThread() {
            // given
            given(rLock.isHeldByCurrentThread()).willReturn(true);

            // when
            sut.unlock(lockKey);

            // then
            then(rLock).should().isHeldByCurrentThread();
            then(rLock).should().unlock();
        }

        @Test
        @DisplayName("무시: 현재 스레드가 Lock을 보유하지 않으면 unlock을 호출하지 않는다")
        void shouldNotUnlockWhenNotHeldByCurrentThread() {
            // given
            given(rLock.isHeldByCurrentThread()).willReturn(false);

            // when
            sut.unlock(lockKey);

            // then
            then(rLock).should().isHeldByCurrentThread();
            then(rLock).should(never()).unlock();
        }
    }

    @Nested
    @DisplayName("isHeldByCurrentThread 메서드")
    class IsHeldByCurrentThreadTest {

        @Test
        @DisplayName("성공: RLock.isHeldByCurrentThread()에 위임하여 true를 반환한다")
        void shouldReturnTrueWhenHeldByCurrentThread() {
            // given
            given(rLock.isHeldByCurrentThread()).willReturn(true);

            // when
            boolean result = sut.isHeldByCurrentThread(lockKey);

            // then
            assertThat(result).isTrue();
            then(redissonClient).should().getLock(lockKey.value());
        }

        @Test
        @DisplayName("성공: RLock.isHeldByCurrentThread()에 위임하여 false를 반환한다")
        void shouldReturnFalseWhenNotHeldByCurrentThread() {
            // given
            given(rLock.isHeldByCurrentThread()).willReturn(false);

            // when
            boolean result = sut.isHeldByCurrentThread(lockKey);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isLocked 메서드")
    class IsLockedTest {

        @Test
        @DisplayName("성공: RLock.isLocked()에 위임하여 true를 반환한다")
        void shouldReturnTrueWhenLocked() {
            // given
            given(rLock.isLocked()).willReturn(true);

            // when
            boolean result = sut.isLocked(lockKey);

            // then
            assertThat(result).isTrue();
            then(redissonClient).should().getLock(lockKey.value());
        }

        @Test
        @DisplayName("성공: RLock.isLocked()에 위임하여 false를 반환한다")
        void shouldReturnFalseWhenNotLocked() {
            // given
            given(rLock.isLocked()).willReturn(false);

            // when
            boolean result = sut.isLocked(lockKey);

            // then
            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("DistributedLockPort 인터페이스를 구현한다")
    void shouldImplementDistributedLockPort() {
        assertThat(sut).isInstanceOf(DistributedLockPort.class);
    }
}

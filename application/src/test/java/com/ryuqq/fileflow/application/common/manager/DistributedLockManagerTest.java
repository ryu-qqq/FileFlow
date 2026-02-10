package com.ryuqq.fileflow.application.common.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.domain.common.vo.LockKey;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("DistributedLockManager 단위 테스트")
class DistributedLockManagerTest {

    @InjectMocks private DistributedLockManager sut;
    @Mock private DistributedLockPort distributedLockPort;

    private static LockKey testLockKey() {
        return () -> "lock:test:resource:1";
    }

    @Nested
    @DisplayName("tryLock 메서드")
    class TryLockTest {

        @Test
        @DisplayName("포트에 위임하여 분산락 획득을 시도하고 성공 결과를 반환한다")
        void tryLock_DelegatesToPort_ReturnsTrue() {
            // given
            LockKey key = testLockKey();
            long waitTime = 10L;
            long leaseTime = 30L;
            TimeUnit unit = TimeUnit.SECONDS;

            given(distributedLockPort.tryLock(key, waitTime, leaseTime, unit)).willReturn(true);

            // when
            boolean result = sut.tryLock(key, waitTime, leaseTime, unit);

            // then
            assertThat(result).isTrue();
            then(distributedLockPort).should().tryLock(key, waitTime, leaseTime, unit);
        }

        @Test
        @DisplayName("포트에 위임하여 분산락 획득을 시도하고 실패 결과를 반환한다")
        void tryLock_DelegatesToPort_ReturnsFalse() {
            // given
            LockKey key = testLockKey();
            long waitTime = 5L;
            long leaseTime = 15L;
            TimeUnit unit = TimeUnit.SECONDS;

            given(distributedLockPort.tryLock(key, waitTime, leaseTime, unit)).willReturn(false);

            // when
            boolean result = sut.tryLock(key, waitTime, leaseTime, unit);

            // then
            assertThat(result).isFalse();
            then(distributedLockPort).should().tryLock(key, waitTime, leaseTime, unit);
        }
    }

    @Nested
    @DisplayName("unlock 메서드")
    class UnlockTest {

        @Test
        @DisplayName("포트에 위임하여 분산락을 해제한다")
        void unlock_DelegatesToPort() {
            // given
            LockKey key = testLockKey();

            // when
            sut.unlock(key);

            // then
            then(distributedLockPort).should().unlock(key);
        }
    }

    @Nested
    @DisplayName("isHeldByCurrentThread 메서드")
    class IsHeldByCurrentThreadTest {

        @Test
        @DisplayName("포트에 위임하여 현재 스레드의 락 보유 여부를 반환한다 - 보유 중")
        void isHeldByCurrentThread_DelegatesToPort_ReturnsTrue() {
            // given
            LockKey key = testLockKey();

            given(distributedLockPort.isHeldByCurrentThread(key)).willReturn(true);

            // when
            boolean result = sut.isHeldByCurrentThread(key);

            // then
            assertThat(result).isTrue();
            then(distributedLockPort).should().isHeldByCurrentThread(key);
        }

        @Test
        @DisplayName("포트에 위임하여 현재 스레드의 락 보유 여부를 반환한다 - 미보유")
        void isHeldByCurrentThread_DelegatesToPort_ReturnsFalse() {
            // given
            LockKey key = testLockKey();

            given(distributedLockPort.isHeldByCurrentThread(key)).willReturn(false);

            // when
            boolean result = sut.isHeldByCurrentThread(key);

            // then
            assertThat(result).isFalse();
            then(distributedLockPort).should().isHeldByCurrentThread(key);
        }
    }
}

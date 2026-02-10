package com.ryuqq.fileflow.application.session.service.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.manager.DistributedLockManager;
import com.ryuqq.fileflow.application.session.port.in.command.ExpireSingleUploadSessionUseCase;
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
@DisplayName("LockedExpireSingleUploadSessionService 단위 테스트")
class LockedExpireSingleUploadSessionServiceTest {

    @InjectMocks private LockedExpireSingleUploadSessionService sut;
    @Mock private DistributedLockManager distributedLockManager;
    @Mock private ExpireSingleUploadSessionUseCase expireSingleUploadSessionUseCase;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("락을 획득하면 만료 로직을 실행하고 락을 해제한다")
        void execute_LockAcquired_ExpiresAndUnlocks() {
            // given
            String sessionId = "single-session-001";

            given(
                            distributedLockManager.tryLock(
                                    any(LockKey.class), eq(0L), eq(30L), eq(TimeUnit.SECONDS)))
                    .willReturn(true);

            // when
            sut.execute(sessionId);

            // then
            then(expireSingleUploadSessionUseCase).should().execute(sessionId);
            then(distributedLockManager).should().unlock(any(LockKey.class));
        }

        @Test
        @DisplayName("락 획득에 실패하면 만료 로직을 실행하지 않는다")
        void execute_LockNotAcquired_SkipsExpiration() {
            // given
            String sessionId = "single-session-001";

            given(
                            distributedLockManager.tryLock(
                                    any(LockKey.class), eq(0L), eq(30L), eq(TimeUnit.SECONDS)))
                    .willReturn(false);

            // when
            sut.execute(sessionId);

            // then
            then(expireSingleUploadSessionUseCase).shouldHaveNoInteractions();
            then(distributedLockManager)
                    .should()
                    .tryLock(any(LockKey.class), eq(0L), eq(30L), eq(TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("만료 로직에서 예외가 발생해도 락을 해제한다")
        void execute_ExceptionDuringExpire_StillUnlocks() {
            // given
            String sessionId = "single-session-001";

            given(
                            distributedLockManager.tryLock(
                                    any(LockKey.class), eq(0L), eq(30L), eq(TimeUnit.SECONDS)))
                    .willReturn(true);

            RuntimeException expectedException = new RuntimeException("Expire failed");
            org.mockito.Mockito.doThrow(expectedException)
                    .when(expireSingleUploadSessionUseCase)
                    .execute(sessionId);

            // when & then
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> sut.execute(sessionId))
                    .isEqualTo(expectedException);

            then(distributedLockManager).should().unlock(any(LockKey.class));
        }
    }
}

package com.ryuqq.fileflow.adapter.in.scheduler.transform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.transform.port.in.command.RecoverStuckTransformOutboxUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("TransformOutboxRecoveryScheduler 단위 테스트")
class TransformOutboxRecoverySchedulerTest {

    private TransformOutboxRecoveryScheduler sut;
    @Mock private RecoverStuckTransformOutboxUseCase recoverStuckTransformOutboxUseCase;

    @BeforeEach
    void setUp() {
        SchedulerProperties.TransformOutboxRecovery config =
                new SchedulerProperties.TransformOutboxRecovery(
                        true, "0 */5 * * * *", "Asia/Seoul", 5);
        SchedulerProperties props =
                new SchedulerProperties(
                        new SchedulerProperties.Jobs(
                                null, null, null, null, null, null, null, config, null, null));
        sut = new TransformOutboxRecoveryScheduler(recoverStuckTransformOutboxUseCase, props);
    }

    @Nested
    @DisplayName("recover 메서드")
    class RecoverTest {

        @Test
        @DisplayName("UseCase에 stuckMinutes를 전달하고 복구 건수를 반환한다")
        void recover_DelegatesToUseCase() {
            given(recoverStuckTransformOutboxUseCase.execute(5)).willReturn(10);

            int result = sut.recover();

            assertThat(result).isEqualTo(10);
            then(recoverStuckTransformOutboxUseCase).should().execute(5);
        }

        @Test
        @DisplayName("복구 대상이 없으면 0을 반환한다")
        void recover_NoStuck_ReturnsZero() {
            given(recoverStuckTransformOutboxUseCase.execute(5)).willReturn(0);

            int result = sut.recover();

            assertThat(result).isZero();
        }
    }
}

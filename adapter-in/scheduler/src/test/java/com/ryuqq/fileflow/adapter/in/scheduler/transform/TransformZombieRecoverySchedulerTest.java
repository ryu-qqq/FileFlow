package com.ryuqq.fileflow.adapter.in.scheduler.transform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.transform.dto.command.RecoverZombieTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.port.in.command.RecoverZombieTransformRequestUseCase;
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
@DisplayName("TransformZombieRecoveryScheduler 단위 테스트")
class TransformZombieRecoverySchedulerTest {

    private TransformZombieRecoveryScheduler sut;

    @Mock private RecoverZombieTransformRequestUseCase useCase;

    private static final int BATCH_SIZE = 100;
    private static final long TIMEOUT_SECONDS = 300;

    @BeforeEach
    void setUp() {
        SchedulerProperties.DownloadZombieRecovery downloadConfig =
                new SchedulerProperties.DownloadZombieRecovery(
                        true, "0 */5 * * * *", "Asia/Seoul", 50, 180);
        SchedulerProperties.TransformZombieRecovery transformConfig =
                new SchedulerProperties.TransformZombieRecovery(
                        true, "0 */5 * * * *", "Asia/Seoul", BATCH_SIZE, TIMEOUT_SECONDS);
        SchedulerProperties.Jobs jobs =
                new SchedulerProperties.Jobs(
                        downloadConfig, transformConfig, null, null, null, null, null);
        SchedulerProperties properties = new SchedulerProperties(jobs);

        sut = new TransformZombieRecoveryScheduler(useCase, properties);
    }

    @Nested
    @DisplayName("recoverZombieRequests 메서드")
    class RecoverZombieRequestsTest {

        @Test
        @DisplayName("Properties의 batchSize와 timeoutSeconds로 Command를 생성하여 UseCase를 호출한다")
        void recoverZombieRequests_CallsUseCaseWithCorrectCommand() {
            // given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(3, 3, 0);
            given(
                            useCase.execute(
                                    argThat(
                                            command ->
                                                    command.batchSize() == BATCH_SIZE
                                                            && command.timeoutSeconds()
                                                                    == TIMEOUT_SECONDS)))
                    .willReturn(expected);

            // when
            SchedulerBatchProcessingResult result = sut.recoverZombieRequests();

            // then
            assertThat(result).isEqualTo(expected);
            then(useCase)
                    .should()
                    .execute(
                            argThat(
                                    (RecoverZombieTransformRequestCommand command) ->
                                            command.batchSize() == BATCH_SIZE
                                                    && command.timeoutSeconds()
                                                            == TIMEOUT_SECONDS));
        }

        @Test
        @DisplayName("UseCase 반환값을 그대로 전달한다")
        void recoverZombieRequests_ReturnsUseCaseResult() {
            // given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(7, 5, 2);
            given(
                            useCase.execute(
                                    argThat(
                                            command ->
                                                    command.batchSize() == BATCH_SIZE
                                                            && command.timeoutSeconds()
                                                                    == TIMEOUT_SECONDS)))
                    .willReturn(expected);

            // when
            SchedulerBatchProcessingResult result = sut.recoverZombieRequests();

            // then
            assertThat(result.total()).isEqualTo(7);
            assertThat(result.success()).isEqualTo(5);
            assertThat(result.failed()).isEqualTo(2);
            assertThat(result.hasFailures()).isTrue();
        }

        @Test
        @DisplayName("처리 대상이 없으면 empty 결과를 반환한다")
        void recoverZombieRequests_NoTargets_ReturnsEmptyResult() {
            // given
            given(
                            useCase.execute(
                                    argThat(
                                            command ->
                                                    command.batchSize() == BATCH_SIZE
                                                            && command.timeoutSeconds()
                                                                    == TIMEOUT_SECONDS)))
                    .willReturn(SchedulerBatchProcessingResult.empty());

            // when
            SchedulerBatchProcessingResult result = sut.recoverZombieRequests();

            // then
            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
            assertThat(result.hasFailures()).isFalse();
        }
    }
}

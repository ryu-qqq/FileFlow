package com.ryuqq.fileflow.adapter.in.scheduler.transform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.transform.port.in.command.ProcessTransformQueueOutboxUseCase;
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
@DisplayName("TransformQueueOutboxScheduler 단위 테스트")
class TransformQueueOutboxSchedulerTest {

    private TransformQueueOutboxScheduler sut;

    @Mock private ProcessTransformQueueOutboxUseCase useCase;

    private static final int BATCH_SIZE = 100;

    @BeforeEach
    void setUp() {
        SchedulerProperties.DownloadZombieRecovery downloadConfig =
                new SchedulerProperties.DownloadZombieRecovery(
                        true, "0 */5 * * * *", "Asia/Seoul", 50, 180);
        SchedulerProperties.TransformZombieRecovery transformConfig =
                new SchedulerProperties.TransformZombieRecovery(
                        true, "0 */5 * * * *", "Asia/Seoul", 50, 180);
        SchedulerProperties.DownloadQueueOutbox downloadOutboxConfig =
                new SchedulerProperties.DownloadQueueOutbox(
                        true, "*/5 * * * * *", "Asia/Seoul", 100);
        SchedulerProperties.TransformQueueOutbox transformOutboxConfig =
                new SchedulerProperties.TransformQueueOutbox(
                        true, "*/5 * * * * *", "Asia/Seoul", BATCH_SIZE);
        SchedulerProperties.Jobs jobs =
                new SchedulerProperties.Jobs(
                        downloadConfig,
                        transformConfig,
                        downloadOutboxConfig,
                        transformOutboxConfig,
                        null,
                        null);
        SchedulerProperties properties = new SchedulerProperties(jobs);

        sut = new TransformQueueOutboxScheduler(useCase, properties);
    }

    @Nested
    @DisplayName("processOutbox 메서드")
    class ProcessOutboxTest {

        @Test
        @DisplayName("UseCase에 batchSize를 전달하여 실행한다")
        void processOutbox_CallsUseCaseWithBatchSize() {
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(5, 4, 1);
            given(useCase.execute(BATCH_SIZE)).willReturn(expected);

            SchedulerBatchProcessingResult result = sut.processOutbox();

            assertThat(result).isEqualTo(expected);
            then(useCase).should().execute(BATCH_SIZE);
        }

        @Test
        @DisplayName("처리 대상이 없으면 empty 결과를 반환한다")
        void processOutbox_NoTargets_ReturnsEmpty() {
            given(useCase.execute(BATCH_SIZE)).willReturn(SchedulerBatchProcessingResult.empty());

            SchedulerBatchProcessingResult result = sut.processOutbox();

            assertThat(result.total()).isZero();
        }
    }
}

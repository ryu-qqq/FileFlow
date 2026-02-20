package com.ryuqq.fileflow.adapter.in.scheduler.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.port.in.command.ProcessDownloadQueueOutboxUseCase;
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
@DisplayName("DownloadQueueOutboxScheduler 단위 테스트")
class DownloadQueueOutboxSchedulerTest {

    private DownloadQueueOutboxScheduler sut;

    @Mock private ProcessDownloadQueueOutboxUseCase useCase;

    private static final int BATCH_SIZE = 100;

    @BeforeEach
    void setUp() {
        SchedulerProperties.DownloadQueueOutbox config =
                new SchedulerProperties.DownloadQueueOutbox(
                        true, "*/10 * * * * *", "Asia/Seoul", BATCH_SIZE);
        SchedulerProperties.Jobs jobs =
                new SchedulerProperties.Jobs(null, null, config, null, null, null);
        SchedulerProperties properties = new SchedulerProperties(jobs);

        sut = new DownloadQueueOutboxScheduler(useCase, properties);
    }

    @Nested
    @DisplayName("processOutbox 메서드")
    class ProcessOutboxTest {

        @Test
        @DisplayName("Properties의 batchSize로 UseCase를 호출한다")
        void processOutbox_CallsUseCaseWithBatchSize() {
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(5, 4, 1);
            given(useCase.execute(BATCH_SIZE)).willReturn(expected);

            SchedulerBatchProcessingResult result = sut.processOutbox();

            assertThat(result).isEqualTo(expected);
            then(useCase).should().execute(BATCH_SIZE);
        }

        @Test
        @DisplayName("UseCase 반환값을 그대로 전달한다")
        void processOutbox_ReturnsUseCaseResult() {
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(10, 8, 2);
            given(useCase.execute(BATCH_SIZE)).willReturn(expected);

            SchedulerBatchProcessingResult result = sut.processOutbox();

            assertThat(result.total()).isEqualTo(10);
            assertThat(result.success()).isEqualTo(8);
            assertThat(result.failed()).isEqualTo(2);
        }

        @Test
        @DisplayName("처리 대상이 없으면 empty 결과를 반환한다")
        void processOutbox_NoTargets_ReturnsEmpty() {
            given(useCase.execute(BATCH_SIZE)).willReturn(SchedulerBatchProcessingResult.empty());

            SchedulerBatchProcessingResult result = sut.processOutbox();

            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
        }
    }
}

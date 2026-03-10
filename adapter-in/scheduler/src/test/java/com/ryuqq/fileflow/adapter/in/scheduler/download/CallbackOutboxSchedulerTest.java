package com.ryuqq.fileflow.adapter.in.scheduler.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.port.in.command.ProcessCallbackOutboxUseCase;
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
@DisplayName("CallbackOutboxScheduler 단위 테스트")
class CallbackOutboxSchedulerTest {

    private CallbackOutboxScheduler sut;

    @Mock private ProcessCallbackOutboxUseCase useCase;

    private static final int BATCH_SIZE = 100;

    @BeforeEach
    void setUp() {
        SchedulerProperties.CallbackOutbox config =
                new SchedulerProperties.CallbackOutbox(
                        true, "*/10 * * * * *", "Asia/Seoul", BATCH_SIZE);
        SchedulerProperties.Jobs jobs =
                new SchedulerProperties.Jobs(null, null, null, null, config, null, null, null);
        SchedulerProperties properties = new SchedulerProperties(jobs);

        sut = new CallbackOutboxScheduler(useCase, properties);
    }

    @Nested
    @DisplayName("processOutbox 메서드")
    class ProcessOutboxTest {

        @Test
        @DisplayName("빈 결과를 반환하면 즉시 종료한다")
        void processOutbox_EmptyResult_StopsImmediately() {
            // given
            given(useCase.execute(BATCH_SIZE)).willReturn(SchedulerBatchProcessingResult.empty());

            // when
            SchedulerBatchProcessingResult result = sut.processOutbox();

            // then
            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
            then(useCase).should(times(1)).execute(BATCH_SIZE);
        }

        @Test
        @DisplayName("batchSize 미만의 결과를 반환하면 1번만 호출하고 결과를 반환한다")
        void processOutbox_PartialBatch_SingleCall() {
            // given
            SchedulerBatchProcessingResult expected = SchedulerBatchProcessingResult.of(50, 48, 2);
            given(useCase.execute(BATCH_SIZE)).willReturn(expected);

            // when
            SchedulerBatchProcessingResult result = sut.processOutbox();

            // then
            assertThat(result.total()).isEqualTo(50);
            assertThat(result.success()).isEqualTo(48);
            assertThat(result.failed()).isEqualTo(2);
            then(useCase).should(times(1)).execute(BATCH_SIZE);
        }

        @Test
        @DisplayName("배치 처리 결과가 batchSize 이상이면 다음 배치를 계속 처리한다")
        void processOutbox_FullBatch_ContinuesLoop() {
            // given
            given(useCase.execute(BATCH_SIZE))
                    .willReturn(SchedulerBatchProcessingResult.of(BATCH_SIZE, BATCH_SIZE, 0))
                    .willReturn(SchedulerBatchProcessingResult.of(30, 28, 2));

            // when
            SchedulerBatchProcessingResult result = sut.processOutbox();

            // then
            assertThat(result.total()).isEqualTo(130);
            assertThat(result.success()).isEqualTo(128);
            assertThat(result.failed()).isEqualTo(2);
            then(useCase).should(times(2)).execute(BATCH_SIZE);
        }

        @Test
        @DisplayName("MAX_LOOPS(10)에 도달하면 루프를 종료한다")
        void processOutbox_MaxLoops_StopsAt10() {
            // given
            given(useCase.execute(BATCH_SIZE))
                    .willReturn(SchedulerBatchProcessingResult.of(BATCH_SIZE, BATCH_SIZE, 0));

            // when
            SchedulerBatchProcessingResult result = sut.processOutbox();

            // then
            assertThat(result.total()).isEqualTo(BATCH_SIZE * 10);
            assertThat(result.success()).isEqualTo(BATCH_SIZE * 10);
            assertThat(result.failed()).isZero();
            then(useCase).should(times(10)).execute(BATCH_SIZE);
        }
    }
}

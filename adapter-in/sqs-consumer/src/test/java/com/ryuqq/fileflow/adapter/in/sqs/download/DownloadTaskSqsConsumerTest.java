package com.ryuqq.fileflow.adapter.in.sqs.download;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.application.download.port.in.command.StartDownloadTaskUseCase;
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
@DisplayName("DownloadTaskSqsConsumer 단위 테스트")
class DownloadTaskSqsConsumerTest {

    @InjectMocks private DownloadTaskSqsConsumer sut;
    @Mock private StartDownloadTaskUseCase startDownloadTaskUseCase;

    @Nested
    @DisplayName("consume 메서드")
    class ConsumeTest {

        @Test
        @DisplayName("유효한 다운로드 태스크 ID를 수신하면 UseCase를 실행한다")
        void consume_ValidDownloadTaskId_ExecutesUseCase() {
            // given
            String downloadTaskId = "download-task-001";

            // when
            sut.consume(downloadTaskId);

            // then
            then(startDownloadTaskUseCase).should().execute(downloadTaskId);
        }

        @Test
        @DisplayName("UseCase 실행 중 예외가 발생하면 SQS 재시도를 위해 예외를 재전파한다")
        void consume_UseCaseThrows_RethrowsForSqsRetry() {
            // given
            String downloadTaskId = "download-task-002";
            willThrow(new RuntimeException("다운로드 처리 실패"))
                    .given(startDownloadTaskUseCase)
                    .execute(downloadTaskId);

            // when & then
            assertThatThrownBy(() -> sut.consume(downloadTaskId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("다운로드 처리 실패");
        }

        @Test
        @DisplayName("정상 처리 완료 후 UseCase가 정확히 한 번만 호출된다")
        void consume_SuccessfulProcessing_UseCaseCalledOnce() {
            // given
            String downloadTaskId = "download-task-003";

            // when
            sut.consume(downloadTaskId);

            // then
            then(startDownloadTaskUseCase).should().execute(downloadTaskId);
            then(startDownloadTaskUseCase).shouldHaveNoMoreInteractions();
        }
    }
}

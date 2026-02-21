package com.ryuqq.fileflow.adapter.in.sqs.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.application.download.port.in.command.StartDownloadTaskUseCase;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.common.exception.DomainExceptionFixture;
import com.ryuqq.fileflow.domain.common.exception.ErrorCodeFixture;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadTaskSqsConsumer 단위 테스트")
class DownloadTaskSqsConsumerTest {

    private MeterRegistry meterRegistry;
    private DownloadTaskSqsConsumer sut;

    @Mock private StartDownloadTaskUseCase startDownloadTaskUseCase;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        sut = new DownloadTaskSqsConsumer(startDownloadTaskUseCase, meterRegistry);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Nested
    @DisplayName("consume 메서드")
    class ConsumeTest {

        @Test
        @DisplayName("유효한 다운로드 태스크 ID를 수신하면 UseCase를 실행한다")
        void consume_ValidDownloadTaskId_ExecutesUseCase() {
            String downloadTaskId = "download-task-001";

            sut.consume(downloadTaskId, "scheduler-abc12345");

            then(startDownloadTaskUseCase).should().execute(downloadTaskId);
        }

        @Test
        @DisplayName("정상 처리 완료 후 UseCase가 정확히 한 번만 호출된다")
        void consume_SuccessfulProcessing_UseCaseCalledOnce() {
            String downloadTaskId = "download-task-003";

            sut.consume(downloadTaskId, "scheduler-abc12345");

            then(startDownloadTaskUseCase).should().execute(downloadTaskId);
            then(startDownloadTaskUseCase).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("traceId가 null이어도 정상 처리된다")
        void consume_NullTraceId_ProcessesSuccessfully() {
            String downloadTaskId = "download-task-004";

            sut.consume(downloadTaskId, null);

            then(startDownloadTaskUseCase).should().execute(downloadTaskId);
        }

        @Test
        @DisplayName("traceId가 전달되면 MDC에 설정된다")
        void consume_WithTraceId_SetsMdc() {
            String downloadTaskId = "download-task-005";

            sut.consume(downloadTaskId, "scheduler-abc12345");

            // MDC.remove("traceId")가 finally에서 호출되므로 처리 후에는 비어있어야 함
            assertThat(MDC.get("traceId")).isNull();
        }
    }

    @Nested
    @DisplayName("에러 분류 — Non-Retryable (ACK)")
    class NonRetryableErrorTest {

        @Test
        @DisplayName("DomainException 4xx(400) 발생 시 예외를 삼키고 ACK 처리한다")
        void consume_DomainException400_AcknowledgesMessage() {
            String downloadTaskId = "download-task-ack-400";
            willThrow(
                            DomainExceptionFixture.aDomainException(
                                    ErrorCodeFixture.TestErrorCode.TEST_ERROR))
                    .given(startDownloadTaskUseCase)
                    .execute(downloadTaskId);

            assertThatCode(() -> sut.consume(downloadTaskId, "scheduler-abc12345"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("DomainException 4xx(404) 발생 시 예외를 삼키고 ACK 처리한다")
        void consume_DomainException404_AcknowledgesMessage() {
            String downloadTaskId = "download-task-ack-404";
            willThrow(
                            DomainExceptionFixture.aDomainException(
                                    ErrorCodeFixture.TestErrorCode.NOT_FOUND))
                    .given(startDownloadTaskUseCase)
                    .execute(downloadTaskId);

            assertThatCode(() -> sut.consume(downloadTaskId, "scheduler-abc12345"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("에러 분류 — Retryable (NACK)")
    class RetryableErrorTest {

        @Test
        @DisplayName("DomainException 5xx(500) 발생 시 예외를 재전파하여 NACK 처리한다")
        void consume_DomainException500_RethrowsForRetry() {
            String downloadTaskId = "download-task-nack-500";
            willThrow(
                            DomainExceptionFixture.aDomainException(
                                    ErrorCodeFixture.TestErrorCode.INTERNAL_ERROR))
                    .given(startDownloadTaskUseCase)
                    .execute(downloadTaskId);

            assertThatThrownBy(() -> sut.consume(downloadTaskId, "scheduler-abc12345"))
                    .isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("일반 RuntimeException 발생 시 예외를 재전파하여 NACK 처리한다")
        void consume_RuntimeException_RethrowsForRetry() {
            String downloadTaskId = "download-task-nack-runtime";
            willThrow(new RuntimeException("다운로드 처리 실패"))
                    .given(startDownloadTaskUseCase)
                    .execute(downloadTaskId);

            assertThatThrownBy(() -> sut.consume(downloadTaskId, "scheduler-abc12345"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("다운로드 처리 실패");
        }

        @Test
        @DisplayName("예외 발생 후에도 MDC가 정리된다")
        void consume_ExceptionThrown_ClearsMdc() {
            String downloadTaskId = "download-task-mdc-cleanup";
            willThrow(new RuntimeException("fail"))
                    .given(startDownloadTaskUseCase)
                    .execute(downloadTaskId);

            assertThatThrownBy(() -> sut.consume(downloadTaskId, "scheduler-abc12345"))
                    .isInstanceOf(RuntimeException.class);

            assertThat(MDC.get("traceId")).isNull();
        }
    }

    @Nested
    @DisplayName("메트릭 기록")
    class MetricsRecording {

        @Test
        @DisplayName("성공 처리 시 success Counter와 duration Timer를 기록한다")
        void shouldRecordSuccessMetrics() {
            sut.consume("download-task-metric-001", "scheduler-abc12345");

            assertThat(
                            meterRegistry
                                    .find("sqs.consumer.messages")
                                    .tag("queue", "download")
                                    .tag("result", "success")
                                    .counter()
                                    .count())
                    .isEqualTo(1.0);
            assertThat(meterRegistry.find("sqs.consumer.duration").tag("queue", "download").timer())
                    .isNotNull();
        }

        @Test
        @DisplayName("ACK 처리 시 ack Counter를 기록한다")
        void shouldRecordAckMetrics() {
            willThrow(
                            DomainExceptionFixture.aDomainException(
                                    ErrorCodeFixture.TestErrorCode.TEST_ERROR))
                    .given(startDownloadTaskUseCase)
                    .execute("download-task-metric-ack");

            sut.consume("download-task-metric-ack", "scheduler-abc12345");

            assertThat(
                            meterRegistry
                                    .find("sqs.consumer.messages")
                                    .tag("queue", "download")
                                    .tag("result", "ack")
                                    .counter()
                                    .count())
                    .isEqualTo(1.0);
        }

        @Test
        @DisplayName("NACK 처리 시 nack Counter를 기록한다")
        void shouldRecordNackMetrics() {
            willThrow(new RuntimeException("fail"))
                    .given(startDownloadTaskUseCase)
                    .execute("download-task-metric-nack");

            assertThatThrownBy(() -> sut.consume("download-task-metric-nack", "scheduler-abc12345"))
                    .isInstanceOf(RuntimeException.class);

            assertThat(
                            meterRegistry
                                    .find("sqs.consumer.messages")
                                    .tag("queue", "download")
                                    .tag("result", "nack")
                                    .counter()
                                    .count())
                    .isEqualTo(1.0);
        }
    }
}

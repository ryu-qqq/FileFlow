package com.ryuqq.fileflow.application.transform.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.exception.PermanentCallbackFailureException;
import com.ryuqq.fileflow.application.transform.dto.response.TransformCallbackPayload;
import com.ryuqq.fileflow.application.transform.manager.client.TransformCallbackNotificationManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformCallbackOutboxReadManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformReadManager;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import com.ryuqq.fileflow.domain.transform.id.TransformCallbackOutboxId;
import java.time.Instant;
import java.util.List;
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
@DisplayName("ProcessTransformCallbackOutboxService 단위 테스트")
class ProcessTransformCallbackOutboxServiceTest {

    @InjectMocks private ProcessTransformCallbackOutboxService sut;
    @Mock private TransformCallbackOutboxReadManager transformCallbackOutboxReadManager;
    @Mock private TransformCallbackOutboxCommandManager transformCallbackOutboxCommandManager;
    @Mock private TransformCallbackNotificationManager transformCallbackNotificationManager;
    @Mock private TransformReadManager transformReadManager;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("PENDING 아웃박스가 없으면 empty 결과를 반환한다")
        void execute_NoPending_ReturnsEmpty() {
            // given
            given(transformCallbackOutboxReadManager.findPendingMessages(10)).willReturn(List.of());

            // when
            SchedulerBatchProcessingResult result = sut.execute(10);

            // then
            assertThat(result.total()).isZero();
        }

        @Test
        @DisplayName("COMPLETED 상태 콜백 전송 성공 시 SENT로 마킹한다")
        void execute_CompletedCallback_Success() {
            // given
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-001"),
                            "transform-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);
            TransformRequest request = TransformRequestFixture.aCompletedRequest();

            given(transformCallbackOutboxReadManager.findPendingMessages(10))
                    .willReturn(List.of(outbox));
            given(transformReadManager.getTransformRequest("transform-001")).willReturn(request);

            // when
            SchedulerBatchProcessingResult result = sut.execute(10);

            // then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            then(transformCallbackNotificationManager)
                    .should()
                    .notify(
                            eq("https://callback.example.com/done"),
                            any(TransformCallbackPayload.class));
            then(transformCallbackOutboxCommandManager).should().persist(outbox);
        }

        @Test
        @DisplayName("FAILED 상태 콜백 전송 성공 시 SENT로 마킹한다")
        void execute_FailedCallback_Success() {
            // given
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-002"),
                            "transform-002",
                            "https://callback.example.com/done",
                            "FAILED",
                            NOW);
            TransformRequest request = TransformRequestFixture.aFailedRequest();

            given(transformCallbackOutboxReadManager.findPendingMessages(10))
                    .willReturn(List.of(outbox));
            given(transformReadManager.getTransformRequest("transform-002")).willReturn(request);

            // when
            SchedulerBatchProcessingResult result = sut.execute(10);

            // then
            assertThat(result.success()).isEqualTo(1);
        }

        @Test
        @DisplayName("PermanentCallbackFailureException 발생 시 영구 실패로 마킹한다")
        void execute_PermanentFailure_MarksFailedPermanently() {
            // given
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-003"),
                            "transform-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);
            TransformRequest request = TransformRequestFixture.aCompletedRequest();

            given(transformCallbackOutboxReadManager.findPendingMessages(10))
                    .willReturn(List.of(outbox));
            given(transformReadManager.getTransformRequest("transform-001")).willReturn(request);
            willThrow(new PermanentCallbackFailureException("404 Not Found"))
                    .given(transformCallbackNotificationManager)
                    .notify(any(), any());

            // when
            SchedulerBatchProcessingResult result = sut.execute(10);

            // then
            assertThat(result.failed()).isEqualTo(1);
            then(transformCallbackOutboxCommandManager).should().persist(outbox);
        }

        @Test
        @DisplayName("일반 예외 발생 시 재시도 가능한 실패로 마킹한다")
        void execute_GeneralException_MarksFailedWithRetry() {
            // given
            TransformCallbackOutbox outbox =
                    TransformCallbackOutbox.forNew(
                            TransformCallbackOutboxId.of("outbox-004"),
                            "transform-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);
            TransformRequest request = TransformRequestFixture.aCompletedRequest();

            given(transformCallbackOutboxReadManager.findPendingMessages(10))
                    .willReturn(List.of(outbox));
            given(transformReadManager.getTransformRequest("transform-001")).willReturn(request);
            willThrow(new RuntimeException("Connection refused"))
                    .given(transformCallbackNotificationManager)
                    .notify(any(), any());

            // when
            SchedulerBatchProcessingResult result = sut.execute(10);

            // then
            assertThat(result.failed()).isEqualTo(1);
            then(transformCallbackOutboxCommandManager).should().persist(outbox);
        }
    }
}

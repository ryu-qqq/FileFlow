package com.ryuqq.fileflow.application.download.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.dto.response.CallbackPayload;
import com.ryuqq.fileflow.application.download.exception.PermanentCallbackFailureException;
import com.ryuqq.fileflow.application.download.manager.client.CallbackNotificationManager;
import com.ryuqq.fileflow.application.download.manager.command.CallbackOutboxCommandManager;
import com.ryuqq.fileflow.application.download.manager.query.DownloadReadManager;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
import com.ryuqq.fileflow.domain.download.id.CallbackOutboxId;
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
@DisplayName("ProcessCallbackOutboxService 단위 테스트")
class ProcessCallbackOutboxServiceTest {

    @InjectMocks private ProcessCallbackOutboxService sut;
    @Mock private CallbackOutboxCommandManager callbackOutboxCommandManager;
    @Mock private CallbackNotificationManager callbackNotificationManager;
    @Mock private DownloadReadManager downloadReadManager;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("PENDING 아웃박스가 없으면 empty 결과를 반환한다")
        void execute_NoPending_ReturnsEmpty() {
            given(callbackOutboxCommandManager.claimPendingMessages(10)).willReturn(List.of());

            SchedulerBatchProcessingResult result = sut.execute(10);

            assertThat(result.total()).isZero();
        }

        @Test
        @DisplayName("COMPLETED 상태 콜백 전송 성공 시 bulkMarkSent가 호출된다")
        void execute_CompletedCallback_Success() {
            CallbackOutbox outbox =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-001"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);
            DownloadTask task = DownloadTaskFixture.aCompletedTask();

            given(callbackOutboxCommandManager.claimPendingMessages(10))
                    .willReturn(List.of(outbox));
            given(downloadReadManager.getDownloadTask("download-001")).willReturn(task);

            SchedulerBatchProcessingResult result = sut.execute(10);

            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            then(callbackNotificationManager)
                    .should()
                    .notify(eq("https://callback.example.com/done"), any(CallbackPayload.class));
            then(callbackOutboxCommandManager)
                    .should()
                    .bulkMarkSent(eq(List.of("outbox-001")), any());
        }

        @Test
        @DisplayName("FAILED 상태 콜백 전송 성공 시 bulkMarkSent가 호출된다")
        void execute_FailedCallback_Success() {
            CallbackOutbox outbox =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-002"),
                            "download-001",
                            "https://callback.example.com/done",
                            "FAILED",
                            NOW);
            DownloadTask task = DownloadTaskFixture.aFailedTask();

            given(callbackOutboxCommandManager.claimPendingMessages(10))
                    .willReturn(List.of(outbox));
            given(downloadReadManager.getDownloadTask("download-001")).willReturn(task);

            SchedulerBatchProcessingResult result = sut.execute(10);

            assertThat(result.success()).isEqualTo(1);
        }

        @Test
        @DisplayName("PermanentCallbackFailureException 발생 시 영구 실패로 마킹한다")
        void execute_PermanentFailure_MarksFailedPermanently() {
            CallbackOutbox outbox =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-003"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);
            DownloadTask task = DownloadTaskFixture.aCompletedTask();

            given(callbackOutboxCommandManager.claimPendingMessages(10))
                    .willReturn(List.of(outbox));
            given(downloadReadManager.getDownloadTask("download-001")).willReturn(task);
            willThrow(new PermanentCallbackFailureException("404 Not Found"))
                    .given(callbackNotificationManager)
                    .notify(any(), any());

            SchedulerBatchProcessingResult result = sut.execute(10);

            assertThat(result.failed()).isEqualTo(1);
            then(callbackOutboxCommandManager).should().persist(outbox);
        }

        @Test
        @DisplayName("일반 예외 발생 시 bulkMarkFailed가 호출된다")
        void execute_GeneralException_BulkMarksFailed() {
            CallbackOutbox outbox =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-004"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);
            DownloadTask task = DownloadTaskFixture.aCompletedTask();

            given(callbackOutboxCommandManager.claimPendingMessages(10))
                    .willReturn(List.of(outbox));
            given(downloadReadManager.getDownloadTask("download-001")).willReturn(task);
            willThrow(new RuntimeException("Connection refused"))
                    .given(callbackNotificationManager)
                    .notify(any(), any());

            SchedulerBatchProcessingResult result = sut.execute(10);

            assertThat(result.failed()).isEqualTo(1);
            then(callbackOutboxCommandManager)
                    .should()
                    .bulkMarkFailed(
                            eq(List.of("outbox-004")), any(), eq("Callback notification failed"));
        }

        @Test
        @DisplayName("bulkMarkSent 예외 발생 시 claimed 전체를 bulkMarkFailed 처리한다")
        void execute_BulkMarkSentException_FallbackToFailed() {
            CallbackOutbox outbox =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-001"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);
            DownloadTask task = DownloadTaskFixture.aCompletedTask();

            given(callbackOutboxCommandManager.claimPendingMessages(10))
                    .willReturn(List.of(outbox));
            given(downloadReadManager.getDownloadTask("download-001")).willReturn(task);
            willThrow(new RuntimeException("DB connection failed"))
                    .given(callbackOutboxCommandManager)
                    .bulkMarkSent(any(), any());

            SchedulerBatchProcessingResult result = sut.execute(10);

            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(1);
            then(callbackOutboxCommandManager)
                    .should()
                    .bulkMarkFailed(eq(List.of("outbox-001")), any(), eq("DB connection failed"));
        }

        @Test
        @DisplayName("여러 건 모두 성공하면 전체 success로 집계된다")
        void execute_MultipleSuccess_AllMarkedSent() {
            CallbackOutbox outbox1 =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-001"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);
            CallbackOutbox outbox2 =
                    CallbackOutbox.forNew(
                            CallbackOutboxId.of("outbox-002"),
                            "download-001",
                            "https://callback.example.com/done",
                            "COMPLETED",
                            NOW);

            DownloadTask task = DownloadTaskFixture.aCompletedTask();

            given(callbackOutboxCommandManager.claimPendingMessages(10))
                    .willReturn(List.of(outbox1, outbox2));
            given(downloadReadManager.getDownloadTask("download-001")).willReturn(task);

            SchedulerBatchProcessingResult result = sut.execute(10);

            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(2);
            assertThat(result.failed()).isZero();
        }
    }
}

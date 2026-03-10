package com.ryuqq.fileflow.application.download.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.application.common.dto.result.OutboxBatchSendResult;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.manager.client.DownloadQueueManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadQueueOutboxCommandManager;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import com.ryuqq.fileflow.domain.download.id.DownloadQueueOutboxId;
import java.time.Instant;
import java.util.Collections;
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
@DisplayName("ProcessDownloadQueueOutboxService 단위 테스트")
class ProcessDownloadQueueOutboxServiceTest {

    @InjectMocks private ProcessDownloadQueueOutboxService sut;
    @Mock private DownloadQueueOutboxCommandManager outboxCommandManager;
    @Mock private DownloadQueueManager downloadQueueManager;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("PENDING 메시지가 없으면 empty 결과를 반환한다")
        void execute_NoPending_ReturnsEmpty() {
            given(outboxCommandManager.claimPendingMessages(100))
                    .willReturn(Collections.emptyList());

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
        }

        @Test
        @DisplayName("PENDING 메시지를 성공적으로 발행하면 SENT로 마킹한다")
        void execute_SuccessfulEnqueue_MarksSent() {
            DownloadQueueOutbox outbox =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-001"), "download-001", NOW);
            given(outboxCommandManager.claimPendingMessages(100)).willReturn(List.of(outbox));
            given(downloadQueueManager.enqueueBatch(List.of("download-001")))
                    .willReturn(OutboxBatchSendResult.allSuccess(List.of("download-001")));

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            then(outboxCommandManager).should().bulkMarkSent(eq(List.of("outbox-001")), any());
        }

        @Test
        @DisplayName("발행 실패 시 bulkMarkFailed가 호출된다")
        void execute_FailedEnqueue_BulkMarksFailed() {
            DownloadQueueOutbox outbox =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-001"), "download-001", NOW);
            given(outboxCommandManager.claimPendingMessages(100)).willReturn(List.of(outbox));
            given(downloadQueueManager.enqueueBatch(List.of("download-001")))
                    .willReturn(
                            OutboxBatchSendResult.of(
                                    List.of(),
                                    List.of(
                                            new OutboxBatchSendResult.FailedEntry(
                                                    "download-001", "SQS error"))));

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(1);
            then(outboxCommandManager).should().bulkMarkFailed(eq(List.of("outbox-001")), any());
        }

        @Test
        @DisplayName("enqueueBatch 예외 발생 시 claimed 전체를 bulkMarkFailed 처리한다")
        void execute_EnqueueException_BulkMarksFailedAll() {
            DownloadQueueOutbox outbox1 =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-001"), "download-001", NOW);
            DownloadQueueOutbox outbox2 =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-002"), "download-002", NOW);
            given(outboxCommandManager.claimPendingMessages(100))
                    .willReturn(List.of(outbox1, outbox2));
            willThrow(new RuntimeException("SQS connection failed"))
                    .given(downloadQueueManager)
                    .enqueueBatch(any());

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(2);
            then(outboxCommandManager)
                    .should()
                    .bulkMarkFailed(eq(List.of("outbox-001", "outbox-002")), any());
        }

        @Test
        @DisplayName("복수 메시지 중 일부 성공, 일부 실패 시 각각 카운트한다")
        void execute_MixedResults_CountsCorrectly() {
            DownloadQueueOutbox successOutbox =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-001"), "download-001", NOW);
            DownloadQueueOutbox failOutbox =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-002"), "download-002", NOW);
            given(outboxCommandManager.claimPendingMessages(100))
                    .willReturn(List.of(successOutbox, failOutbox));
            given(downloadQueueManager.enqueueBatch(List.of("download-001", "download-002")))
                    .willReturn(
                            OutboxBatchSendResult.of(
                                    List.of("download-001"),
                                    List.of(
                                            new OutboxBatchSendResult.FailedEntry(
                                                    "download-002", "SQS error"))));

            SchedulerBatchProcessingResult result = sut.execute(100);

            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
        }
    }
}

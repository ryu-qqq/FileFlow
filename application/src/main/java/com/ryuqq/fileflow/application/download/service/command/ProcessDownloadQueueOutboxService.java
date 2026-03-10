package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.common.dto.result.OutboxBatchSendResult;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.manager.client.DownloadQueueManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadQueueOutboxCommandManager;
import com.ryuqq.fileflow.application.download.port.in.command.ProcessDownloadQueueOutboxUseCase;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessDownloadQueueOutboxService implements ProcessDownloadQueueOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ProcessDownloadQueueOutboxService.class);

    private final DownloadQueueOutboxCommandManager outboxCommandManager;
    private final DownloadQueueManager downloadQueueManager;

    public ProcessDownloadQueueOutboxService(
            DownloadQueueOutboxCommandManager outboxCommandManager,
            DownloadQueueManager downloadQueueManager) {
        this.outboxCommandManager = outboxCommandManager;
        this.downloadQueueManager = downloadQueueManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(int batchSize) {
        List<DownloadQueueOutbox> claimed = outboxCommandManager.claimPendingMessages(batchSize);
        if (claimed.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        List<String> claimedOutboxIds = claimed.stream().map(DownloadQueueOutbox::idValue).toList();

        try {
            List<String> taskIds =
                    claimed.stream().map(DownloadQueueOutbox::downloadTaskId).toList();

            OutboxBatchSendResult sendResult = downloadQueueManager.enqueueBatch(taskIds);

            Instant now = Instant.now();

            List<String> successOutboxIds =
                    claimed.stream()
                            .filter(o -> sendResult.successIds().contains(o.downloadTaskId()))
                            .map(DownloadQueueOutbox::idValue)
                            .toList();
            outboxCommandManager.bulkMarkSent(successOutboxIds, now);

            List<String> failedOutboxIds =
                    claimed.stream()
                            .filter(
                                    o ->
                                            sendResult.failedEntries().stream()
                                                    .anyMatch(
                                                            f -> f.id().equals(o.downloadTaskId())))
                            .map(DownloadQueueOutbox::idValue)
                            .toList();
            outboxCommandManager.bulkMarkFailed(failedOutboxIds, now);

            if (sendResult.hasFailures()) {
                log.warn(
                        "다운로드 큐 배치 발행 부분 실패: total={}, success={}, failed={}",
                        claimed.size(),
                        successOutboxIds.size(),
                        failedOutboxIds.size());
            }

            return SchedulerBatchProcessingResult.of(
                    claimed.size(), successOutboxIds.size(), failedOutboxIds.size());
        } catch (Exception e) {
            log.error(
                    "다운로드 큐 배치 발행 중 예외 발생, PROCESSING → FAILED 복귀: count={}",
                    claimedOutboxIds.size(),
                    e);
            outboxCommandManager.bulkMarkFailed(claimedOutboxIds, Instant.now());
            return SchedulerBatchProcessingResult.of(claimed.size(), 0, claimed.size());
        }
    }
}

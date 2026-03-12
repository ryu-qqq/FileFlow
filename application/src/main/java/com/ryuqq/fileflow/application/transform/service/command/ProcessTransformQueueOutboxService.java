package com.ryuqq.fileflow.application.transform.service.command;

import com.ryuqq.fileflow.application.common.dto.result.OutboxBatchSendResult;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.transform.manager.client.TransformQueueManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformQueueOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.port.in.command.ProcessTransformQueueOutboxUseCase;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformQueueOutbox;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessTransformQueueOutboxService implements ProcessTransformQueueOutboxUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ProcessTransformQueueOutboxService.class);

    private final TransformQueueOutboxCommandManager outboxCommandManager;
    private final TransformQueueManager transformQueueManager;

    public ProcessTransformQueueOutboxService(
            TransformQueueOutboxCommandManager outboxCommandManager,
            TransformQueueManager transformQueueManager) {
        this.outboxCommandManager = outboxCommandManager;
        this.transformQueueManager = transformQueueManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(int batchSize) {
        List<TransformQueueOutbox> claimed = outboxCommandManager.claimPendingMessages(batchSize);
        if (claimed.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        List<String> claimedOutboxIds =
                claimed.stream().map(TransformQueueOutbox::idValue).toList();

        try {
            List<String> requestIds =
                    claimed.stream().map(TransformQueueOutbox::transformRequestId).toList();

            OutboxBatchSendResult sendResult = transformQueueManager.enqueueBatch(requestIds);

            Instant now = Instant.now();

            List<String> successOutboxIds =
                    claimed.stream()
                            .filter(o -> sendResult.successIds().contains(o.transformRequestId()))
                            .map(TransformQueueOutbox::idValue)
                            .toList();
            outboxCommandManager.bulkMarkSent(successOutboxIds, now);

            List<String> failedOutboxIds =
                    claimed.stream()
                            .filter(
                                    o ->
                                            sendResult.failedEntries().stream()
                                                    .anyMatch(
                                                            f ->
                                                                    f.id().equals(
                                                                                    o
                                                                                            .transformRequestId())))
                            .map(TransformQueueOutbox::idValue)
                            .toList();

            if (sendResult.hasFailures()) {
                String errorSummary =
                        sendResult.failedEntries().stream()
                                .map(OutboxBatchSendResult.FailedEntry::errorMessage)
                                .distinct()
                                .limit(3)
                                .collect(java.util.stream.Collectors.joining("; "));
                outboxCommandManager.bulkMarkFailed(failedOutboxIds, now, errorSummary);
                log.warn(
                        "변환 큐 배치 발행 부분 실패: total={}, success={}, failed={}, error={}",
                        claimed.size(),
                        successOutboxIds.size(),
                        failedOutboxIds.size(),
                        errorSummary);
            }

            return SchedulerBatchProcessingResult.of(
                    claimed.size(), successOutboxIds.size(), failedOutboxIds.size());
        } catch (Exception e) {
            log.error(
                    "변환 큐 배치 발행 중 예외 발생, PROCESSING → FAILED 복귀: count={}",
                    claimedOutboxIds.size(),
                    e);
            outboxCommandManager.bulkMarkFailed(claimedOutboxIds, Instant.now(), e.getMessage());
            return SchedulerBatchProcessingResult.of(claimed.size(), 0, claimed.size());
        }
    }
}

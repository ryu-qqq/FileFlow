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
                                                        f -> f.id().equals(o.transformRequestId())))
                        .map(TransformQueueOutbox::idValue)
                        .toList();
        outboxCommandManager.bulkMarkFailed(failedOutboxIds, now);

        if (sendResult.hasFailures()) {
            log.warn(
                    "변환 큐 배치 발행 부분 실패: total={}, success={}, failed={}",
                    claimed.size(),
                    successOutboxIds.size(),
                    failedOutboxIds.size());
        }

        return SchedulerBatchProcessingResult.of(
                claimed.size(), successOutboxIds.size(), failedOutboxIds.size());
    }
}

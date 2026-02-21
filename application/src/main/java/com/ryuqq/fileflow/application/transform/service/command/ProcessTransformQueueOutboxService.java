package com.ryuqq.fileflow.application.transform.service.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.transform.manager.client.TransformQueueManager;
import com.ryuqq.fileflow.application.transform.manager.command.TransformQueueOutboxCommandManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformQueueOutboxReadManager;
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

    private final TransformQueueOutboxReadManager outboxReadManager;
    private final TransformQueueOutboxCommandManager outboxCommandManager;
    private final TransformQueueManager transformQueueManager;

    public ProcessTransformQueueOutboxService(
            TransformQueueOutboxReadManager outboxReadManager,
            TransformQueueOutboxCommandManager outboxCommandManager,
            TransformQueueManager transformQueueManager) {
        this.outboxReadManager = outboxReadManager;
        this.outboxCommandManager = outboxCommandManager;
        this.transformQueueManager = transformQueueManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(int batchSize) {
        List<TransformQueueOutbox> pending = outboxReadManager.findPendingMessages(batchSize);
        if (pending.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        int success = 0;
        int failed = 0;

        Instant now = Instant.now();
        for (TransformQueueOutbox outbox : pending) {
            try {
                transformQueueManager.enqueue(outbox.transformRequestId());
                outbox.markSent(now);
                success++;
            } catch (Exception e) {
                log.error(
                        "변환 큐 아웃박스 발행 실패: outboxId={}, transformRequestId={}",
                        outbox.idValue(),
                        outbox.transformRequestId(),
                        e);
                outbox.markFailed(e.getMessage(), now);
                failed++;
            }
            outboxCommandManager.persist(outbox);
        }

        return SchedulerBatchProcessingResult.of(pending.size(), success, failed);
    }
}

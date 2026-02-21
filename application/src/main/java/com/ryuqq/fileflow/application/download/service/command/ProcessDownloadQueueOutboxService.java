package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.manager.client.DownloadQueueManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadQueueOutboxCommandManager;
import com.ryuqq.fileflow.application.download.manager.query.DownloadQueueOutboxReadManager;
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

    private final DownloadQueueOutboxReadManager outboxReadManager;
    private final DownloadQueueOutboxCommandManager outboxCommandManager;
    private final DownloadQueueManager downloadQueueManager;

    public ProcessDownloadQueueOutboxService(
            DownloadQueueOutboxReadManager outboxReadManager,
            DownloadQueueOutboxCommandManager outboxCommandManager,
            DownloadQueueManager downloadQueueManager) {
        this.outboxReadManager = outboxReadManager;
        this.outboxCommandManager = outboxCommandManager;
        this.downloadQueueManager = downloadQueueManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(int batchSize) {
        List<DownloadQueueOutbox> pending = outboxReadManager.findPendingMessages(batchSize);
        if (pending.isEmpty()) {
            return SchedulerBatchProcessingResult.empty();
        }

        int success = 0;
        int failed = 0;

        Instant now = Instant.now();
        for (DownloadQueueOutbox outbox : pending) {
            try {
                downloadQueueManager.enqueue(outbox.downloadTaskId());
                outbox.markSent(now);
                success++;
            } catch (Exception e) {
                log.error(
                        "다운로드 큐 아웃박스 발행 실패: outboxId={}, downloadTaskId={}",
                        outbox.idValue(),
                        outbox.downloadTaskId(),
                        e);
                outbox.markFailed(e.getMessage(), now);
                failed++;
            }
            outboxCommandManager.persist(outbox);
        }

        return SchedulerBatchProcessingResult.of(pending.size(), success, failed);
    }
}

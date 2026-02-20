package com.ryuqq.fileflow.adapter.in.scheduler.download;

import com.ryuqq.fileflow.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.port.in.command.ProcessDownloadQueueOutboxUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.download-queue-outbox",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DownloadQueueOutboxScheduler {

    private final ProcessDownloadQueueOutboxUseCase processDownloadQueueOutboxUseCase;
    private final SchedulerProperties.DownloadQueueOutbox config;

    public DownloadQueueOutboxScheduler(
            ProcessDownloadQueueOutboxUseCase processDownloadQueueOutboxUseCase,
            SchedulerProperties schedulerProperties) {
        this.processDownloadQueueOutboxUseCase = processDownloadQueueOutboxUseCase;
        this.config = schedulerProperties.jobs().downloadQueueOutbox();
    }

    @Scheduled(
            cron = "${scheduler.jobs.download-queue-outbox.cron}",
            zone = "${scheduler.jobs.download-queue-outbox.timezone}")
    @SchedulerJob("DownloadQueueOutbox")
    public SchedulerBatchProcessingResult processOutbox() {
        return processDownloadQueueOutboxUseCase.execute(config.batchSize());
    }
}

package com.ryuqq.fileflow.adapter.in.scheduler.transform;

import com.ryuqq.fileflow.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.transform.port.in.command.ProcessTransformQueueOutboxUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.transform-queue-outbox",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class TransformQueueOutboxScheduler {

    private final ProcessTransformQueueOutboxUseCase processTransformQueueOutboxUseCase;
    private final SchedulerProperties.TransformQueueOutbox config;

    public TransformQueueOutboxScheduler(
            ProcessTransformQueueOutboxUseCase processTransformQueueOutboxUseCase,
            SchedulerProperties schedulerProperties) {
        this.processTransformQueueOutboxUseCase = processTransformQueueOutboxUseCase;
        this.config = schedulerProperties.jobs().transformQueueOutbox();
    }

    @Scheduled(
            cron = "${scheduler.jobs.transform-queue-outbox.cron}",
            zone = "${scheduler.jobs.transform-queue-outbox.timezone}")
    @SchedulerJob("TransformQueueOutbox")
    public SchedulerBatchProcessingResult processOutbox() {
        return processTransformQueueOutboxUseCase.execute(config.batchSize());
    }
}

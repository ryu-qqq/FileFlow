package com.ryuqq.fileflow.adapter.in.scheduler.transform;

import com.ryuqq.fileflow.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.transform.port.in.command.ProcessTransformCallbackOutboxUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.transform-callback-outbox",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class TransformCallbackOutboxScheduler {

    private static final int MAX_LOOPS = 10;

    private final ProcessTransformCallbackOutboxUseCase processTransformCallbackOutboxUseCase;
    private final SchedulerProperties.TransformCallbackOutbox config;

    public TransformCallbackOutboxScheduler(
            ProcessTransformCallbackOutboxUseCase processTransformCallbackOutboxUseCase,
            SchedulerProperties schedulerProperties) {
        this.processTransformCallbackOutboxUseCase = processTransformCallbackOutboxUseCase;
        this.config = schedulerProperties.jobs().transformCallbackOutbox();
    }

    @Scheduled(
            cron = "${scheduler.jobs.transform-callback-outbox.cron}",
            zone = "${scheduler.jobs.transform-callback-outbox.timezone}")
    @SchedulerJob("TransformCallbackOutbox")
    public SchedulerBatchProcessingResult processOutbox() {
        SchedulerBatchProcessingResult total = SchedulerBatchProcessingResult.empty();
        for (int i = 0; i < MAX_LOOPS; i++) {
            SchedulerBatchProcessingResult result =
                    processTransformCallbackOutboxUseCase.execute(config.batchSize());
            total = total.merge(result);
            if (result.total() < config.batchSize()) {
                break;
            }
        }
        return total;
    }
}

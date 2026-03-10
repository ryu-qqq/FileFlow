package com.ryuqq.fileflow.adapter.in.scheduler.download;

import com.ryuqq.fileflow.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.port.in.command.ProcessCallbackOutboxUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.callback-outbox",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class CallbackOutboxScheduler {

    private static final int MAX_LOOPS = 10;

    private final ProcessCallbackOutboxUseCase processCallbackOutboxUseCase;
    private final SchedulerProperties.CallbackOutbox config;

    public CallbackOutboxScheduler(
            ProcessCallbackOutboxUseCase processCallbackOutboxUseCase,
            SchedulerProperties schedulerProperties) {
        this.processCallbackOutboxUseCase = processCallbackOutboxUseCase;
        this.config = schedulerProperties.jobs().callbackOutbox();
    }

    @Scheduled(
            cron = "${scheduler.jobs.callback-outbox.cron}",
            zone = "${scheduler.jobs.callback-outbox.timezone}")
    @SchedulerJob("CallbackOutbox")
    public SchedulerBatchProcessingResult processOutbox() {
        SchedulerBatchProcessingResult total = SchedulerBatchProcessingResult.empty();
        for (int i = 0; i < MAX_LOOPS; i++) {
            SchedulerBatchProcessingResult result =
                    processCallbackOutboxUseCase.execute(config.batchSize());
            total = total.merge(result);
            if (result.total() < config.batchSize()) {
                break;
            }
        }
        return total;
    }
}

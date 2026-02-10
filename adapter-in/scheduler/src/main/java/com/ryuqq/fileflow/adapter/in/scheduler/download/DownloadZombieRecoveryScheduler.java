package com.ryuqq.fileflow.adapter.in.scheduler.download;

import com.ryuqq.fileflow.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.dto.command.RecoverZombieDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.port.in.command.RecoverZombieDownloadTaskUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 다운로드 좀비 태스크 복구 스케줄러.
 *
 * <p>QUEUED 상태에서 일정 시간 이상 처리되지 않은 다운로드 태스크를 감지하여 SQS에 재큐잉합니다.
 *
 * <p>SQS 발행 실패 또는 워커 처리 실패로 인해 고아 상태로 남은 태스크를 복구하는 안전망 역할을 합니다.
 *
 * @see SchedulerProperties
 */
@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.download-zombie-recovery",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DownloadZombieRecoveryScheduler {

    private final RecoverZombieDownloadTaskUseCase recoverZombieDownloadTaskUseCase;
    private final SchedulerProperties.DownloadZombieRecovery config;

    public DownloadZombieRecoveryScheduler(
            RecoverZombieDownloadTaskUseCase recoverZombieDownloadTaskUseCase,
            SchedulerProperties schedulerProperties) {
        this.recoverZombieDownloadTaskUseCase = recoverZombieDownloadTaskUseCase;
        this.config = schedulerProperties.jobs().downloadZombieRecovery();
    }

    /**
     * QUEUED 상태에서 타임아웃된 좀비 다운로드 태스크를 복구합니다.
     *
     * <p>QUEUED 상태에서 설정된 타임아웃 시간 이상 경과한 태스크를 SQS에 재큐잉합니다.
     */
    @Scheduled(
            cron = "${scheduler.jobs.download-zombie-recovery.cron}",
            zone = "${scheduler.jobs.download-zombie-recovery.timezone}")
    @SchedulerJob("DownloadZombieRecovery")
    public SchedulerBatchProcessingResult recoverZombieTasks() {
        RecoverZombieDownloadTaskCommand command =
                RecoverZombieDownloadTaskCommand.of(config.batchSize(), config.timeoutSeconds());
        return recoverZombieDownloadTaskUseCase.execute(command);
    }
}

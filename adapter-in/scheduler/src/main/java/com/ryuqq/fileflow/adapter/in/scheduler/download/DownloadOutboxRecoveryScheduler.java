package com.ryuqq.fileflow.adapter.in.scheduler.download;

import com.ryuqq.fileflow.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.download.port.in.command.RecoverStuckOutboxUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 다운로드 Outbox PROCESSING 상태 복구 스케줄러.
 *
 * <p>PROCESSING 상태에서 일정 시간 이상 멈춰있는 Outbox 레코드를 감지하여 PENDING으로 복구합니다.
 *
 * <p>Atomic Claim(PENDING → PROCESSING) 이후 예외 발생으로 SENT/FAILED 전환이 안 된 레코드를 복구하는 안전망 역할을 합니다.
 *
 * @see SchedulerProperties
 */
@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.download-outbox-recovery",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DownloadOutboxRecoveryScheduler {

    private final RecoverStuckOutboxUseCase recoverStuckOutboxUseCase;
    private final SchedulerProperties.DownloadOutboxRecovery config;

    public DownloadOutboxRecoveryScheduler(
            RecoverStuckOutboxUseCase recoverStuckOutboxUseCase,
            SchedulerProperties schedulerProperties) {
        this.recoverStuckOutboxUseCase = recoverStuckOutboxUseCase;
        this.config = schedulerProperties.jobs().downloadOutboxRecovery();
    }

    /**
     * PROCESSING 상태에서 타임아웃된 다운로드 Outbox 레코드를 PENDING으로 복구합니다.
     *
     * @return 복구된 레코드 수
     */
    @Scheduled(
            cron = "${scheduler.jobs.download-outbox-recovery.cron}",
            zone = "${scheduler.jobs.download-outbox-recovery.timezone}")
    @SchedulerJob("DownloadOutboxRecovery")
    public int recover() {
        return recoverStuckOutboxUseCase.execute(config.stuckMinutes());
    }
}

package com.ryuqq.fileflow.adapter.in.scheduler.session;

import com.ryuqq.fileflow.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.session.port.in.command.RecoverExpiredMultipartSessionUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 멀티파트 업로드 세션 좀비 복구 스케줄러.
 *
 * <p>만료 시간이 지났지만 활성 상태(INITIATED, UPLOADING)로 남아있는 고아 Multipart 세션을 만료 처리합니다.
 *
 * <p>Redis keyspace notification 유실로 인해 만료 전이가 누락된 세션을 복구하는 안전망 역할을 합니다.
 */
@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.multipart-session-zombie-recovery",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class MultipartSessionZombieRecoveryScheduler {

    private final RecoverExpiredMultipartSessionUseCase recoverExpiredMultipartSessionUseCase;
    private final SchedulerProperties.MultipartSessionZombieRecovery config;

    public MultipartSessionZombieRecoveryScheduler(
            RecoverExpiredMultipartSessionUseCase recoverExpiredMultipartSessionUseCase,
            SchedulerProperties schedulerProperties) {
        this.recoverExpiredMultipartSessionUseCase = recoverExpiredMultipartSessionUseCase;
        this.config = schedulerProperties.jobs().multipartSessionZombieRecovery();
    }

    @Scheduled(
            cron = "${scheduler.jobs.multipart-session-zombie-recovery.cron}",
            zone = "${scheduler.jobs.multipart-session-zombie-recovery.timezone}")
    @SchedulerJob("MultipartSessionZombieRecovery")
    public SchedulerBatchProcessingResult recoverExpiredSessions() {
        return recoverExpiredMultipartSessionUseCase.execute(config.batchSize());
    }
}

package com.ryuqq.fileflow.adapter.in.scheduler.transform;

import com.ryuqq.fileflow.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.fileflow.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.transform.dto.command.RecoverZombieTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.port.in.command.RecoverZombieTransformRequestUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 변환 좀비 요청 복구 스케줄러.
 *
 * <p>QUEUED 상태에서 일정 시간 이상 처리되지 않은 변환 요청을 감지하여 SQS에 재큐잉합니다.
 *
 * <p>SQS 발행 실패 또는 워커 처리 실패로 인해 고아 상태로 남은 요청을 복구하는 안전망 역할을 합니다.
 *
 * @see SchedulerProperties
 */
@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.transform-zombie-recovery",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class TransformZombieRecoveryScheduler {

    private final RecoverZombieTransformRequestUseCase recoverZombieTransformRequestUseCase;
    private final SchedulerProperties.TransformZombieRecovery config;

    public TransformZombieRecoveryScheduler(
            RecoverZombieTransformRequestUseCase recoverZombieTransformRequestUseCase,
            SchedulerProperties schedulerProperties) {
        this.recoverZombieTransformRequestUseCase = recoverZombieTransformRequestUseCase;
        this.config = schedulerProperties.jobs().transformZombieRecovery();
    }

    /**
     * QUEUED 상태에서 타임아웃된 좀비 변환 요청을 복구합니다.
     *
     * <p>QUEUED 상태에서 설정된 타임아웃 시간 이상 경과한 요청을 SQS에 재큐잉합니다.
     */
    @Scheduled(
            cron = "${scheduler.jobs.transform-zombie-recovery.cron}",
            zone = "${scheduler.jobs.transform-zombie-recovery.timezone}")
    @SchedulerJob("TransformZombieRecovery")
    public SchedulerBatchProcessingResult recoverZombieRequests() {
        RecoverZombieTransformRequestCommand command =
                RecoverZombieTransformRequestCommand.of(
                        config.batchSize(), config.timeoutSeconds());
        return recoverZombieTransformRequestUseCase.execute(command);
    }
}

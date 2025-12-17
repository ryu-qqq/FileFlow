package com.ryuqq.fileflow.application.download.scheduler;

import com.ryuqq.fileflow.application.common.metrics.SchedulerMetrics;
import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.application.download.port.in.command.RetryUnsentWebhookUseCase;
import com.ryuqq.fileflow.application.download.port.in.command.RetryUnsentWebhookUseCase.RetryResult;
import com.ryuqq.fileflow.domain.download.vo.OutboxRetryLockKey;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Webhook Outbox 재시도 스케줄러.
 *
 * <p>Webhook 호출에 실패한 Outbox를 주기적으로 조회하여 재시도합니다.
 *
 * <p><strong>실행 주기</strong>: 5분
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>분산 락 획득 시도
 *   <li>PENDING 상태의 WebhookOutbox 조회
 *   <li>각 Outbox에 대해 Webhook 호출 재시도
 *   <li>성공 시 markAsSent(), 최대 재시도 초과 시 markAsFailed()
 *   <li>분산 락 해제
 * </ol>
 *
 * <p><strong>분산 락</strong>: 다중 인스턴스 환경에서 중복 실행 방지
 *
 * <p><strong>활성화 조건</strong>: {@code scheduler.webhook-retry.enabled=true}
 */
@Component
@ConditionalOnProperty(
        name = "scheduler.webhook-retry.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class WebhookOutboxRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(WebhookOutboxRetryScheduler.class);

    private static final String JOB_NAME = "webhook-retry";
    private static final long LOCK_WAIT_TIME = 10;
    private static final long LOCK_LEASE_TIME = 300;

    private final RetryUnsentWebhookUseCase retryUnsentWebhookUseCase;
    private final DistributedLockPort distributedLockPort;
    private final SchedulerMetrics schedulerMetrics;

    public WebhookOutboxRetryScheduler(
            RetryUnsentWebhookUseCase retryUnsentWebhookUseCase,
            DistributedLockPort distributedLockPort,
            SchedulerMetrics schedulerMetrics) {
        this.retryUnsentWebhookUseCase = retryUnsentWebhookUseCase;
        this.distributedLockPort = distributedLockPort;
        this.schedulerMetrics = schedulerMetrics;
    }

    /**
     * PENDING 상태의 Webhook Outbox를 재시도합니다.
     *
     * <p>5분마다 실행됩니다.
     *
     * <p><strong>분산 락</strong>: 다중 인스턴스 환경에서 중복 실행 방지
     */
    @Scheduled(fixedRate = 300000) // 5분
    public void retryUnsentWebhooks() {
        OutboxRetryLockKey lockKey = OutboxRetryLockKey.webhook();

        boolean lockAcquired =
                distributedLockPort.tryLock(
                        lockKey, LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
        if (!lockAcquired) {
            log.info("[WebhookRetry] 락 획득 실패, 다른 인스턴스가 실행 중");
            return;
        }

        log.info("Starting Webhook outbox retry");
        Timer.Sample sample = schedulerMetrics.startJob(JOB_NAME);

        try {
            RetryResult result = retryUnsentWebhookUseCase.execute();

            schedulerMetrics.recordJobItemsProcessed(JOB_NAME, result.totalRetried());
            schedulerMetrics.recordJobSuccess(JOB_NAME, sample);

            log.info(
                    "Webhook outbox retry completed. Retried: {}, Succeeded: {}, Failed: {},"
                            + " Iterations: {}",
                    result.totalRetried(),
                    result.succeeded(),
                    result.failed(),
                    result.iterations());

        } catch (Exception e) {
            schedulerMetrics.recordJobFailure(JOB_NAME, sample, e.getClass().getSimpleName());
            log.error("Webhook outbox retry failed", e);
            throw e;
        } finally {
            distributedLockPort.unlock(lockKey);
        }
    }
}

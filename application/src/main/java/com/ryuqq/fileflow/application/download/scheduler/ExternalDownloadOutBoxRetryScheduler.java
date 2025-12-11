package com.ryuqq.fileflow.application.download.scheduler;

import com.ryuqq.fileflow.application.common.metrics.SchedulerMetrics;
import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.application.download.port.in.command.RetryUnpublishedOutboxUseCase;
import com.ryuqq.fileflow.application.download.port.in.command.RetryUnpublishedOutboxUseCase.RetryResult;
import com.ryuqq.fileflow.domain.download.vo.OutboxRetryLockKey;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ExternalDownload Outbox 재시도 스케줄러.
 *
 * <p>SQS 발행에 실패한 Outbox를 주기적으로 조회하여 재시도합니다.
 *
 * <p><strong>실행 주기</strong>: 5분
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>분산 락 획득 시도
 *   <li>미발행(published=false) 상태의 Outbox 조회
 *   <li>각 Outbox에 대해 SQS 메시지 발행 시도
 *   <li>성공 시 markAsPublished, 실패 시 로깅 후 다음 주기에 재시도
 *   <li>분산 락 해제
 * </ol>
 *
 * <p><strong>분산 락</strong>: 다중 인스턴스 환경에서 중복 실행 방지
 *
 * <p><strong>무한 루프 방지</strong>: MAX_ITERATIONS 제한 적용 (UseCase에서 처리)
 *
 * <p><strong>폴백 역할</strong>: {@link
 * com.ryuqq.fileflow.application.download.listener.ExternalDownloadRegisteredEventListener} 에서 SQS
 * 발행 실패 시 이 스케줄러가 재시도합니다.
 *
 * <p><strong>활성화 조건</strong>: {@code scheduler.outbox-retry.enabled=true}
 *
 * @author Development Team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "scheduler.outbox-retry.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class ExternalDownloadOutBoxRetryScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(ExternalDownloadOutBoxRetryScheduler.class);

    private static final String JOB_NAME = "outbox-retry";
    private static final long LOCK_WAIT_TIME = 10;
    private static final long LOCK_LEASE_TIME = 300;

    private final RetryUnpublishedOutboxUseCase retryUnpublishedOutboxUseCase;
    private final DistributedLockPort distributedLockPort;
    private final SchedulerMetrics schedulerMetrics;

    public ExternalDownloadOutBoxRetryScheduler(
            RetryUnpublishedOutboxUseCase retryUnpublishedOutboxUseCase,
            DistributedLockPort distributedLockPort,
            SchedulerMetrics schedulerMetrics) {
        this.retryUnpublishedOutboxUseCase = retryUnpublishedOutboxUseCase;
        this.distributedLockPort = distributedLockPort;
        this.schedulerMetrics = schedulerMetrics;
    }

    /**
     * 미발행 Outbox를 조회하여 SQS로 재발행합니다.
     *
     * <p>5분마다 실행됩니다.
     *
     * <p><strong>분산 락</strong>: 다중 인스턴스 환경에서 중복 실행 방지
     */
    @Scheduled(fixedRate = 300000) // 5분
    public void retryUnpublishedOutboxes() {
        OutboxRetryLockKey lockKey = OutboxRetryLockKey.externalDownload();

        boolean lockAcquired =
                distributedLockPort.tryLock(
                        lockKey, LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
        if (!lockAcquired) {
            log.info("[OutboxRetry] 락 획득 실패, 다른 인스턴스가 실행 중");
            return;
        }

        log.info("Starting ExternalDownload outbox retry");
        Timer.Sample sample = schedulerMetrics.startJob(JOB_NAME);

        try {
            RetryResult result = retryUnpublishedOutboxUseCase.execute();

            schedulerMetrics.recordJobItemsProcessed(JOB_NAME, result.totalRetried());
            schedulerMetrics.recordJobSuccess(JOB_NAME, sample);

            log.info(
                    "ExternalDownload outbox retry completed. Retried: {}, Succeeded: {}, Failed:"
                            + " {}, Iterations: {}",
                    result.totalRetried(),
                    result.succeeded(),
                    result.failed(),
                    result.iterations());

        } catch (Exception e) {
            schedulerMetrics.recordJobFailure(JOB_NAME, sample, e.getClass().getSimpleName());
            log.error("ExternalDownload outbox retry failed", e);
            throw e;
        } finally {
            distributedLockPort.unlock(lockKey);
        }
    }
}

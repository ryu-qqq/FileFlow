package com.ryuqq.fileflow.application.session.scheduler;

import com.ryuqq.fileflow.application.common.metrics.SchedulerMetrics;
import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.application.session.port.in.command.ExpireUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.SessionExpirationLockKey;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Single Upload Session 만료 스케줄러.
 *
 * <p>Redis TTL 만료 이벤트를 놓친 세션을 주기적으로 정리합니다.
 *
 * <p><strong>실행 주기</strong>: 1시간
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>만료 시간이 지난 PREPARING/ACTIVE 상태의 세션 조회
 *   <li>각 세션에 대해 ExpireUploadSessionUseCase 호출
 *   <li>실패한 세션은 로깅 후 다음 주기에 재시도
 * </ol>
 *
 * <p><strong>활성화 조건</strong>: {@code scheduler.session-expiration.enabled=true}
 */
@Component
@ConditionalOnProperty(
        name = "scheduler.session-expiration.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class SingleUploadSessionExpirationScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(SingleUploadSessionExpirationScheduler.class);

    private static final String JOB_NAME = "single-session-expiration";
    private static final int BATCH_SIZE = 100;
    private static final int MAX_ITERATIONS = 100;
    private static final long LOCK_WAIT_TIME = 10;
    private static final long LOCK_LEASE_TIME = 3600;

    private final FindUploadSessionQueryPort findUploadSessionQueryPort;
    private final ExpireUploadSessionUseCase expireUploadSessionUseCase;
    private final DistributedLockPort distributedLockPort;
    private final SchedulerMetrics schedulerMetrics;

    public SingleUploadSessionExpirationScheduler(
            FindUploadSessionQueryPort findUploadSessionQueryPort,
            ExpireUploadSessionUseCase expireUploadSessionUseCase,
            DistributedLockPort distributedLockPort,
            SchedulerMetrics schedulerMetrics) {
        this.findUploadSessionQueryPort = findUploadSessionQueryPort;
        this.expireUploadSessionUseCase = expireUploadSessionUseCase;
        this.distributedLockPort = distributedLockPort;
        this.schedulerMetrics = schedulerMetrics;
    }

    /**
     * 만료된 단일 업로드 세션을 정리합니다.
     *
     * <p>1시간마다 실행됩니다.
     *
     * <p><strong>분산 락</strong>: 다중 인스턴스 환경에서 중복 실행 방지
     *
     * <p><strong>무한 루프 방지</strong>: MAX_ITERATIONS 제한 적용
     */
    @Scheduled(fixedRate = 3600000) // 1시간
    public void expireStalesSingleUploadSessions() {
        SessionExpirationLockKey lockKey = SessionExpirationLockKey.singleUpload();

        boolean lockAcquired =
                distributedLockPort.tryLock(
                        lockKey, LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
        if (!lockAcquired) {
            log.info("[SingleSessionExpiration] 락 획득 실패, 다른 인스턴스가 실행 중");
            return;
        }

        log.info("Starting single upload session expiration cleanup");
        Timer.Sample sample = schedulerMetrics.startJob(JOB_NAME);

        try {
            executeExpiration();
            schedulerMetrics.recordJobSuccess(JOB_NAME, sample);
        } catch (Exception e) {
            schedulerMetrics.recordJobFailure(JOB_NAME, sample, e.getClass().getSimpleName());
            log.error("Single upload session expiration cleanup failed", e);
            throw e;
        } finally {
            distributedLockPort.unlock(lockKey);
        }
    }

    private void executeExpiration() {
        Instant now = Instant.now();
        int totalExpired = 0;
        int totalFailed = 0;
        int iteration = 0;

        List<SingleUploadSession> expiredSessions =
                findUploadSessionQueryPort.findExpiredSingleUploads(now, BATCH_SIZE);

        while (!expiredSessions.isEmpty() && iteration < MAX_ITERATIONS) {
            iteration++;

            for (SingleUploadSession session : expiredSessions) {
                try {
                    expireUploadSessionUseCase.execute(session);
                    totalExpired++;
                    log.debug("Expired single upload session: {}", session.getIdValue());
                } catch (Exception e) {
                    totalFailed++;
                    log.warn(
                            "Failed to expire single upload session: {}. Reason: {}",
                            session.getIdValue(),
                            e.getMessage());
                }
            }

            if (expiredSessions.size() < BATCH_SIZE) {
                break;
            }

            expiredSessions = findUploadSessionQueryPort.findExpiredSingleUploads(now, BATCH_SIZE);
        }

        if (iteration >= MAX_ITERATIONS) {
            log.warn("[SingleSessionExpiration] MAX_ITERATIONS({}) 도달, 다음 주기에 계속", MAX_ITERATIONS);
        }

        schedulerMetrics.recordJobItemsProcessed(JOB_NAME, totalExpired);
        log.info(
                "Single upload session expiration cleanup completed. Expired: {}, Failed: {},"
                        + " Iterations: {}",
                totalExpired,
                totalFailed,
                iteration);
    }
}

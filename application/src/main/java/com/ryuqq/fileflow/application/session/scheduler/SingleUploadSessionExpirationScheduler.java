package com.ryuqq.fileflow.application.session.scheduler;

import com.ryuqq.fileflow.application.common.metrics.SchedulerMetrics;
import com.ryuqq.fileflow.application.session.dto.command.ExpireUploadSessionCommand;
import com.ryuqq.fileflow.application.session.port.in.command.ExpireUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.List;
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

    private final FindUploadSessionQueryPort findUploadSessionQueryPort;
    private final ExpireUploadSessionUseCase expireUploadSessionUseCase;
    private final SchedulerMetrics schedulerMetrics;

    public SingleUploadSessionExpirationScheduler(
            FindUploadSessionQueryPort findUploadSessionQueryPort,
            ExpireUploadSessionUseCase expireUploadSessionUseCase,
            SchedulerMetrics schedulerMetrics) {
        this.findUploadSessionQueryPort = findUploadSessionQueryPort;
        this.expireUploadSessionUseCase = expireUploadSessionUseCase;
        this.schedulerMetrics = schedulerMetrics;
    }

    /**
     * 만료된 단일 업로드 세션을 정리합니다.
     *
     * <p>1시간마다 실행됩니다.
     */
    @Scheduled(fixedRate = 3600000) // 1시간
    public void expireStalesSingleUploadSessions() {
        log.info("Starting single upload session expiration cleanup");
        Timer.Sample sample = schedulerMetrics.startJob(JOB_NAME);

        Instant now = Instant.now();
        int totalExpired = 0;
        int totalFailed = 0;

        try {
            List<SingleUploadSession> expiredSessions =
                    findUploadSessionQueryPort.findExpiredSingleUploads(now, BATCH_SIZE);

            while (!expiredSessions.isEmpty()) {
                for (SingleUploadSession session : expiredSessions) {
                    try {
                        ExpireUploadSessionCommand command =
                                ExpireUploadSessionCommand.of(session.getIdValue());
                        expireUploadSessionUseCase.execute(command);
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

                expiredSessions =
                        findUploadSessionQueryPort.findExpiredSingleUploads(now, BATCH_SIZE);
            }

            schedulerMetrics.recordJobItemsProcessed(JOB_NAME, totalExpired);
            schedulerMetrics.recordJobSuccess(JOB_NAME, sample);

            log.info(
                    "Single upload session expiration cleanup completed. Expired: {}, Failed: {}",
                    totalExpired,
                    totalFailed);

        } catch (Exception e) {
            schedulerMetrics.recordJobFailure(JOB_NAME, sample, e.getClass().getSimpleName());
            log.error("Single upload session expiration cleanup failed", e);
            throw e;
        }
    }
}

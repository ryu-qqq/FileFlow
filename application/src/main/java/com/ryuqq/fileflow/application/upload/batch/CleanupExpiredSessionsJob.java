package com.ryuqq.fileflow.application.upload.batch;

import com.ryuqq.fileflow.application.upload.manager.UploadSessionStateManager;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.FailureReason;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Cleanup Expired Sessions Job
 *
 * <p>만료된 업로드 세션을 자동 정리하는 Batch Job입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>오래된 PENDING 또는 IN_PROGRESS 세션 조회</li>
 *   <li>세션 상태를 FAILED로 전환</li>
 *   <li>리소스 정리 (S3 미완료 업로드 제거 등)</li>
 * </ul>
 *
 * <p><strong>실행 주기:</strong></p>
 * <ul>
 *   <li>Cron: 매일 새벽 2시 (기본값)</li>
 *   <li>설정: {@code batch.cleanup.cron}</li>
 * </ul>
 *
 * <p><strong>만료 기준:</strong></p>
 * <ul>
 *   <li>PENDING: 30분 이상 (설정 가능)</li>
 *   <li>IN_PROGRESS: 24시간 이상 (설정 가능)</li>
 * </ul>
 *
 * <p><strong>성능 최적화:</strong></p>
 * <ul>
 *   <li>✅ DB Index 활용: idx_status_created_at (status, created_at)</li>
 *   <li>✅ Batch 크기 제한: 최대 1000건씩 처리</li>
 *   <li>✅ 트랜잭션 분리: 조회(readOnly) + 업데이트(write)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class CleanupExpiredSessionsJob {

    private static final Logger log = LoggerFactory.getLogger(CleanupExpiredSessionsJob.class);

    private final LoadUploadSessionPort loadUploadSessionPort;
    private final UploadSessionStateManager uploadSessionStateManager;
    private final int pendingExpirationMinutes;
    private final int inProgressExpirationHours;

    /**
     * 생성자
     *
     * @param loadUploadSessionPort Load UploadSession Port (Query)
     * @param uploadSessionStateManager UploadSession State Manager
     * @param pendingExpirationMinutes PENDING 세션 만료 시간 (분)
     * @param inProgressExpirationHours IN_PROGRESS 세션 만료 시간 (시간)
     */
    public CleanupExpiredSessionsJob(
        LoadUploadSessionPort loadUploadSessionPort,
        UploadSessionStateManager uploadSessionStateManager,
        @Value("${batch.cleanup.pending-expiration-minutes:30}") int pendingExpirationMinutes,
        @Value("${batch.cleanup.in-progress-expiration-hours:24}") int inProgressExpirationHours
    ) {
        this.loadUploadSessionPort = loadUploadSessionPort;
        this.uploadSessionStateManager = uploadSessionStateManager;
        this.pendingExpirationMinutes = pendingExpirationMinutes;
        this.inProgressExpirationHours = inProgressExpirationHours;
    }

    /**
     * 만료된 세션 정리 (Scheduled Job)
     *
     * <p><strong>실행 주기:</strong> 매일 새벽 2시 (기본값)</p>
     *
     * <p><strong>실행 순서:</strong></p>
     * <ol>
     *   <li>만료된 PENDING/IN_PROGRESS 세션 조회 (DB Index 활용)</li>
     *   <li>각 세션을 FAILED 상태로 전환</li>
     *   <li>실행 결과 로깅</li>
     * </ol>
     */
    @Scheduled(cron = "${batch.cleanup.cron:0 0 2 * * *}")
    public void cleanupExpiredSessions() {
        log.info("Starting cleanup expired sessions job");

        try {
            // 1. 만료 기준 시간 계산
            LocalDateTime pendingThreshold = LocalDateTime.now().minusMinutes(pendingExpirationMinutes);
            LocalDateTime inProgressThreshold = LocalDateTime.now().minusHours(inProgressExpirationHours);

            // 2. PENDING 세션 정리
            int pendingCleaned = cleanupPendingSessions(pendingThreshold);

            // 3. IN_PROGRESS 세션 정리
            int inProgressCleaned = cleanupInProgressSessions(inProgressThreshold);

            // 4. 결과 로깅
            int totalCleaned = pendingCleaned + inProgressCleaned;
            log.info("Cleanup expired sessions completed: total={}, pending={}, inProgress={}",
                totalCleaned, pendingCleaned, inProgressCleaned);

        } catch (Exception e) {
            log.error("Cleanup expired sessions job failed", e);
        }
    }

    /**
     * 만료된 PENDING 세션 정리
     *
     * <p>⭐ Index 활용: idx_status_created_at (status, created_at)</p>
     *
     * @param threshold 이 시간 이전에 생성된 세션을 만료 처리
     * @return 정리된 세션 개수
     */
    @Transactional(readOnly = true)
    public int cleanupPendingSessions(LocalDateTime threshold) {
        // 1. 만료된 PENDING 세션 조회
        List<UploadSession> expiredSessions = loadUploadSessionPort.findByStatusAndCreatedBefore(
            SessionStatus.PENDING,
            threshold
        );

        log.debug("Found {} expired PENDING sessions before {}", expiredSessions.size(), threshold);

        // 2. 각 세션을 FAILED 상태로 전환
        int count = 0;
        for (UploadSession session : expiredSessions) {
            try {
                failExpiredSession(session, "Session expired (PENDING > " + pendingExpirationMinutes + " minutes)");
                count++;
            } catch (Exception e) {
                log.error("Failed to cleanup session: sessionId={}", session.getIdValue(), e);
            }
        }

        return count;
    }

    /**
     * 만료된 IN_PROGRESS 세션 정리
     *
     * <p>⭐ Index 활용: idx_status_created_at (status, created_at)</p>
     *
     * @param threshold 이 시간 이전에 생성된 세션을 만료 처리
     * @return 정리된 세션 개수
     */
    @Transactional(readOnly = true)
    public int cleanupInProgressSessions(LocalDateTime threshold) {
        // 1. 만료된 IN_PROGRESS 세션 조회
        List<UploadSession> expiredSessions = loadUploadSessionPort.findByStatusAndCreatedBefore(
            SessionStatus.IN_PROGRESS,
            threshold
        );

        log.debug("Found {} expired IN_PROGRESS sessions before {}", expiredSessions.size(), threshold);

        // 2. 각 세션을 FAILED 상태로 전환
        int count = 0;
        for (UploadSession session : expiredSessions) {
            try {
                failExpiredSession(session, "Session expired (IN_PROGRESS > " + inProgressExpirationHours + " hours)");
                count++;
            } catch (Exception e) {
                log.error("Failed to cleanup session: sessionId={}", session.getIdValue(), e);
            }
        }

        return count;
    }

    /**
     * 만료된 세션을 FAILED 상태로 전환
     *
     * <p>별도 트랜잭션에서 실행 (각 세션별 독립 트랜잭션)</p>
     *
     * @param session UploadSession
     * @param reason 실패 사유
     */
    @Transactional
    public void failExpiredSession(UploadSession session, String reason) {
        // String → FailureReason VO 변환
        FailureReason failureReason = FailureReason.of(reason);

        // Domain 메서드 호출 (Tell, Don't Ask)
        session.fail(failureReason);

        // StateManager를 통한 저장
        uploadSessionStateManager.save(session);

        log.info("Session expired and marked as FAILED: sessionId={}, reason={}",
            session.getIdValue(), reason);
    }

    /**
     * 복수 상태 기반 정리 (선택적 메서드)
     *
     * <p>PENDING과 IN_PROGRESS를 한 번에 조회하여 정리합니다.</p>
     *
     * <p>⚠️ IN 절 사용으로 Index 효율 저하 가능</p>
     *
     * @param threshold 이 시간 이전에 생성된 세션을 만료 처리
     * @return 정리된 세션 개수
     */
    @Transactional(readOnly = true)
    public int cleanupMultipleStatuses(LocalDateTime threshold) {
        List<SessionStatus> targetStatuses = Arrays.asList(
            SessionStatus.PENDING,
            SessionStatus.IN_PROGRESS
        );

        List<UploadSession> expiredSessions = loadUploadSessionPort.findByStatusInAndCreatedBefore(
            targetStatuses,
            threshold
        );

        log.debug("Found {} expired sessions (multiple statuses) before {}", expiredSessions.size(), threshold);

        int count = 0;
        for (UploadSession session : expiredSessions) {
            try {
                String reason = String.format("Session expired (%s state)", session.getStatus());
                failExpiredSession(session, reason);
                count++;
            } catch (Exception e) {
                log.error("Failed to cleanup session: sessionId={}", session.getIdValue(), e);
            }
        }

        return count;
    }
}

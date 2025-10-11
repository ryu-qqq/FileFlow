package com.ryuqq.fileflow.application.upload.batch;

import com.ryuqq.fileflow.application.upload.port.in.FailUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 만료된 업로드 세션 배치 처리 Service (Fallback)
 *
 * Redis TTL Listener의 보상 메커니즘으로 동작합니다.
 * Redis KeyExpiredEvent가 누락되거나 처리 실패한 경우를 대비한 Fallback 처리입니다.
 *
 * 처리 흐름:
 * 1. 만료된 세션 목록 조회 (PENDING/UPLOADING 상태 + expiresAt 지남)
 * 2. 각 세션에 대해 FailUploadUseCase를 통해 실패 처리
 * 3. 개별 세션 처리 실패 시 로깅 후 다음 세션 계속 처리
 *
 * 실행 주기: 1시간마다 (fixedDelay = 3600000ms)
 * - Redis Listener가 실시간으로 처리하므로 배치는 보상용으로만 사용
 * - 주기를 1시간으로 늘려 DB 부하 최소화
 *
 * @author sangwon-ryu
 */
@Component
public class ExpiredUploadSessionBatchService {

    private static final Logger log = LoggerFactory.getLogger(ExpiredUploadSessionBatchService.class);
    private static final String EXPIRATION_REASON = "Session expired";

    private final UploadSessionPort uploadSessionPort;
    private final FailUploadUseCase failUploadUseCase;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param uploadSessionPort 세션 저장소
     * @param failUploadUseCase 세션 실패 처리 UseCase
     */
    public ExpiredUploadSessionBatchService(
            UploadSessionPort uploadSessionPort,
            FailUploadUseCase failUploadUseCase
    ) {
        this.uploadSessionPort = Objects.requireNonNull(
                uploadSessionPort,
                "UploadSessionPort must not be null"
        );
        this.failUploadUseCase = Objects.requireNonNull(
                failUploadUseCase,
                "FailUploadUseCase must not be null"
        );
    }

    /**
     * 만료된 업로드 세션들을 주기적으로 처리합니다 (Fallback).
     *
     * Redis TTL Listener가 대부분의 만료 세션을 실시간으로 처리하므로,
     * 이 배치는 누락된 세션을 처리하는 보상 메커니즘으로만 동작합니다.
     *
     * fixedDelay: 이전 작업 완료 후 1시간 후 다음 작업 시작 (3600000ms)
     * initialDelay: 애플리케이션 시작 후 5분 후 첫 실행 (300000ms)
     *
     * Note: 각 세션 처리는 FailUploadService의 개별 트랜잭션으로 실행됩니다.
     *       배치 메서드 자체에는 @Transactional을 적용하지 않습니다.
     */
    @Scheduled(fixedDelay = 3600000, initialDelay = 300000)
    public void processExpiredSessions() {
        log.info("[FALLBACK] Starting expired upload session batch processing");

        try {
            List<UploadSession> expiredSessions = uploadSessionPort.findExpiredSessions();

            if (expiredSessions.isEmpty()) {
                log.info("[FALLBACK] No expired upload sessions found");
                return;
            }

            log.warn("[FALLBACK] Found {} expired upload sessions that were not processed by Redis Listener",
                    expiredSessions.size());

            int successCount = 0;
            int failureCount = 0;

            for (UploadSession session : expiredSessions) {
                try {
                    failUploadUseCase.failSession(session.getSessionId(), EXPIRATION_REASON + " (Fallback)");
                    successCount++;
                    log.debug("[FALLBACK] Successfully failed expired session: {}", session.getSessionId());
                } catch (Exception e) {
                    failureCount++;
                    log.error("[FALLBACK] Failed to process expired session: {}", session.getSessionId(), e);
                }
            }

            log.info("[FALLBACK] Expired upload session batch processing completed. " +
                            "Success: {}, Failure: {}, Total: {}",
                    successCount, failureCount, expiredSessions.size());

        } catch (Exception e) {
            log.error("[FALLBACK] Unexpected error during expired session batch processing", e);
        }
    }
}

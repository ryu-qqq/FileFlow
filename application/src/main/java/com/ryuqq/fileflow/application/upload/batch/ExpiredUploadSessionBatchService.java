package com.ryuqq.fileflow.application.upload.batch;

import com.ryuqq.fileflow.application.upload.port.in.FailUploadUseCase;
import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 만료된 업로드 세션 배치 처리 Service
 *
 * 주기적으로 실행되어 만료된 업로드 세션들을 FAILED 상태로 전환합니다.
 *
 * 처리 흐름:
 * 1. 만료된 세션 목록 조회 (PENDING/UPLOADING 상태 + expiresAt 지남)
 * 2. 각 세션에 대해 FailUploadUseCase를 통해 실패 처리
 * 3. 개별 세션 처리 실패 시 로깅 후 다음 세션 계속 처리
 *
 * 실행 주기: 5분마다 (fixedDelay = 300000ms)
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
     * 만료된 업로드 세션들을 주기적으로 처리합니다.
     *
     * fixedDelay: 이전 작업 완료 후 5분 후 다음 작업 시작 (겹침 방지)
     * initialDelay: 애플리케이션 시작 후 1분 후 첫 실행
     *
     * @Transactional: 각 배치 실행을 하나의 트랜잭션으로 처리
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    @Transactional
    public void processExpiredSessions() {
        log.info("Starting expired upload session batch processing");

        try {
            List<UploadSession> expiredSessions = uploadSessionPort.findExpiredSessions();

            if (expiredSessions.isEmpty()) {
                log.info("No expired upload sessions found");
                return;
            }

            log.info("Found {} expired upload sessions", expiredSessions.size());

            int successCount = 0;
            int failureCount = 0;

            for (UploadSession session : expiredSessions) {
                try {
                    failUploadUseCase.failSession(session.getSessionId(), EXPIRATION_REASON);
                    successCount++;
                    log.debug("Successfully failed expired session: {}", session.getSessionId());
                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to process expired session: {}, error: {}",
                            session.getSessionId(), e.getMessage(), e);
                }
            }

            log.info("Expired upload session batch processing completed. " +
                            "Success: {}, Failure: {}, Total: {}",
                    successCount, failureCount, expiredSessions.size());

        } catch (Exception e) {
            log.error("Unexpected error during expired session batch processing", e);
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }
}

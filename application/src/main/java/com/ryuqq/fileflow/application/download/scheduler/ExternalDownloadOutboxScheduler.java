package com.ryuqq.fileflow.application.download.scheduler;

import com.ryuqq.fileflow.application.download.config.ExternalDownloadOutboxProperties;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadOutboxManager;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadPort;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.ProcessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * External Download Outbox Scheduler
 * Outbox 패턴으로 저장된 다운로드 요청을 처리하는 Scheduler
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>NEW 상태의 Outbox 메시지 폴링</li>
 *   <li>ExternalDownloadWorker에 작업 위임</li>
 *   <li>Outbox 상태 업데이트 (NEW → PROCESSING → PROCESSED)</li>
 *   <li>실패 시 재처리 가능하도록 상태 관리</li>
 * </ul>
 *
 * <p><strong>실행 전략:</strong></p>
 * <ul>
 *   <li>실행 주기: application.yml에서 설정</li>
 *   <li>배치 크기: application.yml에서 설정</li>
 *   <li>오래된 메시지 우선 처리 (FIFO)</li>
 *   <li>격리: 각 메시지별로 독립적 처리</li>
 * </ul>
 *
 * <p><strong>트랜잭션 경계:</strong></p>
 * <ul>
 *   <li>✅ Manager를 통한 상태 업데이트 (Spring 프록시 정상 작동)</li>
 *   <li>✅ Worker 호출: 비동기 (@Async)</li>
 *   <li>✅ 각 메시지별 독립적 트랜잭션</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExternalDownloadOutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExternalDownloadOutboxScheduler.class);

    private final ExternalDownloadOutboxManager outboxManager;
    private final ExternalDownloadPort downloadPort;
    private final ExternalDownloadWorker downloadWorker;
    private final ExternalDownloadOutboxProperties properties;

    public ExternalDownloadOutboxScheduler(
        ExternalDownloadOutboxManager outboxManager,
        ExternalDownloadPort downloadPort,
        ExternalDownloadWorker downloadWorker,
        ExternalDownloadOutboxProperties properties
    ) {
        this.outboxManager = outboxManager;
        this.downloadPort = downloadPort;
        this.downloadWorker = downloadWorker;
        this.properties = properties;
    }

    /**
     * Outbox 메시지 처리
     *
     * <p>설정된 주기마다 실행되어 PENDING 및 재시도 가능한 메시지를 처리합니다.</p>
     * <p>At-least-once 보장: 실패 시 다음 실행에서 재처리</p>
     */
    @Scheduled(fixedDelayString = "${fileflow.download.outbox.fixed-delay:30000}",
               initialDelayString = "${fileflow.download.outbox.initial-delay:10000}")
    public void processOutboxMessages() {
        LocalDateTime now = LocalDateTime.now();

        // 1. PENDING 상태의 메시지 조회 (오래된 것부터)
        List<ExternalDownloadOutbox> pendingMessages =
            outboxManager.findNewMessages(properties.getBatchSize());

        // 2. 재시도 가능한 FAILED 메시지 조회 (지수 백오프)
        LocalDateTime retryAfter = calculateRetryAfterTime(now);
        List<ExternalDownloadOutbox> retryableMessages =
            outboxManager.findRetryableFailedMessages(
                properties.getMaxRetryCount(),
                retryAfter,
                properties.getBatchSize() - pendingMessages.size()
            );

        // 3. PROCESSING 상태이지만 오래된 메시지 재처리 (장애 복구)
        LocalDateTime staleThreshold = now.minusMinutes(properties.getStaleMinutes());
        int remainingCapacity = properties.getBatchSize() - pendingMessages.size() - retryableMessages.size();
        List<ExternalDownloadOutbox> staleMessages = remainingCapacity > 0 ?
            outboxManager.findStaleProcessingMessages(staleThreshold, remainingCapacity) :
            List.of();

        int totalMessages = pendingMessages.size() + retryableMessages.size() + staleMessages.size();
        if (totalMessages == 0) {
            log.debug("No outbox messages to process");
            return;
        }

        log.info("Processing {} outbox messages (pending: {}, retry: {}, stale: {})",
            totalMessages, pendingMessages.size(), retryableMessages.size(), staleMessages.size());

        int successCount = 0;
        int failureCount = 0;
        int permanentFailureCount = 0;

        // 4. PENDING 메시지 처리
        for (ExternalDownloadOutbox outbox : pendingMessages) {
            ProcessResult result = processOutboxMessage(outbox);
            switch (result) {
                case SUCCESS -> successCount++;
                case RETRY -> failureCount++;
                case PERMANENT_FAILURE -> permanentFailureCount++;
            }
        }

        // 5. 재시도 메시지 처리 (FAILED → PENDING → PROCESSING)
        for (ExternalDownloadOutbox outbox : retryableMessages) {
            log.info("Retrying failed message: outboxId={}, retryCount={}/{}",
                outbox.getIdValue(), outbox.getRetryCount(), properties.getMaxRetryCount());

            // FAILED → PENDING 상태 변경
            outboxManager.prepareForRetry(outbox);

            ProcessResult result = processOutboxMessage(outbox);
            switch (result) {
                case SUCCESS -> successCount++;
                case RETRY -> failureCount++;
                case PERMANENT_FAILURE -> permanentFailureCount++;
            }
        }

        // 6. 오래된 PROCESSING 메시지 처리
        for (ExternalDownloadOutbox outbox : staleMessages) {
            log.warn("Reprocessing stale message: outboxId={}, lastProcessedAt={}",
                outbox.getIdValue(), outbox.getUpdatedAt());

            ProcessResult result = processOutboxMessage(outbox);
            switch (result) {
                case SUCCESS -> successCount++;
                case RETRY -> failureCount++;
                case PERMANENT_FAILURE -> permanentFailureCount++;
            }
        }

        log.info("Outbox processing completed. Success: {}, Retry: {}, Permanent Failure: {}",
            successCount, failureCount, permanentFailureCount);
    }

    /**
     * 개별 Outbox 메시지 처리
     *
     * @param outbox ExternalDownloadOutbox 메시지
     * @return 처리 결과
     */
    private ProcessResult processOutboxMessage(ExternalDownloadOutbox outbox) {
        try {
            log.debug("Processing outbox message: outboxId={}", outbox.getIdValue());

            // 1. Outbox 상태를 PROCESSING으로 변경 (Manager 사용)
            outboxManager.markProcessing(outbox);

            // 2. ExternalDownload 조회
            ExternalDownload download = downloadPort.findById(outbox.getDownloadIdValue())
                .orElseThrow(() -> new IllegalStateException(
                    "Download not found for outbox: " + outbox.getIdValue()
                ));

            // 3. 이미 처리된 다운로드인지 확인 (멱등성)
            if (download.isCompleted() || download.isFailed()) {
                outboxManager.markProcessed(outbox);
                log.info("Download already processed: downloadId={}, status={}",
                    download.getIdValue(), download.getStatus());
                return ProcessResult.SUCCESS;
            }

            // 4. Worker에 비동기 작업 위임
            downloadWorker.startDownload(download.getIdValue());

            // 5. Outbox 상태를 PROCESSED로 변경 (Manager 사용)
            // 주의: Worker는 비동기이므로 실제 다운로드 완료와는 별개
            // Outbox의 역할은 "작업 시작 보장"이지 "작업 완료 보장"이 아님
            outboxManager.markProcessed(outbox);

            log.info("Successfully dispatched download: downloadId={}, outboxId={}",
                download.getIdValue(), outbox.getIdValue());

            return ProcessResult.SUCCESS;

        } catch (Exception e) {
            log.error("Failed to process outbox message: outboxId={}", outbox.getIdValue(), e);

            // 재시도 횟수 확인
            if (outbox.getRetryCount() >= properties.getMaxRetryCount()) {
                // 최대 재시도 횟수 초과 - 영구 실패
                outboxManager.markPermanentlyFailed(
                    outbox,
                    "Max retry count exceeded: " + e.getMessage()
                );
                log.error("Permanently failed after {} retries: outboxId={}",
                    properties.getMaxRetryCount(), outbox.getIdValue());
                return ProcessResult.PERMANENT_FAILURE;
            } else {
                // 재시도 가능 - 상태를 FAILED로 변경 (다음 스케줄링에서 PENDING으로 되돌림)
                outboxManager.markFailed(outbox, e.getMessage());
                return ProcessResult.RETRY;
            }
        }
    }

    /**
     * 재시도 시간 계산 (지수 백오프)
     *
     * <p>현재 시간으로부터 지수 백오프 시간을 뺀 시간을 반환</p>
     * <p>이 시간 이전에 실패한 메시지만 재시도 대상</p>
     *
     * @param now 현재 시간
     * @return 재시도 가능 시간 임계값
     */
    private LocalDateTime calculateRetryAfterTime(LocalDateTime now) {
        // 최소 지연 시간 (기본값)을 기준으로 계산
        // 예: 현재 시간 - 60초 = 60초 전에 실패한 메시지부터 재시도 가능
        return now.minusSeconds(properties.getRetryBaseDelaySeconds());
    }

    // Note: calculateExponentialBackoff() removed as it was unused
    // If needed in the future, implement exponential backoff directly in processOutboxMessage()
}
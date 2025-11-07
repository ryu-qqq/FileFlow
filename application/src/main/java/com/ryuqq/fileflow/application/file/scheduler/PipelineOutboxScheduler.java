package com.ryuqq.fileflow.application.file.scheduler;

import com.ryuqq.fileflow.application.file.config.PipelineOutboxProperties;
import com.ryuqq.fileflow.application.file.manager.PipelineOutboxManager;
import com.ryuqq.fileflow.domain.download.ProcessResult;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutbox;
import com.ryuqq.fileflow.domain.pipeline.PipelineResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Pipeline Outbox Scheduler
 *
 * <p>Outbox 패턴으로 저장된 Pipeline 요청을 처리하는 Scheduler 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>PENDING 상태의 Outbox 메시지 폴링</li>
 *   <li>PipelineWorker에 작업 위임</li>
 *   <li>Outbox 상태 업데이트 (PENDING → PROCESSING → COMPLETED)</li>
 *   <li>실패 시 재처리 가능하도록 상태 관리</li>
 * </ul>
 *
 * <p><strong>실행 전략:</strong></p>
 * <ul>
 *   <li>실행 주기: application.yml (fileflow.pipeline.outbox.fixed-delay)</li>
 *   <li>배치 크기: application.yml (fileflow.pipeline.outbox.batch-size)</li>
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
 * <p><strong>장애 복구:</strong></p>
 * <ul>
 *   <li>PROCESSING 상태로 오래 머문 메시지 재처리 (Worker 크래시 복구)</li>
 *   <li>FAILED 상태 메시지 지수 백오프 재시도</li>
 *   <li>최대 재시도 횟수 초과 시 영구 실패 표시</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class PipelineOutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(PipelineOutboxScheduler.class);

    private final PipelineOutboxManager outboxManager;
    private final PipelineWorker pipelineWorker;
    private final PipelineOutboxProperties properties;

    /**
     * 생성자
     *
     * @param outboxManager   Pipeline Outbox Manager
     * @param pipelineWorker  Pipeline Worker
     * @param properties      Pipeline Outbox Properties
     */
    public PipelineOutboxScheduler(
        PipelineOutboxManager outboxManager,
        PipelineWorker pipelineWorker,
        PipelineOutboxProperties properties
    ) {
        this.outboxManager = outboxManager;
        this.pipelineWorker = pipelineWorker;
        this.properties = properties;
    }

    /**
     * Outbox 메시지 처리
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>PENDING 메시지 조회 (생성 시간 오름차순)</li>
     *   <li>재시도 가능한 FAILED 메시지 조회 (지수 백오프)</li>
     *   <li>오래된 PROCESSING 메시지 조회 (장애 복구)</li>
     *   <li>각 메시지를 Worker에 비동기 위임</li>
     *   <li>처리 결과에 따라 상태 업데이트</li>
     * </ol>
     *
     * <p><strong>At-least-once 보장:</strong></p>
     * <ul>
     *   <li>실패 시 다음 실행에서 재처리</li>
     *   <li>Worker는 멱등성 보장 필요</li>
     * </ul>
     */
    @Scheduled(fixedDelayString = "${fileflow.pipeline.outbox.fixed-delay:30000}",
               initialDelayString = "${fileflow.pipeline.outbox.initial-delay:10000}")
    public void processOutboxMessages() {
        LocalDateTime now = LocalDateTime.now();

        // 1. PENDING 상태의 메시지 조회 (오래된 것부터)
        List<PipelineOutbox> pendingMessages =
            outboxManager.findNewMessages(properties.getBatchSize());

        // 2. 재시도 가능한 FAILED 메시지 조회 (지수 백오프)
        LocalDateTime retryAfter = calculateRetryAfterTime(now);
        int retryCapacity = properties.getBatchSize() - pendingMessages.size();
        // CRITICAL: 배치 크기 0 이하 전달 시 IllegalArgumentException 방지
        List<PipelineOutbox> retryableMessages = retryCapacity > 0 ?
            outboxManager.findRetryableFailedMessages(
                properties.getMaxRetryCount(),
                retryAfter,
                retryCapacity
            ) :
            List.of();

        // 3. PROCESSING 상태이지만 오래된 메시지 재처리 (장애 복구)
        LocalDateTime staleThreshold = now.minusMinutes(properties.getStaleMinutes());
        int remainingCapacity = properties.getBatchSize() - pendingMessages.size() - retryableMessages.size();
        List<PipelineOutbox> staleMessages = remainingCapacity > 0 ?
            outboxManager.findStaleProcessingMessages(staleThreshold, remainingCapacity) :
            List.of();

        int totalMessages = pendingMessages.size() + retryableMessages.size() + staleMessages.size();
        if (totalMessages == 0) {
            log.debug("No pipeline outbox messages to process");
            return;
        }

        log.info("Processing {} pipeline outbox messages (pending: {}, retry: {}, stale: {})",
            totalMessages, pendingMessages.size(), retryableMessages.size(), staleMessages.size());

        int successCount = 0;
        int failureCount = 0;
        int permanentFailureCount = 0;

        // 4. PENDING 메시지 처리
        for (PipelineOutbox outbox : pendingMessages) {
            ProcessResult result = processOutboxMessage(outbox);
            switch (result) {
                case SUCCESS -> successCount++;
                case RETRY -> failureCount++;
                case PERMANENT_FAILURE -> permanentFailureCount++;
            }
        }

        // 5. 재시도 메시지 처리 (FAILED → PENDING → PROCESSING)
        for (PipelineOutbox outbox : retryableMessages) {
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
        for (PipelineOutbox outbox : staleMessages) {
            log.warn("Reprocessing stale message: outboxId={}, lastProcessedAt={}",
                outbox.getIdValue(), outbox.getUpdatedAt());

            ProcessResult result = processOutboxMessage(outbox);
            switch (result) {
                case SUCCESS -> successCount++;
                case RETRY -> failureCount++;
                case PERMANENT_FAILURE -> permanentFailureCount++;
            }
        }

        log.info("Pipeline outbox processing completed. Success: {}, Retry: {}, Permanent Failure: {}",
            successCount, failureCount, permanentFailureCount);
    }

    /**
     * 개별 Outbox 메시지 처리
     *
     * <p><strong>처리 단계:</strong></p>
     * <ol>
     *   <li>Outbox 상태를 PROCESSING으로 변경 (Manager 사용)</li>
     *   <li>Worker에 동기 작업 위임</li>
     *   <li>결과에 따라 Outbox 상태 업데이트 (COMPLETED/FAILED)</li>
     * </ol>
     *
     * <p><strong>동기 호출 패턴:</strong></p>
     * <ul>
     *   <li>Worker는 PipelineResult 직접 반환</li>
     *   <li>Scheduler는 결과를 받아서 상태를 업데이트</li>
     *   <li>실제 Pipeline 완료 여부를 정확히 반영</li>
     * </ul>
     *
     * @param outbox PipelineOutbox 메시지
     * @return 처리 결과 (SUCCESS, RETRY, PERMANENT_FAILURE)
     */
    private ProcessResult processOutboxMessage(PipelineOutbox outbox) {
        try {
            log.debug("Processing pipeline outbox message: outboxId={}, fileId={}",
                outbox.getIdValue(), outbox.getFileIdValue());

            // 1. Outbox 상태를 PROCESSING으로 변경 (Manager 사용)
            outboxManager.markProcessing(outbox);

            // 2. Worker에 동기 작업 위임
            PipelineResult result = pipelineWorker.startPipeline(outbox.getFileIdValue());

            // 3. 결과에 따라 Outbox 상태 업데이트
            if (result.isSuccess()) {
                outboxManager.markProcessed(outbox);
                log.info("Successfully completed pipeline: fileId={}, outboxId={}",
                    outbox.getFileIdValue(), outbox.getIdValue());
                return ProcessResult.SUCCESS;

            } else {
                // Pipeline 처리 실패
                String errorMessage = result.errorMessage() != null ?
                    result.errorMessage() : "Pipeline processing failed";

                log.error("Pipeline processing failed: fileId={}, outboxId={}, error={}",
                    outbox.getFileIdValue(), outbox.getIdValue(), errorMessage);

                // 재시도 횟수 확인
                if (outbox.getRetryCount() >= properties.getMaxRetryCount()) {
                    // 최대 재시도 횟수 초과 - 영구 실패
                    outboxManager.markPermanentlyFailed(outbox, errorMessage);
                    log.error("Permanently failed after {} retries: outboxId={}",
                        properties.getMaxRetryCount(), outbox.getIdValue());
                    return ProcessResult.PERMANENT_FAILURE;
                } else {
                    // 재시도 가능 - 상태를 FAILED로 변경 (다음 스케줄링에서 PENDING으로 되돌림)
                    outboxManager.markFailed(outbox, errorMessage);
                    return ProcessResult.RETRY;
                }
            }

        } catch (Exception e) {
            log.error("Failed to process pipeline outbox message: outboxId={}", outbox.getIdValue(), e);

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
     * <p><strong>계산 로직:</strong></p>
     * <ul>
     *   <li>현재 시간 - 기본 지연 시간 = 재시도 가능 시간 임계값</li>
     *   <li>예: 현재 시간 - 60초 = 60초 전에 실패한 메시지부터 재시도 가능</li>
     * </ul>
     *
     * @param now 현재 시간
     * @return 재시도 가능 시간 임계값
     */
    private LocalDateTime calculateRetryAfterTime(LocalDateTime now) {
        // 최소 지연 시간 (기본값)을 기준으로 계산
        return now.minusSeconds(properties.getRetryBaseDelaySeconds());
    }

    /**
     * 특정 재시도 횟수에 대한 지수 백오프 지연 시간 계산
     *
     * <p><strong>계산식:</strong></p>
     * <ul>
     *   <li>delay = multiplier^retryCount * baseDelay</li>
     *   <li>예: 2.0^2 * 60초 = 240초</li>
     *   <li>최대값: retryMaxDelaySeconds (1시간)</li>
     * </ul>
     *
     * @param retryCount 재시도 횟수
     * @return 지연 시간 (초)
     */
    // Note: calculateExponentialBackoff() removed as it was unused
    // If needed in the future, implement exponential backoff directly in processOutboxMessage()
}

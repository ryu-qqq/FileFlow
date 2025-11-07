package com.ryuqq.fileflow.application.file.listener;

import com.ryuqq.fileflow.application.file.manager.PipelineOutboxManager;
import com.ryuqq.fileflow.application.file.scheduler.PipelineWorker;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutbox;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutboxCreatedEvent;
import com.ryuqq.fileflow.domain.pipeline.PipelineResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Pipeline Outbox Event Listener
 *
 * <p>PipelineOutbox 생성 이벤트를 수신하여 즉시 Pipeline 처리를 시작하는 Listener입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>PipelineOutboxCreatedEvent 수신</li>
 *   <li>트랜잭션 커밋 후 비동기 Pipeline 처리 시작</li>
 *   <li>PipelineWorker에 작업 위임</li>
 *   <li>Outbox 상태 업데이트 (PENDING → PROCESSING → COMPLETED/FAILED)</li>
 * </ul>
 *
 * <p><strong>Event-Driven Architecture:</strong></p>
 * <ul>
 *   <li>FileCommandManager → Event 발행 (트랜잭션 내)</li>
 *   <li>트랜잭션 커밋 완료 → EventListener 실행 (AFTER_COMMIT)</li>
 *   <li>@Async로 비동기 처리 (호출 스레드 블로킹 방지)</li>
 * </ul>
 *
 * <p><strong>트랜잭션 경계:</strong></p>
 * <ul>
 *   <li>✅ @TransactionalEventListener(phase = AFTER_COMMIT)</li>
 *   <li>✅ 원본 트랜잭션이 성공적으로 커밋된 후에만 실행</li>
 *   <li>✅ @Async로 별도 스레드에서 실행 (Spring ThreadPoolTaskExecutor)</li>
 *   <li>❌ Listener 메서드에는 @Transactional 없음 (Worker 내부에서 트랜잭션 관리)</li>
 * </ul>
 *
 * <h3>실행 흐름</h3>
 * <pre>
 * 1. FileCommandManager.save()
 *    └─ @Transactional 시작
 *       ├─ FileAsset 저장
 *       ├─ PipelineOutbox 저장
 *       ├─ PipelineOutboxCreatedEvent 발행 (트랜잭션 내)
 *       └─ 트랜잭션 커밋 ✅
 *
 * 2. Spring Event System
 *    └─ 트랜잭션 커밋 감지
 *       └─ PipelineOutboxEventListener.handlePipelineOutboxCreated() 호출
 *          └─ @Async 별도 스레드에서 실행
 *
 * 3. PipelineWorker.startPipeline()
 *    └─ Pipeline 처리 실행
 *       ├─ 썸네일 생성
 *       ├─ 메타데이터 추출
 *       └─ 결과 저장
 * </pre>
 *
 * <h3>장애 복구</h3>
 * <ul>
 *   <li>Listener 실패 시: PipelineOutbox를 FAILED 상태로 업데이트</li>
 *   <li>PipelineOutboxScheduler가 FAILED 메시지 감지 및 재처리</li>
 *   <li>At-least-once 보장: 최소 한 번은 처리됨</li>
 * </ul>
 *
 * <h3>Race Condition 방지</h3>
 * <ul>
 *   <li>EventListener와 Scheduler가 동시에 같은 Outbox를 처리하는 문제 방지</li>
 *   <li>EventListener에서 Outbox 상태를 PROCESSING으로 즉시 변경</li>
 *   <li>Scheduler는 PENDING 상태의 Outbox만 처리 (PROCESSING은 스킵)</li>
 * </ul>
 *
 * <h3>성능 최적화</h3>
 * <ul>
 *   <li>즉시 처리: Scheduler 폴링 주기(30초) 대기 없이 즉시 처리</li>
 *   <li>비동기 실행: API 응답 시간에 영향 없음</li>
 *   <li>이중 처리 방지: IdempotencyKey로 중복 방지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see PipelineOutboxCreatedEvent
 * @see PipelineWorker
 * @see TransactionalEventListener
 */
@Component
public class PipelineOutboxEventListener {

    private static final Logger log = LoggerFactory.getLogger(PipelineOutboxEventListener.class);

    private final PipelineWorker pipelineWorker;
    private final PipelineOutboxManager outboxManager;

    /**
     * 생성자
     *
     * @param pipelineWorker Pipeline Worker
     * @param outboxManager  Pipeline Outbox Manager
     */
    public PipelineOutboxEventListener(
        PipelineWorker pipelineWorker,
        PipelineOutboxManager outboxManager
    ) {
        this.pipelineWorker = pipelineWorker;
        this.outboxManager = outboxManager;
    }

    /**
     * PipelineOutbox 생성 이벤트 처리
     *
     * <p><strong>트랜잭션 경계:</strong></p>
     * <ul>
     *   <li>AFTER_COMMIT: 원본 트랜잭션이 성공적으로 커밋된 후에만 실행</li>
     *   <li>트랜잭션 실패 시 이벤트 수신 안됨 (Rollback 시 이벤트 무시)</li>
     * </ul>
     *
     * <p><strong>비동기 실행:</strong></p>
     * <ul>
     *   <li>@Async로 별도 스레드에서 실행</li>
     *   <li>호출 스레드(API 요청 스레드)를 블로킹하지 않음</li>
     *   <li>API 응답 시간에 영향 없음</li>
     * </ul>
     *
     * <p><strong>장애 처리:</strong></p>
     * <ul>
     *   <li>Listener 실패 시: PipelineOutbox를 FAILED 상태로 업데이트</li>
     *   <li>PipelineOutboxScheduler가 FAILED 메시지를 감지하여 재처리</li>
     * </ul>
     *
     * <p><strong>Race Condition 방지:</strong></p>
     * <ul>
     *   <li>Outbox 조회 후 즉시 PROCESSING으로 상태 변경</li>
     *   <li>Scheduler는 PENDING 상태만 처리하므로 중복 처리 방지</li>
     * </ul>
     *
     * @param event PipelineOutboxCreatedEvent
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePipelineOutboxCreated(PipelineOutboxCreatedEvent event) {
        PipelineOutbox outbox = null;

        try {
            log.info("Pipeline Outbox 생성 이벤트 수신: fileId={}",
                event.fileId().value());

            // 1. Outbox 조회 (fileId 기반)
            outbox = outboxManager.findByFileId(event.fileId().value())
                .orElseThrow(() -> new IllegalStateException(
                    "PipelineOutbox not found for fileId: " + event.fileId().value()));

            log.debug("PipelineOutbox found: outboxId={}, status={}",
                outbox.getIdValue(), outbox.getStatus());

            // 2. Outbox 상태를 PROCESSING으로 변경 (Race Condition 방지)
            outboxManager.markProcessing(outbox);

            log.debug("PipelineOutbox marked as PROCESSING: outboxId={}",
                outbox.getIdValue());

            // 3. PipelineWorker에 처리 위임
            PipelineResult result = pipelineWorker.startPipeline(event.fileId().value());

            // 4. 결과에 따라 Outbox 상태 업데이트
            if (result.isSuccess()) {
                outboxManager.markProcessed(outbox);
                log.info("Pipeline 처리 성공: outboxId={}, fileId={}",
                    outbox.getIdValue(), event.fileId().value());
            } else {
                String errorMessage = result.errorMessage() != null ?
                    result.errorMessage() : "Pipeline processing failed";
                outboxManager.markFailed(outbox, errorMessage);
                log.error("Pipeline 처리 실패: outboxId={}, fileId={}, error={}",
                    outbox.getIdValue(), event.fileId().value(), errorMessage);
            }

        } catch (Exception e) {
            // 🚨 예외 발생 시 Outbox를 FAILED 상태로 업데이트
            if (outbox != null) {
                try {
                    outboxManager.markFailed(outbox, e.getMessage());
                    log.error("Pipeline Outbox 이벤트 처리 실패 (FAILED로 업데이트): " +
                        "outboxId={}, fileId={}",
                        outbox.getIdValue(), event.fileId().value(), e);
                } catch (Exception updateError) {
                    // 상태 업데이트 실패 시에도 로그만 남기고 계속 진행
                    log.error("Failed to update Outbox status to FAILED: " +
                        "outboxId={}, fileId={}",
                        outbox.getIdValue(), event.fileId().value(), updateError);
                }
            } else {
                // Outbox 조회 실패 시 (드문 경우)
                log.error("Pipeline Outbox 이벤트 처리 실패 (Outbox 조회 실패): " +
                    "fileId={}",
                    event.fileId().value(), e);
            }
        }
    }
}

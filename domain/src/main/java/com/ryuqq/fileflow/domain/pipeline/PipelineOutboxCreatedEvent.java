package com.ryuqq.fileflow.domain.pipeline;

import com.ryuqq.fileflow.domain.file.asset.FileId;

import java.time.LocalDateTime;

/**
 * Pipeline Outbox 생성 Domain Event
 *
 * <p>FileCommandManager가 PipelineOutbox를 저장한 후 발행하는 도메인 이벤트입니다.</p>
 * <p>이 이벤트를 수신한 EventListener는 트랜잭션 커밋 후 비동기로 Pipeline 처리를 시작합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 패턴 사용</li>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Value Object ID 래핑 (PipelineOutboxId, FileId)</li>
 *   <li>✅ Immutable Event (Record는 기본적으로 불변)</li>
 * </ul>
 *
 * <h3>이벤트 흐름</h3>
 * <pre>
 * 1. FileCommandManager.save()
 *    └─ FileAsset 저장
 *    └─ PipelineOutbox 저장
 *    └─ 🔥 PipelineOutboxCreatedEvent 발행
 *    └─ 트랜잭션 커밋
 *
 * 2. PipelineOutboxEventListener (@TransactionalEventListener)
 *    └─ 트랜잭션 커밋 후 이벤트 수신
 *    └─ @Async로 비동기 처리
 *    └─ PipelineWorker.startPipeline() 호출
 * </pre>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // FileCommandManager.java
 * @Transactional
 * public FileAsset save(FileAsset fileAsset) {
 *     FileAsset savedFileAsset = fileCommandPort.save(fileAsset);
 *
 *     PipelineOutbox outbox = PipelineOutbox.forNew(
 *         IdempotencyKey.generate(),
 *         FileId.of(savedFileAsset.getIdValue())
 *     );
 *     pipelineOutboxPort.save(outbox);
 *
 *     // 이벤트 발행
 *     PipelineOutboxCreatedEvent event = new PipelineOutboxCreatedEvent(
 *         outbox.getId(),
 *         outbox.getFileId(),
 *         outbox.getCreatedAt()
 *     );
 *     applicationEventPublisher.publishEvent(event);
 *
 *     return savedFileAsset;
 * }
 *
 * // PipelineOutboxEventListener.java
 * @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
 * @Async
 * public void handlePipelineOutboxCreated(PipelineOutboxCreatedEvent event) {
 *     log.info("Pipeline Outbox 생성 이벤트 수신: outboxId={}, fileId={}",
 *         event.outboxId().value(), event.fileId().value());
 *
 *     pipelineWorker.startPipeline(event.fileId().value());
 * }
 * }</pre>
 *
 * @param outboxId   생성된 PipelineOutbox의 ID
 * @param fileId     처리 대상 FileAsset의 ID
 * @param occurredAt 이벤트 발생 시각
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record PipelineOutboxCreatedEvent(
    PipelineOutboxId outboxId,
    FileId fileId,
    LocalDateTime occurredAt
) {
    /**
     * Record Compact Constructor - 필수 필드 검증
     *
     * <p><strong>Note:</strong> {@code outboxId}는 검증하지 않습니다.
     * {@link PipelineOutbox#forNew()} 메서드가 ID 없이 이벤트를 발행할 수 있기 때문입니다.
     * Event Listener는 {@code fileId}를 사용하여 Outbox를 조회합니다.</p>
     *
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     */
    public PipelineOutboxCreatedEvent {
        if (fileId == null) {
            throw new IllegalArgumentException("File ID는 필수입니다");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("이벤트 발생 시각은 필수입니다");
        }
    }
}

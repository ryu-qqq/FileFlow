package com.ryuqq.fileflow.domain.asset.event;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import java.time.Clock;
import java.time.Instant;

/**
 * 파일 가공 요청 이벤트.
 *
 * <p>FileAsset + StatusHistory + Outbox 저장 완료 후 발행됩니다.
 *
 * <p>트랜잭션 커밋 후 SQS 메시지 발행 트리거로 사용됩니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>FileAssetCreationFacade에서 이벤트 발행
 *   <li>트랜잭션 커밋 (AFTER_COMMIT)
 *   <li>FileProcessingOutboxEventListener에서 SQS 발행
 *   <li>Outbox 상태 업데이트 (SENT/FAILED)
 * </ol>
 */
public record FileProcessingRequestedEvent(
        FileProcessingOutboxId outboxId,
        FileAssetId fileAssetId,
        String eventType,
        String payload,
        Instant occurredAt)
        implements DomainEvent {

    /**
     * 팩토리 메서드.
     *
     * @param outboxId Outbox ID
     * @param fileAssetId FileAsset ID
     * @param eventType 이벤트 타입
     * @param payload JSON 페이로드
     * @param clock 시간 소스
     * @return FileProcessingRequestedEvent
     */
    public static FileProcessingRequestedEvent of(
            FileProcessingOutboxId outboxId,
            FileAssetId fileAssetId,
            String eventType,
            String payload,
            Clock clock) {
        return new FileProcessingRequestedEvent(
                outboxId, fileAssetId, eventType, payload, clock.instant());
    }
}

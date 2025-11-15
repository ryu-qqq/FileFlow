package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.util.UuidV7Generator;
import com.ryuqq.fileflow.domain.vo.OutboxStatus;

import java.time.LocalDateTime;

/**
 * 메시지 아웃박스 Aggregate Root
 * <p>
 * 이벤트 발행 보장을 위한 Transactional Outbox Pattern 구현체입니다.
 * </p>
 */
public class MessageOutbox {

    private final String id;
    private final String eventType;
    private final String aggregateId;
    private final String payload;
    private final OutboxStatus status;
    private final int retryCount;
    private final int maxRetryCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime processedAt;

    /**
     * MessageOutbox Aggregate 생성자
     *
     * @param id            메시지 고유 ID (UUID v7)
     * @param eventType     이벤트 유형 (FileCreated, FileDeleted 등)
     * @param aggregateId   이벤트 발생 Aggregate ID
     * @param payload       이벤트 페이로드 (JSON)
     * @param status        메시지 상태
     * @param retryCount    재시도 횟수
     * @param maxRetryCount 최대 재시도 횟수
     * @param createdAt     생성 시각
     * @param processedAt   처리 완료 시각
     */
    public MessageOutbox(
            String id,
            String eventType,
            String aggregateId,
            String payload,
            OutboxStatus status,
            int retryCount,
            int maxRetryCount,
            LocalDateTime createdAt,
            LocalDateTime processedAt
    ) {
        this.id = id;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.maxRetryCount = maxRetryCount;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * 메시지 고유 ID 조회
     */
    public String getId() {
        return id;
    }

    /**
     * 이벤트 유형 조회
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * 이벤트 발생 Aggregate ID 조회
     */
    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * 이벤트 페이로드 조회
     */
    public String getPayload() {
        return payload;
    }

    /**
     * 메시지 상태 조회
     */
    public OutboxStatus getStatus() {
        return status;
    }

    /**
     * 재시도 횟수 조회
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 최대 재시도 횟수 조회
     */
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * 생성 시각 조회
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 처리 완료 시각 조회
     */
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}

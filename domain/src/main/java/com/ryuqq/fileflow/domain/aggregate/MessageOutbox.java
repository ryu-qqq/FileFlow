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

    /**
     * 메시지 아웃박스 생성 팩토리 메서드
     * <p>
     * UUID v7을 자동 생성하고 초기 상태를 PENDING으로 설정합니다.
     * </p>
     *
     * @param eventType     이벤트 유형
     * @param aggregateId   이벤트 발생 Aggregate ID
     * @param payload       이벤트 페이로드 (JSON)
     * @param maxRetryCount 최대 재시도 횟수
     * @return 생성된 MessageOutbox Aggregate
     */
    public static MessageOutbox create(
            String eventType,
            String aggregateId,
            String payload,
            int maxRetryCount
    ) {
        // UUID v7 자동 생성
        String id = UuidV7Generator.generate();

        // 현재 시각
        LocalDateTime now = LocalDateTime.now();

        return new MessageOutbox(
                id,
                eventType,
                aggregateId,
                payload,
                OutboxStatus.PENDING, // 초기 상태는 PENDING
                0, // 초기 재시도 횟수 0
                maxRetryCount,
                now, // createdAt
                null  // processedAt는 null
        );
    }

    /**
     * 상태 전환 헬퍼 메서드
     * <p>
     * 새로운 상태로 MessageOutbox 객체를 생성합니다.
     * </p>
     *
     * @param newStatus   새로운 메시지 상태
     * @param processedAt 처리 완료 시각 (nullable)
     * @return 새로운 MessageOutbox 객체
     */
    private MessageOutbox withStatus(
            OutboxStatus newStatus,
            LocalDateTime processedAt
    ) {
        return new MessageOutbox(
                this.id,
                this.eventType,
                this.aggregateId,
                this.payload,
                newStatus,
                this.retryCount,
                this.maxRetryCount,
                this.createdAt,
                processedAt
        );
    }

    /**
     * 메시지를 발송 완료 상태로 변경
     *
     * @return 새로운 MessageOutbox 객체 (SENT 상태)
     */
    public MessageOutbox markAsSent() {
        return withStatus(OutboxStatus.SENT, LocalDateTime.now());
    }

    /**
     * 메시지를 실패 상태로 변경
     *
     * @return 새로운 MessageOutbox 객체 (FAILED 상태)
     */
    public MessageOutbox markAsFailed() {
        return withStatus(OutboxStatus.FAILED, LocalDateTime.now());
    }

    /**
     * 재시도 횟수 증가
     *
     * @return 새로운 MessageOutbox 객체 (retryCount + 1)
     */
    public MessageOutbox incrementRetryCount() {
        return new MessageOutbox(
                this.id,
                this.eventType,
                this.aggregateId,
                this.payload,
                this.status,
                this.retryCount + 1,
                this.maxRetryCount,
                this.createdAt,
                this.processedAt
        );
    }

    /**
     * 재시도 가능 여부 확인
     *
     * @return 재시도 가능하면 true, 불가능하면 false
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetryCount;
    }
}

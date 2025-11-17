package com.ryuqq.fileflow.domain.vo;

/**
 * MessageOutbox 검색 조건 Value Object
 * <p>
 * QueryPort의 findByCriteria, countByCriteria에서 사용하는 검색 조건입니다.
 * </p>
 */
public record MessageOutboxSearchCriteria(
        OutboxStatus outboxStatus,
        String aggregateType,
        String eventType
) {
    /**
     * 모든 조건을 만족하는 검색 조건 생성
     */
    public static MessageOutboxSearchCriteria of(OutboxStatus outboxStatus, String aggregateType, String eventType) {
        return new MessageOutboxSearchCriteria(outboxStatus, aggregateType, eventType);
    }

    /**
     * outboxStatus로만 검색
     */
    public static MessageOutboxSearchCriteria byOutboxStatus(OutboxStatus outboxStatus) {
        return new MessageOutboxSearchCriteria(outboxStatus, null, null);
    }

    /**
     * aggregateType으로만 검색
     */
    public static MessageOutboxSearchCriteria byAggregateType(String aggregateType) {
        return new MessageOutboxSearchCriteria(null, aggregateType, null);
    }

    /**
     * eventType으로만 검색
     */
    public static MessageOutboxSearchCriteria byEventType(String eventType) {
        return new MessageOutboxSearchCriteria(null, null, eventType);
    }
}

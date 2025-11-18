package com.ryuqq.fileflow.domain.outbox.vo;

/**
 * 메시지 발행 상태를 나타내는 Value Object
 */
public enum OutboxStatus {
    /**
     * 대기 중 - 메시지 생성됨, 발행 대기 중
     */
    PENDING,

    /**
     * 발송 완료 - 메시지 정상 발행됨
     */
    SENT,

    /**
     * 실패 - 메시지 발행 실패
     */
    FAILED
}

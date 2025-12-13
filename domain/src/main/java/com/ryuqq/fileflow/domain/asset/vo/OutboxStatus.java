package com.ryuqq.fileflow.domain.asset.vo;

/**
 * Outbox 메시지 상태.
 *
 * <p>Transactional Outbox 패턴에서 메시지 발송 상태를 추적합니다.
 */
public enum OutboxStatus {

    /** 발송 대기 중. */
    PENDING,

    /** 발송 완료. */
    SENT,

    /** 발송 실패. */
    FAILED
}

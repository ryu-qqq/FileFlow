package com.ryuqq.fileflow.domain.download.vo;

/**
 * Webhook Outbox 상태.
 *
 * <p><strong>상태 전이 규칙</strong>:
 *
 * <ul>
 *   <li>PENDING: 초기 상태, 발송 대기 중
 *   <li>SENT: 발송 성공 (종료 상태)
 *   <li>FAILED: 최대 재시도 후 최종 실패 (종료 상태)
 * </ul>
 *
 * <p><strong>상태 전이</strong>:
 *
 * <pre>
 * PENDING → SENT (발송 성공)
 * PENDING → FAILED (최대 재시도 초과)
 * </pre>
 */
public enum WebhookOutboxStatus {

    /**
     * 발송 대기 중.
     *
     * <p>초기 상태이며, 재시도 대상.
     */
    PENDING,

    /**
     * 발송 성공.
     *
     * <p>종료 상태이며, 더 이상 처리하지 않음.
     */
    SENT,

    /**
     * 최종 실패.
     *
     * <p>최대 재시도 후 실패한 종료 상태.
     */
    FAILED;

    /**
     * 재시도 가능 여부 확인.
     *
     * @return PENDING 상태이면 true
     */
    public boolean canRetry() {
        return this == PENDING;
    }

    /**
     * 종료 상태 여부 확인.
     *
     * @return SENT 또는 FAILED 상태이면 true
     */
    public boolean isTerminal() {
        return this == SENT || this == FAILED;
    }
}

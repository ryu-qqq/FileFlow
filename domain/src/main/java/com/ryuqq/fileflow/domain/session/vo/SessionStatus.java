package com.ryuqq.fileflow.domain.session.vo;

/**
 * 업로드 세션의 상태를 정의하는 Enum. 상태 전환 규칙: - PREPARING → ACTIVE (허용) - ACTIVE → COMPLETED, EXPIRED, FAILED
 * (허용) - 기타 전환은 모두 거부
 */
public enum SessionStatus {
    PREPARING,
    ACTIVE,
    COMPLETED,
    EXPIRED,
    FAILED;

    /**
     * 현재 상태에서 다음 상태로 전환 가능한지 확인한다.
     *
     * @param next 다음 상태
     * @return 전환 가능하면 true, 불가능하면 false
     */
    public boolean canTransitionTo(SessionStatus next) {
        return switch (this) {
            case PREPARING -> next == ACTIVE;
            case ACTIVE -> next == COMPLETED || next == EXPIRED || next == FAILED;
            case COMPLETED, EXPIRED, FAILED -> false;
        };
    }
}

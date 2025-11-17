package com.ryuqq.fileflow.domain.vo;

/**
 * SessionStatus Value Object
 * <p>
 * Upload/Download 세션의 상태를 나타냅니다.
 * </p>
 *
 * <p>
 * 상태 전이:
 * INITIATED → IN_PROGRESS → COMPLETED (성공)
 *                         → EXPIRED (시간 만료)
 *                         → FAILED (실패)
 * </p>
 *
 * <p>
 * 종료 상태 (Terminal States):
 * - COMPLETED, EXPIRED, FAILED
 * - 종료 상태에서는 더 이상 상태 변경 불가
 * </p>
 */
public enum SessionStatus {

    /**
     * 세션 시작됨 (초기 상태)
     */
    INITIATED,

    /**
     * 진행 중
     */
    IN_PROGRESS,

    /**
     * 완료됨 (종료 상태)
     */
    COMPLETED,

    /**
     * 만료됨 (종료 상태)
     */
    EXPIRED,

    /**
     * 실패 (종료 상태)
     */
    FAILED;

    /**
     * INITIATED 상태 확인
     *
     * @return INITIATED이면 true
     */
    public boolean isInitiated() {
        return this == INITIATED;
    }

    /**
     * IN_PROGRESS 상태 확인
     *
     * @return IN_PROGRESS이면 true
     */
    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    /**
     * COMPLETED 상태 확인
     *
     * @return COMPLETED이면 true
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * EXPIRED 상태 확인
     *
     * @return EXPIRED이면 true
     */
    public boolean isExpired() {
        return this == EXPIRED;
    }

    /**
     * FAILED 상태 확인
     *
     * @return FAILED이면 true
     */
    public boolean isFailed() {
        return this == FAILED;
    }

    /**
     * 종료 상태 확인
     * <p>
     * 종료 상태: COMPLETED, EXPIRED, FAILED
     * </p>
     *
     * @return 종료 상태이면 true
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == EXPIRED || this == FAILED;
    }
}

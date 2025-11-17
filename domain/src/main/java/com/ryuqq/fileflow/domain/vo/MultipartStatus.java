package com.ryuqq.fileflow.domain.vo;

/**
 * MultipartStatus Value Object
 * <p>
 * S3 멀티파트 업로드의 상태를 나타냅니다.
 * </p>
 *
 * <p>
 * 상태 전이:
 * INITIATED → IN_PROGRESS → COMPLETED (성공)
 *                         → ABORTED (중단)
 * </p>
 *
 * <p>
 * 종료 상태 (Terminal States):
 * - COMPLETED: 모든 파트 업로드 완료 및 Complete Multipart Upload 성공
 * - ABORTED: 업로드 중단 또는 실패 (Abort Multipart Upload)
 * </p>
 */
public enum MultipartStatus {

    /**
     * 멀티파트 업로드 시작됨
     * <p>
     * S3 Initiate Multipart Upload 완료 후 상태
     * </p>
     */
    INITIATED,

    /**
     * 파트 업로드 진행 중
     * <p>
     * 최소 1개 파트 업로드 완료 후 상태
     * </p>
     */
    IN_PROGRESS,

    /**
     * 멀티파트 업로드 완료 (종료 상태)
     * <p>
     * S3 Complete Multipart Upload 성공 후 상태
     * </p>
     */
    COMPLETED,

    /**
     * 멀티파트 업로드 중단 (종료 상태)
     * <p>
     * S3 Abort Multipart Upload 성공 후 상태
     * 또는 업로드 실패 시
     * </p>
     */
    ABORTED;

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
     * ABORTED 상태 확인
     *
     * @return ABORTED이면 true
     */
    public boolean isAborted() {
        return this == ABORTED;
    }

    /**
     * 종료 상태 확인
     * <p>
     * 종료 상태: COMPLETED, ABORTED
     * </p>
     *
     * @return 종료 상태이면 true
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == ABORTED;
    }
}

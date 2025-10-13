package com.ryuqq.fileflow.domain.image.model;

/**
 * 이미지 최적화 상태를 정의하는 Enum
 *
 * 상태 전이:
 * PENDING → IN_PROGRESS → COMPLETED
 *         ↘ FAILED
 */
public enum ImageOptimizationStatus {

    /**
     * 최적화 대기 중
     */
    PENDING("Pending optimization"),

    /**
     * 최적화 진행 중
     */
    IN_PROGRESS("Optimization in progress"),

    /**
     * 최적화 완료
     */
    COMPLETED("Optimization completed"),

    /**
     * 최적화 실패
     */
    FAILED("Optimization failed");

    private final String description;

    ImageOptimizationStatus(String description) {
        this.description = description;
    }

    /**
     * 상태 설명을 반환합니다.
     *
     * @return 상태 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 완료 상태인지 확인합니다.
     *
     * @return 완료 상태이면 true
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * 진행 중 상태인지 확인합니다.
     *
     * @return 진행 중 상태이면 true
     */
    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    /**
     * 실패 상태인지 확인합니다.
     *
     * @return 실패 상태이면 true
     */
    public boolean isFailed() {
        return this == FAILED;
    }

    /**
     * 대기 상태인지 확인합니다.
     *
     * @return 대기 상태이면 true
     */
    public boolean isPending() {
        return this == PENDING;
    }
}

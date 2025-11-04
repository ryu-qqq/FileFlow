package com.ryuqq.fileflow.domain.policy;

/**
 * Policy Evaluation Status
 * 정책 평가 상태
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum EvaluationStatus {
    /**
     * 평가 통과
     */
    PASSED,

    /**
     * 평가 실패
     */
    FAILED,

    /**
     * 평가 미적용
     */
    NOT_APPLICABLE
}

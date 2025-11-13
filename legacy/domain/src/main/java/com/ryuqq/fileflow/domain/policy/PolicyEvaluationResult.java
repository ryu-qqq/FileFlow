package com.ryuqq.fileflow.domain.policy;

import java.util.Collections;
import java.util.List;

/**
 * Policy Evaluation Result Value Object
 * 정책 평가 결과를 표현하는 불변 객체
 *
 * @param policyId 정책 ID
 * @param status 평가 상태
 * @param violations 위반 사항 목록 (불변)
 * @param message 메시지
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record PolicyEvaluationResult(
    Long policyId,
    EvaluationStatus status,
    List<String> violations,
    String message
) {

    /**
     * Compact Constructor
     * violations를 불변 리스트로 변환
     */
    public PolicyEvaluationResult {
        violations = violations != null ? List.copyOf(violations) : Collections.emptyList();
    }

    /**
     * 평가 통과 결과 생성
     *
     * @param policyId 정책 ID
     * @return 평가 통과 결과
     */
    public static PolicyEvaluationResult passed(Long policyId) {
        return new PolicyEvaluationResult(
            policyId,
            EvaluationStatus.PASSED,
            Collections.emptyList(),
            "Policy evaluation passed"
        );
    }

    /**
     * 평가 실패 결과 생성
     *
     * @param policyId 정책 ID
     * @param violations 위반 사항 목록
     * @return 평가 실패 결과
     */
    public static PolicyEvaluationResult failed(Long policyId, List<String> violations) {
        return new PolicyEvaluationResult(
            policyId,
            EvaluationStatus.FAILED,
            violations,
            "Policy evaluation failed"
        );
    }

    /**
     * 평가 미적용 결과 생성
     *
     * @param message 메시지
     * @return 평가 미적용 결과
     */
    public static PolicyEvaluationResult notApplicable(String message) {
        return new PolicyEvaluationResult(
            null,
            EvaluationStatus.NOT_APPLICABLE,
            Collections.emptyList(),
            message
        );
    }
}

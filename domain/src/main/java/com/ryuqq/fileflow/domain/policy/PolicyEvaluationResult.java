package com.ryuqq.fileflow.domain.policy;

import java.util.Collections;
import java.util.List;

/**
 * Policy Evaluation Result Value Object
 * 정책 평가 결과를 표현하는 불변 객체
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class PolicyEvaluationResult {

    private final Long policyId;
    private final EvaluationStatus status;
    private final List<String> violations;
    private final String message;

    /**
     * 평가 상태 Enum
     */
    public enum EvaluationStatus {
        PASSED,
        FAILED,
        NOT_APPLICABLE
    }

    /**
     * Private 생성자
     *
     * @param policyId 정책 ID
     * @param status 평가 상태
     * @param violations 위반 사항 목록
     * @param message 메시지
     */
    private PolicyEvaluationResult(
        Long policyId,
        EvaluationStatus status,
        List<String> violations,
        String message
    ) {
        this.policyId = policyId;
        this.status = status;
        this.violations = violations != null ? List.copyOf(violations) : Collections.emptyList();
        this.message = message;
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

    /**
     * 정책 ID를 반환합니다.
     *
     * @return 정책 ID
     */
    public Long getPolicyId() {
        return policyId;
    }

    /**
     * 평가 상태를 반환합니다.
     *
     * @return 평가 상태
     */
    public EvaluationStatus getStatus() {
        return status;
    }

    /**
     * 위반 사항 목록을 반환합니다.
     *
     * @return 위반 사항 목록 (불변)
     */
    public List<String> getViolations() {
        return violations;
    }

    /**
     * 메시지를 반환합니다.
     *
     * @return 메시지
     */
    public String getMessage() {
        return message;
    }

    /**
     * 동등성을 비교합니다.
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PolicyEvaluationResult)) {
            return false;
        }
        PolicyEvaluationResult that = (PolicyEvaluationResult) o;
        return java.util.Objects.equals(policyId, that.policyId) &&
               status == that.status &&
               java.util.Objects.equals(violations, that.violations) &&
               java.util.Objects.equals(message, that.message);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(policyId, status, violations, message);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return PolicyEvaluationResult 정보 문자열
     */
    @Override
    public String toString() {
        return "PolicyEvaluationResult{" +
            "policyId=" + policyId +
            ", status=" + status +
            ", violations=" + violations +
            ", message='" + message + '\'' +
            '}';
    }
}

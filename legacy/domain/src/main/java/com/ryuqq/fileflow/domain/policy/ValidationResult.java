package com.ryuqq.fileflow.domain.policy;

import java.util.Collections;
import java.util.List;

/**
 * Validation Result Value Object
 * 검증 결과를 표현하는 불변 객체
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class ValidationResult {

    private final boolean valid;
    private final List<String> violations;

    /**
     * Private 생성자
     *
     * @param valid 검증 성공 여부
     * @param violations 위반 사항 목록
     */
    private ValidationResult(boolean valid, List<String> violations) {
        this.valid = valid;
        this.violations = violations != null ? List.copyOf(violations) : Collections.emptyList();
    }

    /**
     * 검증 성공 결과 생성
     *
     * @return 검증 성공 결과
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, Collections.emptyList());
    }

    /**
     * 검증 실패 결과 생성
     *
     * @param violations 위반 사항 목록
     * @return 검증 실패 결과
     */
    public static ValidationResult invalid(List<String> violations) {
        return new ValidationResult(false, violations);
    }

    /**
     * 검증 성공 여부를 반환합니다.
     *
     * @return 검증 성공 여부
     */
    public boolean isValid() {
        return valid;
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
        if (!(o instanceof ValidationResult)) {
            return false;
        }
        ValidationResult that = (ValidationResult) o;
        return valid == that.valid &&
               java.util.Objects.equals(violations, that.violations);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(valid, violations);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return ValidationResult 정보 문자열
     */
    @Override
    public String toString() {
        return "ValidationResult{" +
            "valid=" + valid +
            ", violations=" + violations +
            '}';
    }
}

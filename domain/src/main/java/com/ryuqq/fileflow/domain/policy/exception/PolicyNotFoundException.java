package com.ryuqq.fileflow.domain.policy.exception;

/**
 * Domain Exception: 요청한 정책을 찾을 수 없을 때 발생
 *
 * 발생 조건:
 * - PolicyRepository에서 특정 policyKey로 정책 조회 시 결과가 없을 때
 * - 존재하지 않는 정책을 업데이트하려 할 때
 * - 비활성화된 정책을 사용하려 할 때
 *
 * 사용 시나리오:
 * - Application Layer: 정책 조회 실패 시 404 응답
 * - Domain Layer: 정책 참조 무결성 검증
 * - Adapter Layer: 외부 시스템 연동 시 정책 유효성 검증
 */
public class PolicyNotFoundException extends RuntimeException {

    private final String policyKey;

    public PolicyNotFoundException(String policyKey) {
        super(formatMessage(policyKey));
        this.policyKey = policyKey;
    }

    public PolicyNotFoundException(String policyKey, Throwable cause) {
        super(formatMessage(policyKey), cause);
        this.policyKey = policyKey;
    }

    private static String formatMessage(String policyKey) {
        return String.format("Policy not found: %s", policyKey);
    }

    public String getPolicyKey() {
        return policyKey;
    }
}

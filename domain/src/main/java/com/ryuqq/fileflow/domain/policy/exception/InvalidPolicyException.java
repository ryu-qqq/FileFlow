package com.ryuqq.fileflow.domain.policy.exception;

/**
 * Domain Exception: 유효하지 않은 정책 데이터일 때 발생
 *
 * 발생 조건:
 * - 필수 필드 누락 (policyKey, maxFileSize, maxFileCount 등)
 * - 유효하지 않은 값 범위 (음수 파일 크기, 음수 개수 등)
 * - 논리적으로 모순된 정책 설정 (예: maxFileCount < minFileCount)
 * - 허용된 형식과 제한된 형식이 충돌하는 경우
 *
 * 사용 시나리오:
 * - UploadPolicy 생성 시 불변성 검증
 * - 정책 업데이트 시 유효성 검증
 * - 외부 시스템에서 정책 데이터 수신 시 검증
 * - API 요청 데이터 검증
 */
public class InvalidPolicyException extends RuntimeException {

    private final String reason;

    public InvalidPolicyException(String reason) {
        super(formatMessage(reason));
        this.reason = reason;
    }

    public InvalidPolicyException(String reason, Throwable cause) {
        super(formatMessage(reason), cause);
        this.reason = reason;
    }

    private static String formatMessage(String reason) {
        return String.format("Invalid policy: %s", reason);
    }

    public String getReason() {
        return reason;
    }

    /**
     * 편의 팩토리 메서드: 필수 필드 누락
     */
    public static InvalidPolicyException missingRequiredField(String fieldName) {
        return new InvalidPolicyException(String.format("Required field '%s' is missing", fieldName));
    }

    /**
     * 편의 팩토리 메서드: 유효하지 않은 값 범위
     */
    public static InvalidPolicyException invalidRange(String fieldName, Object value, String expectedRange) {
        return new InvalidPolicyException(
            String.format("Field '%s' has invalid value %s, expected %s", fieldName, value, expectedRange)
        );
    }

    /**
     * 편의 팩토리 메서드: 논리적 모순
     */
    public static InvalidPolicyException logicalInconsistency(String description) {
        return new InvalidPolicyException(String.format("Logical inconsistency: %s", description));
    }
}

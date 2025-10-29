package com.ryuqq.fileflow.domain.iam.tenant.exception;

import com.ryuqq.fileflow.domain.common.ErrorCode;

/**
 * TenantErrorCode - Tenant Bounded Context 에러 코드
 *
 * <p>Tenant 도메인에서 발생하는 모든 비즈니스 예외의 에러 코드를 정의합니다.</p>
 *
 * <p><strong>에러 코드 규칙:</strong></p>
 * <ul>
 *   <li>✅ 형식: TENANT-{3자리 숫자}</li>
 *   <li>✅ HTTP 상태 코드 매핑</li>
 *   <li>✅ 명확한 에러 메시지</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * throw new TenantNotFoundException("tenant-123");
 * // → ErrorCode: TENANT-001, HTTP Status: 404
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public enum TenantErrorCode implements ErrorCode {

    /**
     * Tenant를 찾을 수 없음
     */
    TENANT_NOT_FOUND("TENANT-001", 404, "Tenant not found"),

    /**
     * Tenant 이름 중복
     */
    TENANT_NAME_DUPLICATED("TENANT-002", 409, "Tenant name already exists"),

    /**
     * Tenant 생성 실패
     */
    TENANT_CREATION_FAILED("TENANT-003", 500, "Failed to create tenant"),

    /**
     * Tenant 업데이트 실패
     */
    TENANT_UPDATE_FAILED("TENANT-004", 500, "Failed to update tenant"),

    /**
     * Tenant 삭제 실패
     */
    TENANT_DELETION_FAILED("TENANT-005", 500, "Failed to delete tenant"),

    /**
     * 유효하지 않은 Tenant 상태
     */
    INVALID_TENANT_STATUS("TENANT-006", 400, "Invalid tenant status");

    private final String code;
    private final int httpStatus;
    private final String message;

    /**
     * Constructor - ErrorCode 생성
     *
     * @param code 에러 코드 (TENANT-XXX)
     * @param httpStatus HTTP 상태 코드
     * @param message 에러 메시지
     * @author ryu-qqq
     * @since 2025-10-23
     */
    TenantErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    /**
     * 에러 코드 반환
     *
     * @return 에러 코드 문자열 (예: TENANT-001)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * HTTP 상태 코드 반환
     *
     * @return HTTP 상태 코드 (예: 404, 409, 500)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * 에러 메시지 반환
     *
     * @return 에러 메시지 문자열
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public String getMessage() {
        return message;
    }
}

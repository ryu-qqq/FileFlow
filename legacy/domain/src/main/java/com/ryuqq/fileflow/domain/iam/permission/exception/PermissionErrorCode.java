package com.ryuqq.fileflow.domain.iam.permission.exception;

import com.ryuqq.fileflow.domain.common.ErrorCode;

/**
 * PermissionErrorCode - Permission Bounded Context 에러 코드
 *
 * <p>Permission 도메인에서 발생하는 모든 비즈니스 예외의 에러 코드를 정의합니다.</p>
 *
 * <p><strong>에러 코드 규칙:</strong></p>
 * <ul>
 *   <li>✅ 형식: PERMISSION-{3자리 숫자}</li>
 *   <li>✅ HTTP 상태 코드 매핑</li>
 *   <li>✅ 명확한 에러 메시지</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * throw new PermissionDeniedException(DenialReason.NO_GRANT, "file.upload");
 * // → ErrorCode: PERMISSION-001, HTTP Status: 403
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public enum PermissionErrorCode implements ErrorCode {

    /**
     * 권한 거부
     *
     * <p>사용자가 요청한 작업을 수행할 권한이 없습니다.</p>
     */
    PERMISSION_DENIED("PERMISSION-001", 403, "Permission denied"),

    /**
     * Permission을 찾을 수 없음
     */
    PERMISSION_NOT_FOUND("PERMISSION-002", 404, "Permission not found"),

    /**
     * Role을 찾을 수 없음
     */
    ROLE_NOT_FOUND("PERMISSION-003", 404, "Role not found"),

    /**
     * Grant를 찾을 수 없음
     */
    GRANT_NOT_FOUND("PERMISSION-004", 404, "Grant not found"),

    /**
     * 유효하지 않은 Scope
     */
    INVALID_SCOPE("PERMISSION-005", 400, "Invalid scope"),

    /**
     * ABAC 조건 평가 실패
     */
    ABAC_EVALUATION_FAILED("PERMISSION-006", 500, "ABAC condition evaluation failed"),

    /**
     * Permission Code 중복
     */
    PERMISSION_CODE_DUPLICATED("PERMISSION-007", 409, "Permission code already exists"),

    /**
     * Role Code 중복
     */
    ROLE_CODE_DUPLICATED("PERMISSION-008", 409, "Role code already exists");

    private final String code;
    private final int httpStatus;
    private final String message;

    /**
     * Constructor - ErrorCode 생성
     *
     * @param code 에러 코드 (PERMISSION-XXX)
     * @param httpStatus HTTP 상태 코드
     * @param message 에러 메시지
     * @author ryu-qqq
     * @since 2025-10-25
     */
    PermissionErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    /**
     * 에러 코드 반환
     *
     * @return 에러 코드 문자열 (예: PERMISSION-001)
     * @author ryu-qqq
     * @since 2025-10-25
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * HTTP 상태 코드 반환
     *
     * @return HTTP 상태 코드 (예: 403, 404, 500)
     * @author ryu-qqq
     * @since 2025-10-25
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
     * @since 2025-10-25
     */
    @Override
    public String getMessage() {
        return message;
    }
}

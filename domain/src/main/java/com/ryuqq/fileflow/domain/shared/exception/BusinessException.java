package com.ryuqq.fileflow.domain.shared.exception;

/**
 * BusinessException - 비즈니스 예외 추상 클래스
 *
 * <p>모든 비즈니스 예외는 이 클래스를 상속하여 일관된 예외 처리를 보장합니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ ErrorCode 기반 예외 생성</li>
 *   <li>✅ 기본 메시지 또는 커스텀 메시지 지원</li>
 *   <li>✅ RuntimeException 계열 (Unchecked Exception)</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * public class TenantNotFoundException extends BusinessException {
 *     public TenantNotFoundException(String tenantId) {
 *         super(TenantErrorCode.TENANT_NOT_FOUND,
 *               "Tenant not found: " + tenantId);
 *     }
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * ErrorCode 기본 메시지 사용 생성자
     *
     * <p>ErrorCode의 getMessage()를 예외 메시지로 사용합니다.</p>
     *
     * @param errorCode 에러 코드
     * @author ryu-qqq
     * @since 2025-10-23
     */
    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 커스텀 메시지 사용 생성자
     *
     * <p>ErrorCode의 기본 메시지 대신 커스텀 메시지를 사용합니다.</p>
     *
     * @param errorCode 에러 코드
     * @param message 커스텀 에러 메시지
     * @author ryu-qqq
     * @since 2025-10-23
     */
    protected BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Cause 포함 생성자
     *
     * <p>원인 예외를 포함한 BusinessException 생성</p>
     *
     * @param errorCode 에러 코드
     * @param message 커스텀 에러 메시지
     * @param cause 원인 예외
     * @author ryu-qqq
     * @since 2025-10-23
     */
    protected BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode 반환
     *
     * <p>GlobalExceptionHandler에서 HTTP 응답 생성 시 사용됩니다.</p>
     *
     * @return 에러 코드
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

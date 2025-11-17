package com.ryuqq.fileflow.domain.common.exception;

/**
 * Domain Layer 최상위 Exception
 * <p>
 * 모든 Domain Exception은 이 클래스를 상속해야 합니다.
 * </p>
 *
 * <p>
 * <strong>설계 원칙</strong>:
 * <ul>
 *   <li>RuntimeException 상속 (Unchecked Exception)</li>
 *   <li>ErrorCode Enum 기반 에러 처리</li>
 *   <li>Lombok 사용 금지 (Pure Java)</li>
 *   <li>JPA/Spring 어노테이션 금지</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>사용 예시</strong>:
 * <pre>{@code
 * public class InvalidMimeTypeException extends DomainException {
 *     public InvalidMimeTypeException(InvalidMimeTypeErrorCode errorCode) {
 *         super(errorCode);
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author development-team
 * @since 1.0.0
 */
public class DomainException extends RuntimeException {

    /**
     * 에러 코드 Enum
     */
    private final ErrorCode errorCode;

    /**
     * ErrorCode 기반 생성자
     *
     * @param errorCode 에러 코드
     */
    public DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode + Cause 기반 생성자
     *
     * @param errorCode 에러 코드
     * @param cause 원인 예외
     */
    public DomainException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드 Enum 반환
     *
     * @return ErrorCode
     */
    public ErrorCode errorCode() {
        return errorCode;
    }

    /**
     * 에러 코드 문자열 반환
     *
     * @return 에러 코드 (예: FILE-001)
     */
    public String code() {
        return errorCode.getCode();
    }

    /**
     * HTTP 상태 코드 반환
     *
     * @return HTTP 상태 코드 (예: 400, 404, 500)
     */
    public int httpStatus() {
        return errorCode.getHttpStatus();
    }
}

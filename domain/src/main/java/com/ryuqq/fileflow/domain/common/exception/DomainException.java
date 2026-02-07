package com.ryuqq.fileflow.domain.common.exception;

/**
 * 도메인 계층 기본 예외.
 * 모든 도메인 예외는 이 클래스를 상속합니다.
 */
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public DomainException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

    public DomainException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}

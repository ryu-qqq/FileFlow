package com.ryuqq.fileflow.domain.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

/**
 * 테스트 전용 InvalidFileSizeErrorCode Enum
 *
 * <p>
 * DomainExceptionTest에서 ErrorCode 구현체가 필요하기 때문에
 * 임시로 정의된 테스트 전용 Enum입니다.
 * </p>
 */
public enum InvalidFileSizeErrorCode implements ErrorCode {

    NEGATIVE_FILE_SIZE("SIZE-001", "파일 크기는 0보다 커야 합니다", 400),
    FILE_SIZE_LIMIT_EXCEEDED("SIZE-002", "파일 크기 제한을 초과했습니다", 400),
    EMPTY_FILE_SIZE("SIZE-003", "파일 크기가 비어 있습니다", 400);

    private final String code;
    private final String message;
    private final int httpStatus;

    InvalidFileSizeErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }
}


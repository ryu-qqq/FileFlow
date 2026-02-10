package com.ryuqq.fileflow.domain.transform.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

public enum TransformErrorCode implements ErrorCode {
    TRANSFORM_NOT_FOUND("TRANSFORM-001", 404, "변환 요청을 찾을 수 없습니다"),
    NOT_IMAGE_FILE("TRANSFORM-002", 400, "이미지 파일만 변환할 수 있습니다"),
    INVALID_TRANSFORM_PARAMS("TRANSFORM-003", 400, "유효하지 않은 변환 파라미터입니다"),
    INVALID_TRANSFORM_STATUS("TRANSFORM-004", 400, "유효하지 않은 변환 상태입니다");

    private final String code;
    private final int httpStatus;
    private final String message;

    TransformErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

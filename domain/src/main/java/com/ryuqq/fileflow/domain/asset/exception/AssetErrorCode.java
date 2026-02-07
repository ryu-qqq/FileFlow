package com.ryuqq.fileflow.domain.asset.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

public enum AssetErrorCode implements ErrorCode {

    ASSET_NOT_FOUND("ASSET-001", 404, "파일을 찾을 수 없습니다"),
    ASSET_ALREADY_DELETED("ASSET-002", 409, "이미 삭제된 파일입니다");

    private final String code;
    private final int httpStatus;
    private final String message;

    AssetErrorCode(String code, int httpStatus, String message) {
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

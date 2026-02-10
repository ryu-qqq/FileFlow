package com.ryuqq.fileflow.domain.asset.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

public enum AssetErrorCode implements ErrorCode {
    ASSET_NOT_FOUND("ASSET-001", 404, "파일을 찾을 수 없습니다"),
    ASSET_ALREADY_DELETED("ASSET-002", 409, "이미 삭제된 파일입니다"),
    ASSET_METADATA_NOT_FOUND("ASSET-003", 404, "파일 메타데이터를 찾을 수 없습니다"),
    ASSET_ACCESS_DENIED("ASSET-004", 403, "해당 파일에 대한 권한이 없습니다");

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

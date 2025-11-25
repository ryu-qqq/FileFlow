package com.ryuqq.fileflow.domain.asset.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

/** FileAsset 도메인 에러 코드. */
public enum FileAssetErrorCode implements ErrorCode {
    FILE_ASSET_NOT_FOUND("ASSET_001", "FileAsset을 찾을 수 없습니다.", 404);

    private final String code;
    private final String message;
    private final int httpStatus;

    FileAssetErrorCode(String code, String message, int httpStatus) {
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

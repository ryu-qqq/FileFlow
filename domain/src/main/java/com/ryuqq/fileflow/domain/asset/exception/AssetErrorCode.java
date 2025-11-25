package com.ryuqq.fileflow.domain.asset.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

/** FileAsset 도메인 에러 코드. */
public enum AssetErrorCode implements ErrorCode {
    ASSET_NOT_FOUND("ASSET-NOT-FOUND", "파일 에셋을 찾을 수 없습니다.", 404),
    INVALID_ASSET_STATUS("INVALID-ASSET-STATUS", "유효하지 않은 에셋 상태입니다.", 409),
    PROCESSING_FAILED("PROCESSING-FAILED", "파일 가공에 실패했습니다.", 500);

    private final String code;
    private final String message;
    private final int httpStatus;

    AssetErrorCode(String code, String message, int httpStatus) {
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

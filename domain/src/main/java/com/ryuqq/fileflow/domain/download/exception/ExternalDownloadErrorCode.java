package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

/** ExternalDownload 도메인 에러 코드. */
public enum ExternalDownloadErrorCode implements ErrorCode {
    EXTERNAL_DOWNLOAD_NOT_FOUND("DOWNLOAD_001", "외부 다운로드를 찾을 수 없습니다.", 404);

    private final String code;
    private final String message;
    private final int httpStatus;

    ExternalDownloadErrorCode(String code, String message, int httpStatus) {
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

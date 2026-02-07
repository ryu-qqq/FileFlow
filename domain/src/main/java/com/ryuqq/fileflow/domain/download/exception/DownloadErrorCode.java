package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

public enum DownloadErrorCode implements ErrorCode {

    DOWNLOAD_TASK_NOT_FOUND("DOWNLOAD-001", 404, "다운로드 작업을 찾을 수 없습니다"),
    DOWNLOAD_ALREADY_COMPLETED("DOWNLOAD-002", 409, "이미 완료된 다운로드 작업입니다"),
    DOWNLOAD_MAX_RETRIES_EXCEEDED("DOWNLOAD-003", 422, "최대 재시도 횟수를 초과했습니다"),
    INVALID_DOWNLOAD_STATUS("DOWNLOAD-004", 400, "유효하지 않은 다운로드 상태입니다");

    private final String code;
    private final int httpStatus;
    private final String message;

    DownloadErrorCode(String code, int httpStatus, String message) {
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

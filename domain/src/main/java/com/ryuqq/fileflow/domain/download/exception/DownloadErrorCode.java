package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

public enum DownloadErrorCode implements ErrorCode {
    DOWNLOAD_TASK_NOT_FOUND("DOWNLOAD-001", 404, "다운로드 작업을 찾을 수 없습니다"),
    DOWNLOAD_ALREADY_COMPLETED("DOWNLOAD-002", 409, "이미 완료된 다운로드 작업입니다"),
    DOWNLOAD_MAX_RETRIES_EXCEEDED("DOWNLOAD-003", 422, "최대 재시도 횟수를 초과했습니다"),
    INVALID_DOWNLOAD_STATUS("DOWNLOAD-004", 400, "유효하지 않은 다운로드 상태입니다"),
    INVALID_SOURCE_URL("DOWNLOAD-005", 400, "유효하지 않은 소스 URL입니다"),
    INVALID_CALLBACK_URL("DOWNLOAD-006", 400, "유효하지 않은 콜백 URL입니다"),
    INVALID_DOWNLOADED_FILE("DOWNLOAD-007", 400, "유효하지 않은 다운로드 파일 정보입니다");

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

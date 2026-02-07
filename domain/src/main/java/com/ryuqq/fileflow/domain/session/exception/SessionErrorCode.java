package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

public enum SessionErrorCode implements ErrorCode {

    SESSION_NOT_FOUND("SESSION-001", 404, "세션을 찾을 수 없습니다"),
    SESSION_ALREADY_COMPLETED("SESSION-002", 409, "이미 완료된 세션입니다"),
    SESSION_EXPIRED("SESSION-003", 410, "만료된 세션입니다"),
    SESSION_ALREADY_ABORTED("SESSION-004", 409, "이미 중단된 세션입니다"),
    INVALID_SESSION_STATUS("SESSION-005", 400, "유효하지 않은 세션 상태입니다"),
    PART_NUMBER_DUPLICATE("SESSION-006", 409, "중복된 파트 번호입니다"),
    PART_NUMBER_INVALID("SESSION-007", 400, "유효하지 않은 파트 번호입니다");

    private final String code;
    private final int httpStatus;
    private final String message;

    SessionErrorCode(String code, int httpStatus, String message) {
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

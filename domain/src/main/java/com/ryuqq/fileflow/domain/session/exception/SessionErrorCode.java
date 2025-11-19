package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

/**
 * 세션 관련 에러 코드 Enum.
 */
public enum SessionErrorCode implements ErrorCode {

    FILE_SIZE_EXCEEDED("FILE-SIZE-EXCEEDED", "파일 크기가 최대 허용 크기를 초과했습니다", 400),
    UNSUPPORTED_FILE_TYPE("UNSUPPORTED-FILE-TYPE", "지원하지 않는 파일 타입입니다", 400),
    INVALID_SESSION_STATUS("INVALID-SESSION-STATUS", "세션 상태 전환이 불가능합니다", 409),
    SESSION_EXPIRED("SESSION-EXPIRED", "세션이 만료되었습니다", 410);

    private final String code;
    private final String message;
    private final int httpStatus;

    SessionErrorCode(String code, String message, int httpStatus) {
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


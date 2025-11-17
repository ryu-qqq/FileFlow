package com.ryuqq.fileflow.domain.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

/**
 * 파일 크기 검증 ErrorCode Enum
 * <p>
 * 파일 크기 관련 에러 코드를 정의합니다.
 * </p>
 *
 * <p>
 * <strong>코드 형식</strong>: SIZE-{3자리 숫자}
 * </p>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum InvalidFileSizeErrorCode implements ErrorCode {

    /**
     * 음수 파일 크기
     */
    NEGATIVE_FILE_SIZE("SIZE-001", "파일 크기는 0 이상이어야 합니다", 400),

    /**
     * 파일 크기 제한 초과
     */
    FILE_SIZE_LIMIT_EXCEEDED("SIZE-002", "파일 크기 제한을 초과했습니다", 400),

    /**
     * 파일 크기가 0
     */
    EMPTY_FILE_SIZE("SIZE-003", "파일 크기는 0보다 커야 합니다", 400);

    private final String code;
    private final String message;
    private final int httpStatus;

    /**
     * 생성자
     *
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param httpStatus HTTP 상태 코드
     */
    InvalidFileSizeErrorCode(String code, String message, int httpStatus) {
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

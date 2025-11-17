package com.ryuqq.fileflow.domain.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

/**
 * MIME 타입 검증 ErrorCode Enum
 * <p>
 * MIME 타입 관련 에러 코드를 정의합니다.
 * </p>
 *
 * <p>
 * <strong>코드 형식</strong>: MIME-{3자리 숫자}
 * </p>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum InvalidMimeTypeErrorCode implements ErrorCode {

    /**
     * 지원하지 않는 MIME 타입
     */
    INVALID_MIME_TYPE("MIME-001", "지원하지 않는 MIME 타입입니다", 400),

    /**
     * 비어있는 MIME 타입
     */
    EMPTY_MIME_TYPE("MIME-002", "MIME 타입은 비어있을 수 없습니다", 400),

    /**
     * 잘못된 MIME 타입 형식
     */
    MALFORMED_MIME_TYPE("MIME-003", "잘못된 MIME 타입 형식입니다", 400);

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
    InvalidMimeTypeErrorCode(String code, String message, int httpStatus) {
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

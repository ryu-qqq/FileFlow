package com.ryuqq.fileflow.domain.session.exception;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;

/**
 * 세션 관련 에러 코드 Enum.
 *
 * <p>
 * 파일 업로드 세션과 관련된 도메인 예외를 정의합니다.
 * 각 에러 코드는 고유한 코드, 메시지, HTTP 상태 코드를 가집니다.
 * </p>
 *
 * <p>
 * <strong>에러 코드 목록</strong>:
 * </p>
 * <ul>
 *     <li>FILE_SIZE_EXCEEDED: 파일 크기가 최대 허용 크기를 초과한 경우 (400)</li>
 *     <li>UNSUPPORTED_FILE_TYPE: 지원하지 않는 파일 타입인 경우 (400)</li>
 *     <li>INVALID_SESSION_STATUS: 세션 상태 전환이 불가능한 경우 (409)</li>
 *     <li>SESSION_EXPIRED: 세션이 만료된 경우 (410)</li>
 * </ul>
 */
public enum SessionErrorCode implements ErrorCode {

    /**
     * 파일 크기가 최대 허용 크기를 초과한 경우
     */
    FILE_SIZE_EXCEEDED("FILE-SIZE-EXCEEDED", "파일 크기가 최대 허용 크기를 초과했습니다", 400),

    /**
     * 지원하지 않는 파일 타입인 경우
     */
    UNSUPPORTED_FILE_TYPE("UNSUPPORTED-FILE-TYPE", "지원하지 않는 파일 타입입니다", 400),

    /**
     * 세션 상태 전환이 불가능한 경우
     */
    INVALID_SESSION_STATUS("INVALID-SESSION-STATUS", "세션 상태 전환이 불가능합니다", 409),

    /**
     * 세션이 만료된 경우
     */
    SESSION_EXPIRED("SESSION-EXPIRED", "세션이 만료되었습니다", 410);

    private final String code;
    private final String message;
    private final int httpStatus;

    /**
     * SessionErrorCode 생성자
     *
     * @param code 에러 코드 문자열
     * @param message 에러 메시지
     * @param httpStatus HTTP 상태 코드
     */
    SessionErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    /**
     * 에러 코드를 반환한다.
     *
     * @return 에러 코드 (예: "FILE-SIZE-EXCEEDED")
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 에러 메시지를 반환한다.
     *
     * @return 에러 메시지
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * HTTP 상태 코드를 반환한다.
     *
     * @return HTTP 상태 코드 (예: 400, 409, 410)
     */
    @Override
    public int getHttpStatus() {
        return httpStatus;
    }
}


package com.ryuqq.fileflow.domain.common.exception;

import java.util.Collections;
import java.util.Map;

// domain 모듈
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode; // ex) TENANT_NOT_FOUND
    private final Map<String, Object> args; // 메시지 템플릿 파라미터 등 (선택)

    /**
     * Constructor - ErrorCode 기반 예외 생성
     *
     * @param errorCode 에러 코드 (필수)
     */
    protected DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = Collections.emptyMap();
    }

    /**
     * Constructor - ErrorCode + 커스텀 메시지
     *
     * @param errorCode 에러 코드 (필수)
     * @param message 커스텀 에러 메시지
     */
    protected DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = Collections.emptyMap();
    }

    /**
     * Constructor - ErrorCode + 커스텀 메시지 + 컨텍스트 정보
     *
     * @param errorCode 에러 코드 (필수)
     * @param message 커스텀 에러 메시지
     * @param args 디버깅용 컨텍스트 정보
     */
    protected DomainException(ErrorCode errorCode, String message, Map<String, Object> args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args != null ? Map.copyOf(args) : Collections.emptyMap();
    }

    /**
     * 에러 코드 객체 반환
     *
     * @return ErrorCode 객체
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 에러 코드 문자열 반환 (편의 메서드)
     *
     * @return 에러 코드 문자열 (예: "ORDER-001")
     */
    public String code() {
        return errorCode.getCode();
    }

    /**
     * HTTP 상태 코드 반환 (편의 메서드)
     *
     * @return HTTP 상태 코드 (예: 404, 400, 409)
     */
    public int httpStatus() {
        return errorCode.getHttpStatus();
    }

    /**
     * 컨텍스트 정보 반환
     *
     * @return 디버깅용 컨텍스트 정보 (불변 Map)
     */
    public Map<String, Object> args() {
        return args;
    }
}

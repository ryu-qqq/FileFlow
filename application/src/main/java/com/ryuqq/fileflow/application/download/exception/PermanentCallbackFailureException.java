package com.ryuqq.fileflow.application.download.exception;

/**
 * 재시도가 불가능한 영구적 콜백 실패.
 *
 * <p>HTTP 4xx (잘못된 콜백 URL, 인증 실패 등) 재시도해도 결과가 동일한 실패에 사용합니다.
 */
public class PermanentCallbackFailureException extends RuntimeException {

    public PermanentCallbackFailureException(String message) {
        super(message);
    }

    public PermanentCallbackFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}

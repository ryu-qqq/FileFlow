package com.ryuqq.fileflow.adapter.sqs.exception;

/**
 * 세션 매칭 실패 예외
 *
 * S3 key에서 세션 ID를 추출하거나 세션을 조회하는 과정에서 발생하는 예외입니다.
 *
 * @author sangwon-ryu
 */
public class SessionMatchingException extends RuntimeException {

    public SessionMatchingException(String message) {
        super(message);
    }

    public SessionMatchingException(String message, Throwable cause) {
        super(message, cause);
    }
}

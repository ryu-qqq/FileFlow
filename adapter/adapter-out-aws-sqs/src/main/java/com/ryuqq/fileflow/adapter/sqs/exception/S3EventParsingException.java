package com.ryuqq.fileflow.adapter.sqs.exception;

/**
 * S3 이벤트 파싱 실패 예외
 *
 * SQS 메시지에서 S3 이벤트를 파싱하는 과정에서 발생하는 예외입니다.
 *
 * @author sangwon-ryu
 */
public class S3EventParsingException extends RuntimeException {

    public S3EventParsingException(String message) {
        super(message);
    }

    public S3EventParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.ryuqq.fileflow.adapter.in.sqs.listener;

/**
 * 파일 가공 처리 예외.
 *
 * <p>SQS 메시지 처리 중 발생하는 예외입니다. ACK를 전송하지 않아 SQS가 메시지를 재전달합니다.
 */
public class FileProcessingException extends RuntimeException {

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

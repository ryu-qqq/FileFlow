package com.ryuqq.fileflow.adapter.in.sqs.listener;

/**
 * External Download 처리 중 발생한 예외.
 *
 * <p>SQS Listener에서 다운로드 처리 실패 시 발생합니다. 이 예외가 발생하면 SQS가 메시지를 재시도합니다.
 *
 * <p><strong>재시도 정책</strong>:
 *
 * <ul>
 *   <li>visibility_timeout: 360초 후 재전달
 *   <li>max_receive_count: 3회 실패 시 DLQ 이동
 * </ul>
 */
public class ExternalDownloadProcessingException extends RuntimeException {

    public ExternalDownloadProcessingException(String message) {
        super(message);
    }

    public ExternalDownloadProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

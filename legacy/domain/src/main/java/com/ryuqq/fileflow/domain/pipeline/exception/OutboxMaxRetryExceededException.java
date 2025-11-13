package com.ryuqq.fileflow.domain.pipeline.exception;

/**
 * 최대 재시도 횟수를 초과했을 때 발생하는 예외
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class OutboxMaxRetryExceededException extends PipelineException {

    /**
     * 최대 재시도 횟수 초과 예외 생성
     *
     * @param outboxId     Outbox ID
     * @param retryCount   현재 재시도 횟수
     * @param maxRetryCount 최대 재시도 횟수
     */
    public OutboxMaxRetryExceededException(Long outboxId, int retryCount, int maxRetryCount) {
        super(
            PipelineErrorCode.OUTBOX_MAX_RETRY_EXCEEDED,
            "Max retry count exceeded for outbox: " + outboxId
                + " (retryCount: " + retryCount + ", maxRetryCount: " + maxRetryCount + ")"
        );
    }
}


package com.ryuqq.fileflow.domain.pipeline.exception;

/**
 * 잘못된 Outbox 상태 전이 시 발생하는 예외
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InvalidOutboxStateTransitionException extends PipelineException {

    /**
     * 잘못된 상태 전이 예외 생성
     *
     * @param currentStatus 현재 상태
     * @param targetStatus  목표 상태
     */
    public InvalidOutboxStateTransitionException(String currentStatus, String targetStatus) {
        super(
            PipelineErrorCode.OUTBOX_INVALID_STATE_TRANSITION,
            "Invalid state transition from " + currentStatus + " to " + targetStatus
        );
    }
}


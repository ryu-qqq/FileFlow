package com.ryuqq.fileflow.domain.pipeline.exception;

import com.ryuqq.fileflow.domain.common.OutboxStatus;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutboxId;

/**
 * 잘못된 Outbox 상태 전이 시 발생하는 예외
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InvalidOutboxStateTransitionException extends PipelineException {

    /**
     * 잘못된 상태 전이 예외 생성 (타입 안전)
     *
     * @param outboxId Outbox ID
     * @param currentStatus 현재 상태
     * @param targetStatus  목표 상태
     */
    public InvalidOutboxStateTransitionException(
        PipelineOutboxId outboxId,
        OutboxStatus currentStatus,
        OutboxStatus targetStatus
    ) {
        super(
            PipelineErrorCode.OUTBOX_INVALID_STATE_TRANSITION,
            String.format(
                "Invalid state transition for Outbox ID %s: from %s to %s",
                outboxId.value(),
                currentStatus.name(),
                targetStatus.name()
            )
        );
    }

    /**
     * 잘못된 상태 전이 예외 생성 (레거시 지원 - String 파라미터)
     *
     * @param currentStatus 현재 상태 (String)
     * @param targetStatus  목표 상태 (String)
     * @deprecated OutboxStatus enum을 사용하는 생성자를 권장합니다.
     */
    @Deprecated
    public InvalidOutboxStateTransitionException(String currentStatus, String targetStatus) {
        super(
            PipelineErrorCode.OUTBOX_INVALID_STATE_TRANSITION,
            "Invalid state transition from " + currentStatus + " to " + targetStatus
        );
    }
}


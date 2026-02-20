package com.ryuqq.fileflow.domain.transform.aggregate;

import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.transform.id.TransformQueueOutboxId;
import java.time.Instant;
import java.util.Objects;

/**
 * 변환 큐 아웃박스 Aggregate Root.
 *
 * <p>변환 요청 생성 시 SQS 발행을 트랜잭션과 분리하기 위한 아웃박스 패턴 구현체입니다.
 *
 * <p>라이프사이클: PENDING -> SENT | FAILED
 */
public class TransformQueueOutbox {

    private final TransformQueueOutboxId id;
    private final String transformRequestId;
    private OutboxStatus status;
    private int retryCount;
    private String lastError;
    private final Instant createdAt;
    private Instant processedAt;

    private TransformQueueOutbox(
            TransformQueueOutboxId id,
            String transformRequestId,
            OutboxStatus status,
            int retryCount,
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        this.id = id;
        this.transformRequestId = transformRequestId;
        this.status = status;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public static TransformQueueOutbox forNew(
            TransformQueueOutboxId id, String transformRequestId, Instant now) {
        return new TransformQueueOutbox(
                id, transformRequestId, OutboxStatus.PENDING, 0, null, now, null);
    }

    public static TransformQueueOutbox reconstitute(
            TransformQueueOutboxId id,
            String transformRequestId,
            OutboxStatus status,
            int retryCount,
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        return new TransformQueueOutbox(
                id, transformRequestId, status, retryCount, lastError, createdAt, processedAt);
    }

    /** SQS 발행 성공 처리. */
    public void markSent(Instant now) {
        this.status = OutboxStatus.SENT;
        this.processedAt = now;
    }

    /** SQS 발행 실패 처리. */
    public void markFailed(String errorMessage, Instant now) {
        this.status = OutboxStatus.FAILED;
        this.lastError = errorMessage;
        this.retryCount++;
        this.processedAt = now;
    }

    // -- query methods --

    public TransformQueueOutboxId id() {
        return id;
    }

    public String idValue() {
        return id.value();
    }

    public String transformRequestId() {
        return transformRequestId;
    }

    public OutboxStatus status() {
        return status;
    }

    public int retryCount() {
        return retryCount;
    }

    public String lastError() {
        return lastError;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant processedAt() {
        return processedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformQueueOutbox that = (TransformQueueOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

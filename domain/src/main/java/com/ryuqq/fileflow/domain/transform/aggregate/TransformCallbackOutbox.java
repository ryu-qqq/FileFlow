package com.ryuqq.fileflow.domain.transform.aggregate;

import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.transform.id.TransformCallbackOutboxId;
import java.time.Instant;
import java.util.Objects;

/**
 * 변환 콜백 알림 아웃박스 Aggregate Root.
 *
 * <p>변환 완료/실패 시 콜백 알림을 안정적으로 전달하기 위한 아웃박스 패턴 구현체입니다.
 *
 * <p>라이프사이클: PENDING → SENT | FAILED
 */
public class TransformCallbackOutbox {

    private static final int DEFAULT_MAX_RETRIES = 5;

    private final TransformCallbackOutboxId id;
    private final String transformRequestId;
    private final String callbackUrl;
    private final String taskStatus;
    private OutboxStatus outboxStatus;
    private int retryCount;
    private final int maxRetries;
    private String lastError;
    private final Instant createdAt;
    private Instant processedAt;

    private TransformCallbackOutbox(
            TransformCallbackOutboxId id,
            String transformRequestId,
            String callbackUrl,
            String taskStatus,
            OutboxStatus outboxStatus,
            int retryCount,
            int maxRetries,
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        this.id = id;
        this.transformRequestId = transformRequestId;
        this.callbackUrl = callbackUrl;
        this.taskStatus = taskStatus;
        this.outboxStatus = outboxStatus;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public static TransformCallbackOutbox forNew(
            TransformCallbackOutboxId id,
            String transformRequestId,
            String callbackUrl,
            String taskStatus,
            Instant now) {
        return new TransformCallbackOutbox(
                id,
                transformRequestId,
                callbackUrl,
                taskStatus,
                OutboxStatus.PENDING,
                0,
                DEFAULT_MAX_RETRIES,
                null,
                now,
                null);
    }

    public static TransformCallbackOutbox reconstitute(
            TransformCallbackOutboxId id,
            String transformRequestId,
            String callbackUrl,
            String taskStatus,
            OutboxStatus outboxStatus,
            int retryCount,
            int maxRetries,
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        return new TransformCallbackOutbox(
                id,
                transformRequestId,
                callbackUrl,
                taskStatus,
                outboxStatus,
                retryCount,
                maxRetries,
                lastError,
                createdAt,
                processedAt);
    }

    public void markSent(Instant now) {
        this.outboxStatus = OutboxStatus.SENT;
        this.processedAt = now;
    }

    public void markFailed(String errorMessage, Instant now) {
        this.lastError = errorMessage;
        this.retryCount++;
        this.processedAt = now;

        if (this.retryCount >= this.maxRetries) {
            this.outboxStatus = OutboxStatus.FAILED;
        }
    }

    public void markFailedPermanently(String errorMessage, Instant now) {
        this.lastError = errorMessage;
        this.outboxStatus = OutboxStatus.FAILED;
        this.processedAt = now;
    }

    // -- query methods --

    public TransformCallbackOutboxId id() {
        return id;
    }

    public String idValue() {
        return id.value();
    }

    public String transformRequestId() {
        return transformRequestId;
    }

    public String callbackUrl() {
        return callbackUrl;
    }

    public String taskStatus() {
        return taskStatus;
    }

    public OutboxStatus outboxStatus() {
        return outboxStatus;
    }

    public int retryCount() {
        return retryCount;
    }

    public int maxRetries() {
        return maxRetries;
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
        TransformCallbackOutbox that = (TransformCallbackOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

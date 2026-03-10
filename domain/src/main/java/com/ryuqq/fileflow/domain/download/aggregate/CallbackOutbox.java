package com.ryuqq.fileflow.domain.download.aggregate;

import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.download.id.CallbackOutboxId;
import java.time.Instant;
import java.util.Objects;

/**
 * 콜백 알림 아웃박스 Aggregate Root.
 *
 * <p>다운로드 완료/실패 시 콜백 알림을 안정적으로 전달하기 위한 아웃박스 패턴 구현체입니다.
 *
 * <p>라이프사이클: PENDING → SENT | FAILED
 */
public class CallbackOutbox {

    private static final int DEFAULT_MAX_RETRIES = 5;

    private final CallbackOutboxId id;
    private final String downloadTaskId;
    private final String callbackUrl;
    private final String taskStatus;
    private OutboxStatus outboxStatus;
    private int retryCount;
    private final int maxRetries;
    private String lastError;
    private final Instant createdAt;
    private Instant processedAt;

    private CallbackOutbox(
            CallbackOutboxId id,
            String downloadTaskId,
            String callbackUrl,
            String taskStatus,
            OutboxStatus outboxStatus,
            int retryCount,
            int maxRetries,
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        this.id = id;
        this.downloadTaskId = downloadTaskId;
        this.callbackUrl = callbackUrl;
        this.taskStatus = taskStatus;
        this.outboxStatus = outboxStatus;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public static CallbackOutbox forNew(
            CallbackOutboxId id,
            String downloadTaskId,
            String callbackUrl,
            String taskStatus,
            Instant now) {
        return new CallbackOutbox(
                id,
                downloadTaskId,
                callbackUrl,
                taskStatus,
                OutboxStatus.PENDING,
                0,
                DEFAULT_MAX_RETRIES,
                null,
                now,
                null);
    }

    public static CallbackOutbox reconstitute(
            CallbackOutboxId id,
            String downloadTaskId,
            String callbackUrl,
            String taskStatus,
            OutboxStatus outboxStatus,
            int retryCount,
            int maxRetries,
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        return new CallbackOutbox(
                id,
                downloadTaskId,
                callbackUrl,
                taskStatus,
                outboxStatus,
                retryCount,
                maxRetries,
                lastError,
                createdAt,
                processedAt);
    }

    /** 콜백 전송 성공 처리. */
    public void markSent(Instant now) {
        this.outboxStatus = OutboxStatus.SENT;
        this.processedAt = now;
    }

    /** 재시도 가능한 실패 처리. retryCount < maxRetries면 PENDING 복귀(재시도), 아니면 FAILED. */
    public void markFailed(String errorMessage, Instant now) {
        this.lastError = errorMessage;
        this.retryCount++;
        this.processedAt = now;
        this.outboxStatus =
                (this.retryCount >= this.maxRetries) ? OutboxStatus.FAILED : OutboxStatus.PENDING;
    }

    /** 재시도 불필요한 영구 실패 처리 (4xx 등). */
    public void markFailedPermanently(String errorMessage, Instant now) {
        this.lastError = errorMessage;
        this.outboxStatus = OutboxStatus.FAILED;
        this.processedAt = now;
    }

    // -- query methods --

    public CallbackOutboxId id() {
        return id;
    }

    public String idValue() {
        return id.value();
    }

    public String downloadTaskId() {
        return downloadTaskId;
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
        CallbackOutbox that = (CallbackOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

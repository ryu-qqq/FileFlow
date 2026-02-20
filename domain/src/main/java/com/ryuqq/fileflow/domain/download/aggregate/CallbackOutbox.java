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

    private final CallbackOutboxId id;
    private final String downloadTaskId;
    private final String callbackUrl;
    private final String taskStatus;
    private OutboxStatus outboxStatus;
    private int retryCount;
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
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        this.id = id;
        this.downloadTaskId = downloadTaskId;
        this.callbackUrl = callbackUrl;
        this.taskStatus = taskStatus;
        this.outboxStatus = outboxStatus;
        this.retryCount = retryCount;
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
                lastError,
                createdAt,
                processedAt);
    }

    /** 콜백 전송 성공 처리. */
    public void markSent(Instant now) {
        this.outboxStatus = OutboxStatus.SENT;
        this.processedAt = now;
    }

    /** 콜백 전송 실패 처리. */
    public void markFailed(String errorMessage, Instant now) {
        this.outboxStatus = OutboxStatus.FAILED;
        this.lastError = errorMessage;
        this.retryCount++;
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

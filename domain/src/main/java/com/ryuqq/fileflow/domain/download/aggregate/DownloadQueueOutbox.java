package com.ryuqq.fileflow.domain.download.aggregate;

import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.download.id.DownloadQueueOutboxId;
import java.time.Instant;
import java.util.Objects;

/**
 * 다운로드 큐 아웃박스 Aggregate Root.
 *
 * <p>다운로드 태스크 생성 시 SQS 발행을 트랜잭션과 분리하기 위한 아웃박스 패턴 구현체입니다.
 *
 * <p>라이프사이클: PENDING -> SENT | FAILED
 */
public class DownloadQueueOutbox {

    private final DownloadQueueOutboxId id;
    private final String downloadTaskId;
    private OutboxStatus status;
    private int retryCount;
    private String lastError;
    private final Instant createdAt;
    private Instant processedAt;

    private DownloadQueueOutbox(
            DownloadQueueOutboxId id,
            String downloadTaskId,
            OutboxStatus status,
            int retryCount,
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        this.id = id;
        this.downloadTaskId = downloadTaskId;
        this.status = status;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public static DownloadQueueOutbox forNew(
            DownloadQueueOutboxId id, String downloadTaskId, Instant now) {
        return new DownloadQueueOutbox(
                id, downloadTaskId, OutboxStatus.PENDING, 0, null, now, null);
    }

    public static DownloadQueueOutbox reconstitute(
            DownloadQueueOutboxId id,
            String downloadTaskId,
            OutboxStatus status,
            int retryCount,
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        return new DownloadQueueOutbox(
                id, downloadTaskId, status, retryCount, lastError, createdAt, processedAt);
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

    public DownloadQueueOutboxId id() {
        return id;
    }

    public String idValue() {
        return id.value();
    }

    public String downloadTaskId() {
        return downloadTaskId;
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
        DownloadQueueOutbox that = (DownloadQueueOutbox) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

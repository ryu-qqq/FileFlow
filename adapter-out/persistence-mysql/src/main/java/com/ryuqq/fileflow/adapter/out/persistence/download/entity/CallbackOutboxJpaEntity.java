package com.ryuqq.fileflow.adapter.out.persistence.download.entity;

import com.ryuqq.fileflow.domain.download.vo.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "callback_outbox")
public class CallbackOutboxJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "download_task_id", length = 36, nullable = false)
    private String downloadTaskId;

    @Column(name = "callback_url", columnDefinition = "TEXT", nullable = false)
    private String callbackUrl;

    @Column(name = "task_status", length = 20, nullable = false)
    private String taskStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "outbox_status", length = 20, nullable = false)
    private OutboxStatus outboxStatus;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected CallbackOutboxJpaEntity() {}

    private CallbackOutboxJpaEntity(
            String id,
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

    public static CallbackOutboxJpaEntity create(
            String id,
            String downloadTaskId,
            String callbackUrl,
            String taskStatus,
            OutboxStatus outboxStatus,
            int retryCount,
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        return new CallbackOutboxJpaEntity(
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

    public String getId() {
        return id;
    }

    public String getDownloadTaskId() {
        return downloadTaskId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public OutboxStatus getOutboxStatus() {
        return outboxStatus;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}

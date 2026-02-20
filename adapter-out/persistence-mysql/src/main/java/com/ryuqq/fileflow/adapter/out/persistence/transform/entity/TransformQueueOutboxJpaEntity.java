package com.ryuqq.fileflow.adapter.out.persistence.transform.entity;

import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "transform_queue_outbox")
public class TransformQueueOutboxJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "transform_request_id", length = 36, nullable = false)
    private String transformRequestId;

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

    protected TransformQueueOutboxJpaEntity() {}

    private TransformQueueOutboxJpaEntity(
            String id,
            String transformRequestId,
            OutboxStatus outboxStatus,
            int retryCount,
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        this.id = id;
        this.transformRequestId = transformRequestId;
        this.outboxStatus = outboxStatus;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public static TransformQueueOutboxJpaEntity create(
            String id,
            String transformRequestId,
            OutboxStatus outboxStatus,
            int retryCount,
            String lastError,
            Instant createdAt,
            Instant processedAt) {
        return new TransformQueueOutboxJpaEntity(
                id,
                transformRequestId,
                outboxStatus,
                retryCount,
                lastError,
                createdAt,
                processedAt);
    }

    public String getId() {
        return id;
    }

    public String getTransformRequestId() {
        return transformRequestId;
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

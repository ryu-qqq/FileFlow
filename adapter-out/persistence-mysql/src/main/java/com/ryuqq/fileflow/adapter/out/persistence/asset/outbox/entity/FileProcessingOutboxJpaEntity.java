package com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * FileProcessingOutbox JPA Entity.
 *
 * <p>Transactional Outbox 패턴을 위한 메시지 큐 정보를 저장합니다.
 *
 * <p>ID는 UUID v7 (Time-Ordered) 사용.
 */
@Entity
@Table(name = "file_processing_outbox")
public class FileProcessingOutboxJpaEntity extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "file_asset_id", nullable = false, length = 36)
    private String fileAssetId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatusEnum status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected FileProcessingOutboxJpaEntity() {
        super();
    }

    private FileProcessingOutboxJpaEntity(
            UUID id,
            String fileAssetId,
            String eventType,
            String payload,
            OutboxStatusEnum status,
            Integer retryCount,
            String errorMessage,
            Instant processedAt,
            Instant createdAt,
            Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.fileAssetId = fileAssetId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.processedAt = processedAt;
    }

    public static FileProcessingOutboxJpaEntity of(
            UUID id,
            String fileAssetId,
            String eventType,
            String payload,
            OutboxStatusEnum status,
            Integer retryCount,
            String errorMessage,
            Instant processedAt,
            Instant createdAt,
            Instant updatedAt) {
        return new FileProcessingOutboxJpaEntity(
                id,
                fileAssetId,
                eventType,
                payload,
                status,
                retryCount,
                errorMessage,
                processedAt,
                createdAt,
                updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public String getFileAssetId() {
        return fileAssetId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatusEnum getStatus() {
        return status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    /** Outbox 상태 열거형 (Entity 전용). */
    public enum OutboxStatusEnum {
        PENDING,
        SENT,
        FAILED
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.download.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ExternalDownloadOutbox JPA Entity.
 *
 * <p>SQS 발행을 위한 Outbox 패턴 정보를 저장합니다.
 *
 * <p>ID는 UUID v7 (Time-Ordered) 사용.
 */
@Entity
@Table(name = "external_download_outbox")
public class ExternalDownloadOutboxJpaEntity extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "external_download_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID externalDownloadId;

    @Column(name = "published", nullable = false)
    private Boolean published;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    protected ExternalDownloadOutboxJpaEntity() {
        super();
    }

    private ExternalDownloadOutboxJpaEntity(
            UUID id,
            UUID externalDownloadId,
            Boolean published,
            LocalDateTime publishedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.externalDownloadId = externalDownloadId;
        this.published = published;
        this.publishedAt = publishedAt;
    }

    public static ExternalDownloadOutboxJpaEntity of(
            UUID id,
            UUID externalDownloadId,
            Boolean published,
            LocalDateTime publishedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new ExternalDownloadOutboxJpaEntity(
                id, externalDownloadId, published, publishedAt, createdAt, updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getExternalDownloadId() {
        return externalDownloadId;
    }

    public Boolean getPublished() {
        return published;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}

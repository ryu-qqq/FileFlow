package com.ryuqq.fileflow.adapter.out.persistence.download.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * ExternalDownloadOutbox JPA Entity.
 *
 * <p>SQS 발행을 위한 Outbox 패턴 정보를 저장합니다.
 */
@Entity
@Table(name = "external_download_outbox")
public class ExternalDownloadOutboxJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "external_download_id", nullable = false)
    private Long externalDownloadId;

    @Column(name = "published", nullable = false)
    private Boolean published;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    protected ExternalDownloadOutboxJpaEntity() {
        super();
    }

    private ExternalDownloadOutboxJpaEntity(
            Long id,
            Long externalDownloadId,
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
            Long id,
            Long externalDownloadId,
            Boolean published,
            LocalDateTime publishedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new ExternalDownloadOutboxJpaEntity(
                id, externalDownloadId, published, publishedAt, createdAt, updatedAt);
    }

    public Long getId() {
        return id;
    }

    public Long getExternalDownloadId() {
        return externalDownloadId;
    }

    public Boolean getPublished() {
        return published;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.download.entity;

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
 * WebhookOutbox JPA Entity.
 *
 * <p>Webhook 발송을 위한 Outbox 패턴 정보를 저장합니다.
 *
 * <p>ID는 UUID v7 (Time-Ordered) 사용.
 */
@Entity
@Table(name = "webhook_outbox")
public class WebhookOutboxJpaEntity extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "external_download_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID externalDownloadId;

    @Column(name = "webhook_url", nullable = false, length = 2048)
    private String webhookUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WebhookOutboxStatusJpa status;

    @Enumerated(EnumType.STRING)
    @Column(name = "download_status", nullable = false, length = 20)
    private ExternalDownloadStatusJpa downloadStatus;

    @Column(name = "file_asset_id", columnDefinition = "BINARY(16)")
    private UUID fileAssetId;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;

    @Column(name = "sent_at")
    private Instant sentAt;

    protected WebhookOutboxJpaEntity() {
        super();
    }

    private WebhookOutboxJpaEntity(
            UUID id,
            UUID externalDownloadId,
            String webhookUrl,
            WebhookOutboxStatusJpa status,
            ExternalDownloadStatusJpa downloadStatus,
            UUID fileAssetId,
            String errorMessage,
            Integer retryCount,
            String lastErrorMessage,
            Instant sentAt,
            Instant createdAt,
            Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.externalDownloadId = externalDownloadId;
        this.webhookUrl = webhookUrl;
        this.status = status;
        this.downloadStatus = downloadStatus;
        this.fileAssetId = fileAssetId;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.lastErrorMessage = lastErrorMessage;
        this.sentAt = sentAt;
    }

    public static WebhookOutboxJpaEntity of(
            UUID id,
            UUID externalDownloadId,
            String webhookUrl,
            WebhookOutboxStatusJpa status,
            ExternalDownloadStatusJpa downloadStatus,
            UUID fileAssetId,
            String errorMessage,
            Integer retryCount,
            String lastErrorMessage,
            Instant sentAt,
            Instant createdAt,
            Instant updatedAt) {
        return new WebhookOutboxJpaEntity(
                id,
                externalDownloadId,
                webhookUrl,
                status,
                downloadStatus,
                fileAssetId,
                errorMessage,
                retryCount,
                lastErrorMessage,
                sentAt,
                createdAt,
                updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getExternalDownloadId() {
        return externalDownloadId;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public WebhookOutboxStatusJpa getStatus() {
        return status;
    }

    public ExternalDownloadStatusJpa getDownloadStatus() {
        return downloadStatus;
    }

    public UUID getFileAssetId() {
        return fileAssetId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    /** WebhookOutbox 상태 (JPA용 Enum). */
    public enum WebhookOutboxStatusJpa {
        PENDING,
        SENT,
        FAILED
    }

    /** ExternalDownload 상태 (JPA용 Enum). */
    public enum ExternalDownloadStatusJpa {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}

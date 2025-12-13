package com.ryuqq.fileflow.adapter.out.persistence.download.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

/**
 * ExternalDownload JPA Entity.
 *
 * <p>외부 다운로드 요청 정보를 저장합니다.
 *
 * <p>ID는 UUID v7 (Time-Ordered) 사용.
 */
@Entity
@Table(name = "external_download")
public class ExternalDownloadJpaEntity extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "source_url", nullable = false, length = 2048)
    private String sourceUrl;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "organization_id", nullable = false, length = 36)
    private String organizationId;

    @Column(name = "s3_bucket", nullable = false, length = 63)
    private String s3Bucket;

    @Column(name = "s3_path_prefix", nullable = false, length = 255)
    private String s3PathPrefix;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ExternalDownloadStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "file_asset_id", length = 36)
    private String fileAssetId;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "webhook_url", length = 2048)
    private String webhookUrl;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected ExternalDownloadJpaEntity() {
        super();
    }

    private ExternalDownloadJpaEntity(
            UUID id,
            String sourceUrl,
            String tenantId,
            String organizationId,
            String s3Bucket,
            String s3PathPrefix,
            ExternalDownloadStatus status,
            Integer retryCount,
            String fileAssetId,
            String errorMessage,
            String webhookUrl,
            Long version,
            Instant createdAt,
            Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.s3Bucket = s3Bucket;
        this.s3PathPrefix = s3PathPrefix;
        this.status = status;
        this.retryCount = retryCount;
        this.fileAssetId = fileAssetId;
        this.errorMessage = errorMessage;
        this.webhookUrl = webhookUrl;
        this.version = version;
    }

    public static ExternalDownloadJpaEntity of(
            UUID id,
            String sourceUrl,
            String tenantId,
            String organizationId,
            String s3Bucket,
            String s3PathPrefix,
            ExternalDownloadStatus status,
            Integer retryCount,
            String fileAssetId,
            String errorMessage,
            String webhookUrl,
            Long version,
            Instant createdAt,
            Instant updatedAt) {
        return new ExternalDownloadJpaEntity(
                id,
                sourceUrl,
                tenantId,
                organizationId,
                s3Bucket,
                s3PathPrefix,
                status,
                retryCount,
                fileAssetId,
                errorMessage,
                webhookUrl,
                version,
                createdAt,
                updatedAt);
    }

    public UUID getId() {
        return id;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getS3PathPrefix() {
        return s3PathPrefix;
    }

    public ExternalDownloadStatus getStatus() {
        return status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getFileAssetId() {
        return fileAssetId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public Long getVersion() {
        return version;
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.asset.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * FileAsset JPA Entity.
 *
 * <p>업로드 완료된 파일 자산 정보를 저장합니다.
 */
@Entity
@Table(name = "file_asset")
public class FileAssetJpaEntity extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "category", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private FileCategory category;

    @Column(name = "bucket", nullable = false, length = 63)
    private String bucket;

    @Column(name = "s3_key", nullable = false, length = 1024)
    private String s3Key;

    @Column(name = "etag", nullable = false, length = 64)
    private String etag;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FileAssetStatus status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    protected FileAssetJpaEntity() {
        super();
    }

    private FileAssetJpaEntity(
            String id,
            String sessionId,
            String fileName,
            Long fileSize,
            String contentType,
            FileCategory category,
            String bucket,
            String s3Key,
            String etag,
            Long userId,
            Long organizationId,
            Long tenantId,
            FileAssetStatus status,
            LocalDateTime processedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.sessionId = sessionId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.category = category;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.etag = etag;
        this.userId = userId;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
        this.status = status;
        this.processedAt = processedAt;
    }

    public static FileAssetJpaEntity of(
            String id,
            String sessionId,
            String fileName,
            Long fileSize,
            String contentType,
            FileCategory category,
            String bucket,
            String s3Key,
            String etag,
            Long userId,
            Long organizationId,
            Long tenantId,
            FileAssetStatus status,
            LocalDateTime processedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new FileAssetJpaEntity(
                id,
                sessionId,
                fileName,
                fileSize,
                contentType,
                category,
                bucket,
                s3Key,
                etag,
                userId,
                organizationId,
                tenantId,
                status,
                processedAt,
                createdAt,
                updatedAt);
    }

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public FileCategory getCategory() {
        return category;
    }

    public String getBucket() {
        return bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public String getEtag() {
        return etag;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public FileAssetStatus getStatus() {
        return status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.asset.entity;

import com.ryuqq.fileflow.domain.asset.vo.ImageFormatType;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariantType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * ProcessedFileAsset JPA Entity.
 *
 * <p>처리된 파일 에셋 (리사이징, 포맷 변환 결과)을 저장합니다.
 */
@Entity
@Table(name = "processed_file_asset")
public class ProcessedFileAssetJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "original_asset_id", nullable = false, length = 36)
    private String originalAssetId;

    @Column(name = "parent_asset_id", length = 36)
    private String parentAssetId;

    @Column(name = "variant_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ImageVariantType variantType;

    @Column(name = "format_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ImageFormatType formatType;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "bucket", nullable = false, length = 63)
    private String bucket;

    @Column(name = "s3_key", nullable = false, length = 1024)
    private String s3Key;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "organization_id", nullable = false, length = 36)
    private String organizationId;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProcessedFileAssetJpaEntity() {}

    private ProcessedFileAssetJpaEntity(
            UUID id,
            String originalAssetId,
            String parentAssetId,
            ImageVariantType variantType,
            ImageFormatType formatType,
            String fileName,
            Long fileSize,
            String bucket,
            String s3Key,
            String userId,
            String organizationId,
            String tenantId,
            Instant createdAt) {
        this.id = id;
        this.originalAssetId = originalAssetId;
        this.parentAssetId = parentAssetId;
        this.variantType = variantType;
        this.formatType = formatType;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.userId = userId;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
    }

    public static ProcessedFileAssetJpaEntity of(
            UUID id,
            String originalAssetId,
            String parentAssetId,
            ImageVariantType variantType,
            ImageFormatType formatType,
            String fileName,
            Long fileSize,
            String bucket,
            String s3Key,
            String userId,
            String organizationId,
            String tenantId,
            Instant createdAt) {
        return new ProcessedFileAssetJpaEntity(
                id,
                originalAssetId,
                parentAssetId,
                variantType,
                formatType,
                fileName,
                fileSize,
                bucket,
                s3Key,
                userId,
                organizationId,
                tenantId,
                createdAt);
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalAssetId() {
        return originalAssetId;
    }

    public String getParentAssetId() {
        return parentAssetId;
    }

    public ImageVariantType getVariantType() {
        return variantType;
    }

    public ImageFormatType getFormatType() {
        return formatType;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getBucket() {
        return bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public String getUserId() {
        return userId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

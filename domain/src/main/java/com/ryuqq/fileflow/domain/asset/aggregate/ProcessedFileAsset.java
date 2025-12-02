package com.ryuqq.fileflow.domain.asset.aggregate;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormatType;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariantType;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedFileAssetId;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.LocalDateTime;

public class ProcessedFileAsset {

    private final ProcessedFileAssetId id;
    private final FileAssetId originalAssetId;
    private final FileAssetId parentAssetId;

    private final ImageVariant variant;
    private final ImageFormat format;

    private final FileName fileName;
    private final FileSize fileSize;
    private final Integer width;
    private final Integer height;

    private final S3Bucket bucket;
    private final S3Key s3Key;

    private final Long userId;
    private final Long organizationId;
    private final Long tenantId;

    private final LocalDateTime createdAt;

    private ProcessedFileAsset(
            ProcessedFileAssetId id,
            FileAssetId originalAssetId,
            FileAssetId parentAssetId,
            ImageVariant variant,
            ImageFormat format,
            FileName fileName,
            FileSize fileSize,
            Integer width,
            Integer height,
            S3Bucket bucket,
            S3Key s3Key,
            Long userId,
            Long organizationId,
            Long tenantId,
            LocalDateTime createdAt) {
        this.id = id;
        this.originalAssetId = originalAssetId;
        this.parentAssetId = parentAssetId;
        this.variant = variant;
        this.format = format;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.userId = userId;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
    }

    public static ProcessedFileAsset forNew(
            FileAssetId originalAssetId,
            ImageVariant variant,
            ImageFormat format,
            FileName fileName,
            FileSize fileSize,
            Integer width,
            Integer height,
            S3Bucket bucket,
            S3Key s3Key,
            Long userId,
            Long organizationId,
            Long tenantId) {
        return new ProcessedFileAsset(
                ProcessedFileAssetId.forNew(),
                originalAssetId,
                null,
                variant,
                format,
                fileName,
                fileSize,
                width,
                height,
                bucket,
                s3Key,
                userId,
                organizationId,
                tenantId,
                LocalDateTime.now());
    }

    public static ProcessedFileAsset forHtmlExtractedImage(
            FileAssetId parentAssetId,
            FileAssetId originalAssetId,
            ImageVariant variant,
            ImageFormat format,
            FileName fileName,
            FileSize fileSize,
            Integer width,
            Integer height,
            S3Bucket bucket,
            S3Key s3Key,
            Long userId,
            Long organizationId,
            Long tenantId) {
        return new ProcessedFileAsset(
                ProcessedFileAssetId.forNew(),
                originalAssetId,
                parentAssetId,
                variant,
                format,
                fileName,
                fileSize,
                width,
                height,
                bucket,
                s3Key,
                userId,
                organizationId,
                tenantId,
                LocalDateTime.now());
    }

    public static ProcessedFileAsset reconstitute(
            ProcessedFileAssetId id,
            FileAssetId originalAssetId,
            FileAssetId parentAssetId,
            ImageVariant variant,
            ImageFormat format,
            FileName fileName,
            FileSize fileSize,
            Integer width,
            Integer height,
            S3Bucket bucket,
            S3Key s3Key,
            Long userId,
            Long organizationId,
            Long tenantId,
            LocalDateTime createdAt) {
        return new ProcessedFileAsset(
                id,
                originalAssetId,
                parentAssetId,
                variant,
                format,
                fileName,
                fileSize,
                width,
                height,
                bucket,
                s3Key,
                userId,
                organizationId,
                tenantId,
                createdAt);
    }

    public boolean hasParentAsset() {
        return parentAssetId != null;
    }

    public boolean isOriginalVariant() {
        return variant.type() == ImageVariantType.ORIGINAL;
    }

    public boolean isWebpFormat() {
        return format.type() == ImageFormatType.WEBP;
    }

    public ProcessedFileAssetId getId() {
        return id;
    }

    public FileAssetId getOriginalAssetId() {
        return originalAssetId;
    }

    public FileAssetId getParentAssetId() {
        return parentAssetId;
    }

    public ImageVariant getVariant() {
        return variant;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public FileName getFileName() {
        return fileName;
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public S3Bucket getBucket() {
        return bucket;
    }

    public S3Key getS3Key() {
        return s3Key;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

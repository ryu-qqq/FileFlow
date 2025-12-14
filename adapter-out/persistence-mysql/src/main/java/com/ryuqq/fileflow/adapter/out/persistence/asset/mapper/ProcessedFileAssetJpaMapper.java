package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.ProcessedFileAssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedFileAssetId;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * ProcessedFileAsset Domain ↔ JPA Entity 매퍼.
 */
@Component
public class ProcessedFileAssetJpaMapper {

    /**
     * Domain → JPA Entity 변환.
     *
     * @param domain ProcessedFileAsset 도메인 객체
     * @return JPA Entity
     */
    public ProcessedFileAssetJpaEntity toEntity(ProcessedFileAsset domain) {
        return ProcessedFileAssetJpaEntity.of(
                domain.getId().value(),
                domain.getOriginalAssetId().value().toString(),
                domain.getParentAssetId() != null
                        ? domain.getParentAssetId().value().toString()
                        : null,
                domain.getVariant().type(),
                domain.getFormat().type(),
                domain.getFileName().name(),
                domain.getFileSize().size(),
                domain.getBucket().bucketName(),
                domain.getS3Key().key(),
                domain.getUserId() != null ? domain.getUserId().value() : null,
                domain.getOrganizationId().value(),
                domain.getTenantId().value(),
                domain.getCreatedAt());
    }

    /**
     * JPA Entity → Domain 변환.
     *
     * @param entity ProcessedFileAssetJpaEntity
     * @return ProcessedFileAsset 도메인 객체
     */
    public ProcessedFileAsset toDomain(ProcessedFileAssetJpaEntity entity) {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(entity.getId()),
                new FileAssetId(UUID.fromString(entity.getOriginalAssetId())),
                entity.getParentAssetId() != null
                        ? new FileAssetId(UUID.fromString(entity.getParentAssetId()))
                        : null,
                resolveVariant(entity.getVariantType()),
                resolveFormat(entity.getFormatType()),
                new FileName(entity.getFileName()),
                new FileSize(entity.getFileSize()),
                new S3Bucket(entity.getBucket()),
                new S3Key(entity.getS3Key()),
                entity.getUserId() != null ? new UserId(entity.getUserId()) : null,
                new OrganizationId(entity.getOrganizationId()),
                new TenantId(entity.getTenantId()),
                entity.getCreatedAt());
    }

    private ImageVariant resolveVariant(
            com.ryuqq.fileflow.domain.asset.vo.ImageVariantType type) {
        return switch (type) {
            case ORIGINAL -> ImageVariant.ORIGINAL;
            case LARGE -> ImageVariant.LARGE;
            case MEDIUM -> ImageVariant.MEDIUM;
            case THUMBNAIL -> ImageVariant.THUMBNAIL;
        };
    }

    private ImageFormat resolveFormat(
            com.ryuqq.fileflow.domain.asset.vo.ImageFormatType type) {
        return switch (type) {
            case WEBP -> ImageFormat.WEBP;
            case JPEG -> ImageFormat.JPEG;
            case PNG -> ImageFormat.PNG;
        };
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * FileAsset Domain ↔ JPA Entity 변환 Mapper.
 *
 * <p>Domain Aggregate와 JPA Entity 간의 변환을 담당합니다.
 */
@Component
public class FileAssetJpaEntityMapper {

    private final ClockHolder clockHolder;

    public FileAssetJpaEntityMapper(ClockHolder clockHolder) {
        this.clockHolder = clockHolder;
    }

    /**
     * Domain → JPA Entity 변환.
     *
     * @param domain FileAsset Domain Aggregate
     * @return FileAssetJpaEntity
     */
    public FileAssetJpaEntity toEntity(FileAsset domain) {
        return FileAssetJpaEntity.of(
                domain.getIdValue(),
                domain.getSessionIdValue(),
                domain.getFileNameValue(),
                domain.getFileSizeValue(),
                domain.getContentTypeValue(),
                domain.getCategory(),
                domain.getBucketValue(),
                domain.getS3KeyValue(),
                domain.getEtagValue(),
                domain.getUserId(),
                domain.getOrganizationId(),
                domain.getTenantId(),
                domain.getStatus(),
                domain.getProcessedAt(),
                domain.getDeletedAt(),
                domain.getCreatedAt(),
                domain.getCreatedAt());
    }

    /**
     * JPA Entity → Domain 변환.
     *
     * @param entity FileAssetJpaEntity
     * @return FileAsset Domain Aggregate
     */
    public FileAsset toDomain(FileAssetJpaEntity entity) {
        return FileAsset.reconstitute(
                FileAssetId.of(entity.getId()),
                UploadSessionId.of(UUID.fromString(entity.getSessionId())),
                FileName.of(entity.getFileName()),
                FileSize.of(entity.getFileSize()),
                ContentType.of(entity.getContentType()),
                entity.getCategory(),
                S3Bucket.of(entity.getBucket()),
                S3Key.of(entity.getS3Key()),
                ETag.of(entity.getEtag()),
                entity.getUserId(),
                entity.getOrganizationId(),
                entity.getTenantId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getProcessedAt(),
                entity.getDeletedAt(),
                clockHolder.getClock());
    }
}

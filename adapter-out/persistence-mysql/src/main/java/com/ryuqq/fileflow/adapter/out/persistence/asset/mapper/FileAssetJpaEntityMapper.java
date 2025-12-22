package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.ImageDimension;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
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

    /**
     * Domain → JPA Entity 변환.
     *
     * @param domain FileAsset Domain Aggregate
     * @return FileAssetJpaEntity
     */
    public FileAssetJpaEntity toEntity(FileAsset domain) {
        Integer imageWidth = domain.getDimension() != null ? domain.getDimension().width() : null;
        Integer imageHeight = domain.getDimension() != null ? domain.getDimension().height() : null;

        return FileAssetJpaEntity.of(
                domain.getIdValue(),
                domain.getSessionIdValue(),
                domain.getFileNameValue(),
                domain.getFileSizeValue(),
                domain.getContentTypeValue(),
                domain.getCategory(),
                imageWidth,
                imageHeight,
                domain.getBucketValue(),
                domain.getS3KeyValue(),
                domain.getEtagValue(),
                toUserIdValue(domain.getUserId()),
                domain.getOrganizationId().value(),
                domain.getTenantId().value(),
                domain.getStatus(),
                domain.getProcessedAt(),
                domain.getDeletedAt(),
                domain.getLastErrorMessage(),
                domain.getCreatedAt(),
                domain.getCreatedAt());
    }

    private String toUserIdValue(UserId userId) {
        return userId != null ? userId.value() : null;
    }

    /**
     * JPA Entity → Domain 변환.
     *
     * @param entity FileAssetJpaEntity
     * @return FileAsset Domain Aggregate
     */
    public FileAsset toDomain(FileAssetJpaEntity entity) {
        ImageDimension dimension =
                toImageDimension(entity.getImageWidth(), entity.getImageHeight());

        return FileAsset.reconstitute(
                FileAssetId.of(entity.getId()),
                toSessionId(entity.getSessionId()),
                FileName.of(entity.getFileName()),
                FileSize.of(entity.getFileSize()),
                ContentType.of(entity.getContentType()),
                entity.getCategory(),
                dimension,
                S3Bucket.of(entity.getBucket()),
                S3Key.of(entity.getS3Key()),
                ETag.of(entity.getEtag()),
                toUserId(entity.getUserId()),
                OrganizationId.of(entity.getOrganizationId()),
                TenantId.of(entity.getTenantId()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getProcessedAt(),
                entity.getDeletedAt(),
                entity.getLastErrorMessage());
    }

    private UserId toUserId(String userId) {
        return userId != null ? UserId.of(userId) : null;
    }

    private UploadSessionId toSessionId(String sessionId) {
        return sessionId != null ? UploadSessionId.of(UUID.fromString(sessionId)) : null;
    }

    /**
     * width, height로부터 ImageDimension을 생성합니다.
     *
     * @param width 이미지 너비 (nullable)
     * @param height 이미지 높이 (nullable)
     * @return ImageDimension 또는 null
     */
    private ImageDimension toImageDimension(Integer width, Integer height) {
        if (width == null || height == null) {
            return null;
        }
        return ImageDimension.of(width, height);
    }
}

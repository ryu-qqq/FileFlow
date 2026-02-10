package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileInfo;
import com.ryuqq.fileflow.domain.common.vo.StorageInfo;
import org.springframework.stereotype.Component;

@Component
public class AssetJpaMapper {

    public AssetJpaEntity toEntity(Asset domain) {
        return AssetJpaEntity.create(
                domain.idValue(),
                domain.bucket(),
                domain.s3Key(),
                domain.accessType(),
                domain.fileName(),
                domain.fileSize(),
                domain.contentType(),
                domain.etag(),
                domain.extension(),
                domain.origin(),
                domain.originId(),
                domain.purpose(),
                domain.source(),
                domain.createdAt(),
                domain.updatedAt(),
                domain.deletedAt());
    }

    public Asset toDomain(AssetJpaEntity entity) {
        return Asset.reconstitute(
                AssetId.of(entity.getId()),
                StorageInfo.of(entity.getBucket(), entity.getS3Key(), entity.getAccessType()),
                FileInfo.of(
                        entity.getFileName(),
                        entity.getFileSize(),
                        entity.getContentType(),
                        entity.getEtag(),
                        entity.getExtension()),
                entity.getOrigin(),
                entity.getOriginId(),
                entity.getPurpose(),
                entity.getSource(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}

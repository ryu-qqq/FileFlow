package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetMetadataJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.asset.id.AssetMetadataId;
import org.springframework.stereotype.Component;

@Component
public class AssetMetadataJpaMapper {

    public AssetMetadataJpaEntity toEntity(AssetMetadata domain) {
        return AssetMetadataJpaEntity.create(
                domain.idValue(),
                domain.assetIdValue(),
                domain.width(),
                domain.height(),
                domain.transformType(),
                domain.createdAt(),
                domain.updatedAt());
    }

    public AssetMetadata toDomain(AssetMetadataJpaEntity entity) {
        return AssetMetadata.reconstitute(
                AssetMetadataId.of(entity.getId()),
                AssetId.of(entity.getAssetId()),
                entity.getWidth(),
                entity.getHeight(),
                entity.getTransformType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}

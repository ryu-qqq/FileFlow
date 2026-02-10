package com.ryuqq.fileflow.adapter.out.persistence.asset.condition;

import static com.ryuqq.fileflow.adapter.out.persistence.asset.entity.QAssetJpaEntity.assetJpaEntity;
import static com.ryuqq.fileflow.adapter.out.persistence.asset.entity.QAssetMetadataJpaEntity.assetMetadataJpaEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.stereotype.Component;

@Component
public class AssetConditionBuilder {

    public BooleanExpression assetIdEq(String id) {
        if (id == null) return null;
        return assetJpaEntity.id.eq(id);
    }

    public BooleanExpression notDeleted() {
        return assetJpaEntity.deletedAt.isNull();
    }

    public BooleanExpression metadataAssetIdEq(String assetId) {
        if (assetId == null) return null;
        return assetMetadataJpaEntity.assetId.eq(assetId);
    }
}

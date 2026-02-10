package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.asset.entity.QAssetMetadataJpaEntity.assetMetadataJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.asset.condition.AssetConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetMetadataJpaEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AssetMetadataQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final AssetConditionBuilder conditionBuilder;

    public AssetMetadataQueryDslRepository(
            JPAQueryFactory queryFactory, AssetConditionBuilder conditionBuilder) {
        this.queryFactory = queryFactory;
        this.conditionBuilder = conditionBuilder;
    }

    public Optional<AssetMetadataJpaEntity> findByAssetId(String assetId) {
        AssetMetadataJpaEntity result =
                queryFactory
                        .selectFrom(assetMetadataJpaEntity)
                        .where(conditionBuilder.metadataAssetIdEq(assetId))
                        .fetchOne();
        return Optional.ofNullable(result);
    }
}

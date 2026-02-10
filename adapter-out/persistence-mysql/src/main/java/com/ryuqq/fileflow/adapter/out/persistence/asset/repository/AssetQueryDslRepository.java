package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.asset.entity.QAssetJpaEntity.assetJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.asset.condition.AssetConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AssetQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final AssetConditionBuilder conditionBuilder;

    public AssetQueryDslRepository(
            JPAQueryFactory queryFactory, AssetConditionBuilder conditionBuilder) {
        this.queryFactory = queryFactory;
        this.conditionBuilder = conditionBuilder;
    }

    public Optional<AssetJpaEntity> findById(String id) {
        AssetJpaEntity result =
                queryFactory
                        .selectFrom(assetJpaEntity)
                        .where(conditionBuilder.assetIdEq(id), conditionBuilder.notDeleted())
                        .fetchOne();
        return Optional.ofNullable(result);
    }
}

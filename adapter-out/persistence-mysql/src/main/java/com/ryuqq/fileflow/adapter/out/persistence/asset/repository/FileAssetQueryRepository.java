package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.asset.entity.QFileAssetJpaEntity.fileAssetJpaEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * FileAsset QueryDSL Repository.
 *
 * <p>Entity를 반환하고 Adapter에서 Domain으로 변환합니다.
 */
@Repository
public class FileAssetQueryRepository {

    private final JPAQueryFactory queryFactory;

    public FileAssetQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<FileAssetJpaEntity> findById(String id, Long organizationId, Long tenantId) {
        FileAssetJpaEntity result =
                queryFactory
                        .selectFrom(fileAssetJpaEntity)
                        .where(
                                fileAssetJpaEntity.id.eq(id),
                                fileAssetJpaEntity.organizationId.eq(organizationId),
                                fileAssetJpaEntity.tenantId.eq(tenantId))
                        .fetchOne();

        return Optional.ofNullable(result);
    }

    public List<FileAssetJpaEntity> findAll(
            Long organizationId,
            Long tenantId,
            FileAssetStatus status,
            FileCategory category,
            long offset,
            int limit) {
        return queryFactory
                .selectFrom(fileAssetJpaEntity)
                .where(
                        organizationIdEq(organizationId),
                        tenantIdEq(tenantId),
                        statusEq(status),
                        categoryEq(category))
                .orderBy(fileAssetJpaEntity.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public long count(
            Long organizationId, Long tenantId, FileAssetStatus status, FileCategory category) {
        Long count =
                queryFactory
                        .select(fileAssetJpaEntity.count())
                        .from(fileAssetJpaEntity)
                        .where(
                                organizationIdEq(organizationId),
                                tenantIdEq(tenantId),
                                statusEq(status),
                                categoryEq(category))
                        .fetchOne();

        return count != null ? count : 0L;
    }

    private BooleanExpression organizationIdEq(Long organizationId) {
        return organizationId != null ? fileAssetJpaEntity.organizationId.eq(organizationId) : null;
    }

    private BooleanExpression tenantIdEq(Long tenantId) {
        return tenantId != null ? fileAssetJpaEntity.tenantId.eq(tenantId) : null;
    }

    private BooleanExpression statusEq(FileAssetStatus status) {
        return status != null ? fileAssetJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression categoryEq(FileCategory category) {
        return category != null ? fileAssetJpaEntity.category.eq(category) : null;
    }
}

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
public class FileAssetQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public FileAssetQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<FileAssetJpaEntity> findById(
            String id, String organizationId, String tenantId) {
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

    /**
     * ID만으로 FileAsset 조회 (DLQ 처리용).
     *
     * <p>DLQ 처리 시 organizationId/tenantId 없이 조회가 필요한 경우 사용합니다.
     *
     * @param id FileAsset ID (UUID 문자열)
     * @return FileAssetJpaEntity Optional
     */
    public Optional<FileAssetJpaEntity> findById(String id) {
        FileAssetJpaEntity result =
                queryFactory
                        .selectFrom(fileAssetJpaEntity)
                        .where(fileAssetJpaEntity.id.eq(id))
                        .fetchOne();

        return Optional.ofNullable(result);
    }

    public List<FileAssetJpaEntity> findByCriteria(
            String organizationId,
            String tenantId,
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

    public long countByCriteria(
            String organizationId, String tenantId, FileAssetStatus status, FileCategory category) {
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

    private BooleanExpression organizationIdEq(String organizationId) {
        return organizationId != null ? fileAssetJpaEntity.organizationId.eq(organizationId) : null;
    }

    private BooleanExpression tenantIdEq(String tenantId) {
        return tenantId != null ? fileAssetJpaEntity.tenantId.eq(tenantId) : null;
    }

    private BooleanExpression statusEq(FileAssetStatus status) {
        return status != null ? fileAssetJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression categoryEq(FileCategory category) {
        return category != null ? fileAssetJpaEntity.category.eq(category) : null;
    }
}

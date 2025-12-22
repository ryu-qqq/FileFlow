package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.asset.entity.QFileAssetJpaEntity.fileAssetJpaEntity;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            String fileName,
            Instant createdAtFrom,
            Instant createdAtTo,
            String sortBy,
            boolean ascending,
            long offset,
            int limit) {
        return queryFactory
                .selectFrom(fileAssetJpaEntity)
                .where(
                        organizationIdEq(organizationId),
                        tenantIdEq(tenantId),
                        statusEq(status),
                        categoryEq(category),
                        fileNameContains(fileName),
                        createdAtGoe(createdAtFrom),
                        createdAtLoe(createdAtTo))
                .orderBy(getOrderSpecifier(sortBy, ascending))
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    private OrderSpecifier<?> getOrderSpecifier(String sortBy, boolean ascending) {
        return switch (sortBy != null ? sortBy : "CREATED_AT") {
            case "FILE_NAME" ->
                    ascending
                            ? fileAssetJpaEntity.fileName.asc()
                            : fileAssetJpaEntity.fileName.desc();
            case "FILE_SIZE" ->
                    ascending
                            ? fileAssetJpaEntity.fileSize.asc()
                            : fileAssetJpaEntity.fileSize.desc();
            case "PROCESSED_AT" ->
                    ascending
                            ? fileAssetJpaEntity.processedAt.asc()
                            : fileAssetJpaEntity.processedAt.desc();
            default ->
                    ascending
                            ? fileAssetJpaEntity.createdAt.asc()
                            : fileAssetJpaEntity.createdAt.desc();
        };
    }

    public long countByCriteria(
            String organizationId,
            String tenantId,
            FileAssetStatus status,
            FileCategory category,
            String fileName,
            Instant createdAtFrom,
            Instant createdAtTo) {
        Long count =
                queryFactory
                        .select(fileAssetJpaEntity.count())
                        .from(fileAssetJpaEntity)
                        .where(
                                organizationIdEq(organizationId),
                                tenantIdEq(tenantId),
                                statusEq(status),
                                categoryEq(category),
                                fileNameContains(fileName),
                                createdAtGoe(createdAtFrom),
                                createdAtLoe(createdAtTo))
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

    private BooleanExpression fileNameContains(String fileName) {
        return fileName != null && !fileName.isBlank()
                ? fileAssetJpaEntity.fileName.containsIgnoreCase(fileName)
                : null;
    }

    private BooleanExpression createdAtGoe(Instant createdAtFrom) {
        return createdAtFrom != null ? fileAssetJpaEntity.createdAt.goe(createdAtFrom) : null;
    }

    private BooleanExpression createdAtLoe(Instant createdAtTo) {
        return createdAtTo != null ? fileAssetJpaEntity.createdAt.loe(createdAtTo) : null;
    }

    /**
     * 상태별 FileAsset 개수 조회.
     *
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @return 상태별 개수 맵
     */
    public Map<String, Long> countByStatus(String organizationId, String tenantId) {
        List<Tuple> results =
                queryFactory
                        .select(fileAssetJpaEntity.status, fileAssetJpaEntity.count())
                        .from(fileAssetJpaEntity)
                        .where(organizationIdEq(organizationId), tenantIdEq(tenantId))
                        .groupBy(fileAssetJpaEntity.status)
                        .fetch();

        Map<String, Long> statusCounts = new HashMap<>();
        for (Tuple tuple : results) {
            FileAssetStatus status = tuple.get(fileAssetJpaEntity.status);
            Long count = tuple.get(fileAssetJpaEntity.count());
            if (status != null && count != null) {
                statusCounts.put(status.name(), count);
            }
        }
        return statusCounts;
    }

    /**
     * 카테고리별 FileAsset 개수 조회.
     *
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @return 카테고리별 개수 맵
     */
    public Map<String, Long> countByCategory(String organizationId, String tenantId) {
        List<Tuple> results =
                queryFactory
                        .select(fileAssetJpaEntity.category, fileAssetJpaEntity.count())
                        .from(fileAssetJpaEntity)
                        .where(organizationIdEq(organizationId), tenantIdEq(tenantId))
                        .groupBy(fileAssetJpaEntity.category)
                        .fetch();

        Map<String, Long> categoryCounts = new HashMap<>();
        for (Tuple tuple : results) {
            FileCategory category = tuple.get(fileAssetJpaEntity.category);
            Long count = tuple.get(fileAssetJpaEntity.count());
            if (category != null && count != null) {
                categoryCounts.put(category.name(), count);
            }
        }
        return categoryCounts;
    }

    /**
     * 전체 FileAsset 개수 조회.
     *
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @return 전체 개수
     */
    public long countTotal(String organizationId, String tenantId) {
        Long count =
                queryFactory
                        .select(fileAssetJpaEntity.count())
                        .from(fileAssetJpaEntity)
                        .where(organizationIdEq(organizationId), tenantIdEq(tenantId))
                        .fetchOne();
        return count != null ? count : 0L;
    }
}

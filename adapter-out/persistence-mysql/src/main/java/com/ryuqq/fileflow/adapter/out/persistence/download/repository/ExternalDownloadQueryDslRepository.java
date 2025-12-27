package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.entity.QExternalDownloadJpaEntity;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * ExternalDownload QueryDSL Repository.
 *
 * <p>ExternalDownload 조회를 담당하는 QueryDSL Repository입니다.
 */
@Repository
public class ExternalDownloadQueryDslRepository {

    private static final QExternalDownloadJpaEntity download =
            QExternalDownloadJpaEntity.externalDownloadJpaEntity;

    private final JPAQueryFactory queryFactory;

    public ExternalDownloadQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 ExternalDownload를 조회한다.
     *
     * @param id ExternalDownload ID (UUID)
     * @return ExternalDownloadJpaEntity Optional
     */
    public Optional<ExternalDownloadJpaEntity> findById(UUID id) {
        ExternalDownloadJpaEntity result =
                queryFactory.selectFrom(download).where(download.id.eq(id)).fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * ID와 테넌트 ID로 ExternalDownload를 조회한다.
     *
     * @param id ExternalDownload ID (UUID)
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return ExternalDownloadJpaEntity Optional
     */
    public Optional<ExternalDownloadJpaEntity> findByIdAndTenantId(UUID id, String tenantId) {
        ExternalDownloadJpaEntity result =
                queryFactory
                        .selectFrom(download)
                        .where(download.id.eq(id), download.tenantId.eq(tenantId))
                        .fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * ID로 존재 여부를 확인한다.
     *
     * @param id ExternalDownload ID (UUID)
     * @return 존재 여부
     */
    public boolean existsById(UUID id) {
        Integer result =
                queryFactory.selectOne().from(download).where(download.id.eq(id)).fetchFirst();
        return result != null;
    }

    /**
     * 테넌트 ID와 멱등성 키로 ExternalDownload를 조회한다.
     *
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @param idempotencyKey 멱등성 키 (UUID 문자열)
     * @return ExternalDownloadJpaEntity Optional
     */
    public Optional<ExternalDownloadJpaEntity> findByTenantIdAndIdempotencyKey(
            String tenantId, String idempotencyKey) {
        ExternalDownloadJpaEntity result =
                queryFactory
                        .selectFrom(download)
                        .where(
                                download.tenantId.eq(tenantId),
                                download.idempotencyKey.eq(idempotencyKey))
                        .fetchOne();
        return Optional.ofNullable(result);
    }

    /**
     * 조건에 맞는 ExternalDownload 목록을 조회한다.
     *
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @param status 상태 필터 (nullable)
     * @param offset 오프셋
     * @param limit 조회 개수
     * @return ExternalDownloadJpaEntity 목록
     */
    public List<ExternalDownloadJpaEntity> findByCriteria(
            String organizationId,
            String tenantId,
            ExternalDownloadStatus status,
            long offset,
            int limit) {
        BooleanBuilder whereClause = buildWhereClause(organizationId, tenantId, status);

        return queryFactory
                .selectFrom(download)
                .where(whereClause)
                .orderBy(download.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    /**
     * 조건에 맞는 ExternalDownload 개수를 조회한다.
     *
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @param status 상태 필터 (nullable)
     * @return 총 개수
     */
    public long countByCriteria(
            String organizationId, String tenantId, ExternalDownloadStatus status) {
        BooleanBuilder whereClause = buildWhereClause(organizationId, tenantId, status);

        Long count =
                queryFactory.select(download.count()).from(download).where(whereClause).fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanBuilder buildWhereClause(
            String organizationId, String tenantId, ExternalDownloadStatus status) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(download.organizationId.eq(organizationId));
        builder.and(download.tenantId.eq(tenantId));

        if (status != null) {
            builder.and(download.status.eq(status));
        }

        return builder;
    }
}

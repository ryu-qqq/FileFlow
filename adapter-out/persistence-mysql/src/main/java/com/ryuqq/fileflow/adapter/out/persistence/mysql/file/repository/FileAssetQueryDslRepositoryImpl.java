package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository;

import jakarta.persistence.EntityManager;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.querydsl.FileAssetQueryDslRepository;
import com.ryuqq.fileflow.application.file.dto.query.ListFilesQuery;
import com.ryuqq.fileflow.domain.file.asset.FileStatus;
import com.ryuqq.fileflow.domain.file.asset.Visibility;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.QFileAssetJpaEntity.fileAssetJpaEntity;

/**
 * FileAsset QueryDSL Custom Repository 구현체
 *
 * <p>QueryDSL을 사용한 동적 쿼리 구현</p>
 *
 * <p><strong>핵심 구현</strong>:</p>
 * <ul>
 *   <li>JPAQueryFactory: QueryDSL 쿼리 실행</li>
 *   <li>BooleanBuilder: 동적 필터 조건 조립</li>
 *   <li>QFileAssetJpaEntity: QueryDSL 메타모델</li>
 * </ul>
 *
 * <p><strong>성능 고려사항</strong>:</p>
 * <ul>
 *   <li>인덱스 활용: idx_tenant_org_uploaded, idx_owner, idx_status</li>
 *   <li>Soft Delete 필터: deletedAt IS NULL (모든 쿼리)</li>
 *   <li>DB 레벨 페이징: offset/limit 사용</li>
 *   <li>COUNT 쿼리: 동일한 BooleanBuilder 재사용</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Repository
public class FileAssetQueryDslRepositoryImpl implements FileAssetQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 생성자
     *
     * <p>EntityManager를 주입받아 JPAQueryFactory 생성</p>
     *
     * @param entityManager JPA EntityManager
     */
    public FileAssetQueryDslRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    /**
     * 동적 쿼리를 사용한 파일 목록 조회
     *
     * <p><strong>쿼리 구성</strong>:</p>
     * <pre>{@code
     * SELECT f
     * FROM FileAssetJpaEntity f
     * WHERE f.deletedAt IS NULL              -- Soft Delete 필터 (필수)
     *   AND f.tenantId = :tenantId           -- Tenant 필터 (필수)
     *   AND f.organizationId = :orgId        -- Organization 필터 (선택)
     *   AND f.ownerUserId = :ownerId         -- 소유자 필터 (선택)
     *   AND f.status = :status               -- 상태 필터 (선택)
     *   AND f.visibility = :visibility       -- 가시성 필터 (선택)
     *   AND f.uploadedAt >= :uploadedAfter   -- 기간 필터 (선택)
     *   AND f.uploadedAt < :uploadedBefore   -- 기간 필터 (선택)
     * ORDER BY f.uploadedAt DESC
     * LIMIT :limit OFFSET :offset
     * }</pre>
     *
     * @param query 파일 목록 조회 Query
     * @return FileAssetJpaEntity 목록
     */
    @Override
    public List<FileAssetJpaEntity> searchWithFilters(ListFilesQuery query) {
        BooleanBuilder builder = buildWhereClause(query);

        int offset = query.page() * query.size();
        int limit = query.size();

        return queryFactory
            .selectFrom(fileAssetJpaEntity)
            .where(builder)
            .orderBy(fileAssetJpaEntity.uploadedAt.desc())
            .offset(offset)
            .limit(limit)
            .fetch();
    }

    /**
     * 동적 쿼리를 사용한 파일 개수 조회
     *
     * <p>searchWithFilters()와 동일한 WHERE 조건 사용</p>
     *
     * @param query 파일 목록 조회 Query
     * @return 전체 개수
     */
    @Override
    public long countWithFilters(ListFilesQuery query) {
        BooleanBuilder builder = buildWhereClause(query);

        Long count = queryFactory
            .select(fileAssetJpaEntity.count())
            .from(fileAssetJpaEntity)
            .where(builder)
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * WHERE 조건 동적 생성
     *
     * <p><strong>필수 조건</strong>:</p>
     * <ul>
     *   <li>deletedAt IS NULL (Soft Delete 필터)</li>
     *   <li>tenantId = :tenantId (Tenant 스코프)</li>
     * </ul>
     *
     * <p><strong>선택 조건</strong>:</p>
     * <ul>
     *   <li>organizationId = :organizationId (Organization 스코프)</li>
     *   <li>ownerUserId = :ownerUserId (소유자 필터)</li>
     *   <li>status = :status (상태 필터)</li>
     *   <li>visibility = :visibility (가시성 필터)</li>
     *   <li>uploadedAt >= :uploadedAfter (기간 필터 시작)</li>
     *   <li>uploadedAt < :uploadedBefore (기간 필터 종료)</li>
     * </ul>
     *
     * @param query 파일 목록 조회 Query
     * @return BooleanBuilder (WHERE 조건)
     */
    private BooleanBuilder buildWhereClause(ListFilesQuery query) {
        BooleanBuilder builder = new BooleanBuilder();

        // 필수 조건: Soft Delete 필터
        builder.and(fileAssetJpaEntity.deletedAt.isNull());

        // 필수 조건: Tenant 스코프
        Long tenantId = query.tenantId().value();
        builder.and(fileAssetJpaEntity.tenantId.eq(tenantId));

        // 선택 조건: Organization 스코프
        Long organizationId = query.organizationId();
        if (organizationId != null) {
            builder.and(fileAssetJpaEntity.organizationId.eq(organizationId));
        }

        // 선택 조건: 소유자 필터
        Long ownerUserId = query.ownerUserId();
        if (ownerUserId != null) {
            builder.and(fileAssetJpaEntity.ownerUserId.eq(ownerUserId));
        }

        // 선택 조건: 상태 필터
        FileStatus status = query.status();
        if (status != null) {
            builder.and(fileAssetJpaEntity.status.eq(status));
        }

        // 선택 조건: 가시성 필터
        Visibility visibility = query.visibility();
        if (visibility != null) {
            builder.and(fileAssetJpaEntity.visibility.eq(visibility));
        }

        // 선택 조건: 기간 필터 (시작)
        LocalDateTime uploadedAfter = query.uploadedAfter();
        if (uploadedAfter != null) {
            builder.and(fileAssetJpaEntity.uploadedAt.goe(uploadedAfter));
        }

        // 선택 조건: 기간 필터 (종료)
        LocalDateTime uploadedBefore = query.uploadedBefore();
        if (uploadedBefore != null) {
            builder.and(fileAssetJpaEntity.uploadedAt.lt(uploadedBefore));
        }

        return builder;
    }
}

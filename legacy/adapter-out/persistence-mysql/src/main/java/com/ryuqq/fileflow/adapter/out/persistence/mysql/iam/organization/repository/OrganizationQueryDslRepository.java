package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.entity.OrganizationJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.organization.entity.QOrganizationJpaEntity.organizationJpaEntity;

/**
 * OrganizationQueryDslRepository - Organization QueryDSL 전용 Repository
 *
 * <p>QueryDSL을 사용한 동적 쿼리 구현</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ 구현체만 존재 (인터페이스 추상화 불필요)</li>
 *   <li>✅ JPAQueryFactory를 통한 QueryDSL 쿼리 실행</li>
 *   <li>✅ BooleanExpression Helper 메서드로 동적 쿼리 조건 관리</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ CQRS Query Side 전용 (조회만)</li>
 * </ul>
 *
 * <p><strong>성능 고려사항</strong>:</p>
 * <ul>
 *   <li>인덱스 활용: idx_tenant_org, idx_created_at</li>
 *   <li>Soft Delete 필터: deleted = false (모든 쿼리)</li>
 *   <li>DB 레벨 페이징: offset/limit 사용</li>
 *   <li>COUNT 쿼리: 동일한 BooleanExpression 재사용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Component
public class OrganizationQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 생성자
     *
     * <p>EntityManager를 주입받아 JPAQueryFactory 생성</p>
     *
     * @param queryFactory JPA EntityManager
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public OrganizationQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Organization ID로 단건 조회
     *
     * @param organizationId Organization ID
     * @return Optional<OrganizationJpaEntity>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public Optional<OrganizationJpaEntity> findById(Long organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("OrganizationId는 필수입니다");
        }

        OrganizationJpaEntity entity = queryFactory
            .selectFrom(organizationJpaEntity)
            .where(
                organizationJpaEntity.id.eq(organizationId),
                organizationJpaEntity.deleted.isFalse()
            )
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * Organization 목록 조회 (Offset-based Pagination)
     *
     * @param tenantId Tenant ID 필터 (Long - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param offset 시작 위치 (0부터 시작)
     * @param limit 조회 개수
     * @return OrganizationJpaEntity 목록
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public List<OrganizationJpaEntity> findAllWithOffset(
        Long tenantId,
        String orgCodeContains,
        String nameContains,
        Boolean deleted,
        int offset,
        int limit
    ) {
        return queryFactory
            .selectFrom(organizationJpaEntity)
            .where(
                eqTenantId(tenantId),
                containsOrgCode(orgCodeContains),
                containsName(nameContains),
                eqDeleted(deleted)
            )
            .orderBy(organizationJpaEntity.createdAt.asc())
            .offset(offset)
            .limit(limit)
            .fetch();
    }

    /**
     * Organization 목록 총 개수 조회
     *
     * @param tenantId Tenant ID 필터 (Long - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @return 전체 개수
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public long countAll(
        Long tenantId,
        String orgCodeContains,
        String nameContains,
        Boolean deleted
    ) {
        Long count = queryFactory
            .select(organizationJpaEntity.count())
            .from(organizationJpaEntity)
            .where(
                eqTenantId(tenantId),
                containsOrgCode(orgCodeContains),
                containsName(nameContains),
                eqDeleted(deleted)
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * Organization 목록 조회 (Cursor-based Pagination)
     *
     * @param tenantId Tenant ID 필터 (Long - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param cursor 커서 값 (null이면 처음부터 조회)
     * @param limit 조회 개수
     * @return OrganizationJpaEntity 목록 (limit + 1개까지 조회)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public List<OrganizationJpaEntity> findAllWithCursor(
        Long tenantId,
        String orgCodeContains,
        String nameContains,
        Boolean deleted,
        String cursor,
        int limit
    ) {
        return queryFactory
            .selectFrom(organizationJpaEntity)
            .where(
                eqTenantId(tenantId),
                containsOrgCode(orgCodeContains),
                containsName(nameContains),
                eqDeleted(deleted),
                gtCursor(cursor)
            )
            .orderBy(
                organizationJpaEntity.createdAt.asc(),
                organizationJpaEntity.id.asc()
            )
            .limit(limit)
            .fetch();
    }

    // ========================================
    // Private Helper Methods (동적 쿼리 조건)
    // ========================================

    /**
     * Tenant ID 일치 조건
     *
     * @param tenantId Tenant ID (Long - Tenant PK 타입과 일치, null이면 조건 제외)
     * @return BooleanExpression
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private BooleanExpression eqTenantId(Long tenantId) {
        if (tenantId == null || tenantId <= 0) {
            return null;
        }
        return organizationJpaEntity.tenantId.eq(tenantId);
    }

    /**
     * 조직 코드 부분 일치 조건
     *
     * @param orgCodeContains 검색어 (null이면 조건 제외)
     * @return BooleanExpression
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private BooleanExpression containsOrgCode(String orgCodeContains) {
        if (orgCodeContains == null || orgCodeContains.isBlank()) {
            return null;
        }
        return organizationJpaEntity.orgCode.containsIgnoreCase(orgCodeContains);
    }

    /**
     * 이름 부분 일치 조건
     *
     * @param nameContains 검색어 (null이면 조건 제외)
     * @return BooleanExpression
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private BooleanExpression containsName(String nameContains) {
        if (nameContains == null || nameContains.isBlank()) {
            return null;
        }
        return organizationJpaEntity.name.containsIgnoreCase(nameContains);
    }

    /**
     * 삭제 여부 일치 조건
     *
     * @param deleted 삭제 여부 (null이면 조건 제외)
     * @return BooleanExpression
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private BooleanExpression eqDeleted(Boolean deleted) {
        if (deleted == null) {
            return null;
        }
        return organizationJpaEntity.deleted.eq(deleted);
    }

    /**
     * Cursor보다 큰 (createdAt, id) 복합 조건 (Cursor-based Pagination)
     *
     * <p>일관된 페이지네이션을 위해 createdAt + id 복합 정렬을 사용합니다.
     * Cursor 형식: Base64("createdAt|id")</p>
     *
     * @param cursor Base64 인코딩된 "createdAt|id" (null이면 조건 제외)
     * @return BooleanExpression
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private BooleanExpression gtCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String decodedCursor = new String(Base64.getUrlDecoder().decode(cursor));
            String[] parts = decodedCursor.split("\\|");

            if (parts.length != 2) {
                return null;
            }

            String createdAtStr = parts[0];
            String idStr = parts[1];
            Long id = Long.parseLong(idStr);

            return organizationJpaEntity.createdAt.gt(java.time.LocalDateTime.parse(createdAtStr))
                .or(
                    organizationJpaEntity.createdAt.eq(java.time.LocalDateTime.parse(createdAtStr))
                        .and(organizationJpaEntity.id.gt(id))
                );
        } catch (Exception e) {
            return null;
        }
    }
}

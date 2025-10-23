package com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.adapter;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.entity.OrganizationJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.mapper.OrganizationEntityMapper;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.entity.QOrganizationJpaEntity.organizationJpaEntity;

/**
 * OrganizationQueryRepositoryAdapter - Organization Query 전용 Persistence Adapter
 *
 * <p>CQRS 패턴의 Query 전용 Adapter입니다.
 * QueryDSL을 사용하여 성능 최적화된 조회 쿼리를 실행합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Organization 조회 작업만 담당 (CUD 작업 없음)</li>
 *   <li>QueryDSL을 통한 동적 쿼리 생성</li>
 *   <li>Pagination 지원 (Offset-based, Cursor-based)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ {@code @Component} 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code OrganizationQueryRepositoryPort} 구현</li>
 *   <li>✅ QueryDSL JPAQueryFactory 사용</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Component
public class OrganizationQueryRepositoryAdapter implements OrganizationQueryRepositoryPort {

    private final JPAQueryFactory queryFactory;

    /**
     * Constructor - 의존성 주입
     *
     * @param queryFactory QueryDSL JPAQueryFactory
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public OrganizationQueryRepositoryAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Organization ID로 단건 조회
     *
     * @param organizationId Organization ID
     * @return Optional<Organization>
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public Optional<Organization> findById(OrganizationId organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("OrganizationId는 필수입니다");
        }

        OrganizationJpaEntity entity = queryFactory
            .selectFrom(organizationJpaEntity)
            .where(
                organizationJpaEntity.id.eq(organizationId.value()),
                organizationJpaEntity.deleted.isFalse()
            )
            .fetchOne();

        return Optional.ofNullable(entity)
            .map(OrganizationEntityMapper::toDomain);
    }

    /**
     * Organization 목록 조회 (Offset-based Pagination)
     *
     * @param tenantId Tenant ID 필터 (String - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param offset 시작 위치 (0부터 시작)
     * @param limit 조회 개수
     * @return Organization 목록
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public List<Organization> findAllWithOffset(
        String tenantId,
        String orgCodeContains,
        String nameContains,
        Boolean deleted,
        int offset,
        int limit
    ) {
        List<OrganizationJpaEntity> entities = queryFactory
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

        return entities.stream()
            .map(OrganizationEntityMapper::toDomain)
            .toList();
    }

    /**
     * Organization 목록 총 개수 조회
     *
     * @param tenantId Tenant ID 필터 (String - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @return 전체 개수
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public long countAll(
        String tenantId,
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
     * <p>Cursor는 Base64로 인코딩된 Organization ID입니다.</p>
     *
     * @param tenantId Tenant ID 필터 (String - Tenant PK 타입과 일치, null 허용)
     * @param orgCodeContains 조직 코드 검색어 (부분 일치, null 허용)
     * @param nameContains 이름 검색어 (부분 일치, null 허용)
     * @param deleted 삭제 여부 필터 (null이면 전체 조회)
     * @param cursor 커서 값 (null이면 처음부터 조회)
     * @param limit 조회 개수
     * @return Organization 목록 (limit + 1개까지 조회)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Override
    public List<Organization> findAllWithCursor(
        String tenantId,
        String orgCodeContains,
        String nameContains,
        Boolean deleted,
        String cursor,
        int limit
    ) {
        List<OrganizationJpaEntity> entities = queryFactory
            .selectFrom(organizationJpaEntity)
            .where(
                eqTenantId(tenantId),
                containsOrgCode(orgCodeContains),
                containsName(nameContains),
                eqDeleted(deleted),
                gtCursor(cursor)
            )
            .orderBy(organizationJpaEntity.id.asc())
            .limit(limit)
            .fetch();

        return entities.stream()
            .map(OrganizationEntityMapper::toDomain)
            .toList();
    }

    // ========================================
    // Private Helper Methods (동적 쿼리 조건)
    // ========================================

    /**
     * Tenant ID 일치 조건
     *
     * @param tenantId Tenant ID (String - Tenant PK 타입과 일치, null이면 조건 제외)
     * @return BooleanExpression
     * @author ryu-qqq
     * @since 2025-10-23
     */
    private BooleanExpression eqTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
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
     * Cursor보다 큰 ID 조건 (Cursor-based Pagination)
     *
     * @param cursor Base64 인코딩된 Organization ID (null이면 조건 제외)
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
            Long organizationId = Long.parseLong(decodedCursor);
            return organizationJpaEntity.id.gt(organizationId);
        } catch (IllegalArgumentException e) {
            // 잘못된 cursor는 무시하고 처음부터 조회
            return null;
        }
    }
}

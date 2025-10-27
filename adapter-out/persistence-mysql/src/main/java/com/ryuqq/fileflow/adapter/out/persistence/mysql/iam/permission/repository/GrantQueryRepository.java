package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.dto.GrantReadModel;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.QPermissionJpaEntity.permissionJpaEntity;
import static com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.QRoleJpaEntity.roleJpaEntity;
import static com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.QRolePermissionJpaEntity.rolePermissionJpaEntity;
import static com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.QUserRoleMappingJpaEntity.userRoleMappingJpaEntity;

/**
 * Grant Query Repository - QueryDSL 기반 4-table JOIN 최적화 Repository
 *
 * <p><strong>역할</strong>: {@code buildEffectiveGrants()} 메서드를 위한 QueryDSL 쿼리 실행</p>
 *
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ CQRS Query 전용 Repository</li>
 *   <li>✅ QueryDSL JPAQueryFactory 사용</li>
 *   <li>✅ 4-table JOIN을 단일 쿼리로 최적화 (N+1 문제 해결)</li>
 *   <li>✅ DTO Projection으로 메모리 효율성 확보</li>
 *   <li>✅ {@code @Repository} 사용 (Spring Data JPA Repository 아님)</li>
 * </ul>
 *
 * <h3>성능 최적화</h3>
 * <p>기존 방식 (N+1 문제):</p>
 * <ul>
 *   <li>1. UserRoleMapping 조회 (1 query)</li>
 *   <li>2. Role 조회 (N queries - Role별 1개씩)</li>
 *   <li>3. RolePermission 조회 (N queries - Role별 1개씩)</li>
 *   <li>4. Permission 조회 (M queries - Permission별 1개씩)</li>
 *   <li>→ 총 1 + N + N + M = <strong>2N + M + 1 queries</strong></li>
 * </ul>
 *
 * <p>QueryDSL 최적화 (단일 쿼리):</p>
 * <ul>
 *   <li>1. 4-table INNER JOIN → <strong>1 query</strong></li>
 *   <li>2. DTO Projection으로 필요한 컬럼만 조회</li>
 *   <li>→ 성능 향상: <strong>O(2N + M) → O(1)</strong></li>
 * </ul>
 *
 * <h3>SQL 쿼리 예시</h3>
 * <pre>{@code
 * SELECT
 *   urm.role_code,
 *   p.code,
 *   p.default_scope
 * FROM user_role_mapping urm
 * INNER JOIN role r ON urm.role_code = r.code AND r.deleted = false
 * INNER JOIN role_permission rp ON r.code = rp.role_code
 * INNER JOIN permission p ON rp.permission_code = p.code AND p.deleted = false
 * WHERE urm.user_context_id = ?
 *   AND urm.tenant_id = ?
 *   AND urm.organization_id = ?
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Repository
public class GrantQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * Constructor - 의존성 주입
     *
     * @param queryFactory QueryDSL JPAQueryFactory
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public GrantQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Effective Grants 조회 (4-table JOIN 최적화)
     *
     * <p>UserContext + Tenant + Organization 컨텍스트에서 사용자가 가진 모든 Grant를 조회합니다.</p>
     *
     * <p><strong>성능 특징</strong>:</p>
     * <ul>
     *   <li>✅ 단일 쿼리로 모든 Grant 조회 (N+1 문제 해결)</li>
     *   <li>✅ DTO Projection으로 필요한 컬럼만 조회</li>
     *   <li>✅ INNER JOIN으로 삭제된 Role/Permission 자동 필터링</li>
     * </ul>
     *
     * @param userContextId UserContext ID
     * @param tenantId Tenant ID (Long - AUTO_INCREMENT)
     * @param organizationId Organization ID
     * @return Grant Read Model 목록
     * @throws IllegalArgumentException 파라미터가 null 또는 유효하지 않은 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public List<GrantReadModel> findEffectiveGrants(
        Long userContextId,
        Long tenantId,  // Long AUTO_INCREMENT
        Long organizationId
    ) {
        if (userContextId == null) {
            throw new IllegalArgumentException("UserContextId must not be null");
        }
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("TenantId must not be null and must be positive");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("OrganizationId must not be null");
        }

        return queryFactory
            .select(
                Projections.constructor(
                    GrantReadModel.class,
                    userRoleMappingJpaEntity.roleCode,
                    permissionJpaEntity.code,
                    permissionJpaEntity.defaultScope
                )
            )
            .from(userRoleMappingJpaEntity)
            .innerJoin(roleJpaEntity)
                .on(userRoleMappingJpaEntity.roleCode.eq(roleJpaEntity.code)
                    .and(roleJpaEntity.deleted.isFalse()))
            .innerJoin(rolePermissionJpaEntity)
                .on(roleJpaEntity.code.eq(rolePermissionJpaEntity.roleCode))
            .innerJoin(permissionJpaEntity)
                .on(rolePermissionJpaEntity.permissionCode.eq(permissionJpaEntity.code)
                    .and(permissionJpaEntity.deleted.isFalse()))
            .where(
                userRoleMappingJpaEntity.userContextId.eq(userContextId),
                userRoleMappingJpaEntity.tenantId.eq(tenantId),
                userRoleMappingJpaEntity.organizationId.eq(organizationId)
            )
            .fetch();
    }
}

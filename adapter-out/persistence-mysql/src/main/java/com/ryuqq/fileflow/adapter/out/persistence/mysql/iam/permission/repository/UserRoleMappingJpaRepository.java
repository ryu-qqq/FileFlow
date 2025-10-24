package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.UserRoleMappingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * UserRoleMapping JPA Repository
 *
 * <p><strong>역할</strong>: User-Role Mapping 관계에 대한 Spring Data JPA Repository</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 표준 인터페이스</li>
 *   <li>✅ Long FK 전략 (관계 어노테이션 금지)</li>
 *   <li>✅ Query Method 네이밍 규칙 준수</li>
 * </ul>
 *
 * <h3>Query Methods</h3>
 * <p>UserContext + Tenant + Organization 컨텍스트에서 Role 조회:</p>
 * <ul>
 *   <li>{@code findAllByUserContextIdAndTenantIdAndOrganizationId}: 특정 컨텍스트의 모든 Role 조회</li>
 * </ul>
 *
 * <h3>buildEffectiveGrants() 쿼리 지원</h3>
 * <p>이 Repository는 {@code RoleRepositoryPort.buildEffectiveGrants()} 메서드에서 핵심 역할을 합니다.</p>
 * <p>복잡한 4-table JOIN 쿼리를 위해 QueryDSL 또는 @Query를 사용할 수 있습니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public interface UserRoleMappingJpaRepository extends JpaRepository<UserRoleMappingJpaEntity, Long> {

    /**
     * UserContext + Tenant + Organization으로 모든 UserRoleMapping 조회
     *
     * <p>특정 사용자가 특정 Tenant/Organization 컨텍스트에서 가진 모든 Role을 조회합니다.</p>
     *
     * @param userContextId UserContext ID
     * @param tenantId Tenant ID (String UUID)
     * @param organizationId Organization ID
     * @return UserRoleMapping Entity 리스트
     * @author ryu-qqq
     * @since 2025-10-24
     */
    List<UserRoleMappingJpaEntity> findAllByUserContextIdAndTenantIdAndOrganizationId(
        Long userContextId,
        String tenantId,
        Long organizationId
    );
}

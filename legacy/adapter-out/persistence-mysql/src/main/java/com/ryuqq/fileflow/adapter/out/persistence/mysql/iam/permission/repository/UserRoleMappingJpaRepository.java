package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.UserRoleMappingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * UserRoleMapping Spring Data JPA Repository (CQRS Command Side)
 *
 * <p><strong>역할</strong>: User-Role Mapping Entity에 대한 기본 CRUD 및 쿼리 메서드 제공</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스 (구현체 자동 생성)</li>
 *   <li>✅ Long FK 전략 (관계 어노테이션 금지)</li>
 *   <li>✅ 메서드 네이밍 규칙 준수 (Spring Data JPA Query Methods)</li>
 *   <li>✅ CQRS Command Side 전용 (복잡한 조회는 Query Adapter 사용)</li>
 *   <li>❌ {@code @Repository} 어노테이션 불필요 (JpaRepository 상속 시 자동)</li>
 *   <li>❌ {@code @Query} 어노테이션 금지 (Command Side에서는 불필요)</li>
 * </ul>
 *
 * <h3>CQRS 분리 원칙</h3>
 * <ul>
 *   <li><strong>Command Side (이 Repository)</strong>: 기본 CRUD 및 단순 조회</li>
 *   <li><strong>Query Side (UserRoleMappingQueryRepositoryAdapter)</strong>: 복잡한 JOIN 쿼리, buildEffectiveGrants()</li>
 * </ul>
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
     * @param tenantId Tenant ID (Long AUTO_INCREMENT)
     * @param organizationId Organization ID
     * @return UserRoleMapping Entity 리스트
     * @author ryu-qqq
     * @since 2025-10-24
     */
    List<UserRoleMappingJpaEntity> findAllByUserContextIdAndTenantIdAndOrganizationId(
        Long userContextId,
        Long tenantId,
        Long organizationId
    );
}

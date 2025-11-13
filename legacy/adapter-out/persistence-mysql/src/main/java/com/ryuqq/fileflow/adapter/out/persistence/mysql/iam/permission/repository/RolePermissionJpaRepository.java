package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RolePermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * RolePermission JPA Repository
 *
 * <p><strong>역할</strong>: Role-Permission 연결 테이블에 대한 Spring Data JPA Repository</p>
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
 * <p>Role과 Permission 간의 N:M 관계를 관리하는 연결 테이블 조회:</p>
 * <ul>
 *   <li>{@code findAllByRoleCode}: 특정 Role의 모든 Permission 조회</li>
 *   <li>{@code findAllByPermissionCode}: 특정 Permission을 가진 모든 Role 조회</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionJpaEntity, Long> {

    /**
     * Role Code로 모든 RolePermission 조회
     *
     * <p>특정 Role이 가진 모든 Permission을 조회합니다.</p>
     *
     * @param roleCode Role Code
     * @return RolePermission Entity 리스트
     * @author ryu-qqq
     * @since 2025-10-24
     */
    List<RolePermissionJpaEntity> findAllByRoleCode(String roleCode);

    /**
     * Permission Code로 모든 RolePermission 조회
     *
     * <p>특정 Permission을 가진 모든 Role을 조회합니다.</p>
     *
     * @param permissionCode Permission Code
     * @return RolePermission Entity 리스트
     * @author ryu-qqq
     * @since 2025-10-24
     */
    List<RolePermissionJpaEntity> findAllByPermissionCode(String permissionCode);
}

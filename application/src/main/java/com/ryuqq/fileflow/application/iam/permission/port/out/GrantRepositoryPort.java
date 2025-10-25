package com.ryuqq.fileflow.application.iam.permission.port.out;

import com.ryuqq.fileflow.domain.iam.permission.Grant;

import java.util.List;

/**
 * Grant Repository Port - Grant 조회 Outbound Port
 *
 * <p>Application Layer에서 정의하고 Persistence Adapter에서 구현하는
 * Hexagonal Architecture의 Driven Port (Outbound Port)입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>사용자의 유효 Grants 조회 (user:tenant:org 기반)</li>
 *   <li>4-table JOIN 쿼리 수행 (UserRoleMapping, Role, RolePermission, Permission)</li>
 *   <li>Cache 통합 (선택적)</li>
 * </ul>
 *
 * <p><strong>구현 위치:</strong></p>
 * <ul>
 *   <li>{@code adapter-out/persistence-mysql/iam/permission/adapter/GrantPersistenceAdapter.java}</li>
 *   <li>{@code adapter-out/persistence-mysql/iam/permission/repository/GrantQueryRepository.java}</li>
 * </ul>
 *
 * <p><strong>쿼리 예시:</strong></p>
 * <pre>{@code
 * SELECT
 *   urm.role_code,
 *   p.code AS permission_code,
 *   p.default_scope,
 *   rp.condition_expr
 * FROM user_role_mapping urm
 * INNER JOIN role r ON urm.role_code = r.code AND r.deleted = false
 * INNER JOIN role_permission rp ON r.code = rp.role_code
 * INNER JOIN permission p ON rp.permission_code = p.code AND p.deleted = false
 * WHERE urm.user_context_id = ?
 *   AND urm.tenant_id = ?
 *   AND urm.organization_id = ?
 * }</pre>
 *
 * <p><strong>성능 요구사항:</strong></p>
 * <ul>
 *   <li>P95 Latency < 30ms (DB 조회)</li>
 *   <li>Cache Hit 시 P95 < 5ms 목표</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-25
 */
public interface GrantRepositoryPort {

    /**
     * 사용자의 유효 Grants 조회
     *
     * <p>주어진 사용자 컨텍스트 (userId, tenantId, organizationId)에 대해
     * 유효한 모든 Grant를 조회합니다.</p>
     *
     * <p><strong>조회 로직:</strong></p>
     * <ol>
     *   <li>UserRoleMapping에서 사용자의 Role 조회</li>
     *   <li>Role의 deleted가 false인 것만 필터링</li>
     *   <li>RolePermission에서 Role에 매핑된 Permission 조회</li>
     *   <li>Permission의 deleted가 false인 것만 필터링</li>
     *   <li>Grant 객체로 조합하여 반환</li>
     * </ol>
     *
     * <p><strong>반환값:</strong></p>
     * <ul>
     *   <li>Grant가 없으면 빈 List 반환 (null 반환 불가)</li>
     *   <li>각 Grant는 roleCode, permissionCode, scope, conditionExpr 포함</li>
     *   <li>conditionExpr는 nullable (조건 없는 권한도 있음)</li>
     * </ul>
     *
     * <p><strong>Cache 키 권장:</strong></p>
     * <pre>
     * "grants:user:{userId}:tenant:{tenantId}:org:{organizationId}"
     * TTL: 5-10분 (권한 변경 빈도에 따라 조정)
     * </pre>
     *
     * @param userId 사용자 ID (Not null)
     * @param tenantId 테넌트 ID (Not null)
     * @param organizationId 조직 ID (Not null)
     * @return Grant 리스트 (빈 List 가능, null 불가)
     * @throws IllegalArgumentException userId, tenantId, organizationId가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-25
     */
    List<Grant> findEffectiveGrants(Long userId, Long tenantId, Long organizationId);
}

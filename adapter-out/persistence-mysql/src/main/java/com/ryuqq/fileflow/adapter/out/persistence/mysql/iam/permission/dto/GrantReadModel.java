package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.dto;

/**
 * Grant Read Model - CQRS Query 전용 DTO
 *
 * <p><strong>역할</strong>: {@code buildEffectiveGrants()} 쿼리의 조회 전용 DTO입니다.</p>
 *
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/dto/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ CQRS Read Model - Query 전용</li>
 *   <li>✅ QueryDSL Projections 지원</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ 4-table JOIN 결과를 효율적으로 표현</li>
 * </ul>
 *
 * <h3>테이블 매핑</h3>
 * <p>이 DTO는 다음 4개 테이블의 JOIN 결과를 나타냅니다:</p>
 * <ul>
 *   <li>{@code user_role_mapping} - 사용자-역할 매핑</li>
 *   <li>{@code role} - 역할 정보</li>
 *   <li>{@code role_permission} - 역할-권한 매핑</li>
 *   <li>{@code permission} - 권한 정보</li>
 * </ul>
 *
 * <h3>SQL 쿼리 예시</h3>
 * <pre>{@code
 * SELECT
 *   urm.role_code AS roleCode,
 *   p.code AS permissionCode,
 *   p.default_scope AS defaultScope
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
public class GrantReadModel {

    private final String roleCode;
    private final String permissionCode;
    private final String defaultScope;

    /**
     * Constructor - QueryDSL Projections용
     *
     * <p>QueryDSL의 {@code Projections.constructor()}에서 사용됩니다.</p>
     *
     * @param roleCode Role Code (예: "ADMIN", "USER")
     * @param permissionCode Permission Code (예: "product:read", "order:write")
     * @param defaultScope Default Scope (예: "ORGANIZATION", "TENANT", "GLOBAL")
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public GrantReadModel(String roleCode, String permissionCode, String defaultScope) {
        this.roleCode = roleCode;
        this.permissionCode = permissionCode;
        this.defaultScope = defaultScope;
    }

    /**
     * Role Code 조회
     *
     * @return Role Code
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getRoleCode() {
        return roleCode;
    }

    /**
     * Permission Code 조회
     *
     * @return Permission Code
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getPermissionCode() {
        return permissionCode;
    }

    /**
     * Default Scope 조회
     *
     * @return Default Scope (ORGANIZATION, TENANT, GLOBAL)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public String getDefaultScope() {
        return defaultScope;
    }
}

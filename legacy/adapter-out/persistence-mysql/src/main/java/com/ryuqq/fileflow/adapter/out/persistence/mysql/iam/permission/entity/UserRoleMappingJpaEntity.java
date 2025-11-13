package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * UserRoleMapping JPA Entity (Join Table)
 *
 * <p><strong>역할</strong>: User와 Role 간의 매핑을 테넌트/조직 컨텍스트 내에서 표현하는 연결 테이블</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/entity/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ Getter만 제공 (Setter 금지)</li>
 *   <li>✅ 기본 생성자 + PK 포함/제외 생성자만 제공</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지</li>
 *   <li>❌ Static Factory Method 금지</li>
 *   <li>❌ 비즈니스 로직 금지</li>
 * </ul>
 *
 * <h3>테이블 구조</h3>
 * <p>UserContext가 특정 Tenant/Organization 컨텍스트에서 어떤 Role을 가지는지 표현합니다.</p>
 * <ul>
 *   <li>UserContext (1) ← user_context_id ← UserRoleMapping (N)</li>
 *   <li>Role (1) ← role_code ← UserRoleMapping (N)</li>
 *   <li>Tenant (1) ← tenant_id ← UserRoleMapping (N)</li>
 *   <li>Organization (1) ← organization_id ← UserRoleMapping (N)</li>
 *   <li>조회 시: {@code findAllByUserContextIdAndTenantIdAndOrganizationId()}로 컨텍스트별 Role 조회</li>
 * </ul>
 *
 * <h3>buildEffectiveGrants() 쿼리에서의 역할</h3>
 * <p>이 테이블은 {@code RoleRepositoryPort.buildEffectiveGrants()}에서 핵심 역할을 합니다:</p>
 * <pre>
 * SELECT DISTINCT rp.permission_code, p.default_scope, r.code as role_code
 * FROM user_role_mappings urm
 * JOIN roles r ON urm.role_code = r.code
 * JOIN role_permissions rp ON r.code = rp.role_code
 * JOIN permissions p ON rp.permission_code = p.code
 * WHERE urm.user_context_id = ?
 *   AND urm.tenant_id = ?
 *   AND urm.organization_id = ?
 *   AND r.deleted = false
 *   AND p.deleted = false
 * </pre>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Entity
@Table(name = "user_role_mappings")
public class UserRoleMappingJpaEntity {

    /**
     * UserRoleMapping 고유 식별자 (Primary Key)
     *
     * <p><strong>생성 전략</strong>: Auto Increment (MySQL BIGINT AUTO_INCREMENT)</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * UserContext ID (FK - Long 전략)
     *
     * <p>Domain {@code UserContextId}와 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, Index 권장 (Composite Index: user_context_id + tenant_id + organization_id)</p>
     */
    @Column(name = "user_context_id", nullable = false)
    private Long userContextId;

    /**
     * Role 코드 (FK - String 전략)
     *
     * <p>Domain {@code RoleCode}와 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, Index 권장</p>
     */
    @Column(name = "role_code", nullable = false, length = 100)
    private String roleCode;

    /**
     * Tenant ID (FK - Long AUTO_INCREMENT 전략)
     *
     * <p>Domain {@code TenantId}와 매핑됩니다.</p>
     * <p><strong>주의</strong>: TenantJpaEntity의 Long PK와 일치해야 합니다.</p>
     * <p><strong>제약</strong>: NOT NULL, BIGINT, Index 권장 (Composite Index: user_context_id + tenant_id + organization_id)</p>
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * Organization ID (FK - Long 전략)
     *
     * <p>Domain {@code OrganizationId}와 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, Index 권장 (Composite Index: user_context_id + tenant_id + organization_id)</p>
     */
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    /**
     * JPA 전용 기본 생성자 (Protected)
     *
     * <p>JPA 스펙 요구사항입니다. 직접 호출 금지!</p>
     */
    protected UserRoleMappingJpaEntity() {
    }

    /**
     * PK 제외 생성자 (새로 생성)
     *
     * <p>User와 Role을 특정 Tenant/Organization 컨텍스트에서 연결할 때 사용합니다.</p>
     *
     * @param userContextId UserContext ID
     * @param roleCode Role 코드
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     */
    public UserRoleMappingJpaEntity(
        Long userContextId,
        String roleCode,
        Long tenantId,
        Long organizationId
    ) {
        this.userContextId = userContextId;
        this.roleCode = roleCode;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
    }

    /**
     * PK 포함 전체 생성자
     *
     * <p>DB에서 조회한 데이터를 Entity로 변환할 때 사용합니다.</p>
     *
     * @param id UserRoleMapping ID (PK)
     * @param userContextId UserContext ID
     * @param roleCode Role 코드
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     */
    public UserRoleMappingJpaEntity(
        Long id,
        Long userContextId,
        String roleCode,
        Long tenantId,
        Long organizationId
    ) {
        this.id = id;
        this.userContextId = userContextId;
        this.roleCode = roleCode;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
    }

    // ========================================
    // Getters
    // ========================================

    public Long getId() {
        return id;
    }

    public Long getUserContextId() {
        return userContextId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }
}

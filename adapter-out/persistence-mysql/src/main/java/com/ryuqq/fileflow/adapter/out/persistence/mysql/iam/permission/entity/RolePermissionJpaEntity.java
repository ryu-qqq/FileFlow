package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * RolePermission JPA Entity (Join Table)
 *
 * <p><strong>역할</strong>: Role과 Permission 간의 N:M 관계를 표현하는 연결 테이블</p>
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
 * <p>Role과 Permission의 N:M 관계를 1:N + N:1로 분해한 연결 테이블입니다.</p>
 * <ul>
 *   <li>Role (1) ← role_code ← RolePermission (N)</li>
 *   <li>Permission (1) ← permission_code ← RolePermission (N)</li>
 *   <li>조회 시: {@code findAllByRoleCode(roleCode)}로 해당 Role의 Permission들 조회</li>
 *   <li>조회 시: {@code findAllByPermissionCode(permissionCode)}로 해당 Permission을 가진 Role들 조회</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Entity
@Table(name = "role_permissions")
public class RolePermissionJpaEntity {

    /**
     * RolePermission 고유 식별자 (Primary Key)
     *
     * <p><strong>생성 전략</strong>: Auto Increment (MySQL BIGINT AUTO_INCREMENT)</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Role 코드 (FK - String 전략)
     *
     * <p>Domain {@code RoleCode}와 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, Index 권장</p>
     */
    @Column(name = "role_code", nullable = false, length = 100)
    private String roleCode;

    /**
     * Permission 코드 (FK - String 전략)
     *
     * <p>Domain {@code PermissionCode}와 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, Index 권장</p>
     */
    @Column(name = "permission_code", nullable = false, length = 100)
    private String permissionCode;

    /**
     * JPA 전용 기본 생성자 (Protected)
     *
     * <p>JPA 스펙 요구사항입니다. 직접 호출 금지!</p>
     */
    protected RolePermissionJpaEntity() {
    }

    /**
     * PK 제외 생성자 (새로 생성)
     *
     * <p>Role과 Permission을 연결할 때 사용합니다.</p>
     *
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     */
    public RolePermissionJpaEntity(
        String roleCode,
        String permissionCode
    ) {
        this.roleCode = roleCode;
        this.permissionCode = permissionCode;
    }

    /**
     * PK 포함 전체 생성자
     *
     * <p>DB에서 조회한 데이터를 Entity로 변환할 때 사용합니다.</p>
     *
     * @param id RolePermission ID (PK)
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     */
    public RolePermissionJpaEntity(
        Long id,
        String roleCode,
        String permissionCode
    ) {
        this.id = id;
        this.roleCode = roleCode;
        this.permissionCode = permissionCode;
    }

    // ========================================
    // Getters
    // ========================================

    public Long getId() {
        return id;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getPermissionCode() {
        return permissionCode;
    }
}

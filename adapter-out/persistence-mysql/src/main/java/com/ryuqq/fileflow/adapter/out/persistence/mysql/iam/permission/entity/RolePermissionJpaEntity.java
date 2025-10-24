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
 *   <li>✅ {@code private final} 필드 (변경 불가능한 필드)</li>
 *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지</li>
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
    private final String roleCode;

    /**
     * Permission 코드 (FK - String 전략)
     *
     * <p>Domain {@code PermissionCode}와 매핑됩니다.</p>
     * <p><strong>제약</strong>: NOT NULL, Index 권장</p>
     */
    @Column(name = "permission_code", nullable = false, length = 100)
    private final String permissionCode;

    /**
     * JPA 전용 기본 생성자 (Protected)
     *
     * <p>JPA Proxy 생성을 위해 필요합니다. 직접 호출 금지!</p>
     */
    protected RolePermissionJpaEntity() {
        this.roleCode = null;
        this.permissionCode = null;
    }

    /**
     * Private 전체 생성자
     *
     * <p>Static Factory Method에서만 사용합니다.</p>
     */
    private RolePermissionJpaEntity(
        Long id,
        String roleCode,
        String permissionCode
    ) {
        this.id = id;
        this.roleCode = roleCode;
        this.permissionCode = permissionCode;
    }

    /**
     * 새로운 RolePermission Entity 생성 (Static Factory Method)
     *
     * <p>Role과 Permission을 연결할 때 사용합니다.</p>
     *
     * <p><strong>검증</strong>: 필수 필드 null 체크만 수행 (비즈니스 검증은 Domain Layer에서)</p>
     *
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     * @return 새로운 RolePermissionJpaEntity
     * @throws IllegalArgumentException 필수 필드가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static RolePermissionJpaEntity create(
        String roleCode,
        String permissionCode
    ) {
        if (roleCode == null || permissionCode == null) {
            throw new IllegalArgumentException(
                "Required fields (roleCode, permissionCode) must not be null"
            );
        }

        return new RolePermissionJpaEntity(
            null,               // id는 DB에서 자동 생성
            roleCode,
            permissionCode
        );
    }

    /**
     * DB에서 조회한 데이터로 Entity 재구성 (Static Factory Method)
     *
     * <p>DB 조회 결과를 Entity로 변환할 때 사용합니다.</p>
     *
     * @param id RolePermission ID
     * @param roleCode Role 코드
     * @param permissionCode Permission 코드
     * @return 재구성된 RolePermissionJpaEntity
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static RolePermissionJpaEntity reconstitute(
        Long id,
        String roleCode,
        String permissionCode
    ) {
        return new RolePermissionJpaEntity(
            id,
            roleCode,
            permissionCode
        );
    }

    // ========================================
    // Getters (Public, 비즈니스 메서드 없음!)
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

package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RoleJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RolePermissionJpaEntity;
import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;
import com.ryuqq.fileflow.domain.iam.permission.Role;
import com.ryuqq.fileflow.domain.iam.permission.RoleCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Role Entity Mapper
 *
 * <p><strong>역할</strong>: Domain Model {@code Role} ↔ JPA Entity {@code RoleJpaEntity} 상호 변환</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/mapper/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ 상태 없는(Stateless) 유틸리티 클래스</li>
 *   <li>✅ {@code toDomain()}: Entity → Domain 변환</li>
 *   <li>✅ {@code toEntity()}: Domain → Entity 변환</li>
 *   <li>✅ Value Object 변환 포함 (RoleCode, Set&lt;PermissionCode&gt;)</li>
 *   <li>✅ 연결 테이블 변환: RolePermissionJpaEntity ↔ Set&lt;PermissionCode&gt;</li>
 *   <li>❌ Lombok 금지 (Pure Java)</li>
 *   <li>❌ 비즈니스 로직 금지 (단순 변환만)</li>
 * </ul>
 *
 * <h3>Long FK 전략</h3>
 * <p>Role과 Permission은 N:M 관계로, 연결 테이블 {@code role_permissions}를 통해 관리됩니다.</p>
 * <ul>
 *   <li>Role (1) ← role_code ← RolePermission (N) → permission_code → Permission (1)</li>
 *   <li>조회 시: {@code RolePermissionRepository.findAllByRoleCode()}로 별도 조회</li>
 *   <li>저장 시: Role과 RolePermission을 각각 저장</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public final class RoleEntityMapper {

    /**
     * Private 생성자 - 인스턴스화 방지
     */
    private RoleEntityMapper() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * JPA Entity → Domain Model 변환
     *
     * <p>DB에서 조회한 {@code RoleJpaEntity}를 Domain {@code Role}로 변환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>Value Object 생성: {@code RoleCode}</li>
     *   <li>RolePermission Entity 리스트를 Set&lt;PermissionCode&gt;로 변환</li>
     *   <li>Domain Aggregate 재구성</li>
     * </ol>
     *
     * @param entity JPA Entity
     * @param rolePermissionEntities RolePermission JPA Entity 리스트
     * @return Domain Role
     * @throws IllegalArgumentException entity가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Role toDomain(
        RoleJpaEntity entity,
        List<RolePermissionJpaEntity> rolePermissionEntities
    ) {
        if (entity == null) {
            throw new IllegalArgumentException("RoleJpaEntity must not be null");
        }

        // Value Object 변환 (Static Factory Method 사용)
        RoleCode roleCode = RoleCode.of(entity.getCode());

        // RolePermission Entity → Set<PermissionCode> 변환
        Set<PermissionCode> permissionCodes = new HashSet<>();
        if (rolePermissionEntities != null) {
            for (RolePermissionJpaEntity rpEntity : rolePermissionEntities) {
                permissionCodes.add(PermissionCode.of(rpEntity.getPermissionCode()));
            }
        }

        // Domain Aggregate 재구성
        return Role.reconstitute(
            roleCode,
            entity.getDescription(),
            permissionCodes,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.isDeleted()
        );
    }

    /**
     * Domain Model → JPA Entity 변환
     *
     * <p>Domain {@code Role}을 JPA {@code RoleJpaEntity}로 변환합니다.</p>
     * <p><strong>주의</strong>: RolePermission은 별도 테이블이므로 Role Entity만 반환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>Value Object 원시 타입 추출: {@code code.value()}</li>
     *   <li>JPA Entity 생성 (reconstitute)</li>
     * </ol>
     *
     * @param role Domain Role
     * @return JPA Entity (Role만, RolePermission 제외)
     * @throws IllegalArgumentException role이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static RoleJpaEntity toEntity(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }

        // Value Object → 원시 타입 (Law of Demeter 준수)
        String code = role.getCodeValue();

        // Role은 Code를 PK로 사용하므로 항상 PK 포함 생성자 사용
        return new RoleJpaEntity(
            code,
            role.getDescription(),
            role.getCreatedAt(),
            role.getUpdatedAt(),
            role.getDeletedAt()
        );
    }

    /**
     * Domain PermissionCode → JPA RolePermission Entity 변환
     *
     * <p>Domain {@code PermissionCode}를 JPA {@code RolePermissionJpaEntity}로 변환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>RoleCode, PermissionCode 원시 값 추출</li>
     *   <li>JPA Entity 생성 (create)</li>
     * </ol>
     *
     * @param roleCode Role Code (String FK)
     * @param permissionCode Permission Code
     * @return JPA Entity
     * @throws IllegalArgumentException permissionCode가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static RolePermissionJpaEntity toRolePermissionEntity(
        String roleCode,
        PermissionCode permissionCode
    ) {
        if (permissionCode == null) {
            throw new IllegalArgumentException("PermissionCode must not be null");
        }

        // Value Object → 원시 타입
        // 주의: PermissionCode는 getValue() 메서드 사용
        String permissionCodeValue = permissionCode.getValue();

        // RolePermission은 새로 생성 (PK 제외 생성자 사용)
        return new RolePermissionJpaEntity(roleCode, permissionCodeValue);
    }

    /**
     * Domain Permission Code Set → JPA RolePermission Entity List 변환
     *
     * <p>Domain {@code Set<PermissionCode>}를 JPA {@code RolePermissionJpaEntity} 리스트로 일괄 변환합니다.</p>
     *
     * @param roleCode Role Code (String FK)
     * @param permissionCodes Domain PermissionCode Set
     * @return JPA Entity 리스트
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static List<RolePermissionJpaEntity> toRolePermissionEntities(
        String roleCode,
        Set<PermissionCode> permissionCodes
    ) {
        List<RolePermissionJpaEntity> entities = new ArrayList<>();

        if (permissionCodes != null) {
            for (PermissionCode permissionCode : permissionCodes) {
                entities.add(toRolePermissionEntity(roleCode, permissionCode));
            }
        }

        return entities;
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.PermissionJpaEntity;
import com.ryuqq.fileflow.domain.iam.permission.Permission;
import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;
import com.ryuqq.fileflow.domain.iam.permission.Scope;

/**
 * Permission Entity Mapper
 *
 * <p><strong>역할</strong>: Domain Model {@code Permission} ↔ JPA Entity {@code PermissionJpaEntity} 상호 변환</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/mapper/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ 상태 없는(Stateless) 유틸리티 클래스</li>
 *   <li>✅ {@code toDomain()}: Entity → Domain 변환</li>
 *   <li>✅ {@code toEntity()}: Domain → Entity 변환</li>
 *   <li>✅ Value Object 변환 포함 (PermissionCode, Scope)</li>
 *   <li>❌ Lombok 금지 (Pure Java)</li>
 *   <li>❌ 비즈니스 로직 금지 (단순 변환만)</li>
 * </ul>
 *
 * <h3>주의사항</h3>
 * <p>Permission은 Code를 Primary Key로 사용합니다 (Auto Increment 아님).</p>
 * <p>Scope는 Domain Enum과 DB 문자열 간 직접 매핑됩니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public final class PermissionEntityMapper {

    /**
     * Private 생성자 - 인스턴스화 방지
     */
    private PermissionEntityMapper() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * JPA Entity → Domain Model 변환
     *
     * <p>DB에서 조회한 {@code PermissionJpaEntity}를 Domain {@code Permission}으로 변환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>Value Object 생성: {@code PermissionCode}</li>
     *   <li>Scope Enum 변환: String → Enum</li>
     *   <li>Domain Aggregate 재구성</li>
     * </ol>
     *
     * @param entity JPA Entity
     * @return Domain Permission
     * @throws IllegalArgumentException entity가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static Permission toDomain(PermissionJpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("PermissionJpaEntity must not be null");
        }

        // Value Object 변환 (Static Factory Method 사용)
        PermissionCode code = PermissionCode.of(entity.getCode());

        // Scope Enum 변환
        Scope scope = Scope.valueOf(entity.getDefaultScope());

        // Domain Aggregate 재구성
        return Permission.reconstitute(
            code,
            entity.getDescription(),
            scope,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.isDeleted()
        );
    }

    /**
     * Domain Model → JPA Entity 변환
     *
     * <p>Domain {@code Permission}을 JPA {@code PermissionJpaEntity}로 변환합니다.</p>
     *
     * <h4>변환 과정</h4>
     * <ol>
     *   <li>Value Object 원시 타입 추출: {@code code.value()}</li>
     *   <li>Scope Enum → String 변환</li>
     *   <li>JPA Entity 생성 (reconstitute)</li>
     * </ol>
     *
     * @param permission Domain Permission
     * @return JPA Entity
     * @throws IllegalArgumentException permission이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    public static PermissionJpaEntity toEntity(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission must not be null");
        }

        // Value Object → 원시 타입 (Law of Demeter 준수)
        String code = permission.getCodeValue();
        String defaultScope = permission.getDefaultScope().name();

        // Permission은 Code를 PK로 사용하므로 항상 PK 포함 생성자 사용
        return new PermissionJpaEntity(
            code,
            permission.getDescription(),
            defaultScope,
            permission.getCreatedAt(),
            permission.getUpdatedAt(),
            permission.getDeletedAt()
        );
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Permission JPA Repository
 *
 * <p><strong>역할</strong>: Permission Entity에 대한 Spring Data JPA Repository 인터페이스</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 표준 인터페이스</li>
 *   <li>✅ String PK 전략 (Code가 Primary Key)</li>
 *   <li>✅ Query Method 네이밍 규칙 준수</li>
 *   <li>❌ Native Query 지양</li>
 * </ul>
 *
 * <h3>주의사항</h3>
 * <p>Permission은 Code를 Primary Key로 사용하므로 {@code JpaRepository<PermissionJpaEntity, String>}입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, String> {

    /**
     * Permission Code로 조회 (삭제되지 않은 Permission만)
     *
     * <p>Soft Delete를 고려하여 {@code deletedAt IS NULL}인 Permission만 조회합니다.</p>
     *
     * @param code Permission Code
     * @return Permission Entity (Optional)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Optional<PermissionJpaEntity> findByCodeAndDeletedAtIsNull(String code);

    /**
     * Permission Code 존재 여부 확인 (삭제되지 않은 Permission만)
     *
     * <p>Permission 생성 전 중복 확인을 위해 사용합니다.</p>
     * <p>Soft Delete를 고려하여 {@code deletedAt IS NULL}인 Permission만 확인합니다.</p>
     *
     * @param code Permission Code
     * @return 존재 여부
     * @author ryu-qqq
     * @since 2025-10-24
     */
    boolean existsByCodeAndDeletedAtIsNull(String code);
}

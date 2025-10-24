package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Role JPA Repository
 *
 * <p><strong>역할</strong>: Role Entity에 대한 Spring Data JPA Repository 인터페이스</p>
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
 * <p>Role은 Code를 Primary Key로 사용하므로 {@code JpaRepository<RoleJpaEntity, String>}입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, String> {

    /**
     * Role Code로 조회
     *
     * <p>Role의 Primary Key가 Code이므로, 이 메서드는 {@code findById}와 동일합니다.</p>
     * <p>명시적인 의미 전달을 위해 제공됩니다.</p>
     *
     * @param code Role Code
     * @return Role Entity (Optional)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Optional<RoleJpaEntity> findByCode(String code);

    /**
     * Role Code 존재 여부 확인
     *
     * <p>Role 생성 전 중복 확인을 위해 사용합니다.</p>
     *
     * @param code Role Code
     * @return 존재 여부
     * @author ryu-qqq
     * @since 2025-10-24
     */
    boolean existsByCode(String code);
}

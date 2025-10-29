package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.tenant.entity.TenantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Tenant Spring Data JPA Repository
 *
 * <p><strong>역할</strong>: Tenant Entity에 대한 기본 CRUD 및 쿼리 메서드 제공</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/tenant/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스 (구현체 자동 생성)</li>
 *   <li>✅ Long PK 전략 (AUTO_INCREMENT)</li>
 *   <li>✅ 소프트 삭제 고려 (deleted=false 조건 추가)</li>
 *   <li>✅ 메서드 네이밍 규칙 준수 (Spring Data JPA Query Methods)</li>
 *   <li>❌ {@code @Repository} 어노테이션 불필요 (JpaRepository 상속 시 자동)</li>
 *   <li>❌ 복잡한 쿼리는 QueryDSL 사용 (Custom Repository)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public interface TenantJpaRepository extends JpaRepository<TenantJpaEntity, Long> {

    /**
     * ID로 활성 Tenant 조회
     *
     * <p>소프트 삭제되지 않은 Tenant만 조회합니다.</p>
     *
     * @param id Tenant ID (Long - AUTO_INCREMENT PK)
     * @return Tenant Entity (삭제되었거나 존재하지 않으면 {@code Optional.empty()})
     */
    Optional<TenantJpaEntity> findByIdAndDeletedIsFalse(Long id);

    /**
     * 모든 활성 Tenant 조회
     *
     * <p>소프트 삭제되지 않은 Tenant 목록을 생성일시 오름차순으로 반환합니다.</p>
     *
     * @return 활성 Tenant 목록 (빈 리스트 가능)
     */
    List<TenantJpaEntity> findByDeletedIsFalseOrderByCreatedAtAsc();

    /**
     * Tenant 이름 중복 확인 (활성 Tenant 기준)
     *
     * <p>소프트 삭제되지 않은 Tenant 중 동일한 이름이 존재하는지 확인합니다.</p>
     *
     * @param name Tenant 이름
     * @return 존재하면 {@code true}, 없으면 {@code false}
     */
    boolean existsByNameAndDeletedIsFalse(String name);

    /**
     * 활성 Tenant 개수 조회
     *
     * <p>소프트 삭제되지 않은 Tenant의 총 개수를 반환합니다.</p>
     *
     * @return 활성 Tenant 개수
     */
    long countByDeletedIsFalse();
}

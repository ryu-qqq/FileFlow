package com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.entity.OrganizationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Organization Spring Data JPA Repository
 *
 * <p><strong>역할</strong>: Organization Entity에 대한 기본 CRUD 및 쿼리 메서드 제공</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/organization/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스 (구현체 자동 생성)</li>
 *   <li>✅ Long FK 전략 (tenantId는 Long 타입)</li>
 *   <li>✅ 소프트 삭제 고려 (deleted=false 조건 추가)</li>
 *   <li>✅ 메서드 네이밍 규칙 준수 (Spring Data JPA Query Methods)</li>
 *   <li>❌ {@code @Repository} 어노테이션 불필요 (JpaRepository 상속 시 자동)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public interface OrganizationJpaRepository extends JpaRepository<OrganizationJpaEntity, Long> {

    /**
     * ID로 활성 Organization 조회
     *
     * <p>소프트 삭제되지 않은 Organization만 조회합니다.</p>
     *
     * @param id Organization ID
     * @return Organization Entity (삭제되었거나 존재하지 않으면 {@code Optional.empty()})
     */
    Optional<OrganizationJpaEntity> findByIdAndDeletedIsFalse(Long id);

    /**
     * Tenant ID로 활성 Organization 목록 조회
     *
     * <p>특정 Tenant에 속한 모든 활성 Organization을 생성일시 오름차순으로 조회합니다.</p>
     *
     * <p><strong>Long FK 전략</strong>: Tenant 객체가 아닌 Long tenantId 사용</p>
     *
     * @param tenantId Tenant ID (Long FK)
     * @return Organization 목록 (빈 리스트 가능)
     */
    List<OrganizationJpaEntity> findByTenantIdAndDeletedIsFalseOrderByCreatedAtAsc(Long tenantId);

    /**
     * Tenant ID와 조직 코드로 활성 Organization 조회
     *
     * <p>특정 Tenant 내에서 조직 코드로 Organization을 찾습니다.
     * 조직 코드는 Tenant 내에서 유니크하므로 최대 1건만 반환됩니다.</p>
     *
     * @param tenantId Tenant ID (Long FK)
     * @param orgCode 조직 코드
     * @return Organization Entity (삭제되었거나 존재하지 않으면 {@code Optional.empty()})
     */
    Optional<OrganizationJpaEntity> findByTenantIdAndOrgCodeAndDeletedIsFalse(Long tenantId, String orgCode);

    /**
     * Tenant ID와 조직 코드 중복 확인 (활성 Organization 기준)
     *
     * <p>특정 Tenant 내에서 동일한 조직 코드가 이미 사용 중인지 확인합니다.
     * 소프트 삭제된 Organization은 제외됩니다.</p>
     *
     * @param tenantId Tenant ID (Long FK)
     * @param orgCode 조직 코드
     * @return 존재하면 {@code true}, 없으면 {@code false}
     */
    boolean existsByTenantIdAndOrgCodeAndDeletedIsFalse(Long tenantId, String orgCode);

    /**
     * Tenant ID로 활성 Organization 개수 조회
     *
     * <p>특정 Tenant에 속한 활성 Organization의 개수를 반환합니다.</p>
     *
     * @param tenantId Tenant ID (Long FK)
     * @return 활성 Organization 개수
     */
    long countByTenantIdAndDeletedIsFalse(Long tenantId);
}

package com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.entity.OrganizationJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.mapper.OrganizationEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.organization.repository.OrganizationJpaRepository;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.OrgCode;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Organization Persistence Adapter (Hexagonal Architecture - Driven Adapter)
 *
 * <p><strong>역할</strong>: Application Layer의 {@link OrganizationRepositoryPort}를 구현하여
 * 실제 MySQL 영속성 작업을 수행합니다.</p>
 *
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/organization/adapter/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ {@code @Component} 어노테이션 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code OrganizationRepositoryPort} 인터페이스 구현 (DIP)</li>
 *   <li>✅ Mapper로 Domain ↔ Entity 변환</li>
 *   <li>✅ Long FK 전략 적용 (tenantId는 Long 타입)</li>
 *   <li>✅ JpaRepository 사용하여 실제 DB 작업 수행</li>
 *   <li>❌ {@code @Repository} 사용 금지 ({@code @Component} 사용)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 * </ul>
 *
 * <h3>의존성 방향</h3>
 * <pre>
 * Application Layer (Port) ← Adapter Layer (Implementation)
 *                             ↓
 *                         JPA Repository
 *                             ↓
 *                           MySQL
 * </pre>
 *
 * @see OrganizationRepositoryPort Application Layer Port
 * @see OrganizationJpaRepository Spring Data JPA Repository
 * @see OrganizationEntityMapper Domain ↔ Entity Mapper
 * @since 1.0.0
 */
@Component
public class OrganizationPersistenceAdapter implements OrganizationRepositoryPort {

    private final OrganizationJpaRepository organizationJpaRepository;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * @param organizationJpaRepository Spring Data JPA Repository
     */
    public OrganizationPersistenceAdapter(OrganizationJpaRepository organizationJpaRepository) {
        this.organizationJpaRepository = organizationJpaRepository;
    }

    /**
     * Organization 저장 (생성 또는 수정)
     *
     * <p>Domain {@code Organization}을 JPA Entity로 변환한 후 저장하고,
     * 저장된 Entity를 다시 Domain으로 변환하여 반환합니다.</p>
     *
     * <h4>처리 흐름</h4>
     * <ol>
     *   <li>Domain → Entity 변환 (Mapper)</li>
     *   <li>JPA Repository로 저장</li>
     *   <li>Entity → Domain 변환 (Mapper)</li>
     *   <li>Domain 반환</li>
     * </ol>
     *
     * @param organization 저장할 Organization Domain
     * @return 저장된 Organization Domain
     * @throws IllegalArgumentException organization이 null인 경우
     */
    @Override
    public Organization save(Organization organization) {
        if (organization == null) {
            throw new IllegalArgumentException("Organization must not be null");
        }

        // Domain → Entity
        OrganizationJpaEntity entity = OrganizationEntityMapper.toEntity(organization);

        // JPA 저장
        OrganizationJpaEntity savedEntity = organizationJpaRepository.save(entity);

        // Entity → Domain
        return OrganizationEntityMapper.toDomain(savedEntity);
    }

    /**
     * ID로 Organization 조회
     *
     * <p>소프트 삭제된 Organization은 조회되지 않습니다.</p>
     *
     * @param id 조회할 Organization ID
     * @return Organization Domain (존재하지 않거나 삭제된 경우 {@code Optional.empty()})
     * @throws IllegalArgumentException id가 null인 경우
     */
    @Override
    public Optional<Organization> findById(OrganizationId id) {
        if (id == null) {
            throw new IllegalArgumentException("OrganizationId must not be null");
        }

        Long idValue = id.value();

        return organizationJpaRepository.findByIdAndDeletedIsFalse(idValue)
            .map(OrganizationEntityMapper::toDomain);
    }

    /**
     * Tenant ID로 Organization 목록 조회
     *
     * <p>특정 Tenant에 속한 모든 Organization을 조회합니다.
     * 소프트 삭제된 Organization은 제외됩니다.
     * 반환 순서는 생성일시(createdAt) 오름차순입니다.</p>
     *
     * <p><strong>String FK 전략:</strong></p>
     * <ul>
     *   <li>Tenant 객체 참조가 아닌 String tenantId 사용 (Tenant PK 타입과 일치)</li>
     * </ul>
     *
     * @param tenantId 조회할 Tenant ID (String - Tenant PK 타입과 일치)
     * @return Organization 목록 (존재하지 않으면 빈 리스트)
     * @throws IllegalArgumentException tenantId가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public List<Organization> findByTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }

        return organizationJpaRepository.findByTenantIdAndDeletedIsFalseOrderByCreatedAtAsc(tenantId)
            .stream()
            .map(OrganizationEntityMapper::toDomain)
            .toList();
    }

    /**
     * Tenant ID와 조직 코드로 Organization 조회
     *
     * <p>특정 Tenant 내에서 조직 코드로 Organization을 찾습니다.
     * 조직 코드는 Tenant 내에서 유니크하므로 최대 1건만 반환됩니다.</p>
     *
     * <p><strong>사용 예:</strong></p>
     * <ul>
     *   <li>조직 코드 기반 Organization 조회</li>
     * </ul>
     *
     * @param tenantId 조회할 Tenant ID (String - Tenant PK 타입과 일치)
     * @param orgCode 조회할 조직 코드
     * @return Organization (존재하지 않거나 삭제된 경우 {@code Optional.empty()})
     * @throws IllegalArgumentException tenantId가 null이거나 빈 문자열이거나 orgCode가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public Optional<Organization> findByTenantIdAndOrgCode(String tenantId, OrgCode orgCode) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }
        if (orgCode == null) {
            throw new IllegalArgumentException("OrgCode must not be null");
        }

        String orgCodeValue = orgCode.getValue();

        return organizationJpaRepository.findByTenantIdAndOrgCodeAndDeletedIsFalse(tenantId, orgCodeValue)
            .map(OrganizationEntityMapper::toDomain);
    }

    /**
     * Tenant ID와 조직 코드 중복 확인
     *
     * <p>특정 Tenant 내에서 조직 코드가 이미 사용 중인지 확인합니다.
     * 소프트 삭제된 Organization은 제외됩니다.</p>
     *
     * <p><strong>사용 예:</strong></p>
     * <ul>
     *   <li>Organization 생성 시 조직 코드 중복 검증</li>
     * </ul>
     *
     * @param tenantId 확인할 Tenant ID (String - Tenant PK 타입과 일치)
     * @param orgCode 확인할 조직 코드
     * @return 존재하면 {@code true}, 없으면 {@code false}
     * @throws IllegalArgumentException tenantId가 null이거나 빈 문자열이거나 orgCode가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public boolean existsByTenantIdAndOrgCode(String tenantId, OrgCode orgCode) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }
        if (orgCode == null) {
            throw new IllegalArgumentException("OrgCode must not be null");
        }

        String orgCodeValue = orgCode.getValue();

        return organizationJpaRepository.existsByTenantIdAndOrgCodeAndDeletedIsFalse(tenantId, orgCodeValue);
    }

    /**
     * ID로 Organization 삭제 (Hard Delete)
     *
     * <p><strong>주의</strong>: 물리적 삭제입니다.
     * 일반적으로 소프트 삭제({@link Organization#softDelete()})를 권장합니다.</p>
     *
     * @param id 삭제할 Organization ID
     * @throws IllegalArgumentException id가 null인 경우
     */
    @Override
    public void deleteById(OrganizationId id) {
        if (id == null) {
            throw new IllegalArgumentException("OrganizationId must not be null");
        }

        Long idValue = id.value();

        organizationJpaRepository.deleteById(idValue);
    }

    /**
     * Tenant ID로 Organization 개수 조회
     *
     * <p>특정 Tenant에 속한 활성 Organization의 개수를 반환합니다.
     * 소프트 삭제된 Organization은 제외됩니다.</p>
     *
     * <p><strong>사용 예:</strong></p>
     * <ul>
     *   <li>통계</li>
     *   <li>Tenant별 Organization 제한 확인</li>
     * </ul>
     *
     * @param tenantId 확인할 Tenant ID (String - Tenant PK 타입과 일치)
     * @return 활성 Organization 개수
     * @throws IllegalArgumentException tenantId가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-22
     */
    @Override
    public long countByTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be null or blank");
        }

        return organizationJpaRepository.countByTenantIdAndDeletedIsFalse(tenantId);
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.entity.TenantJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.mapper.TenantEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.tenant.repository.TenantJpaRepository;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Tenant Persistence Adapter (Hexagonal Architecture - Driven Adapter)
 *
 * <p><strong>역할</strong>: Application Layer의 {@link TenantRepositoryPort}를 구현하여
 * 실제 MySQL 영속성 작업을 수행합니다.</p>
 *
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/tenant/adapter/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ {@code @Component} 어노테이션 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code TenantRepositoryPort} 인터페이스 구현 (DIP)</li>
 *   <li>✅ Mapper로 Domain ↔ Entity 변환</li>
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
 * @see TenantRepositoryPort Application Layer Port
 * @see TenantJpaRepository Spring Data JPA Repository
 * @see TenantEntityMapper Domain ↔ Entity Mapper
 * @since 1.0.0
 */
@Component
public class TenantPersistenceAdapter implements TenantRepositoryPort {

    private final TenantJpaRepository tenantJpaRepository;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * @param tenantJpaRepository Spring Data JPA Repository
     */
    public TenantPersistenceAdapter(TenantJpaRepository tenantJpaRepository) {
        this.tenantJpaRepository = tenantJpaRepository;
    }

    /**
     * Tenant 저장 (생성 또는 수정)
     *
     * <p>Domain {@code Tenant}를 JPA Entity로 변환한 후 저장하고,
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
     * @param tenant 저장할 Tenant Domain
     * @return 저장된 Tenant Domain
     * @throws IllegalArgumentException tenant가 null인 경우
     */
    @Override
    public Tenant save(Tenant tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant must not be null");
        }

        // Domain → Entity
        TenantJpaEntity entity = TenantEntityMapper.toEntity(tenant);

        // JPA 저장
        TenantJpaEntity savedEntity = tenantJpaRepository.save(entity);

        // Entity → Domain
        return TenantEntityMapper.toDomain(savedEntity);
    }

    /**
     * ID로 Tenant 조회
     *
     * <p>소프트 삭제된 Tenant는 조회되지 않습니다.</p>
     *
     * @param id 조회할 Tenant ID
     * @return Tenant Domain (존재하지 않거나 삭제된 경우 {@code Optional.empty()})
     * @throws IllegalArgumentException id가 null인 경우
     */
    @Override
    public Optional<Tenant> findById(TenantId id) {
        if (id == null) {
            throw new IllegalArgumentException("TenantId must not be null");
        }

        String idValue = id.value();

        return tenantJpaRepository.findByIdAndDeletedIsFalse(idValue)
            .map(TenantEntityMapper::toDomain);
    }

    /**
     * 모든 활성 Tenant 조회
     *
     * <p>소프트 삭제되지 않은 모든 Tenant를 조회합니다.
     * 반환 순서는 생성일시(createdAt) 오름차순입니다.</p>
     *
     * @return 활성 Tenant Domain 목록 (빈 리스트 가능)
     */
    @Override
    public List<Tenant> findAll() {
        return tenantJpaRepository.findByDeletedIsFalseOrderByCreatedAtAsc()
            .stream()
            .map(TenantEntityMapper::toDomain)
            .toList();
    }

    /**
     * Tenant 이름 중복 확인
     *
     * <p>소프트 삭제된 Tenant는 제외됩니다.</p>
     *
     * @param name 확인할 Tenant 이름
     * @return 존재하면 {@code true}, 없으면 {@code false}
     * @throws IllegalArgumentException name이 null인 경우
     */
    @Override
    public boolean existsByName(TenantName name) {
        if (name == null) {
            throw new IllegalArgumentException("TenantName must not be null");
        }

        String nameValue = name.getValue();

        return tenantJpaRepository.existsByNameAndDeletedIsFalse(nameValue);
    }

    /**
     * ID로 Tenant 삭제 (Hard Delete)
     *
     * <p><strong>주의</strong>: 물리적 삭제입니다.
     * 일반적으로 소프트 삭제({@link Tenant#softDelete()})를 권장합니다.</p>
     *
     * @param id 삭제할 Tenant ID
     * @throws IllegalArgumentException id가 null인 경우
     */
    @Override
    public void deleteById(TenantId id) {
        if (id == null) {
            throw new IllegalArgumentException("TenantId must not be null");
        }

        String idValue = id.value();

        tenantJpaRepository.deleteById(idValue);
    }

    /**
     * 활성 Tenant 개수 조회
     *
     * <p>소프트 삭제되지 않은 Tenant의 총 개수를 반환합니다.</p>
     *
     * @return 활성 Tenant 개수
     */
    @Override
    public long count() {
        return tenantJpaRepository.countByDeletedIsFalse();
    }
}

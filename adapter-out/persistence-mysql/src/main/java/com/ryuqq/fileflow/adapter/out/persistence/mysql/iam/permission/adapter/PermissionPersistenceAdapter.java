package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.PermissionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.mapper.PermissionEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository.PermissionJpaRepository;
import com.ryuqq.fileflow.application.iam.permission.port.out.PermissionRepositoryPort;
import com.ryuqq.fileflow.domain.iam.permission.Permission;
import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Permission Persistence Adapter (Hexagonal Architecture - Driven Adapter)
 *
 * <p><strong>역할</strong>: Application Layer의 {@link PermissionRepositoryPort}를 구현하여
 * 실제 MySQL 영속성 작업을 수행합니다.</p>
 *
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/adapter/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ {@code @Component} 어노테이션 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code PermissionRepositoryPort} 인터페이스 구현 (DIP)</li>
 *   <li>✅ Mapper로 Domain ↔ Entity 변환</li>
 *   <li>✅ String PK 전략 (Code가 Primary Key)</li>
 *   <li>❌ {@code @Repository} 사용 금지 ({@code @Component} 사용)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 * </ul>
 *
 * <h3>주의사항</h3>
 * <p>Permission은 Code를 Primary Key로 사용하며, RolePermission 연결 테이블을 통해 Role과 연결됩니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Component
public class PermissionPersistenceAdapter implements PermissionRepositoryPort {

    private final PermissionJpaRepository permissionJpaRepository;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * @param permissionJpaRepository Permission Repository
     */
    public PermissionPersistenceAdapter(PermissionJpaRepository permissionJpaRepository) {
        this.permissionJpaRepository = permissionJpaRepository;
    }

    /**
     * Permission 저장 (생성 또는 수정)
     *
     * <p>Domain {@code Permission}을 JPA Entity로 변환한 후 저장합니다.</p>
     *
     * @param permission 저장할 Permission Domain
     * @return 저장된 Permission Domain
     * @throws IllegalArgumentException permission이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public Permission save(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission must not be null");
        }

        // Domain → Entity
        PermissionJpaEntity entity = PermissionEntityMapper.toEntity(permission);

        // JPA 저장
        PermissionJpaEntity savedEntity = permissionJpaRepository.save(entity);

        // Entity → Domain
        return PermissionEntityMapper.toDomain(savedEntity);
    }

    /**
     * Permission Code로 조회
     *
     * <p>Permission의 Primary Key가 Code이므로 Code로 조회합니다.</p>
     *
     * @param code Permission Code
     * @return Permission Domain (존재하지 않으면 {@code Optional.empty()})
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public Optional<Permission> findByCode(PermissionCode code) {
        if (code == null) {
            throw new IllegalArgumentException("PermissionCode must not be null");
        }

        String codeValue = code.getValue();

        return permissionJpaRepository.findByCodeAndDeletedAtIsNull(codeValue)
            .map(PermissionEntityMapper::toDomain);
    }

    /**
     * 모든 Permission 조회 (삭제되지 않은 Permission만)
     *
     * <p>시스템에 정의된 모든 Permission을 조회합니다 (deleted=false).</p>
     *
     * @return Permission Domain 목록 (빈 리스트 가능)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public List<Permission> findAll() {
        return permissionJpaRepository.findAll()
            .stream()
            .filter(entity -> !entity.isDeleted())
            .map(PermissionEntityMapper::toDomain)
            .toList();
    }

    /**
     * Permission Code 존재 여부 확인 (삭제되지 않은 Permission만)
     *
     * <p>Permission 생성 전 중복 확인을 위해 사용합니다 (deleted=false).</p>
     *
     * @param code Permission Code
     * @return 존재 여부
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public boolean existsByCode(PermissionCode code) {
        if (code == null) {
            throw new IllegalArgumentException("PermissionCode must not be null");
        }

        String codeValue = code.getValue();

        return permissionJpaRepository.existsByCodeAndDeletedAtIsNull(codeValue);
    }

    /**
     * Permission Code로 삭제 (Hard Delete)
     *
     * <p><strong>주의</strong>: 물리적 삭제입니다.</p>
     *
     * @param code 삭제할 Permission Code
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public void deleteByCode(PermissionCode code) {
        if (code == null) {
            throw new IllegalArgumentException("PermissionCode must not be null");
        }

        String codeValue = code.getValue();

        permissionJpaRepository.deleteById(codeValue);
    }
}

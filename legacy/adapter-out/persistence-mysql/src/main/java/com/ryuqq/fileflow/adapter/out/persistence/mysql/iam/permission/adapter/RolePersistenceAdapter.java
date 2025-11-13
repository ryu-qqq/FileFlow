package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.dto.GrantReadModel;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RoleJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RolePermissionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.mapper.RoleEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository.GrantQueryRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository.RoleJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository.RolePermissionJpaRepository;
import com.ryuqq.fileflow.application.iam.permission.port.out.RoleRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.permission.Grant;
import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;
import com.ryuqq.fileflow.domain.iam.permission.Role;
import com.ryuqq.fileflow.domain.iam.permission.RoleCode;
import com.ryuqq.fileflow.domain.iam.permission.Scope;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContextId;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Role Persistence Adapter (Hexagonal Architecture - Driven Adapter)
 *
 * <p><strong>역할</strong>: Application Layer의 {@link RoleRepositoryPort}를 구현하여
 * 실제 MySQL 영속성 작업을 수행합니다.</p>
 *
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/iam/permission/adapter/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ {@code @Component} 어노테이션 사용 (Spring Bean 등록)</li>
 *   <li>✅ {@code RoleRepositoryPort} 인터페이스 구현 (DIP)</li>
 *   <li>✅ Mapper로 Domain ↔ Entity 변환</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ String PK 전략 (Code가 Primary Key)</li>
 *   <li>✅ 복잡한 쿼리 최적화 (buildEffectiveGrants)</li>
 *   <li>❌ {@code @Repository} 사용 금지 ({@code @Component} 사용)</li>
 *   <li>❌ {@code @Transactional} 사용 금지 (Application Layer에서만)</li>
 * </ul>
 *
 * <h3>Long FK 전략</h3>
 * <p>Role과 Permission은 N:M 관계로, RolePermission 연결 테이블을 통해 관리됩니다:</p>
 * <ul>
 *   <li>1. Role 저장 → RoleJpaEntity 저장</li>
 *   <li>2. Permission 연결 → RolePermissionJpaEntity 별도 저장</li>
 *   <li>3. 조회 시 → Role 조회 + RolePermission 별도 조회 → Aggregate 재구성</li>
 * </ul>
 *
 * <h3>buildEffectiveGrants() 최적화</h3>
 * <p>가장 중요한 메서드로, 4-table JOIN 쿼리를 통해 사용자의 모든 권한을 조회합니다:</p>
 * <pre>
 * SELECT DISTINCT rp.permission_code, p.default_scope, r.code as role_code
 * FROM user_role_mappings urm
 * JOIN roles r ON urm.role_code = r.code
 * JOIN role_permissions rp ON r.code = rp.role_code
 * JOIN permissions p ON rp.permission_code = p.code
 * WHERE urm.user_context_id = ?
 *   AND urm.tenant_id = ?
 *   AND urm.organization_id = ?
 *   AND r.deleted = false
 *   AND p.deleted = false
 * </pre>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Component
public class RolePersistenceAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository roleJpaRepository;
    private final RolePermissionJpaRepository rolePermissionJpaRepository;
    private final GrantQueryRepository grantQueryRepository;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * @param roleJpaRepository Role Repository
     * @param rolePermissionJpaRepository RolePermission Repository
     * @param grantQueryRepository Grant Query Repository (QueryDSL 기반 CQRS Read Model)
     */
    public RolePersistenceAdapter(
        RoleJpaRepository roleJpaRepository,
        RolePermissionJpaRepository rolePermissionJpaRepository,
        GrantQueryRepository grantQueryRepository
    ) {
        this.roleJpaRepository = roleJpaRepository;
        this.rolePermissionJpaRepository = rolePermissionJpaRepository;
        this.grantQueryRepository = grantQueryRepository;
    }

    /**
     * Role 저장 (생성 또는 수정)
     *
     * <p>Domain {@code Role}을 JPA Entity로 변환한 후 저장합니다.</p>
     * <p><strong>주의</strong>: RolePermission은 별도로 저장해야 합니다 (Long FK 전략).</p>
     *
     * <h4>처리 흐름</h4>
     * <ol>
     *   <li>Domain → Entity 변환 (Role만)</li>
     *   <li>Role 저장</li>
     *   <li>기존 RolePermission 삭제 (업데이트 시)</li>
     *   <li>새로운 RolePermission 저장</li>
     *   <li>Entity + RolePermission → Domain 변환</li>
     *   <li>Domain 반환</li>
     * </ol>
     *
     * @param role 저장할 Role Domain
     * @return 저장된 Role Domain (PermissionCode Set 포함)
     * @throws IllegalArgumentException role이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public Role save(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }

        // 1. Domain → Entity 변환 (Role만)
        RoleJpaEntity entity = RoleEntityMapper.toEntity(role);

        // 2. Role 저장
        RoleJpaEntity savedEntity = roleJpaRepository.save(entity);
        String roleCode = savedEntity.getCode();

        // 3. 기존 RolePermission 삭제 (업데이트 시 기존 데이터 제거)
        List<RolePermissionJpaEntity> existingRolePermissions =
            rolePermissionJpaRepository.findAllByRoleCode(roleCode);
        if (!existingRolePermissions.isEmpty()) {
            rolePermissionJpaRepository.deleteAll(existingRolePermissions);
        }

        // 4. 새로운 RolePermission 저장
        Set<PermissionCode> permissionCodes = role.getPermissionCodes();
        List<RolePermissionJpaEntity> rolePermissionEntities =
            RoleEntityMapper.toRolePermissionEntities(roleCode, permissionCodes);
        List<RolePermissionJpaEntity> savedRolePermissionEntities =
            rolePermissionJpaRepository.saveAll(rolePermissionEntities);

        // 5. Entity + RolePermission → Domain 변환
        return RoleEntityMapper.toDomain(savedEntity, savedRolePermissionEntities);
    }

    /**
     * Role Code로 조회
     *
     * <p>Role과 RolePermission을 별도로 조회한 후 Aggregate로 재구성합니다.</p>
     *
     * @param code 조회할 Role Code
     * @return Role Domain (PermissionCode Set 포함, 존재하지 않으면 {@code Optional.empty()})
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public Optional<Role> findByCode(RoleCode code) {
        if (code == null) {
            throw new IllegalArgumentException("RoleCode must not be null");
        }

        String codeValue = code.getValue();

        // Role 조회
        Optional<RoleJpaEntity> entityOptional = roleJpaRepository.findByCode(codeValue);
        if (entityOptional.isEmpty()) {
            return Optional.empty();
        }

        RoleJpaEntity entity = entityOptional.get();

        // RolePermission 별도 조회
        List<RolePermissionJpaEntity> rolePermissionEntities =
            rolePermissionJpaRepository.findAllByRoleCode(codeValue);

        // Aggregate 재구성
        return Optional.of(RoleEntityMapper.toDomain(entity, rolePermissionEntities));
    }

    /**
     * 모든 Role 조회
     *
     * <p>시스템에 정의된 모든 Role을 조회합니다.</p>
     *
     * @return Role Domain 목록 (PermissionCode Set 포함, 빈 리스트 가능)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public List<Role> findAll() {
        return roleJpaRepository.findAll()
            .stream()
            .map(entity -> {
                String roleCode = entity.getCode();
                List<RolePermissionJpaEntity> rolePermissionEntities =
                    rolePermissionJpaRepository.findAllByRoleCode(roleCode);
                return RoleEntityMapper.toDomain(entity, rolePermissionEntities);
            })
            .toList();
    }

    /**
     * Role Code 존재 여부 확인
     *
     * <p>Role 생성 전 중복 확인을 위해 사용합니다.</p>
     *
     * @param code Role Code
     * @return 존재 여부
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public boolean existsByCode(RoleCode code) {
        if (code == null) {
            throw new IllegalArgumentException("RoleCode must not be null");
        }

        String codeValue = code.getValue();

        return roleJpaRepository.existsByCode(codeValue);
    }

    /**
     * Role Code로 삭제 (Hard Delete)
     *
     * <p><strong>주의</strong>: 물리적 삭제입니다.</p>
     * <p>RolePermission도 함께 삭제됩니다.</p>
     *
     * @param code 삭제할 Role Code
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public void deleteByCode(RoleCode code) {
        if (code == null) {
            throw new IllegalArgumentException("RoleCode must not be null");
        }

        String codeValue = code.getValue();

        // RolePermission 먼저 삭제
        List<RolePermissionJpaEntity> rolePermissionEntities =
            rolePermissionJpaRepository.findAllByRoleCode(codeValue);
        if (!rolePermissionEntities.isEmpty()) {
            rolePermissionJpaRepository.deleteAll(rolePermissionEntities);
        }

        // Role 삭제
        roleJpaRepository.deleteById(codeValue);
    }

    /**
     * 사용자의 유효 권한 Grant 목록 조회 (buildEffectiveGrants)
     *
     * <p><strong>핵심 메서드</strong>: UserContext + Tenant + Organization 컨텍스트에서
     * 사용자가 가진 모든 권한을 Grant Set으로 반환합니다.</p>
     *
     * <h4>CQRS 쿼리 전략</h4>
     * <p>QueryDSL 기반 4-table JOIN을 통해 N+1 문제 없이 한 번의 쿼리로 모든 권한을 조회합니다:</p>
     * <ol>
     *   <li>UserRoleMapping → Role → RolePermission → Permission 4-table INNER JOIN</li>
     *   <li>DTO Projection으로 필요한 컬럼만 조회 (roleCode, permissionCode, defaultScope)</li>
     *   <li>삭제된 Role/Permission 자동 필터링 (INNER JOIN + deleted = false 조건)</li>
     *   <li>GrantReadModel → Grant Domain 변환</li>
     * </ol>
     *
     * <h4>성능 최적화</h4>
     * <ul>
     *   <li>✅ O(2N + M) → O(1): 여러 쿼리 → 단일 쿼리</li>
     *   <li>✅ N+1 문제 해결: 모든 데이터를 한 번에 조회</li>
     *   <li>✅ DTO Projection: 필요한 컬럼만 SELECT</li>
     *   <li>✅ INNER JOIN: 삭제된 데이터 자동 필터링</li>
     * </ul>
     *
     * @param userContextId UserContext ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @return 유효 권한 Grant Set (빈 Set 가능)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    @Override
    public Set<Grant> buildEffectiveGrants(
        UserContextId userContextId,
        TenantId tenantId,
        OrganizationId organizationId
    ) {
        if (userContextId == null || tenantId == null || organizationId == null) {
            throw new IllegalArgumentException(
                "UserContextId, TenantId, OrganizationId must not be null"
            );
        }

        // Value Object → 원시 타입 추출
        Long userContextIdValue = userContextId.value();
        Long tenantIdValue = tenantId.value();  // TenantId는 Long AUTO_INCREMENT 그대로 사용
        Long organizationIdValue = organizationId.value();

        // QueryDSL 기반 4-table JOIN (CQRS Read Model)
        List<GrantReadModel> grantReadModels = grantQueryRepository.findEffectiveGrants(
            userContextIdValue,
            tenantIdValue,
            organizationIdValue
        );

        // GrantReadModel → Grant Domain 변환
        Set<Grant> grants = new HashSet<>();
        for (GrantReadModel readModel : grantReadModels) {
            String roleCode = readModel.getRoleCode();
            String permissionCode = readModel.getPermissionCode();
            Scope scope = Scope.valueOf(readModel.getDefaultScope());

            Grant grant = Grant.withoutCondition(roleCode, permissionCode, scope);
            grants.add(grant);
        }

        return grants;
    }
}

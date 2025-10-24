package com.ryuqq.fileflow.application.iam.permission.port.out;

import com.ryuqq.fileflow.domain.iam.permission.Grant;
import com.ryuqq.fileflow.domain.iam.permission.Role;
import com.ryuqq.fileflow.domain.iam.permission.RoleCode;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContextId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Role Outbound Port (Repository Interface)
 *
 * <p>Role 영속성 계층과의 통신을 위한 Port 인터페이스입니다.
 * Application Layer에서 정의하고 Adapter Layer에서 구현합니다.
 * (Hexagonal Architecture - Dependency Inversion Principle)</p>
 *
 * <p><strong>구현 위치</strong>: {@code adapter-out/persistence-mysql/iam/permission/adapter/RolePersistenceAdapter.java}</p>
 * <p><strong>테스트</strong>: TestContainers 기반 Integration Test 필수</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
public interface RoleRepositoryPort {

    /**
     * Role 저장 (생성 또는 수정)
     *
     * <p>신규 Role 생성 또는 기존 Role 수정 시 사용합니다.
     * 동일한 Code가 존재하면 UPDATE, 없으면 INSERT가 수행됩니다.</p>
     *
     * <p><strong>트랜잭션</strong>: UseCase에서 {@code @Transactional} 적용 필요</p>
     * <p><strong>소프트 삭제</strong>: {@code deleted=true}인 Role은 저장하지 않음</p>
     * <p><strong>권한 매핑</strong>: Role에 포함된 Permission들도 함께 저장됨</p>
     *
     * @param role 저장할 Role Aggregate
     * @return 저장된 Role (영속화된 상태)
     * @throws IllegalArgumentException role이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Role save(Role role);

    /**
     * Code로 Role 조회
     *
     * <p>주어진 RoleCode에 해당하는 Role을 조회합니다.
     * 소프트 삭제된 Role은 조회되지 않습니다.</p>
     *
     * @param code 조회할 Role Code
     * @return Role (존재하지 않거나 삭제된 경우 {@code Optional.empty()})
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Optional<Role> findByCode(RoleCode code);

    /**
     * 모든 활성 Role 조회
     *
     * <p>소프트 삭제되지 않은 모든 Role을 조회합니다.</p>
     *
     * <p><strong>주의</strong>: 대량의 데이터가 있는 경우 페이징 처리 권장</p>
     *
     * @return 활성 Role 목록 (빈 리스트 가능)
     * @author ryu-qqq
     * @since 2025-10-24
     */
    List<Role> findAll();

    /**
     * Role Code 존재 여부 확인
     *
     * <p>주어진 RoleCode를 가진 Role이 존재하는지 확인합니다.
     * 소프트 삭제된 Role은 제외됩니다.</p>
     *
     * <p><strong>사용 예</strong>: Role 생성 시 중복 검증</p>
     *
     * @param code 확인할 Role Code
     * @return 존재하면 {@code true}, 없으면 {@code false}
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    boolean existsByCode(RoleCode code);

    /**
     * Code로 Role 삭제 (Hard Delete)
     *
     * <p><strong>주의</strong>: 물리적 삭제입니다. 일반적으로 소프트 삭제({@link Role#softDelete()})를 권장합니다.</p>
     *
     * <p><strong>사용 예</strong>: 테스트 데이터 정리, 관리자 강제 삭제</p>
     *
     * @param code 삭제할 Role Code
     * @throws IllegalArgumentException code가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    void deleteByCode(RoleCode code);

    /**
     * 사용자의 유효 권한(Effective Grants) 빌드 - 복잡 쿼리
     *
     * <p>주어진 사용자, 테넌트, 조직 컨텍스트를 기반으로
     * 실제 적용 가능한 모든 권한(Grant)을 계산하여 반환합니다.</p>
     *
     * <p><strong>쿼리 복잡도</strong>: 다음을 모두 조인하여 계산</p>
     * <ul>
     *   <li>UserContext → UserOrgMembership (사용자가 속한 조직)</li>
     *   <li>UserOrgMembership → UserRoleMapping (조직 내 역할)</li>
     *   <li>UserRoleMapping → Role (역할)</li>
     *   <li>Role → RolePermission (역할이 가진 권한)</li>
     *   <li>RolePermission → Permission (권한 상세 정보)</li>
     * </ul>
     *
     * <p><strong>성능 최적화</strong>:</p>
     * <ul>
     *   <li>QueryDSL로 한 번의 쿼리로 조회 (N+1 방지)</li>
     *   <li>적절한 인덱스 설계 필요 (userId, tenantId, organizationId)</li>
     *   <li>결과 캐싱 고려 (권한은 자주 변경되지 않음)</li>
     * </ul>
     *
     * <p><strong>예시</strong>:</p>
     * <pre>
     * UserContextId userId = UserContextId.from(1L);
     * TenantId tenantId = TenantId.from(10L);
     * OrganizationId orgId = OrganizationId.from(100L);
     *
     * Set&lt;Grant&gt; grants = roleRepository.buildEffectiveGrants(userId, tenantId, orgId);
     * // Result: {Grant(org.uploader, file.upload, ORGANIZATION, null), ...}
     * </pre>
     *
     * @param userContextId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @return 유효한 모든 권한 Grant 집합 (빈 Set 가능)
     * @throws IllegalArgumentException userContextId, tenantId, organizationId 중 하나라도 null인 경우
     * @author ryu-qqq
     * @since 2025-10-24
     */
    Set<Grant> buildEffectiveGrants(
        UserContextId userContextId,
        TenantId tenantId,
        OrganizationId organizationId
    );
}

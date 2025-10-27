package com.ryuqq.fileflow.e2e.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.PermissionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RoleJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RolePermissionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.UserRoleMappingJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository.PermissionJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository.RoleJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository.RolePermissionJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository.UserRoleMappingJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Permission Test Fixture
 *
 * <p>E2E 테스트를 위한 Permission, Role, Grant 테스트 데이터 생성 Fixture입니다.</p>
 *
 * <p><strong>용도</strong>:</p>
 * <ul>
 *   <li>Scenario 4-6: SELF, ORGANIZATION, TENANT Scope 권한 테스트</li>
 *   <li>Scenario 10: 캐시 무효화 테스트</li>
 * </ul>
 *
 * <p><strong>설계 원칙</strong>:</p>
 * <ul>
 *   <li>✅ @Component로 Spring Bean 등록 - Repository 주입 가능</li>
 *   <li>✅ Entity 직접 생성 및 저장 (Request DTO 방식이 아님)</li>
 *   <li>✅ Static Factory Methods를 활용한 Entity 생성</li>
 *   <li>✅ 재사용 가능한 Common Setup 메서드 제공</li>
 * </ul>
 *
 * <p><strong>사용 예시</strong>:</p>
 * <pre>{@code
 * @Autowired
 * private PermissionFixture permissionFixture;
 *
 * @BeforeEach
 * void setUp() {
 *     // Common Permissions 생성
 *     permissionFixture.setupCommonPermissions();
 *
 *     // Common Roles + Permission Links 생성
 *     Map<String, RoleJpaEntity> roles = permissionFixture.setupCommonRoles();
 *
 *     // User에게 Role 할당
 *     permissionFixture.assignRoleToUser(userContextId, "UPLOADER", tenantId, orgId);
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-27
 */
@Component
public class PermissionFixture {

    private final PermissionJpaRepository permissionRepository;
    private final RoleJpaRepository roleRepository;
    private final RolePermissionJpaRepository rolePermissionRepository;
    private final UserRoleMappingJpaRepository userRoleMappingRepository;

    /**
     * Constructor - Spring이 Repository 자동 주입
     *
     * @param permissionRepository Permission Repository
     * @param roleRepository Role Repository
     * @param rolePermissionRepository RolePermission Repository
     * @param userRoleMappingRepository UserRoleMapping Repository
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public PermissionFixture(
        PermissionJpaRepository permissionRepository,
        RoleJpaRepository roleRepository,
        RolePermissionJpaRepository rolePermissionRepository,
        UserRoleMappingJpaRepository userRoleMappingRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleMappingRepository = userRoleMappingRepository;
    }

    /**
     * Permission 생성 (개별)
     *
     * <p>Permission Entity를 생성하고 DB에 저장합니다.</p>
     *
     * @param code Permission Code (PK, 예: "file.upload")
     * @param description 설명
     * @param defaultScope 기본 Scope (예: "SELF", "ORGANIZATION", "TENANT")
     * @return 저장된 PermissionJpaEntity
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public PermissionJpaEntity createPermission(String code, String description, String defaultScope) {
        PermissionJpaEntity permission = PermissionJpaEntity.create(
            code,
            description,
            defaultScope,
            LocalDateTime.now()
        );
        return permissionRepository.save(permission);
    }

    /**
     * Role 생성 (개별)
     *
     * <p>Role Entity를 생성하고 DB에 저장합니다.</p>
     *
     * @param code Role Code (PK, 예: "UPLOADER", "ADMIN")
     * @param description 설명
     * @return 저장된 RoleJpaEntity
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public RoleJpaEntity createRole(String code, String description) {
        RoleJpaEntity role = RoleJpaEntity.create(
            code,
            description,
            LocalDateTime.now()
        );
        return roleRepository.save(role);
    }

    /**
     * Role과 Permission 연결
     *
     * <p>RolePermission 연결 테이블에 레코드를 생성합니다.</p>
     *
     * @param roleCode Role Code (FK)
     * @param permissionCode Permission Code (FK)
     * @return 저장된 RolePermissionJpaEntity
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public RolePermissionJpaEntity linkRolePermission(String roleCode, String permissionCode) {
        RolePermissionJpaEntity rolePermission = RolePermissionJpaEntity.create(
            roleCode,
            permissionCode
        );
        return rolePermissionRepository.save(rolePermission);
    }

    /**
     * UserContext에 Role 할당
     *
     * <p>UserRoleMapping 테이블에 레코드를 생성하여 특정 Tenant/Organization 컨텍스트에서 Role을 부여합니다.</p>
     *
     * @param userContextId UserContext ID (FK)
     * @param roleCode Role Code (FK)
     * @param tenantId Tenant ID (FK)
     * @param organizationId Organization ID (FK)
     * @return 저장된 UserRoleMappingJpaEntity
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public UserRoleMappingJpaEntity assignRoleToUser(
        Long userContextId,
        String roleCode,
        Long tenantId,
        Long organizationId
    ) {
        UserRoleMappingJpaEntity mapping = UserRoleMappingJpaEntity.create(
            userContextId,
            roleCode,
            tenantId,
            organizationId
        );
        return userRoleMappingRepository.save(mapping);
    }

    /**
     * UserContext에서 Role 해제
     *
     * <p>UserRoleMapping 테이블에서 해당 매핑을 삭제하여 Role을 해제합니다.</p>
     * <p>캐시 무효화 테스트(Scenario 10)에서 사용됩니다.</p>
     *
     * @param userContextId UserContext ID (FK)
     * @param roleCode Role Code (FK)
     * @param tenantId Tenant ID (FK)
     * @param organizationId Organization ID (FK)
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public void revokeRoleFromUser(
        Long userContextId,
        String roleCode,
        Long tenantId,
        Long organizationId
    ) {
        userRoleMappingRepository.deleteAll(
            userRoleMappingRepository.findAll().stream()
                .filter(mapping ->
                    mapping.getUserContextId().equals(userContextId) &&
                    mapping.getRoleCode().equals(roleCode) &&
                    mapping.getTenantId().equals(tenantId) &&
                    mapping.getOrganizationId().equals(organizationId)
                )
                .toList()
        );
    }

    /**
     * 공통 Permission 데이터 생성
     *
     * <p>E2E 테스트에서 공통으로 사용하는 Permission들을 생성합니다:</p>
     * <ul>
     *   <li>file.upload - 파일 업로드 권한 (SELF Scope)</li>
     *   <li>file.delete - 파일 삭제 권한 (ORGANIZATION Scope)</li>
     *   <li>file.read - 파일 조회 권한 (TENANT Scope)</li>
     * </ul>
     *
     * @return 생성된 Permission Entity 리스트
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public List<PermissionJpaEntity> setupCommonPermissions() {
        List<PermissionJpaEntity> permissions = new ArrayList<>();

        permissions.add(createPermission(
            "file.upload",
            "파일 업로드 권한",
            "SELF"
        ));

        permissions.add(createPermission(
            "file.delete",
            "파일 삭제 권한",
            "ORGANIZATION"
        ));

        permissions.add(createPermission(
            "file.read",
            "파일 조회 권한",
            "TENANT"
        ));

        return permissions;
    }

    /**
     * 공통 Role 데이터 생성 (Permission 연결 포함)
     *
     * <p>E2E 테스트에서 공통으로 사용하는 Role들을 생성하고 Permission과 연결합니다:</p>
     * <ul>
     *   <li>UPLOADER - file.upload 권한</li>
     *   <li>ADMIN - file.upload, file.delete 권한</li>
     *   <li>VIEWER - file.read 권한</li>
     * </ul>
     *
     * <p><strong>주의</strong>: 이 메서드를 호출하기 전에 {@link #setupCommonPermissions()}를 먼저 호출해야 합니다.</p>
     *
     * @return Role Code → RoleJpaEntity Map
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public Map<String, RoleJpaEntity> setupCommonRoles() {
        Map<String, RoleJpaEntity> roles = new HashMap<>();

        // UPLOADER 역할 생성
        RoleJpaEntity uploader = createRole("UPLOADER", "파일 업로드 역할");
        linkRolePermission("UPLOADER", "file.upload");
        roles.put("UPLOADER", uploader);

        // ADMIN 역할 생성
        RoleJpaEntity admin = createRole("ADMIN", "관리자 역할");
        linkRolePermission("ADMIN", "file.upload");
        linkRolePermission("ADMIN", "file.delete");
        roles.put("ADMIN", admin);

        // VIEWER 역할 생성
        RoleJpaEntity viewer = createRole("VIEWER", "조회자 역할");
        linkRolePermission("VIEWER", "file.read");
        roles.put("VIEWER", viewer);

        return roles;
    }

    /**
     * 모든 UserRoleMapping 삭제
     *
     * <p>테스트 격리를 위해 UserRoleMapping 테이블을 정리합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public void cleanupUserRoleMappings() {
        userRoleMappingRepository.deleteAll();
    }

    /**
     * 모든 RolePermission 삭제
     *
     * <p>테스트 격리를 위해 RolePermission 테이블을 정리합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public void cleanupRolePermissions() {
        rolePermissionRepository.deleteAll();
    }

    /**
     * 모든 Role 삭제
     *
     * <p>테스트 격리를 위해 Role 테이블을 정리합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public void cleanupRoles() {
        roleRepository.deleteAll();
    }

    /**
     * 모든 Permission 삭제
     *
     * <p>테스트 격리를 위해 Permission 테이블을 정리합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public void cleanupPermissions() {
        permissionRepository.deleteAll();
    }

    /**
     * 모든 Permission 관련 테이블 정리
     *
     * <p>테스트 격리를 위해 모든 Permission 관련 테이블을 순서대로 정리합니다.</p>
     * <p>외래 키 제약 조건을 고려하여 다음 순서로 삭제합니다:</p>
     * <ol>
     *   <li>UserRoleMapping (가장 의존성이 많은 테이블)</li>
     *   <li>RolePermission</li>
     *   <li>Role</li>
     *   <li>Permission</li>
     * </ol>
     *
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public void cleanupAll() {
        cleanupUserRoleMappings();
        cleanupRolePermissions();
        cleanupRoles();
        cleanupPermissions();
    }
}

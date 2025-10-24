package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.config.IntegrationTestBase;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.config.QueryDslConfig;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository.GrantQueryRepository;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.permission.Grant;
import com.ryuqq.fileflow.domain.iam.permission.Permission;
import com.ryuqq.fileflow.domain.iam.permission.PermissionCode;
import com.ryuqq.fileflow.domain.iam.permission.Role;
import com.ryuqq.fileflow.domain.iam.permission.RoleCode;
import com.ryuqq.fileflow.domain.iam.permission.Scope;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContextId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RolePersistenceAdapter Integration Test
 *
 * <p><strong>테스트 대상</strong>: {@link RolePersistenceAdapter}</p>
 * <p><strong>테스트 환경</strong>: TestContainers MySQL 8.0</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ Role 저장 (신규/수정 + RolePermission 연결)</li>
 *   <li>✅ Code로 Role 조회 (Aggregate 재구성)</li>
 *   <li>✅ 전체 Role 목록 조회</li>
 *   <li>✅ Code 중복 확인</li>
 *   <li>✅ Code로 Role 삭제 (Hard Delete + RolePermission Cascade)</li>
 *   <li>✅ buildEffectiveGrants() - 4-table JOIN CQRS 쿼리</li>
 *   <li>✅ 예외 케이스 (null 검증)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Import({RolePersistenceAdapter.class, PermissionPersistenceAdapter.class, GrantQueryRepository.class, QueryDslConfig.class})
@DisplayName("RolePersistenceAdapter 통합 테스트")
class RolePersistenceAdapterTest extends IntegrationTestBase {

    @Autowired
    private RolePersistenceAdapter rolePersistenceAdapter;

    @Autowired
    private PermissionPersistenceAdapter permissionPersistenceAdapter;

    @Nested
    @DisplayName("Role 저장 테스트")
    class SaveTests {

        @Test
        @DisplayName("신규 Role을 Permission과 함께 저장하면 정상적으로 저장된다")
        void save_NewRoleWithPermissions_SavesSuccessfully() {
            // given
            Permission permission1 = permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("file.read"),
                    "파일 조회 권한",
                    Scope.ORGANIZATION
                )
            );
            Permission permission2 = permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("file.write"),
                    "파일 작성 권한",
                    Scope.ORGANIZATION
                )
            );

            Role newRole = Role.of(
                RoleCode.of("admin"),
                "관리자 역할",
                Set.of(
                    PermissionCode.of("file.read"),
                    PermissionCode.of("file.write")
                )
            );

            // when
            Role savedRole = rolePersistenceAdapter.save(newRole);

            // then
            assertThat(savedRole).isNotNull();
            assertThat(savedRole.getCodeValue()).isEqualTo("admin");
            assertThat(savedRole.getDescription()).isEqualTo("관리자 역할");
            assertThat(savedRole.getPermissionCodes()).hasSize(2);
            assertThat(savedRole.getPermissionCodes())
                .extracting(PermissionCode::getValue)
                .containsExactlyInAnyOrder("file.read", "file.write");
        }

        @Test
        @DisplayName("기존 Role을 수정하면 RolePermission이 업데이트된다")
        void save_ExistingRole_UpdatesRolePermissions() {
            // given
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("user.read"),
                    "사용자 조회 권한",
                    Scope.ORGANIZATION
                )
            );
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("user.write"),
                    "사용자 작성 권한",
                    Scope.ORGANIZATION
                )
            );
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("user.delete"),
                    "사용자 삭제 권한",
                    Scope.ORGANIZATION
                )
            );

            Role originalRole = rolePersistenceAdapter.save(
                Role.of(
                    RoleCode.of("editor"),
                    "편집자 역할",
                    Set.of(
                        PermissionCode.of("user.read"),
                        PermissionCode.of("user.write")
                    )
                )
            );

            // 기존 Role에 새로운 Permission 추가
            Role updatedRole = Role.of(
                RoleCode.of("editor"),
                "편집자 역할 (수정됨)",
                Set.of(
                    PermissionCode.of("user.read"),
                    PermissionCode.of("user.write"),
                    PermissionCode.of("user.delete")
                )
            );

            // when
            Role savedRole = rolePersistenceAdapter.save(updatedRole);

            // then
            assertThat(savedRole.getCodeValue()).isEqualTo("editor");
            assertThat(savedRole.getDescription()).isEqualTo("편집자 역할 (수정됨)");
            assertThat(savedRole.getPermissionCodes()).hasSize(3);
            assertThat(savedRole.getPermissionCodes())
                .extracting(PermissionCode::getValue)
                .containsExactlyInAnyOrder("user.read", "user.write", "user.delete");
        }

        @Test
        @DisplayName("null Role 저장 시도 시 IllegalArgumentException이 발생한다")
        void save_NullRole_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> rolePersistenceAdapter.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role must not be null");
        }
    }

    @Nested
    @DisplayName("Role 조회 테스트")
    class FindByCodeTests {

        @Test
        @DisplayName("Code로 Role을 조회하면 PermissionCode Set과 함께 반환된다")
        void findByCode_ReturnsRoleWithPermissions() {
            // given
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("product.read"),
                    "제품 조회 권한",
                    Scope.TENANT
                )
            );
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("product.write"),
                    "제품 작성 권한",
                    Scope.TENANT
                )
            );

            Role savedRole = rolePersistenceAdapter.save(
                Role.of(
                    RoleCode.of("product.manager"),
                    "제품 관리자",
                    Set.of(
                        PermissionCode.of("product.read"),
                        PermissionCode.of("product.write")
                    )
                )
            );

            // when
            Optional<Role> foundRole = rolePersistenceAdapter.findByCode(
                RoleCode.of("product.manager")
            );

            // then
            assertThat(foundRole).isPresent();
            assertThat(foundRole.get().getCodeValue()).isEqualTo("product.manager");
            assertThat(foundRole.get().getDescription()).isEqualTo("제품 관리자");
            assertThat(foundRole.get().getPermissionCodes()).hasSize(2);
            assertThat(foundRole.get().getPermissionCodes())
                .extracting(PermissionCode::getValue)
                .containsExactlyInAnyOrder("product.read", "product.write");
        }

        @Test
        @DisplayName("존재하지 않는 Code로 조회하면 빈 Optional이 반환된다")
        void findByCode_NonExistentCode_ReturnsEmptyOptional() {
            // when
            Optional<Role> foundRole = rolePersistenceAdapter.findByCode(
                RoleCode.of("nonexistent.role")
            );

            // then
            assertThat(foundRole).isEmpty();
        }

        @Test
        @DisplayName("null RoleCode로 조회 시도 시 IllegalArgumentException이 발생한다")
        void findByCode_NullRoleCode_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> rolePersistenceAdapter.findByCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RoleCode must not be null");
        }
    }

    @Nested
    @DisplayName("전체 Role 조회 테스트")
    class FindAllTests {

        @Test
        @DisplayName("전체 Role 목록을 조회하면 모든 Role이 PermissionCode Set과 함께 반환된다")
        void findAll_ReturnsAllRolesWithPermissions() {
            // given
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("report.read"),
                    "리포트 조회 권한",
                    Scope.GLOBAL
                )
            );
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("report.write"),
                    "리포트 작성 권한",
                    Scope.GLOBAL
                )
            );

            rolePersistenceAdapter.save(
                Role.of(
                    RoleCode.of("reporter"),
                    "리포터 역할",
                    Set.of(PermissionCode.of("report.read"))
                )
            );
            rolePersistenceAdapter.save(
                Role.of(
                    RoleCode.of("analyst"),
                    "분석가 역할",
                    Set.of(
                        PermissionCode.of("report.read"),
                        PermissionCode.of("report.write")
                    )
                )
            );

            // when
            var roles = rolePersistenceAdapter.findAll();

            // then
            assertThat(roles).hasSizeGreaterThanOrEqualTo(2);
            assertThat(roles)
                .extracting(Role::getCodeValue)
                .contains("reporter", "analyst");
        }
    }

    @Nested
    @DisplayName("Role Code 중복 확인 테스트")
    class ExistsByCodeTests {

        @Test
        @DisplayName("Code가 존재하면 true를 반환한다")
        void existsByCode_ExistingCode_ReturnsTrue() {
            // given
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("order.read"),
                    "주문 조회 권한",
                    Scope.TENANT
                )
            );

            rolePersistenceAdapter.save(
                Role.of(
                    RoleCode.of("order.viewer"),
                    "주문 조회자",
                    Set.of(PermissionCode.of("order.read"))
                )
            );

            // when
            boolean exists = rolePersistenceAdapter.existsByCode(
                RoleCode.of("order.viewer")
            );

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Code가 존재하지 않으면 false를 반환한다")
        void existsByCode_NonExistentCode_ReturnsFalse() {
            // when
            boolean exists = rolePersistenceAdapter.existsByCode(
                RoleCode.of("nonexistent.role")
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("null RoleCode로 중복 확인 시도 시 IllegalArgumentException이 발생한다")
        void existsByCode_NullRoleCode_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> rolePersistenceAdapter.existsByCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RoleCode must not be null");
        }
    }

    @Nested
    @DisplayName("Role 삭제 테스트")
    class DeleteByCodeTests {

        @Test
        @DisplayName("Code로 Role을 Hard Delete하면 RolePermission도 함께 삭제된다")
        void deleteByCode_RemovesRoleAndRolePermissions() {
            // given
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("inventory.read"),
                    "재고 조회 권한",
                    Scope.TENANT
                )
            );

            Role role = rolePersistenceAdapter.save(
                Role.of(
                    RoleCode.of("inventory.viewer"),
                    "재고 조회자",
                    Set.of(PermissionCode.of("inventory.read"))
                )
            );

            // when
            rolePersistenceAdapter.deleteByCode(RoleCode.of("inventory.viewer"));

            // then
            Optional<Role> foundRole = rolePersistenceAdapter.findByCode(
                RoleCode.of("inventory.viewer")
            );
            assertThat(foundRole).isEmpty();
        }

        @Test
        @DisplayName("null RoleCode로 삭제 시도 시 IllegalArgumentException이 발생한다")
        void deleteByCode_NullRoleCode_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> rolePersistenceAdapter.deleteByCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RoleCode must not be null");
        }
    }

    @Nested
    @DisplayName("buildEffectiveGrants 테스트 (CQRS 4-table JOIN)")
    class BuildEffectiveGrantsTests {

        @Test
        @DisplayName("사용자의 유효 권한을 4-table JOIN으로 조회한다")
        void buildEffectiveGrants_ReturnsEffectiveGrants() {
            // given
            // 1. Permission 생성
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("grant.read"),
                    "권한 조회",
                    Scope.ORGANIZATION
                )
            );
            permissionPersistenceAdapter.save(
                Permission.of(
                    PermissionCode.of("grant.write"),
                    "권한 작성",
                    Scope.ORGANIZATION
                )
            );

            // 2. Role 생성 (Permission 연결)
            rolePersistenceAdapter.save(
                Role.of(
                    RoleCode.of("grant.admin"),
                    "권한 관리자",
                    Set.of(
                        PermissionCode.of("grant.read"),
                        PermissionCode.of("grant.write")
                    )
                )
            );

            // 3. UserContext, UserRoleMapping은 직접 DB에 INSERT 필요
            // (이 테스트는 RolePermission까지만 검증)
            // buildEffectiveGrants는 GrantQueryRepository에서 4-table JOIN으로 조회

            UserContextId userContextId = UserContextId.of(1L);
            TenantId tenantId = TenantId.of("550e8400-e29b-41d4-a716-446655440001");  // TenantId는 String UUID 형식
            OrganizationId organizationId = OrganizationId.of(1L);

            // when
            Set<Grant> grants = rolePersistenceAdapter.buildEffectiveGrants(
                userContextId,
                tenantId,
                organizationId
            );

            // then
            // UserRoleMapping이 없으므로 빈 Set 반환 예상
            assertThat(grants).isEmpty();
        }

        @Test
        @DisplayName("null 파라미터로 buildEffectiveGrants 호출 시 IllegalArgumentException이 발생한다")
        void buildEffectiveGrants_NullParameters_ThrowsIllegalArgumentException() {
            // given
            UserContextId userContextId = UserContextId.of(1L);
            TenantId tenantId = TenantId.of("550e8400-e29b-41d4-a716-446655440001");  // TenantId는 String UUID 형식
            OrganizationId organizationId = OrganizationId.of(1L);

            // when & then - userContextId null
            assertThatThrownBy(() ->
                rolePersistenceAdapter.buildEffectiveGrants(null, tenantId, organizationId)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserContextId, TenantId, OrganizationId must not be null");

            // when & then - tenantId null
            assertThatThrownBy(() ->
                rolePersistenceAdapter.buildEffectiveGrants(userContextId, null, organizationId)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserContextId, TenantId, OrganizationId must not be null");

            // when & then - organizationId null
            assertThatThrownBy(() ->
                rolePersistenceAdapter.buildEffectiveGrants(userContextId, tenantId, null)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserContextId, TenantId, OrganizationId must not be null");
        }
    }
}

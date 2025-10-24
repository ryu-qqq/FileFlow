package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.config.IntegrationTestBase;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.dto.GrantReadModel;
import org.springframework.context.annotation.Import;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.PermissionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RoleJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.RolePermissionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.permission.entity.UserRoleMappingJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserContextJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.repository.UserContextJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * GrantQueryRepository Integration Test
 *
 * <p><strong>테스트 대상</strong>: {@link GrantQueryRepository}</p>
 * <p><strong>테스트 환경</strong>: TestContainers MySQL 8.0</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ 4-table JOIN 쿼리 정상 동작 확인</li>
 *   <li>✅ UserContext + Tenant + Organization 필터링</li>
 *   <li>✅ 삭제된 Role/Permission 자동 필터링 (INNER JOIN)</li>
 *   <li>✅ DTO Projection 정확성 검증</li>
 *   <li>✅ 다중 Role/Permission 처리</li>
 *   <li>✅ 예외 케이스 (null 검증)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Import(GrantQueryRepository.class)
@DisplayName("GrantQueryRepository 통합 테스트 (QueryDSL 4-table JOIN)")
class GrantQueryRepositoryTest extends IntegrationTestBase {

    @Autowired
    private GrantQueryRepository grantQueryRepository;

    @Autowired
    private UserContextJpaRepository userContextJpaRepository;

    @Autowired
    private PermissionJpaRepository permissionJpaRepository;

    @Autowired
    private RoleJpaRepository roleJpaRepository;

    @Autowired
    private RolePermissionJpaRepository rolePermissionJpaRepository;

    @Autowired
    private UserRoleMappingJpaRepository userRoleMappingJpaRepository;

    @Nested
    @DisplayName("findEffectiveGrants 4-table JOIN 테스트")
    class FindEffectiveGrantsTests {

        @Test
        @DisplayName("4-table JOIN으로 사용자의 유효 권한을 조회한다")
        void findEffectiveGrants_WithCompleteSetup_ReturnsGrants() {
            // given
            // 1. UserContext 생성
            UserContextJpaEntity userContext = UserContextJpaEntity.create(
                "auth0|test-user",
                "test@example.com",
                java.time.LocalDateTime.now()
            );
            UserContextJpaEntity savedUserContext = userContextJpaRepository.save(userContext);
            Long userContextId = savedUserContext.getId();

            // 2. Permission 생성
            PermissionJpaEntity permission1 = PermissionJpaEntity.create(
                "file.read",
                "파일 조회 권한",
                "ORGANIZATION",
                java.time.LocalDateTime.now()
            );
            permissionJpaRepository.save(permission1);

            PermissionJpaEntity permission2 = PermissionJpaEntity.create(
                "file.write",
                "파일 작성 권한",
                "ORGANIZATION",
                java.time.LocalDateTime.now()
            );
            permissionJpaRepository.save(permission2);

            // 3. Role 생성
            RoleJpaEntity role = RoleJpaEntity.create(
                "admin",
                "관리자 역할",
                java.time.LocalDateTime.now()
            );
            roleJpaRepository.save(role);

            // 4. RolePermission 연결
            RolePermissionJpaEntity rolePermission1 = RolePermissionJpaEntity.create(
                "admin",
                "file.read"
            );
            rolePermissionJpaRepository.save(rolePermission1);

            RolePermissionJpaEntity rolePermission2 = RolePermissionJpaEntity.create(
                "admin",
                "file.write"
            );
            rolePermissionJpaRepository.save(rolePermission2);

            // 5. UserRoleMapping 생성
            UserRoleMappingJpaEntity userRoleMapping = UserRoleMappingJpaEntity.create(
                userContextId,
                "admin",
                "550e8400-e29b-41d4-a716-446655440001",
                1L
            );
            userRoleMappingJpaRepository.save(userRoleMapping);

            // when
            List<GrantReadModel> grants = grantQueryRepository.findEffectiveGrants(
                userContextId,
                "550e8400-e29b-41d4-a716-446655440001",
                1L
            );

            // then
            assertThat(grants).hasSize(2);
            assertThat(grants)
                .extracting(GrantReadModel::getPermissionCode)
                .containsExactlyInAnyOrder("file.read", "file.write");
            assertThat(grants)
                .allMatch(g -> g.getRoleCode().equals("admin"));
            assertThat(grants)
                .allMatch(g -> g.getDefaultScope().equals("ORGANIZATION"));
        }

        @Test
        @DisplayName("삭제된 Permission은 조회되지 않는다 (INNER JOIN 필터링)")
        void findEffectiveGrants_DeletedPermission_IsFilteredOut() {
            // given
            // 1. UserContext 생성
            UserContextJpaEntity userContext = UserContextJpaEntity.create(
                "auth0|test-user2",
                "test2@example.com",
                java.time.LocalDateTime.now()
            );
            UserContextJpaEntity savedUserContext = userContextJpaRepository.save(userContext);
            Long userContextId = savedUserContext.getId();

            // 2. Permission 생성 (하나는 삭제 상태)
            PermissionJpaEntity activePermission = PermissionJpaEntity.create(
                "active.permission",
                "활성 권한",
                "TENANT",
                java.time.LocalDateTime.now()
            );
            permissionJpaRepository.save(activePermission);

            PermissionJpaEntity deletedPermission = PermissionJpaEntity.reconstitute(
                "deleted.permission",
                "삭제된 권한",
                "TENANT",
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                true  // deleted = true
            );
            permissionJpaRepository.save(deletedPermission);

            // 3. Role 생성
            RoleJpaEntity role = RoleJpaEntity.create(
                "test.role",
                "테스트 역할",
                java.time.LocalDateTime.now()
            );
            roleJpaRepository.save(role);

            // 4. RolePermission 연결 (활성 + 삭제)
            RolePermissionJpaEntity activeRolePermission = RolePermissionJpaEntity.create(
                "test.role",
                "active.permission"
            );
            rolePermissionJpaRepository.save(activeRolePermission);

            RolePermissionJpaEntity deletedRolePermission = RolePermissionJpaEntity.create(
                "test.role",
                "deleted.permission"
            );
            rolePermissionJpaRepository.save(deletedRolePermission);

            // 5. UserRoleMapping 생성
            UserRoleMappingJpaEntity userRoleMapping = UserRoleMappingJpaEntity.create(
                userContextId,
                "test.role",
                "550e8400-e29b-41d4-a716-446655440002",
                2L
            );
            userRoleMappingJpaRepository.save(userRoleMapping);

            // when
            List<GrantReadModel> grants = grantQueryRepository.findEffectiveGrants(
                userContextId,
                "550e8400-e29b-41d4-a716-446655440002",
                2L
            );

            // then
            assertThat(grants).hasSize(1);
            assertThat(grants.get(0).getPermissionCode()).isEqualTo("active.permission");
            // 삭제된 Permission은 조회되지 않음
            assertThat(grants)
                .extracting(GrantReadModel::getPermissionCode)
                .doesNotContain("deleted.permission");
        }

        @Test
        @DisplayName("삭제된 Role은 조회되지 않는다 (INNER JOIN 필터링)")
        void findEffectiveGrants_DeletedRole_IsFilteredOut() {
            // given
            // 1. UserContext 생성
            UserContextJpaEntity userContext = UserContextJpaEntity.create(
                "auth0|test-user3",
                "test3@example.com",
                java.time.LocalDateTime.now()
            );
            UserContextJpaEntity savedUserContext = userContextJpaRepository.save(userContext);
            Long userContextId = savedUserContext.getId();

            // 2. Permission 생성
            PermissionJpaEntity permission = PermissionJpaEntity.create(
                "test.permission",
                "테스트 권한",
                "GLOBAL",
                java.time.LocalDateTime.now()
            );
            permissionJpaRepository.save(permission);

            // 3. Role 생성 (삭제 상태)
            RoleJpaEntity deletedRole = RoleJpaEntity.reconstitute(
                "deleted.role",
                "삭제된 역할",
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                true  // deleted = true
            );
            roleJpaRepository.save(deletedRole);

            // 4. RolePermission 연결
            RolePermissionJpaEntity rolePermission = RolePermissionJpaEntity.create(
                "deleted.role",
                "test.permission"
            );
            rolePermissionJpaRepository.save(rolePermission);

            // 5. UserRoleMapping 생성
            UserRoleMappingJpaEntity userRoleMapping = UserRoleMappingJpaEntity.create(
                userContextId,
                "deleted.role",
                "550e8400-e29b-41d4-a716-446655440003",
                3L
            );
            userRoleMappingJpaRepository.save(userRoleMapping);

            // when
            List<GrantReadModel> grants = grantQueryRepository.findEffectiveGrants(
                userContextId,
                "550e8400-e29b-41d4-a716-446655440003",
                3L
            );

            // then
            // 삭제된 Role의 권한은 조회되지 않음
            assertThat(grants).isEmpty();
        }

        @Test
        @DisplayName("다른 Tenant/Organization의 권한은 조회되지 않는다")
        void findEffectiveGrants_DifferentTenantOrg_ReturnsEmpty() {
            // given
            // 1. UserContext 생성
            UserContextJpaEntity userContext = UserContextJpaEntity.create(
                "auth0|test-user4",
                "test4@example.com",
                java.time.LocalDateTime.now()
            );
            UserContextJpaEntity savedUserContext = userContextJpaRepository.save(userContext);
            Long userContextId = savedUserContext.getId();

            // 2. Permission 생성
            PermissionJpaEntity permission = PermissionJpaEntity.create(
                "isolated.permission",
                "격리된 권한",
                "TENANT",
                java.time.LocalDateTime.now()
            );
            permissionJpaRepository.save(permission);

            // 3. Role 생성
            RoleJpaEntity role = RoleJpaEntity.create(
                "isolated.role",
                "격리된 역할",
                java.time.LocalDateTime.now()
            );
            roleJpaRepository.save(role);

            // 4. RolePermission 연결
            RolePermissionJpaEntity rolePermission = RolePermissionJpaEntity.create(
                "isolated.role",
                "isolated.permission"
            );
            rolePermissionJpaRepository.save(rolePermission);

            // 5. UserRoleMapping 생성 (Tenant 4, Org 4)
            UserRoleMappingJpaEntity userRoleMapping = UserRoleMappingJpaEntity.create(
                userContextId,
                "isolated.role",
                "550e8400-e29b-41d4-a716-446655440004",
                4L
            );
            userRoleMappingJpaRepository.save(userRoleMapping);

            // when - 다른 Tenant/Org로 조회
            List<GrantReadModel> grants = grantQueryRepository.findEffectiveGrants(
                userContextId,
                "999e8400-e29b-41d4-a716-446655440999", // 다른 tenantId
                4L
            );

            // then
            assertThat(grants).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 UserContext로 조회하면 빈 리스트를 반환한다")
        void findEffectiveGrants_NonExistentUserContext_ReturnsEmptyList() {
            // when
            List<GrantReadModel> grants = grantQueryRepository.findEffectiveGrants(
                99999L,
                "550e8400-e29b-41d4-a716-446655440001",
                1L
            );

            // then
            assertThat(grants).isEmpty();
        }
    }

    @Nested
    @DisplayName("파라미터 검증 테스트")
    class ParameterValidationTests {

        @Test
        @DisplayName("null UserContextId로 조회 시도 시 IllegalArgumentException이 발생한다")
        void findEffectiveGrants_NullUserContextId_ThrowsException() {
            // when & then
            assertThatThrownBy(() ->
                grantQueryRepository.findEffectiveGrants(null, "550e8400-e29b-41d4-a716-446655440001", 1L)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserContextId must not be null");
        }

        @Test
        @DisplayName("null TenantId로 조회 시도 시 IllegalArgumentException이 발생한다")
        void findEffectiveGrants_NullTenantId_ThrowsException() {
            // when & then
            assertThatThrownBy(() ->
                grantQueryRepository.findEffectiveGrants(1L, null, 1L)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TenantId must not be null");
        }

        @Test
        @DisplayName("null OrganizationId로 조회 시도 시 IllegalArgumentException이 발생한다")
        void findEffectiveGrants_NullOrganizationId_ThrowsException() {
            // when & then
            assertThatThrownBy(() ->
                grantQueryRepository.findEffectiveGrants(1L, "550e8400-e29b-41d4-a716-446655440001", null)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OrganizationId must not be null");
        }
    }
}

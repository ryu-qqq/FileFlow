package com.ryuqq.fileflow.adapter.in.rest.auth.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ResourceAccessChecker 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ResourceAccessChecker")
class ResourceAccessCheckerTest {

    private ResourceAccessChecker checker;

    @BeforeEach
    void setUp() {
        checker = new ResourceAccessChecker();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Nested
    @DisplayName("authenticated()")
    class AuthenticatedTest {

        @Test
        @DisplayName("UserContext가 있으면 true 반환")
        void returnsTrueWhenUserContextExists() {
            // given
            UserContext userContext =
                    UserContext.seller(OrganizationId.generate(), "Test Org", "seller@test.com");
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.authenticated()).isTrue();
        }

        @Test
        @DisplayName("UserContext가 없으면 false 반환")
        void returnsFalseWhenNoUserContext() {
            // when & then
            assertThat(checker.authenticated()).isFalse();
        }
    }

    @Nested
    @DisplayName("superAdmin()")
    class SuperAdminTest {

        @Test
        @DisplayName("SUPER_ADMIN이면 true 반환")
        void returnsTrueForSuperAdmin() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("SUPER_ADMIN"), List.of());
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.superAdmin()).isTrue();
        }

        @Test
        @DisplayName("SUPER_ADMIN이 아니면 false 반환")
        void returnsFalseForNonSuperAdmin() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of());
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.superAdmin()).isFalse();
        }
    }

    @Nested
    @DisplayName("admin()")
    class AdminTest {

        @Test
        @DisplayName("ADMIN이면 true 반환")
        void returnsTrueForAdmin() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of());
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.admin()).isTrue();
        }
    }

    @Nested
    @DisplayName("seller()")
    class SellerTest {

        @Test
        @DisplayName("SELLER이면 true 반환")
        void returnsTrueForSeller() {
            // given
            UserContext userContext =
                    UserContext.seller(OrganizationId.generate(), "Test Org", "seller@test.com");
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.seller()).isTrue();
        }
    }

    @Nested
    @DisplayName("customer()")
    class CustomerTest {

        @Test
        @DisplayName("DEFAULT(Customer)이면 true 반환")
        void returnsTrueForCustomer() {
            // given
            UserContext userContext = UserContext.customer(UserId.generate());
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.customer()).isTrue();
        }
    }

    @Nested
    @DisplayName("hasPermission()")
    class HasPermissionTest {

        @Test
        @DisplayName("권한이 있으면 true 반환")
        void returnsTrueWhenHasPermission() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of("file:read"));
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.hasPermission("file:read")).isTrue();
        }

        @Test
        @DisplayName("권한이 없으면 false 반환")
        void returnsFalseWhenNoPermission() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of("file:read"));
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.hasPermission("file:write")).isFalse();
        }

        @Test
        @DisplayName("SUPER_ADMIN은 모든 권한 보유")
        void superAdminHasAllPermissions() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("SUPER_ADMIN"), List.of());
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.hasPermission("file:read")).isTrue();
            assertThat(checker.hasPermission("file:write")).isTrue();
            assertThat(checker.hasPermission("any:permission")).isTrue();
        }

        @Test
        @DisplayName("UserContext가 없으면 false 반환")
        void returnsFalseWhenNoUserContext() {
            // when & then
            assertThat(checker.hasPermission("file:read")).isFalse();
        }
    }

    @Nested
    @DisplayName("canRead(), canWrite(), canDelete(), canDownload()")
    class FilePermissionTest {

        @Test
        @DisplayName("file:read 권한이 있으면 canRead() true")
        void canReadWithPermission() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of("file:read"));
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.canRead()).isTrue();
        }

        @Test
        @DisplayName("file:write 권한이 있으면 canWrite() true")
        void canWriteWithPermission() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of("file:write"));
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.canWrite()).isTrue();
        }

        @Test
        @DisplayName("file:delete 권한이 있으면 canDelete() true")
        void canDeleteWithPermission() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of("file:delete"));
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.canDelete()).isTrue();
        }

        @Test
        @DisplayName("file:download 권한이 있으면 canDownload() true")
        void canDownloadWithPermission() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of("file:download"));
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.canDownload()).isTrue();
        }
    }

    @Nested
    @DisplayName("hasAnyPermission()")
    class HasAnyPermissionTest {

        @Test
        @DisplayName("권한 중 하나라도 있으면 true 반환")
        void returnsTrueWhenHasAnyPermission() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of("file:read"));
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.hasAnyPermission("file:read", "file:write")).isTrue();
        }

        @Test
        @DisplayName("권한이 모두 없으면 false 반환")
        void returnsFalseWhenNoPermissions() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of("file:read"));
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.hasAnyPermission("file:write", "file:delete")).isFalse();
        }

        @Test
        @DisplayName("UserContext가 없으면 false 반환")
        void returnsFalseWhenNoUserContext() {
            // when & then
            assertThat(checker.hasAnyPermission("file:read")).isFalse();
        }
    }

    @Nested
    @DisplayName("hasRole(), hasAnyRole()")
    class RoleTest {

        @Test
        @DisplayName("역할이 있으면 true 반환")
        void hasRoleReturnsTrue() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of());
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.hasRole("ADMIN")).isTrue();
        }

        @Test
        @DisplayName("역할 중 하나라도 있으면 hasAnyRole true")
        void hasAnyRoleReturnsTrue() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of());
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.hasAnyRole("SUPER_ADMIN", "ADMIN")).isTrue();
        }

        @Test
        @DisplayName("UserContext가 없으면 false 반환")
        void returnsFalseWhenNoUserContext() {
            // when & then
            assertThat(checker.hasRole("ADMIN")).isFalse();
            assertThat(checker.hasAnyRole("ADMIN")).isFalse();
        }
    }

    @Nested
    @DisplayName("sameTenant()")
    class SameTenantTest {

        @Test
        @DisplayName("같은 테넌트면 true 반환")
        void returnsTrueForSameTenant() {
            // given
            OrganizationId organizationId = OrganizationId.generate();
            UserContext userContext =
                    UserContext.seller(organizationId, "Test Org", "seller@test.com");
            UserContextHolder.set(userContext);

            // when & then - 생성된 컨텍스트의 테넌트 ID로 검증
            assertThat(checker.sameTenant(userContext.getTenantId().value())).isTrue();
        }

        @Test
        @DisplayName("다른 테넌트면 false 반환")
        void returnsFalseForDifferentTenant() {
            // given
            OrganizationId organizationId = OrganizationId.generate();
            UserContext userContext =
                    UserContext.seller(organizationId, "Test Org", "seller@test.com");
            UserContextHolder.set(userContext);

            // when & then - 다른 테넌트 ID로 검증
            assertThat(checker.sameTenant("different-tenant-id")).isFalse();
        }

        @Test
        @DisplayName("SUPER_ADMIN은 모든 테넌트 접근 가능")
        void superAdminCanAccessAllTenants() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("SUPER_ADMIN"), List.of());
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.sameTenant("any-tenant")).isTrue();
        }

        @Test
        @DisplayName("UserContext가 없으면 false 반환")
        void returnsFalseWhenNoUserContext() {
            // when & then
            assertThat(checker.sameTenant("any-tenant")).isFalse();
        }
    }

    @Nested
    @DisplayName("sameOrganization()")
    class SameOrganizationTest {

        @Test
        @DisplayName("같은 조직이면 true 반환")
        void returnsTrueForSameOrganization() {
            // given
            OrganizationId organizationId = OrganizationId.generate();
            UserContext userContext =
                    UserContext.seller(organizationId, "Test Org", "seller@test.com");
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.sameOrganization(organizationId.value())).isTrue();
        }

        @Test
        @DisplayName("다른 조직이면 false 반환")
        void returnsFalseForDifferentOrganization() {
            // given
            OrganizationId organizationId = OrganizationId.generate();
            UserContext userContext =
                    UserContext.seller(organizationId, "Test Org", "seller@test.com");
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.sameOrganization(OrganizationId.generate().value())).isFalse();
        }

        @Test
        @DisplayName("ADMIN은 자기 테넌트 내 모든 조직 접근 가능")
        void adminCanAccessAllOrganizationsInTenant() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of());
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.sameOrganization("any-organization")).isTrue();
        }

        @Test
        @DisplayName("UserContext가 없으면 false 반환")
        void returnsFalseWhenNoUserContext() {
            // when & then
            assertThat(checker.sameOrganization("any-organization")).isFalse();
        }
    }

    @Nested
    @DisplayName("myself()")
    class MyselfTest {

        @Test
        @DisplayName("본인이면 true 반환")
        void returnsTrueForSameUser() {
            // given
            UserId userId = UserId.generate();
            UserContext userContext = UserContext.customer(userId);
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.myself(userId.value())).isTrue();
        }

        @Test
        @DisplayName("다른 사용자면 false 반환")
        void returnsFalseForDifferentUser() {
            // given
            UserId userId = UserId.generate();
            UserContext userContext = UserContext.customer(userId);
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.myself(UserId.generate().value())).isFalse();
        }

        @Test
        @DisplayName("UserContext가 없으면 false 반환")
        void returnsFalseWhenNoUserContext() {
            // when & then
            assertThat(checker.myself("any-user")).isFalse();
        }
    }

    @Nested
    @DisplayName("myselfOr()")
    class MyselfOrTest {

        @Test
        @DisplayName("본인이면 true 반환")
        void returnsTrueWhenMyself() {
            // given
            UserId userId = UserId.generate();
            UserContext userContext = UserContext.customer(userId);
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.myselfOr(userId.value(), "admin:all")).isTrue();
        }

        @Test
        @DisplayName("권한이 있으면 true 반환")
        void returnsTrueWhenHasPermission() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of("admin:all"));
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.myselfOr("different-user", "admin:all")).isTrue();
        }
    }

    @Nested
    @DisplayName("file()")
    class FileTest {

        @Test
        @DisplayName("file:read 권한 확인")
        void fileReadPermission() {
            // given
            UserContext userContext =
                    UserContext.admin("admin@test.com", List.of("ADMIN"), List.of("file:read"));
            UserContextHolder.set(userContext);

            // when & then
            assertThat(checker.file("read")).isTrue();
            assertThat(checker.file("write")).isFalse();
        }
    }
}

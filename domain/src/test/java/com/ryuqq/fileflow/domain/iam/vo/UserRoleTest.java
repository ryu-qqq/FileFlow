package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.vo.FileSize;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("UserRole 단위 테스트")
class UserRoleTest {

    @Nested
    @DisplayName("namespace 테스트")
    class NamespaceTest {

        @Test
        @DisplayName("SUPER_ADMIN은 connectly namespace를 가진다")
        void superAdmin_ShouldHaveConnectlyNamespace() {
            assertThat(UserRole.SUPER_ADMIN.namespace()).isEqualTo("connectly");
            assertThat(UserRole.SUPER_ADMIN.getNamespace()).isEqualTo("connectly");
        }

        @Test
        @DisplayName("ADMIN은 connectly namespace를 가진다")
        void admin_ShouldHaveConnectlyNamespace() {
            assertThat(UserRole.ADMIN.namespace()).isEqualTo("connectly");
            assertThat(UserRole.ADMIN.getNamespace()).isEqualTo("connectly");
        }

        @Test
        @DisplayName("SELLER는 setof namespace를 가진다")
        void seller_ShouldHaveSetofNamespace() {
            assertThat(UserRole.SELLER.namespace()).isEqualTo("setof");
            assertThat(UserRole.SELLER.getNamespace()).isEqualTo("setof");
        }

        @Test
        @DisplayName("DEFAULT는 setof namespace를 가진다")
        void default_ShouldHaveSetofNamespace() {
            assertThat(UserRole.DEFAULT.namespace()).isEqualTo("setof");
            assertThat(UserRole.DEFAULT.getNamespace()).isEqualTo("setof");
        }
    }

    @Nested
    @DisplayName("maxFileSize 테스트")
    class MaxFileSizeTest {

        @Test
        @DisplayName("SUPER_ADMIN은 5TB 제한을 가진다")
        void superAdmin_ShouldHave5TBLimit() {
            long expected = 5L * 1024 * 1024 * 1024 * 1024;
            assertThat(UserRole.SUPER_ADMIN.getMaxFileSizeBytes()).isEqualTo(expected);
        }

        @Test
        @DisplayName("ADMIN은 5TB 제한을 가진다")
        void admin_ShouldHave5TBLimit() {
            long expected = 5L * 1024 * 1024 * 1024 * 1024;
            assertThat(UserRole.ADMIN.getMaxFileSizeBytes()).isEqualTo(expected);
        }

        @Test
        @DisplayName("SELLER는 5GB 제한을 가진다")
        void seller_ShouldHave5GBLimit() {
            long expected = 5L * 1024 * 1024 * 1024;
            assertThat(UserRole.SELLER.getMaxFileSizeBytes()).isEqualTo(expected);
        }

        @Test
        @DisplayName("DEFAULT는 1GB 제한을 가진다")
        void default_ShouldHave1GBLimit() {
            long expected = (long) 1024 * 1024 * 1024;
            assertThat(UserRole.DEFAULT.getMaxFileSizeBytes()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("hasUploadPermission 테스트")
    class HasUploadPermissionTest {

        @ParameterizedTest
        @EnumSource(UserRole.class)
        @DisplayName("모든 역할은 업로드 권한을 가진다")
        void allRoles_ShouldHaveUploadPermission(UserRole role) {
            assertThat(role.hasUploadPermission()).isTrue();
        }
    }

    @Nested
    @DisplayName("canUpload(long) 테스트")
    class CanUploadLongTest {

        @Test
        @DisplayName("제한 이내의 파일은 업로드 가능하다")
        void withinLimit_ShouldReturnTrue() {
            long oneGB = 1024L * 1024 * 1024;
            assertThat(UserRole.ADMIN.canUpload(oneGB)).isTrue();
            assertThat(UserRole.SELLER.canUpload(oneGB)).isTrue();
            assertThat(UserRole.DEFAULT.canUpload(oneGB)).isTrue();
        }

        @Test
        @DisplayName("제한과 동일한 크기도 업로드 가능하다")
        void atLimit_ShouldReturnTrue() {
            long oneGB = 1024L * 1024 * 1024;
            assertThat(UserRole.DEFAULT.canUpload(oneGB)).isTrue();
        }

        @Test
        @DisplayName("제한을 초과하면 업로드 불가하다")
        void exceedsLimit_ShouldReturnFalse() {
            long twoGB = 2L * 1024 * 1024 * 1024;
            assertThat(UserRole.DEFAULT.canUpload(twoGB)).isFalse();
        }

        @Test
        @DisplayName("0 이하의 파일 크기는 업로드 불가하다")
        void zeroOrNegative_ShouldReturnFalse() {
            assertThat(UserRole.ADMIN.canUpload(0)).isFalse();
            assertThat(UserRole.ADMIN.canUpload(-1)).isFalse();
        }
    }

    @Nested
    @DisplayName("canUpload(FileSize) 테스트")
    class CanUploadFileSizeTest {

        @Test
        @DisplayName("유효한 FileSize는 업로드 가능 여부를 반환한다")
        void validFileSize_ShouldCheckUploadable() {
            FileSize oneGB = FileSize.of(1024L * 1024 * 1024);
            assertThat(UserRole.DEFAULT.canUpload(oneGB)).isTrue();
        }

        @Test
        @DisplayName("null FileSize는 업로드 불가하다")
        void nullFileSize_ShouldReturnFalse() {
            assertThat(UserRole.ADMIN.canUpload((FileSize) null)).isFalse();
        }

        @Test
        @DisplayName("제한을 초과하는 FileSize는 업로드 불가하다")
        void exceedsLimit_ShouldReturnFalse() {
            FileSize twoGB = FileSize.of(2L * 1024 * 1024 * 1024);
            assertThat(UserRole.DEFAULT.canUpload(twoGB)).isFalse();
        }
    }

    @Nested
    @DisplayName("Role 확인 메서드 테스트")
    class RoleCheckTest {

        @Test
        @DisplayName("isAdmin은 SUPER_ADMIN과 ADMIN일 때 true")
        void isAdmin_ShouldReturnTrueForAdminRoles() {
            assertThat(UserRole.SUPER_ADMIN.isAdmin()).isTrue();
            assertThat(UserRole.ADMIN.isAdmin()).isTrue();
            assertThat(UserRole.SELLER.isAdmin()).isFalse();
            assertThat(UserRole.DEFAULT.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("isSuperAdmin은 SUPER_ADMIN일 때만 true")
        void isSuperAdmin_ShouldReturnTrueOnlyForSuperAdmin() {
            assertThat(UserRole.SUPER_ADMIN.isSuperAdmin()).isTrue();
            assertThat(UserRole.ADMIN.isSuperAdmin()).isFalse();
            assertThat(UserRole.SELLER.isSuperAdmin()).isFalse();
            assertThat(UserRole.DEFAULT.isSuperAdmin()).isFalse();
        }

        @Test
        @DisplayName("isSeller는 SELLER일 때만 true")
        void isSeller_ShouldReturnTrueOnlyForSeller() {
            assertThat(UserRole.SUPER_ADMIN.isSeller()).isFalse();
            assertThat(UserRole.ADMIN.isSeller()).isFalse();
            assertThat(UserRole.SELLER.isSeller()).isTrue();
            assertThat(UserRole.DEFAULT.isSeller()).isFalse();
        }

        @Test
        @DisplayName("isDefault는 DEFAULT일 때만 true")
        void isDefault_ShouldReturnTrueOnlyForDefault() {
            assertThat(UserRole.SUPER_ADMIN.isDefault()).isFalse();
            assertThat(UserRole.ADMIN.isDefault()).isFalse();
            assertThat(UserRole.SELLER.isDefault()).isFalse();
            assertThat(UserRole.DEFAULT.isDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("getMaxFileSizeFormatted 테스트")
    class MaxFileSizeFormattedTest {

        @Test
        @DisplayName("ADMIN은 사람이 읽기 쉬운 형식을 반환한다")
        void admin_ShouldReturnHumanReadableFormat() {
            String formatted = UserRole.ADMIN.getMaxFileSizeFormatted();
            assertThat(formatted).isNotEmpty();
        }

        @Test
        @DisplayName("SELLER는 사람이 읽기 쉬운 형식을 반환한다")
        void seller_ShouldReturnHumanReadableFormat() {
            String formatted = UserRole.SELLER.getMaxFileSizeFormatted();
            assertThat(formatted).isNotEmpty();
        }

        @Test
        @DisplayName("DEFAULT는 사람이 읽기 쉬운 형식을 반환한다")
        void default_ShouldReturnHumanReadableFormat() {
            String formatted = UserRole.DEFAULT.getMaxFileSizeFormatted();
            assertThat(formatted).isNotEmpty();
        }

        @Test
        @DisplayName("SUPER_ADMIN은 사람이 읽기 쉬운 형식을 반환한다")
        void superAdmin_ShouldReturnHumanReadableFormat() {
            String formatted = UserRole.SUPER_ADMIN.getMaxFileSizeFormatted();
            assertThat(formatted).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("fromString 테스트")
    class FromStringTest {

        @Test
        @DisplayName("유효한 역할 문자열은 해당 UserRole을 반환한다")
        void validRoleString_ShouldReturnMatchingRole() {
            assertThat(UserRole.fromString("SUPER_ADMIN")).isEqualTo(UserRole.SUPER_ADMIN);
            assertThat(UserRole.fromString("ADMIN")).isEqualTo(UserRole.ADMIN);
            assertThat(UserRole.fromString("SELLER")).isEqualTo(UserRole.SELLER);
            assertThat(UserRole.fromString("DEFAULT")).isEqualTo(UserRole.DEFAULT);
        }

        @Test
        @DisplayName("소문자 역할 문자열도 정상 파싱된다")
        void lowercaseRoleString_ShouldReturnMatchingRole() {
            assertThat(UserRole.fromString("super_admin")).isEqualTo(UserRole.SUPER_ADMIN);
            assertThat(UserRole.fromString("admin")).isEqualTo(UserRole.ADMIN);
            assertThat(UserRole.fromString("seller")).isEqualTo(UserRole.SELLER);
            assertThat(UserRole.fromString("default")).isEqualTo(UserRole.DEFAULT);
        }

        @Test
        @DisplayName("혼합 대소문자 역할 문자열도 정상 파싱된다")
        void mixedCaseRoleString_ShouldReturnMatchingRole() {
            assertThat(UserRole.fromString("Super_Admin")).isEqualTo(UserRole.SUPER_ADMIN);
            assertThat(UserRole.fromString("Admin")).isEqualTo(UserRole.ADMIN);
            assertThat(UserRole.fromString("Seller")).isEqualTo(UserRole.SELLER);
        }

        @Test
        @DisplayName("앞뒤 공백이 있는 역할 문자열도 정상 파싱된다")
        void trimmedRoleString_ShouldReturnMatchingRole() {
            assertThat(UserRole.fromString("  ADMIN  ")).isEqualTo(UserRole.ADMIN);
            assertThat(UserRole.fromString("\tSELLER\n")).isEqualTo(UserRole.SELLER);
        }

        @Test
        @DisplayName("null 역할 문자열은 DEFAULT를 반환한다")
        void nullRoleString_ShouldReturnDefault() {
            assertThat(UserRole.fromString(null)).isEqualTo(UserRole.DEFAULT);
        }

        @Test
        @DisplayName("빈 문자열은 DEFAULT를 반환한다")
        void emptyRoleString_ShouldReturnDefault() {
            assertThat(UserRole.fromString("")).isEqualTo(UserRole.DEFAULT);
            assertThat(UserRole.fromString("   ")).isEqualTo(UserRole.DEFAULT);
        }

        @Test
        @DisplayName("알 수 없는 역할 문자열은 DEFAULT를 반환한다")
        void unknownRoleString_ShouldReturnDefault() {
            assertThat(UserRole.fromString("UNKNOWN")).isEqualTo(UserRole.DEFAULT);
            assertThat(UserRole.fromString("MANAGER")).isEqualTo(UserRole.DEFAULT);
            assertThat(UserRole.fromString("USER")).isEqualTo(UserRole.DEFAULT);
        }
    }

    @Nested
    @DisplayName("highestPriority 테스트")
    class HighestPriorityTest {

        @Test
        @DisplayName("역할 목록에서 가장 높은 우선순위를 반환한다")
        void roleList_ShouldReturnHighestPriority() {
            List<String> roles = Arrays.asList("SELLER", "ADMIN", "DEFAULT");
            assertThat(UserRole.highestPriority(roles)).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("SUPER_ADMIN이 포함되면 SUPER_ADMIN을 반환한다")
        void withSuperAdmin_ShouldReturnSuperAdmin() {
            List<String> roles = Arrays.asList("ADMIN", "SUPER_ADMIN", "SELLER");
            assertThat(UserRole.highestPriority(roles)).isEqualTo(UserRole.SUPER_ADMIN);
        }

        @Test
        @DisplayName("SELLER만 있으면 SELLER를 반환한다")
        void onlySeller_ShouldReturnSeller() {
            List<String> roles = Arrays.asList("SELLER", "DEFAULT");
            assertThat(UserRole.highestPriority(roles)).isEqualTo(UserRole.SELLER);
        }

        @Test
        @DisplayName("DEFAULT만 있으면 DEFAULT를 반환한다")
        void onlyDefault_ShouldReturnDefault() {
            List<String> roles = List.of("DEFAULT");
            assertThat(UserRole.highestPriority(roles)).isEqualTo(UserRole.DEFAULT);
        }

        @Test
        @DisplayName("빈 목록은 DEFAULT를 반환한다")
        void emptyList_ShouldReturnDefault() {
            assertThat(UserRole.highestPriority(Collections.emptyList()))
                    .isEqualTo(UserRole.DEFAULT);
        }

        @Test
        @DisplayName("null 목록은 DEFAULT를 반환한다")
        void nullList_ShouldReturnDefault() {
            assertThat(UserRole.highestPriority(null)).isEqualTo(UserRole.DEFAULT);
        }

        @Test
        @DisplayName("알 수 없는 역할이 포함되어도 유효한 역할 중 최고 우선순위를 반환한다")
        void withUnknownRoles_ShouldReturnHighestValidPriority() {
            List<String> roles = Arrays.asList("UNKNOWN", "SELLER", "GUEST");
            assertThat(UserRole.highestPriority(roles)).isEqualTo(UserRole.SELLER);
        }

        @Test
        @DisplayName("대소문자 혼합된 역할 목록도 정상 처리된다")
        void mixedCaseRoles_ShouldWork() {
            List<String> roles = Arrays.asList("admin", "seller");
            assertThat(UserRole.highestPriority(roles)).isEqualTo(UserRole.ADMIN);
        }
    }
}

package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.vo.FileSize;
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
        @DisplayName("isAdmin은 ADMIN일 때만 true")
        void isAdmin_ShouldReturnTrueOnlyForAdmin() {
            assertThat(UserRole.ADMIN.isAdmin()).isTrue();
            assertThat(UserRole.SELLER.isAdmin()).isFalse();
            assertThat(UserRole.DEFAULT.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("isSeller는 SELLER일 때만 true")
        void isSeller_ShouldReturnTrueOnlyForSeller() {
            assertThat(UserRole.ADMIN.isSeller()).isFalse();
            assertThat(UserRole.SELLER.isSeller()).isTrue();
            assertThat(UserRole.DEFAULT.isSeller()).isFalse();
        }

        @Test
        @DisplayName("isDefault는 DEFAULT일 때만 true")
        void isDefault_ShouldReturnTrueOnlyForDefault() {
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
    }
}

package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.session.vo.FileSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("UserRole 단위 테스트")
class UserRoleTest {

    @Nested
    @DisplayName("Enum 기본 테스트")
    class EnumBasicTest {

        @Test
        @DisplayName("모든 역할이 정의되어 있다")
        void values_ShouldContainAllRoles() {
            // when
            UserRole[] values = UserRole.values();

            // then
            assertThat(values).hasSize(3);
            assertThat(values).containsExactly(UserRole.ADMIN, UserRole.SELLER, UserRole.DEFAULT);
        }
    }

    @Nested
    @DisplayName("네임스페이스 테스트")
    class NamespaceTest {

        @Test
        @DisplayName("ADMIN은 connectly 네임스페이스를 가진다")
        void admin_ShouldHaveConnectlyNamespace() {
            assertThat(UserRole.ADMIN.namespace()).isEqualTo("connectly");
            assertThat(UserRole.ADMIN.getNamespace()).isEqualTo("connectly");
        }

        @Test
        @DisplayName("SELLER는 setof 네임스페이스를 가진다")
        void seller_ShouldHaveSetofNamespace() {
            assertThat(UserRole.SELLER.namespace()).isEqualTo("setof");
            assertThat(UserRole.SELLER.getNamespace()).isEqualTo("setof");
        }

        @Test
        @DisplayName("DEFAULT는 setof 네임스페이스를 가진다")
        void default_ShouldHaveSetofNamespace() {
            assertThat(UserRole.DEFAULT.namespace()).isEqualTo("setof");
            assertThat(UserRole.DEFAULT.getNamespace()).isEqualTo("setof");
        }
    }

    @Nested
    @DisplayName("최대 파일 크기 테스트")
    class MaxFileSizeTest {

        @Test
        @DisplayName("ADMIN은 5TB 최대 파일 크기를 가진다")
        void admin_ShouldHave5TBMaxFileSize() {
            // given
            long expected5TB = 5L * 1024 * 1024 * 1024 * 1024;

            // then
            assertThat(UserRole.ADMIN.getMaxFileSizeBytes()).isEqualTo(expected5TB);
        }

        @Test
        @DisplayName("SELLER는 5GB 최대 파일 크기를 가진다")
        void seller_ShouldHave5GBMaxFileSize() {
            // given
            long expected5GB = 5L * 1024 * 1024 * 1024;

            // then
            assertThat(UserRole.SELLER.getMaxFileSizeBytes()).isEqualTo(expected5GB);
        }

        @Test
        @DisplayName("DEFAULT는 1GB 최대 파일 크기를 가진다")
        void default_ShouldHave1GBMaxFileSize() {
            // given
            long expected1GB = 1024L * 1024 * 1024;

            // then
            assertThat(UserRole.DEFAULT.getMaxFileSizeBytes()).isEqualTo(expected1GB);
        }
    }

    @Nested
    @DisplayName("업로드 권한 테스트")
    class UploadPermissionTest {

        @Test
        @DisplayName("모든 역할은 업로드 권한을 가진다")
        void allRoles_ShouldHaveUploadPermission() {
            for (UserRole role : UserRole.values()) {
                assertThat(role.hasUploadPermission()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("canUpload 테스트")
    class CanUploadTest {

        @ParameterizedTest
        @CsvSource({
            "ADMIN, 1, true",
            "ADMIN, 1073741824, true",
            "SELLER, 1, true",
            "SELLER, 5368709120, true",
            "DEFAULT, 1, true",
            "DEFAULT, 1073741824, true"
        })
        @DisplayName("허용 범위 내 파일 크기는 업로드 가능하다")
        void canUpload_WithinLimit_ShouldReturnTrue(
                UserRole role, long fileSize, boolean expected) {
            assertThat(role.canUpload(fileSize)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({"SELLER, 5368709121, false", "DEFAULT, 1073741825, false"})
        @DisplayName("허용 범위 초과 파일 크기는 업로드 불가능하다")
        void canUpload_ExceedingLimit_ShouldReturnFalse(
                UserRole role, long fileSize, boolean expected) {
            assertThat(role.canUpload(fileSize)).isEqualTo(expected);
        }

        @Test
        @DisplayName("0 또는 음수 파일 크기는 업로드 불가능하다")
        void canUpload_WithZeroOrNegativeSize_ShouldReturnFalse() {
            for (UserRole role : UserRole.values()) {
                assertThat(role.canUpload(0L)).isFalse();
                assertThat(role.canUpload(-1L)).isFalse();
            }
        }

        @Test
        @DisplayName("FileSize VO로 업로드 가능 여부를 확인할 수 있다")
        void canUpload_WithFileSizeVO_ShouldWork() {
            // given
            FileSize validSize = FileSize.of(1024L * 1024); // 1MB

            // then
            assertThat(UserRole.ADMIN.canUpload(validSize)).isTrue();
            assertThat(UserRole.SELLER.canUpload(validSize)).isTrue();
            assertThat(UserRole.DEFAULT.canUpload(validSize)).isTrue();
        }

        @Test
        @DisplayName("null FileSize는 업로드 불가능하다")
        void canUpload_WithNullFileSize_ShouldReturnFalse() {
            for (UserRole role : UserRole.values()) {
                assertThat(role.canUpload((FileSize) null)).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("역할 확인 테스트")
    class RoleCheckTest {

        @Test
        @DisplayName("ADMIN.isAdmin()은 true를 반환한다")
        void isAdmin_WithAdmin_ShouldReturnTrue() {
            assertThat(UserRole.ADMIN.isAdmin()).isTrue();
            assertThat(UserRole.SELLER.isAdmin()).isFalse();
            assertThat(UserRole.DEFAULT.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("SELLER.isSeller()은 true를 반환한다")
        void isSeller_WithSeller_ShouldReturnTrue() {
            assertThat(UserRole.SELLER.isSeller()).isTrue();
            assertThat(UserRole.ADMIN.isSeller()).isFalse();
            assertThat(UserRole.DEFAULT.isSeller()).isFalse();
        }

        @Test
        @DisplayName("DEFAULT.isDefault()은 true를 반환한다")
        void isDefault_WithDefault_ShouldReturnTrue() {
            assertThat(UserRole.DEFAULT.isDefault()).isTrue();
            assertThat(UserRole.ADMIN.isDefault()).isFalse();
            assertThat(UserRole.SELLER.isDefault()).isFalse();
        }
    }

    @Nested
    @DisplayName("getMaxFileSizeFormatted 테스트")
    class MaxFileSizeFormattedTest {

        @Test
        @DisplayName("ADMIN의 최대 파일 크기를 포맷팅하여 반환한다")
        void getMaxFileSizeFormatted_Admin_ShouldReturnFormatted() {
            // when
            String formatted = UserRole.ADMIN.getMaxFileSizeFormatted();

            // then
            assertThat(formatted).contains("TB");
        }

        @Test
        @DisplayName("SELLER의 최대 파일 크기를 포맷팅하여 반환한다")
        void getMaxFileSizeFormatted_Seller_ShouldReturnFormatted() {
            // when
            String formatted = UserRole.SELLER.getMaxFileSizeFormatted();

            // then
            assertThat(formatted).contains("GB");
        }

        @Test
        @DisplayName("DEFAULT의 최대 파일 크기를 포맷팅하여 반환한다")
        void getMaxFileSizeFormatted_Default_ShouldReturnFormatted() {
            // when
            String formatted = UserRole.DEFAULT.getMaxFileSizeFormatted();

            // then
            assertThat(formatted).contains("GB");
        }
    }
}

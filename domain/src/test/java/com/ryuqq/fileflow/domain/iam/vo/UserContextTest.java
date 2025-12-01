package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadCategory;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("UserContext 단위 테스트")
class UserContextTest {

    @Nested
    @DisplayName("Admin 사용자 컨텍스트 생성 테스트")
    class AdminContextTest {

        @Test
        @DisplayName("Admin 사용자 컨텍스트를 생성할 수 있다")
        void admin_WithValidEmail_ShouldCreateContext() {
            // given
            String email = "admin@fileflow.com";

            // when
            UserContext context = UserContext.admin(email);

            // then
            assertThat(context).isNotNull();
            assertThat(context.tenant()).isEqualTo(Tenant.connectly());
            assertThat(context.organization()).isEqualTo(Organization.admin());
            assertThat(context.email()).isEqualTo(email);
            assertThat(context.userId()).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Admin 이메일이 null이거나 빈 문자열이면 예외가 발생한다")
        void admin_WithInvalidEmail_ShouldThrowException(String invalidEmail) {
            // when & then
            assertThatThrownBy(() -> UserContext.admin(invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Admin 이메일은 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("Admin 사용자인지 확인할 수 있다")
        void isAdmin_WithAdminContext_ShouldReturnTrue() {
            // given
            UserContext context = UserContext.admin("admin@test.com");

            // then
            assertThat(context.isAdmin()).isTrue();
            assertThat(context.isSeller()).isFalse();
            assertThat(context.isCustomer()).isFalse();
        }
    }

    @Nested
    @DisplayName("Seller 사용자 컨텍스트 생성 테스트")
    class SellerContextTest {

        @Test
        @DisplayName("Seller 사용자 컨텍스트를 생성할 수 있다")
        void seller_WithValidParams_ShouldCreateContext() {
            // given
            long organizationId = 100L;
            String companyName = "Test Company";
            String email = "seller@company.com";

            // when
            UserContext context = UserContext.seller(organizationId, companyName, email);

            // then
            assertThat(context).isNotNull();
            assertThat(context.tenant()).isEqualTo(Tenant.connectly());
            assertThat(context.organization().id()).isEqualTo(organizationId);
            assertThat(context.organization().name()).isEqualTo(companyName);
            assertThat(context.email()).isEqualTo(email);
            assertThat(context.userId()).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Seller 이메일이 null이거나 빈 문자열이면 예외가 발생한다")
        void seller_WithInvalidEmail_ShouldThrowException(String invalidEmail) {
            // when & then
            assertThatThrownBy(() -> UserContext.seller(1L, "Company", invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Seller 이메일은 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("Seller 사용자인지 확인할 수 있다")
        void isSeller_WithSellerContext_ShouldReturnTrue() {
            // given
            UserContext context = UserContext.seller(1L, "Company", "seller@test.com");

            // then
            assertThat(context.isSeller()).isTrue();
            assertThat(context.isAdmin()).isFalse();
            assertThat(context.isCustomer()).isFalse();
        }
    }

    @Nested
    @DisplayName("Customer 사용자 컨텍스트 생성 테스트")
    class CustomerContextTest {

        @Test
        @DisplayName("Customer 사용자 컨텍스트를 생성할 수 있다")
        void customer_WithValidUserId_ShouldCreateContext() {
            // given
            long userId = 9999L;

            // when
            UserContext context = UserContext.customer(userId);

            // then
            assertThat(context).isNotNull();
            assertThat(context.tenant()).isEqualTo(Tenant.connectly());
            assertThat(context.organization()).isEqualTo(Organization.customer());
            assertThat(context.email()).isNull();
            assertThat(context.userId()).isEqualTo(userId);
        }

        @ParameterizedTest
        @ValueSource(longs = {0, -1, -100})
        @DisplayName("Customer userId가 0 이하이면 예외가 발생한다")
        void customer_WithInvalidUserId_ShouldThrowException(long invalidUserId) {
            // when & then
            assertThatThrownBy(() -> UserContext.customer(invalidUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Customer userId는 1 이상이어야 합니다");
        }

        @Test
        @DisplayName("Customer 사용자인지 확인할 수 있다")
        void isCustomer_WithCustomerContext_ShouldReturnTrue() {
            // given
            UserContext context = UserContext.customer(1L);

            // then
            assertThat(context.isCustomer()).isTrue();
            assertThat(context.isAdmin()).isFalse();
            assertThat(context.isSeller()).isFalse();
        }
    }

    @Nested
    @DisplayName("of 메서드 테스트")
    class OfMethodTest {

        @Test
        @DisplayName("of 메서드로 사용자 컨텍스트를 생성할 수 있다")
        void of_WithValidParams_ShouldCreateContext() {
            // given
            Tenant tenant = Tenant.connectly();
            Organization organization = Organization.admin();
            String email = "admin@test.com";

            // when
            UserContext context = UserContext.of(tenant, organization, email, null);

            // then
            assertThat(context.tenant()).isEqualTo(tenant);
            assertThat(context.organization()).isEqualTo(organization);
            assertThat(context.email()).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("테넌트가 null이면 예외가 발생한다")
        void constructor_WithNullTenant_ShouldThrowException() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    UserContext.of(
                                            null, Organization.admin(), "admin@test.com", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("테넌트는 null일 수 없습니다");
        }

        @Test
        @DisplayName("조직이 null이면 예외가 발생한다")
        void constructor_WithNullOrganization_ShouldThrowException() {
            // when & then
            assertThatThrownBy(
                            () -> UserContext.of(Tenant.connectly(), null, "admin@test.com", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("조직은 null일 수 없습니다");
        }

        @Test
        @DisplayName("Admin/Seller가 userId를 가지면 예외가 발생한다")
        void constructor_AdminWithUserId_ShouldThrowException() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    UserContext.of(
                                            Tenant.connectly(),
                                            Organization.admin(),
                                            "admin@test.com",
                                            100L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("userId를 가질 수 없습니다");
        }

        @Test
        @DisplayName("Customer가 email을 가지면 예외가 발생한다")
        void constructor_CustomerWithEmail_ShouldThrowException() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    UserContext.of(
                                            Tenant.connectly(),
                                            Organization.customer(),
                                            "customer@test.com",
                                            100L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email을 가질 수 없습니다");
        }
    }

    @Nested
    @DisplayName("역할 조회 테스트")
    class RoleTest {

        @Test
        @DisplayName("getRole로 사용자 역할을 조회할 수 있다")
        void getRole_ShouldReturnUserRole() {
            // given
            UserContext admin = UserContext.admin("admin@test.com");
            UserContext seller = UserContext.seller(1L, "Company", "seller@test.com");
            UserContext customer = UserContext.customer(1L);

            // then
            assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
            assertThat(seller.getRole()).isEqualTo(UserRole.SELLER);
            assertThat(customer.getRole()).isEqualTo(UserRole.DEFAULT);
        }
    }

    @Nested
    @DisplayName("S3 관련 테스트")
    class S3Test {

        @Test
        @DisplayName("getS3Bucket으로 S3 버킷을 조회할 수 있다")
        void getS3Bucket_ShouldReturnS3Bucket() {
            // given
            UserContext admin = UserContext.admin("admin@test.com");

            // when
            S3Bucket bucket = admin.getS3Bucket();

            // then
            assertThat(bucket.bucketName()).isEqualTo("fileflow-uploads-prod");
        }

        @Test
        @DisplayName("Admin 사용자의 S3 키를 생성할 수 있다")
        void generateS3Key_Admin_ShouldGenerateCorrectKey() {
            // given
            UserContext admin = UserContext.admin("admin@test.com");
            LocalDate uploadDate = LocalDate.of(2025, 11, 15);

            // when
            S3Key s3Key = admin.generateS3Key(UploadCategory.BANNER, "test.jpg", uploadDate);

            // then
            assertThat(s3Key.key()).contains("connectly/");
            assertThat(s3Key.key()).contains("banner");
            assertThat(s3Key.key()).contains("2025");
            assertThat(s3Key.key()).contains("11");
            assertThat(s3Key.key()).contains("test.jpg");
        }

        @Test
        @DisplayName("Seller 사용자의 S3 키를 생성할 수 있다")
        void generateS3Key_Seller_ShouldGenerateCorrectKey() {
            // given
            UserContext seller = UserContext.seller(100L, "Company", "seller@test.com");
            LocalDate uploadDate = LocalDate.of(2025, 11, 15);

            // when
            S3Key s3Key =
                    seller.generateS3Key(UploadCategory.PRODUCT_IMAGE, "product.jpg", uploadDate);

            // then
            assertThat(s3Key.key()).contains("seller-100/");
            assertThat(s3Key.key()).contains("product");
            assertThat(s3Key.key()).contains("2025");
        }

        @Test
        @DisplayName("Customer 사용자의 S3 키를 생성할 수 있다 (카테고리 없음)")
        void generateS3Key_Customer_ShouldGenerateCorrectKey() {
            // given
            UserContext customer = UserContext.customer(1L);
            LocalDate uploadDate = LocalDate.of(2025, 11, 15);

            // when
            S3Key s3Key = customer.generateS3Key(null, "file.jpg", uploadDate);

            // then
            assertThat(s3Key.key()).contains("customer/");
            assertThat(s3Key.key()).contains("2025");
        }

        @Test
        @DisplayName("Admin/Seller는 카테고리가 필수이다")
        void generateS3Key_AdminWithoutCategory_ShouldThrowException() {
            // given
            UserContext admin = UserContext.admin("admin@test.com");

            // when & then
            assertThatThrownBy(() -> admin.generateS3Key(null, "test.jpg", LocalDate.now()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("업로드 카테고리가 필수입니다");
        }

        @Test
        @DisplayName("파일명이 null이면 예외가 발생한다")
        void generateS3Key_WithNullFileName_ShouldThrowException() {
            // given
            UserContext admin = UserContext.admin("admin@test.com");

            // when & then
            assertThatThrownBy(
                            () -> admin.generateS3Key(UploadCategory.BANNER, null, LocalDate.now()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일명은 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("업로드 날짜가 null이면 예외가 발생한다")
        void generateS3Key_WithNullUploadDate_ShouldThrowException() {
            // given
            UserContext admin = UserContext.admin("admin@test.com");

            // when & then
            assertThatThrownBy(() -> admin.generateS3Key(UploadCategory.BANNER, "test.jpg", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("업로드 날짜는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("사용자 식별자 테스트")
    class UserIdentifierTest {

        @Test
        @DisplayName("Admin/Seller의 식별자는 email이다")
        void getUserIdentifier_AdminSeller_ShouldReturnEmail() {
            // given
            UserContext admin = UserContext.admin("admin@test.com");
            UserContext seller = UserContext.seller(1L, "Company", "seller@test.com");

            // then
            assertThat(admin.getUserIdentifier()).isEqualTo("admin@test.com");
            assertThat(seller.getUserIdentifier()).isEqualTo("seller@test.com");
        }

        @Test
        @DisplayName("Customer의 식별자는 user-{userId}이다")
        void getUserIdentifier_Customer_ShouldReturnUserIdFormat() {
            // given
            UserContext customer = UserContext.customer(9999L);

            // then
            assertThat(customer.getUserIdentifier()).isEqualTo("user-9999");
        }
    }

    @Nested
    @DisplayName("조직 ID 조회 테스트")
    class OrganizationIdTest {

        @Test
        @DisplayName("getOrganizationId로 조직 ID를 조회할 수 있다")
        void getOrganizationId_ShouldReturnOrganizationId() {
            // given
            UserContext admin = UserContext.admin("admin@test.com");
            UserContext seller = UserContext.seller(100L, "Company", "seller@test.com");
            UserContext customer = UserContext.customer(1L);

            // then
            assertThat(admin.getOrganizationId()).isEqualTo(0L);
            assertThat(seller.getOrganizationId()).isEqualTo(100L);
            assertThat(customer.getOrganizationId()).isEqualTo(-1L);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 UserContext는 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            UserContext context1 = UserContext.admin("admin@test.com");
            UserContext context2 = UserContext.admin("admin@test.com");

            // when & then
            assertThat(context1).isEqualTo(context2);
            assertThat(context1.hashCode()).isEqualTo(context2.hashCode());
        }

        @Test
        @DisplayName("다른 이메일을 가진 UserContext는 동등하지 않다")
        void equals_WithDifferentEmail_ShouldNotBeEqual() {
            // given
            UserContext context1 = UserContext.admin("admin1@test.com");
            UserContext context2 = UserContext.admin("admin2@test.com");

            // when & then
            assertThat(context1).isNotEqualTo(context2);
        }
    }
}

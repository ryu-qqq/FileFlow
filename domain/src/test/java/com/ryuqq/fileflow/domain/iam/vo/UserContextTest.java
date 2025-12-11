package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadCategory;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserContext 단위 테스트")
class UserContextTest {

    @Nested
    @DisplayName("팩토리 메서드 테스트")
    class FactoryMethodTest {

        @Test
        @DisplayName("admin 메서드로 Admin 컨텍스트를 생성할 수 있다")
        void admin_ShouldCreateAdminContext() {
            // when
            UserContext context = UserContext.admin("admin@test.com");

            // then
            assertThat(context.tenant()).isEqualTo(Tenant.connectly());
            assertThat(context.organization()).isEqualTo(Organization.admin());
            assertThat(context.email()).isEqualTo("admin@test.com");
            assertThat(context.userId()).isNull();
        }

        @Test
        @DisplayName("seller 메서드로 Seller 컨텍스트를 생성할 수 있다")
        void seller_ShouldCreateSellerContext() {
            // given
            OrganizationId organizationId = OrganizationId.generate();

            // when
            UserContext context =
                    UserContext.seller(organizationId, "TestCompany", "seller@test.com");

            // then
            assertThat(context.tenant()).isEqualTo(Tenant.connectly());
            assertThat(context.organization().id()).isEqualTo(organizationId);
            assertThat(context.organization().name()).isEqualTo("TestCompany");
            assertThat(context.email()).isEqualTo("seller@test.com");
            assertThat(context.userId()).isNull();
        }

        @Test
        @DisplayName("customer 메서드로 Customer 컨텍스트를 생성할 수 있다")
        void customer_ShouldCreateCustomerContext() {
            // given
            UserId userId = UserId.generate();

            // when
            UserContext context = UserContext.customer(userId);

            // then
            assertThat(context.tenant()).isEqualTo(Tenant.connectly());
            assertThat(context.organization()).isEqualTo(Organization.customer());
            assertThat(context.email()).isNull();
            assertThat(context.userId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("of 메서드로 컨텍스트를 생성할 수 있다")
        void of_ShouldCreateContext() {
            // when
            UserContext context =
                    UserContext.of(
                            Tenant.connectly(), Organization.admin(), "admin@test.com", null);

            // then
            assertThat(context.email()).isEqualTo("admin@test.com");
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("테넌트가 null이면 예외가 발생한다")
        void nullTenant_ShouldThrowException() {
            assertThatThrownBy(
                            () ->
                                    new UserContext(
                                            null, Organization.admin(), "admin@test.com", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("테넌트는 null일 수 없습니다");
        }

        @Test
        @DisplayName("조직이 null이면 예외가 발생한다")
        void nullOrganization_ShouldThrowException() {
            assertThatThrownBy(
                            () -> new UserContext(Tenant.connectly(), null, "admin@test.com", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("조직은 null일 수 없습니다");
        }

        @Test
        @DisplayName("Admin 이메일이 null이면 예외가 발생한다")
        void adminWithNullEmail_ShouldThrowException() {
            assertThatThrownBy(() -> UserContext.admin(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Admin 이메일은 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("Admin 이메일이 빈 문자열이면 예외가 발생한다")
        void adminWithBlankEmail_ShouldThrowException() {
            assertThatThrownBy(() -> UserContext.admin("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Admin 이메일은 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("Seller 이메일이 null이면 예외가 발생한다")
        void sellerWithNullEmail_ShouldThrowException() {
            // given
            OrganizationId organizationId = OrganizationId.generate();

            // when & then
            assertThatThrownBy(() -> UserContext.seller(organizationId, "Company", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Seller 이메일은 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("Customer userId가 null이면 예외가 발생한다")
        void customerWithNullUserId_ShouldThrowException() {
            assertThatThrownBy(() -> UserContext.customer(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Customer userId는 null일 수 없습니다");
        }

        @Test
        @DisplayName("Admin에 userId가 있으면 예외가 발생한다")
        void adminWithUserId_ShouldThrowException() {
            assertThatThrownBy(
                            () ->
                                    new UserContext(
                                            Tenant.connectly(),
                                            Organization.admin(),
                                            "admin@test.com",
                                            UserId.generate()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ADMIN 사용자는 userId를 가질 수 없습니다");
        }

        @Test
        @DisplayName("Customer에 email이 있으면 예외가 발생한다")
        void customerWithEmail_ShouldThrowException() {
            assertThatThrownBy(
                            () ->
                                    new UserContext(
                                            Tenant.connectly(),
                                            Organization.customer(),
                                            "test@test.com",
                                            UserId.generate()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("DEFAULT 사용자는 email을 가질 수 없습니다");
        }
    }

    @Nested
    @DisplayName("역할 확인 테스트")
    class RoleCheckTest {

        @Test
        @DisplayName("getRole은 조직의 역할을 반환한다")
        void getRole_ShouldReturnOrganizationRole() {
            // given
            OrganizationId orgId = OrganizationId.generate();
            UserId userId = UserId.generate();

            UserContext admin = UserContext.admin("admin@test.com");
            UserContext seller = UserContext.seller(orgId, "Company", "seller@test.com");
            UserContext customer = UserContext.customer(userId);

            // when & then
            assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
            assertThat(seller.getRole()).isEqualTo(UserRole.SELLER);
            assertThat(customer.getRole()).isEqualTo(UserRole.DEFAULT);
        }

        @Test
        @DisplayName("isAdmin은 Admin일 때만 true")
        void isAdmin_ShouldReturnTrueOnlyForAdmin() {
            // given
            OrganizationId orgId = OrganizationId.generate();
            UserId userId = UserId.generate();

            // when & then
            assertThat(UserContext.admin("admin@test.com").isAdmin()).isTrue();
            assertThat(UserContext.seller(orgId, "Company", "seller@test.com").isAdmin()).isFalse();
            assertThat(UserContext.customer(userId).isAdmin()).isFalse();
        }

        @Test
        @DisplayName("isSeller는 Seller일 때만 true")
        void isSeller_ShouldReturnTrueOnlyForSeller() {
            // given
            OrganizationId orgId = OrganizationId.generate();
            UserId userId = UserId.generate();

            // when & then
            assertThat(UserContext.admin("admin@test.com").isSeller()).isFalse();
            assertThat(UserContext.seller(orgId, "Company", "seller@test.com").isSeller()).isTrue();
            assertThat(UserContext.customer(userId).isSeller()).isFalse();
        }

        @Test
        @DisplayName("isCustomer는 Customer일 때만 true")
        void isCustomer_ShouldReturnTrueOnlyForCustomer() {
            // given
            OrganizationId orgId = OrganizationId.generate();
            UserId userId = UserId.generate();

            // when & then
            assertThat(UserContext.admin("admin@test.com").isCustomer()).isFalse();
            assertThat(UserContext.seller(orgId, "Company", "seller@test.com").isCustomer())
                    .isFalse();
            assertThat(UserContext.customer(userId).isCustomer()).isTrue();
        }
    }

    @Nested
    @DisplayName("S3 관련 테스트")
    class S3Test {

        @Test
        @DisplayName("getS3Bucket은 S3Bucket을 반환한다")
        void getS3Bucket_ShouldReturnS3Bucket() {
            UserContext context = UserContext.admin("admin@test.com");
            S3Bucket bucket = context.getS3Bucket();

            assertThat(bucket).isNotNull();
            assertThat(bucket.bucketName()).isEqualTo("fileflow-uploads-prod");
        }

        @Test
        @DisplayName("Admin의 S3 키는 카테고리를 포함한다")
        void adminS3Key_ShouldIncludeCategory() {
            UserContext context = UserContext.admin("admin@test.com");
            LocalDate date = LocalDate.of(2024, 3, 15);

            S3Key key = context.generateS3Key(UploadCategory.PRODUCT_IMAGE, "test.jpg", date);

            assertThat(key.key()).contains("connectly/");
            assertThat(key.key()).contains("product/");
            assertThat(key.key()).contains("2024");
            assertThat(key.key()).contains("03");
            assertThat(key.key()).contains("test.jpg");
        }

        @Test
        @DisplayName("Seller의 S3 키는 조직 ID를 포함한다")
        void sellerS3Key_ShouldIncludeOrgId() {
            // given
            OrganizationId organizationId = OrganizationId.generate();
            UserContext context =
                    UserContext.seller(organizationId, "Company", "seller@test.com");
            LocalDate date = LocalDate.of(2024, 3, 15);

            // when
            S3Key key = context.generateS3Key(UploadCategory.PRODUCT_IMAGE, "test.jpg", date);

            // then
            assertThat(key.key()).contains("setof/seller-" + organizationId.value() + "/");
        }

        @Test
        @DisplayName("Customer의 S3 키는 카테고리를 포함하지 않는다")
        void customerS3Key_ShouldNotIncludeCategory() {
            // given
            UserId userId = UserId.generate();
            UserContext context = UserContext.customer(userId);
            LocalDate date = LocalDate.of(2024, 3, 15);

            // when
            S3Key key = context.generateS3Key(null, "test.jpg", date);

            // then
            assertThat(key.key()).contains("setof/customer/");
            assertThat(key.key()).doesNotContain("product");
        }

        @Test
        @DisplayName("Admin/Seller에 카테고리가 없으면 예외가 발생한다")
        void adminWithoutCategory_ShouldThrowException() {
            UserContext context = UserContext.admin("admin@test.com");
            LocalDate date = LocalDate.of(2024, 3, 15);

            assertThatThrownBy(() -> context.generateS3Key(null, "test.jpg", date))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Admin/Seller는 업로드 카테고리가 필수입니다");
        }

        @Test
        @DisplayName("파일명이 null이면 예외가 발생한다")
        void nullFileName_ShouldThrowException() {
            UserContext context = UserContext.admin("admin@test.com");
            LocalDate date = LocalDate.of(2024, 3, 15);

            assertThatThrownBy(
                            () -> context.generateS3Key(UploadCategory.PRODUCT_IMAGE, null, date))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일명은 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("날짜가 null이면 예외가 발생한다")
        void nullDate_ShouldThrowException() {
            UserContext context = UserContext.admin("admin@test.com");

            assertThatThrownBy(
                            () ->
                                    context.generateS3Key(
                                            UploadCategory.PRODUCT_IMAGE, "test.jpg", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("업로드 날짜는 null일 수 없습니다");
        }

        @Test
        @DisplayName("generateS3KeyToday는 오늘 날짜로 키를 생성한다")
        void generateS3KeyToday_ShouldUseToday() {
            UserContext context = UserContext.admin("admin@test.com");

            S3Key key = context.generateS3KeyToday(UploadCategory.PRODUCT_IMAGE, "test.jpg");

            assertThat(key).isNotNull();
            assertThat(key.key()).contains("test.jpg");
        }
    }

    @Nested
    @DisplayName("사용자 식별자 테스트")
    class UserIdentifierTest {

        @Test
        @DisplayName("Admin의 식별자는 이메일이다")
        void adminIdentifier_ShouldBeEmail() {
            UserContext context = UserContext.admin("admin@test.com");
            assertThat(context.getUserIdentifier()).isEqualTo("admin@test.com");
        }

        @Test
        @DisplayName("Seller의 식별자는 이메일이다")
        void sellerIdentifier_ShouldBeEmail() {
            // given
            OrganizationId organizationId = OrganizationId.generate();
            UserContext context =
                    UserContext.seller(organizationId, "Company", "seller@test.com");

            // when & then
            assertThat(context.getUserIdentifier()).isEqualTo("seller@test.com");
        }

        @Test
        @DisplayName("Customer의 식별자는 user-{id} 형식이다")
        void customerIdentifier_ShouldBeUserFormat() {
            // given
            UserId userId = UserId.generate();
            UserContext context = UserContext.customer(userId);

            // when & then
            assertThat(context.getUserIdentifier()).isEqualTo("user-" + userId.value());
        }
    }

    @Nested
    @DisplayName("조직 ID 테스트")
    class OrganizationIdGetterTest {

        @Test
        @DisplayName("getOrganizationId는 조직 ID를 반환한다")
        void getOrganizationId_ShouldReturnOrgId() {
            // given
            OrganizationId orgId = OrganizationId.generate();
            UserId userId = UserId.generate();

            // when & then
            assertThat(UserContext.admin("admin@test.com").getOrganizationId()).isNull();
            assertThat(UserContext.seller(orgId, "Company", "seller@test.com").getOrganizationId())
                    .isEqualTo(orgId);
            assertThat(UserContext.customer(userId).getOrganizationId()).isNull();
        }
    }
}

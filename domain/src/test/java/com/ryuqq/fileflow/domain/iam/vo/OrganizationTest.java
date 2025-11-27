package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Organization 단위 테스트")
class OrganizationTest {

    @Nested
    @DisplayName("Admin 조직 생성 테스트")
    class AdminOrganizationTest {

        @Test
        @DisplayName("Admin 조직을 생성할 수 있다")
        void admin_ShouldCreateAdminOrganization() {
            // when
            Organization org = Organization.admin();

            // then
            assertThat(org.id()).isEqualTo(0L);
            assertThat(org.name()).isEqualTo("Connectly Admin");
            assertThat(org.namespace()).isEqualTo("connectly");
            assertThat(org.role()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("Admin 조직인지 확인할 수 있다")
        void isAdmin_WithAdminOrg_ShouldReturnTrue() {
            // given
            Organization org = Organization.admin();

            // then
            assertThat(org.isAdmin()).isTrue();
            assertThat(org.isSeller()).isFalse();
            assertThat(org.isCustomer()).isFalse();
        }
    }

    @Nested
    @DisplayName("Seller 조직 생성 테스트")
    class SellerOrganizationTest {

        @Test
        @DisplayName("Seller 조직을 생성할 수 있다")
        void seller_WithValidParams_ShouldCreateSellerOrganization() {
            // given
            long id = 100L;
            String companyName = "Test Company";

            // when
            Organization org = Organization.seller(id, companyName);

            // then
            assertThat(org.id()).isEqualTo(id);
            assertThat(org.name()).isEqualTo(companyName);
            assertThat(org.namespace()).isEqualTo("setof");
            assertThat(org.role()).isEqualTo(UserRole.SELLER);
        }

        @Test
        @DisplayName("Seller 조직인지 확인할 수 있다")
        void isSeller_WithSellerOrg_ShouldReturnTrue() {
            // given
            Organization org = Organization.seller(1L, "Company");

            // then
            assertThat(org.isSeller()).isTrue();
            assertThat(org.isAdmin()).isFalse();
            assertThat(org.isCustomer()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(longs = {0, -1, -100})
        @DisplayName("Seller 조직 ID가 1 미만이면 예외가 발생한다")
        void seller_WithInvalidId_ShouldThrowException(long invalidId) {
            // when & then
            assertThatThrownBy(() -> Organization.seller(invalidId, "Company"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Seller 조직 ID는 1 이상이어야 합니다");
        }
    }

    @Nested
    @DisplayName("Customer 조직 생성 테스트")
    class CustomerOrganizationTest {

        @Test
        @DisplayName("Customer 조직을 생성할 수 있다")
        void customer_ShouldCreateCustomerOrganization() {
            // when
            Organization org = Organization.customer();

            // then
            assertThat(org.id()).isEqualTo(-1L);
            assertThat(org.name()).isEqualTo("Customer");
            assertThat(org.namespace()).isEqualTo("setof");
            assertThat(org.role()).isEqualTo(UserRole.DEFAULT);
        }

        @Test
        @DisplayName("Customer 조직인지 확인할 수 있다")
        void isCustomer_WithCustomerOrg_ShouldReturnTrue() {
            // given
            Organization org = Organization.customer();

            // then
            assertThat(org.isCustomer()).isTrue();
            assertThat(org.isAdmin()).isFalse();
            assertThat(org.isSeller()).isFalse();
        }
    }

    @Nested
    @DisplayName("of 메서드 생성 테스트")
    class OfMethodTest {

        @Test
        @DisplayName("of 메서드로 조직을 생성할 수 있다")
        void of_WithValidParams_ShouldCreateOrganization() {
            // given
            long id = 0L;
            String name = "Connectly Admin";
            String namespace = "connectly";
            UserRole role = UserRole.ADMIN;

            // when
            Organization org = Organization.of(id, name, namespace, role);

            // then
            assertThat(org.id()).isEqualTo(id);
            assertThat(org.name()).isEqualTo(name);
            assertThat(org.namespace()).isEqualTo(namespace);
            assertThat(org.role()).isEqualTo(role);
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("조직명이 null이거나 빈 문자열이면 예외가 발생한다")
        void constructor_WithInvalidName_ShouldThrowException(String invalidName) {
            // when & then
            assertThatThrownBy(() -> Organization.of(0L, invalidName, "connectly", UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("조직명은 null이거나 빈 문자열일 수 없습니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("네임스페이스가 null이거나 빈 문자열이면 예외가 발생한다")
        void constructor_WithInvalidNamespace_ShouldThrowException(String invalidNamespace) {
            // when & then
            assertThatThrownBy(() -> Organization.of(0L, "Admin", invalidNamespace, UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("네임스페이스는 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("조직 역할이 null이면 예외가 발생한다")
        void constructor_WithNullRole_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> Organization.of(0L, "Admin", "connectly", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("조직 역할은 null일 수 없습니다");
        }

        @Test
        @DisplayName("Admin 조직 (id=0)은 ADMIN role이어야 한다")
        void constructor_AdminOrgWithNonAdminRole_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> Organization.of(0L, "Admin", "connectly", UserRole.SELLER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Admin 조직 (id=0)은 ADMIN role이어야 합니다");
        }

        @Test
        @DisplayName("Admin 조직은 connectly namespace여야 한다")
        void constructor_AdminOrgWithNonConnectlyNamespace_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> Organization.of(0L, "Admin", "setof", UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Admin 조직은 connectly namespace여야 합니다");
        }

        @Test
        @DisplayName("Seller 조직 (id>0)은 SELLER role이어야 한다")
        void constructor_SellerOrgWithNonSellerRole_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> Organization.of(1L, "Company", "setof", UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Seller 조직 (id>0)은 SELLER role이어야 합니다");
        }

        @Test
        @DisplayName("Seller 조직은 setof namespace여야 한다")
        void constructor_SellerOrgWithNonSetofNamespace_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> Organization.of(1L, "Company", "connectly", UserRole.SELLER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Seller 조직은 setof namespace여야 합니다");
        }

        @Test
        @DisplayName("Customer 조직 (id=-1)은 DEFAULT role이어야 한다")
        void constructor_CustomerOrgWithNonDefaultRole_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> Organization.of(-1L, "Customer", "setof", UserRole.SELLER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Customer 조직 (id=-1)은 DEFAULT role이어야 합니다");
        }

        @Test
        @DisplayName("Customer 조직은 setof namespace여야 한다")
        void constructor_CustomerOrgWithNonSetofNamespace_ShouldThrowException() {
            // when & then
            assertThatThrownBy(
                            () -> Organization.of(-1L, "Customer", "connectly", UserRole.DEFAULT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Customer 조직은 setof namespace여야 합니다");
        }
    }

    @Nested
    @DisplayName("S3 경로 테스트")
    class S3PathTest {

        @Test
        @DisplayName("getS3BucketName은 네임스페이스를 반환한다")
        void getS3BucketName_ShouldReturnNamespace() {
            // given
            Organization admin = Organization.admin();
            Organization seller = Organization.seller(1L, "Company");
            Organization customer = Organization.customer();

            // then
            assertThat(admin.getS3BucketName()).isEqualTo("connectly");
            assertThat(seller.getS3BucketName()).isEqualTo("setof");
            assertThat(customer.getS3BucketName()).isEqualTo("setof");
        }

        @Test
        @DisplayName("Admin 조직의 S3 경로 prefix는 admin/이다")
        void getS3PathPrefix_Admin_ShouldReturnAdminPrefix() {
            // given
            Organization org = Organization.admin();

            // then
            assertThat(org.getS3PathPrefix()).isEqualTo("admin/");
        }

        @Test
        @DisplayName("Seller 조직의 S3 경로 prefix는 seller-{id}/이다")
        void getS3PathPrefix_Seller_ShouldReturnSellerPrefix() {
            // given
            Organization org = Organization.seller(100L, "Company");

            // then
            assertThat(org.getS3PathPrefix()).isEqualTo("seller-100/");
        }

        @Test
        @DisplayName("Customer 조직의 S3 경로 prefix는 customer/이다")
        void getS3PathPrefix_Customer_ShouldReturnCustomerPrefix() {
            // given
            Organization org = Organization.customer();

            // then
            assertThat(org.getS3PathPrefix()).isEqualTo("customer/");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 Organization은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Organization org1 = Organization.admin();
            Organization org2 = Organization.admin();

            // when & then
            assertThat(org1).isEqualTo(org2);
            assertThat(org1.hashCode()).isEqualTo(org2.hashCode());
        }

        @Test
        @DisplayName("다른 id를 가진 Organization은 동등하지 않다")
        void equals_WithDifferentId_ShouldNotBeEqual() {
            // given
            Organization org1 = Organization.seller(1L, "Company");
            Organization org2 = Organization.seller(2L, "Company");

            // when & then
            assertThat(org1).isNotEqualTo(org2);
        }
    }
}

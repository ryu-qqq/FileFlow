package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Organization 단위 테스트")
class OrganizationTest {

    @Nested
    @DisplayName("팩토리 메서드 테스트")
    class FactoryMethodTest {

        @Test
        @DisplayName("admin 메서드로 Admin 조직을 생성할 수 있다")
        void admin_ShouldCreateAdminOrganization() {
            // when
            Organization org = Organization.admin();

            // then
            assertThat(org.id()).isNull();
            assertThat(org.name()).isEqualTo("Connectly Admin");
            assertThat(org.namespace()).isEqualTo("connectly");
            assertThat(org.role()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("seller 메서드로 Seller 조직을 생성할 수 있다")
        void seller_ShouldCreateSellerOrganization() {
            // given
            OrganizationId orgId = OrganizationId.generate();

            // when
            Organization org = Organization.seller(orgId, "TestCompany");

            // then
            assertThat(org.id()).isEqualTo(orgId);
            assertThat(org.name()).isEqualTo("TestCompany");
            assertThat(org.namespace()).isEqualTo("setof");
            assertThat(org.role()).isEqualTo(UserRole.SELLER);
        }

        @Test
        @DisplayName("newSeller 메서드로 새로운 Seller 조직을 생성할 수 있다")
        void newSeller_ShouldCreateNewSellerOrganization() {
            // when
            Organization org = Organization.newSeller("TestCompany");

            // then
            assertThat(org.id()).isNotNull();
            assertThat(org.name()).isEqualTo("TestCompany");
            assertThat(org.namespace()).isEqualTo("setof");
            assertThat(org.role()).isEqualTo(UserRole.SELLER);
        }

        @Test
        @DisplayName("customer 메서드로 Customer 조직을 생성할 수 있다")
        void customer_ShouldCreateCustomerOrganization() {
            // when
            Organization org = Organization.customer();

            // then
            assertThat(org.id()).isNull();
            assertThat(org.name()).isEqualTo("Customer");
            assertThat(org.namespace()).isEqualTo("setof");
            assertThat(org.role()).isEqualTo(UserRole.DEFAULT);
        }

        @Test
        @DisplayName("of 메서드로 조직을 생성할 수 있다")
        void of_ShouldCreateOrganization() {
            // given
            OrganizationId orgId = OrganizationId.generate();

            // when
            Organization org = Organization.of(orgId, "Company", "setof", UserRole.SELLER);

            // then
            assertThat(org.id()).isEqualTo(orgId);
            assertThat(org.name()).isEqualTo("Company");
            assertThat(org.namespace()).isEqualTo("setof");
            assertThat(org.role()).isEqualTo(UserRole.SELLER);
        }

        @Test
        @DisplayName("seller ID가 null이면 예외가 발생한다")
        void sellerWithNullId_ShouldThrowException() {
            assertThatThrownBy(() -> Organization.seller(null, "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Seller 조직 ID는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("이름이 null이면 예외가 발생한다")
        void nullName_ShouldThrowException() {
            assertThatThrownBy(() -> new Organization(null, null, "connectly", UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("조직명은 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("이름이 빈 문자열이면 예외가 발생한다")
        void emptyName_ShouldThrowException() {
            assertThatThrownBy(() -> new Organization(null, "", "connectly", UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("조직명은 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("네임스페이스가 null이면 예외가 발생한다")
        void nullNamespace_ShouldThrowException() {
            assertThatThrownBy(() -> new Organization(null, "Test", null, UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("네임스페이스는 null이거나 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("역할이 null이면 예외가 발생한다")
        void nullRole_ShouldThrowException() {
            assertThatThrownBy(() -> new Organization(null, "Test", "connectly", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("조직 역할은 null일 수 없습니다");
        }

        @Test
        @DisplayName("Admin 조직에 OrganizationId가 있어도 정상 생성된다")
        void adminWithId_ShouldSucceed() {
            // given
            OrganizationId orgId = OrganizationId.generate();

            // when
            Organization org = new Organization(orgId, "Test", "connectly", UserRole.ADMIN);

            // then - AuthHub JWT의 oid 클레임으로 OrganizationId가 전달되는 경우 허용
            assertThat(org.id()).isEqualTo(orgId);
            assertThat(org.role()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("Admin 조직에 잘못된 네임스페이스면 예외가 발생한다")
        void adminIdWithWrongNamespace_ShouldThrowException() {
            assertThatThrownBy(() -> new Organization(null, "Test", "setof", UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Admin 조직은 connectly namespace여야 합니다");
        }

        @Test
        @DisplayName("Seller 조직에 OrganizationId가 없으면 예외가 발생한다")
        void sellerWithoutId_ShouldThrowException() {
            assertThatThrownBy(() -> new Organization(null, "Test", "setof", UserRole.SELLER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Seller 조직은 OrganizationId가 필수입니다");
        }

        @Test
        @DisplayName("Seller 조직에 잘못된 네임스페이스면 예외가 발생한다")
        void sellerIdWithWrongNamespace_ShouldThrowException() {
            // given
            OrganizationId orgId = OrganizationId.generate();

            // when & then
            assertThatThrownBy(() -> new Organization(orgId, "Test", "connectly", UserRole.SELLER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Seller 조직은 setof namespace여야 합니다");
        }

        @Test
        @DisplayName("Customer 조직에 OrganizationId가 있으면 예외가 발생한다")
        void customerWithId_ShouldThrowException() {
            // given
            OrganizationId orgId = OrganizationId.generate();

            // when & then
            assertThatThrownBy(() -> new Organization(orgId, "Test", "setof", UserRole.DEFAULT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Customer 조직은 OrganizationId를 가질 수 없습니다");
        }

        @Test
        @DisplayName("Customer 조직에 잘못된 네임스페이스면 예외가 발생한다")
        void customerIdWithWrongNamespace_ShouldThrowException() {
            assertThatThrownBy(() -> new Organization(null, "Test", "connectly", UserRole.DEFAULT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Customer 조직은 setof namespace여야 합니다");
        }
    }

    @Nested
    @DisplayName("조직 유형 확인 테스트")
    class OrganizationTypeTest {

        @Test
        @DisplayName("isAdmin은 Admin 조직일 때만 true")
        void isAdmin_ShouldReturnTrueOnlyForAdmin() {
            assertThat(Organization.admin().isAdmin()).isTrue();
            assertThat(Organization.newSeller("Test").isAdmin()).isFalse();
            assertThat(Organization.customer().isAdmin()).isFalse();
        }

        @Test
        @DisplayName("isSeller는 Seller 조직일 때만 true")
        void isSeller_ShouldReturnTrueOnlyForSeller() {
            assertThat(Organization.admin().isSeller()).isFalse();
            assertThat(Organization.newSeller("Test").isSeller()).isTrue();
            assertThat(Organization.customer().isSeller()).isFalse();
        }

        @Test
        @DisplayName("isCustomer는 Customer 조직일 때만 true")
        void isCustomer_ShouldReturnTrueOnlyForCustomer() {
            assertThat(Organization.admin().isCustomer()).isFalse();
            assertThat(Organization.newSeller("Test").isCustomer()).isFalse();
            assertThat(Organization.customer().isCustomer()).isTrue();
        }
    }

    @Nested
    @DisplayName("S3 경로 테스트")
    class S3PathTest {

        @Test
        @DisplayName("getS3BucketName은 고정 버킷명을 반환한다")
        void getS3BucketName_ShouldReturnFixedBucketName() {
            Organization org = Organization.admin();
            assertThat(org.getS3BucketName()).isEqualTo("fileflow-uploads-prod");
        }

        @Test
        @DisplayName("Admin의 S3 경로 prefix는 connectly/이다")
        void admin_S3PathPrefix_ShouldBeConnectly() {
            Organization org = Organization.admin();
            assertThat(org.getS3PathPrefix()).isEqualTo("connectly/");
        }

        @Test
        @DisplayName("Seller의 S3 경로 prefix는 setof/seller-{id}/이다")
        void seller_S3PathPrefix_ShouldIncludeId() {
            // given
            OrganizationId orgId = OrganizationId.generate();
            Organization org = Organization.seller(orgId, "TestCompany");

            // then
            assertThat(org.getS3PathPrefix()).isEqualTo("setof/seller-" + orgId.value() + "/");
        }

        @Test
        @DisplayName("Customer의 S3 경로 prefix는 setof/customer/이다")
        void customer_S3PathPrefix_ShouldBeCustomer() {
            Organization org = Organization.customer();
            assertThat(org.getS3PathPrefix()).isEqualTo("setof/customer/");
        }
    }

    @Nested
    @DisplayName("CDN 경로 테스트")
    class CdnPathTest {

        @Nested
        @DisplayName("Public S3 Path Prefix (CDN 접근용)")
        class PublicS3PathPrefixTest {

            @Test
            @DisplayName("Admin의 Public 경로는 uploads/connectly/이다")
            void admin_PublicPathPrefix_ShouldHaveUploadsPrefix() {
                // given
                Organization org = Organization.admin();

                // when
                String path = org.getPublicS3PathPrefix();

                // then
                assertThat(path).isEqualTo("uploads/connectly/");
            }

            @Test
            @DisplayName("Seller의 Public 경로는 uploads/setof/seller-{id}/이다")
            void seller_PublicPathPrefix_ShouldHaveUploadsPrefix() {
                // given
                OrganizationId orgId = OrganizationId.generate();
                Organization org = Organization.seller(orgId, "TestCompany");

                // when
                String path = org.getPublicS3PathPrefix();

                // then
                assertThat(path).isEqualTo("uploads/setof/seller-" + orgId.value() + "/");
            }

            @Test
            @DisplayName("Customer의 Public 경로는 uploads/setof/customer/이다")
            void customer_PublicPathPrefix_ShouldHaveUploadsPrefix() {
                // given
                Organization org = Organization.customer();

                // when
                String path = org.getPublicS3PathPrefix();

                // then
                assertThat(path).isEqualTo("uploads/setof/customer/");
            }
        }

        @Nested
        @DisplayName("Internal S3 Path Prefix (내부 전용)")
        class InternalS3PathPrefixTest {

            @Test
            @DisplayName("Admin의 Internal 경로는 internal/connectly/이다")
            void admin_InternalPathPrefix_ShouldHaveInternalPrefix() {
                // given
                Organization org = Organization.admin();

                // when
                String path = org.getInternalS3PathPrefix();

                // then
                assertThat(path).isEqualTo("internal/connectly/");
            }

            @Test
            @DisplayName("Seller의 Internal 경로는 internal/setof/seller-{id}/이다")
            void seller_InternalPathPrefix_ShouldHaveInternalPrefix() {
                // given
                OrganizationId orgId = OrganizationId.generate();
                Organization org = Organization.seller(orgId, "TestCompany");

                // when
                String path = org.getInternalS3PathPrefix();

                // then
                assertThat(path).isEqualTo("internal/setof/seller-" + orgId.value() + "/");
            }

            @Test
            @DisplayName("Customer의 Internal 경로는 internal/setof/customer/이다")
            void customer_InternalPathPrefix_ShouldHaveInternalPrefix() {
                // given
                Organization org = Organization.customer();

                // when
                String path = org.getInternalS3PathPrefix();

                // then
                assertThat(path).isEqualTo("internal/setof/customer/");
            }
        }
    }

    @Nested
    @DisplayName("레코드 동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 Organization은 동등하다")
        void sameValues_ShouldBeEqual() {
            Organization org1 = Organization.admin();
            Organization org2 = Organization.admin();

            assertThat(org1).isEqualTo(org2);
            assertThat(org1.hashCode()).isEqualTo(org2.hashCode());
        }

        @Test
        @DisplayName("다른 이름을 가진 Organization은 동등하지 않다")
        void differentName_ShouldNotBeEqual() {
            // given
            OrganizationId orgId = OrganizationId.generate();
            Organization org1 = Organization.seller(orgId, "Company1");
            Organization org2 = Organization.seller(orgId, "Company2");

            assertThat(org1).isNotEqualTo(org2);
        }

        @Test
        @DisplayName("다른 ID를 가진 Organization은 동등하지 않다")
        void differentId_ShouldNotBeEqual() {
            Organization org1 = Organization.newSeller("Company");
            Organization org2 = Organization.newSeller("Company");

            assertThat(org1).isNotEqualTo(org2);
        }
    }
}

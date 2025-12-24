package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadCategory;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
                                            null,
                                            Organization.admin(),
                                            "admin@test.com",
                                            null,
                                            List.of("ADMIN"),
                                            Collections.emptyList(),
                                            null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("테넌트는 null일 수 없습니다");
        }

        @Test
        @DisplayName("조직이 null이면 예외가 발생한다")
        void nullOrganization_ShouldThrowException() {
            assertThatThrownBy(
                            () ->
                                    new UserContext(
                                            Tenant.connectly(),
                                            null,
                                            "admin@test.com",
                                            null,
                                            List.of("ADMIN"),
                                            Collections.emptyList(),
                                            null))
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
        @DisplayName("Admin에 userId가 있어도 정상 생성된다")
        void adminWithUserId_ShouldSucceed() {
            // given
            UserId userId = UserId.generate();

            // when
            UserContext context =
                    new UserContext(
                            Tenant.connectly(),
                            Organization.admin(),
                            "admin@test.com",
                            userId,
                            List.of("ADMIN"),
                            Collections.emptyList(),
                            null);

            // then
            assertThat(context.userId()).isEqualTo(userId);
            assertThat(context.email()).isEqualTo("admin@test.com");
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
                                            UserId.generate(),
                                            List.of("DEFAULT"),
                                            Collections.emptyList(),
                                            null))
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
            UserContext context = UserContext.seller(organizationId, "Company", "seller@test.com");
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

        @Nested
        @DisplayName("CDN 경로 분기 테스트")
        class CdnPathPrefixTest {

            @Test
            @DisplayName("CDN 접근 필요 카테고리는 uploads/ 접두사를 사용한다 - BANNER")
            void cdnCategory_ShouldUseUploadsPrefix_Banner() {
                // given
                UserContext context = UserContext.admin("admin@test.com");
                LocalDate date = LocalDate.of(2024, 3, 15);

                // when
                S3Key key = context.generateS3Key(UploadCategory.BANNER, "hero.jpg", date);

                // then
                assertThat(key.key()).startsWith("uploads/");
                assertThat(key.key()).contains("connectly/banner/");
            }

            @Test
            @DisplayName("CDN 접근 필요 카테고리는 uploads/ 접두사를 사용한다 - PRODUCT_IMAGE")
            void cdnCategory_ShouldUseUploadsPrefix_ProductImage() {
                // given
                OrganizationId orgId = OrganizationId.generate();
                UserContext context = UserContext.seller(orgId, "Company", "seller@test.com");
                LocalDate date = LocalDate.of(2024, 3, 15);

                // when
                S3Key key =
                        context.generateS3Key(UploadCategory.PRODUCT_IMAGE, "product.jpg", date);

                // then
                assertThat(key.key()).startsWith("uploads/");
                assertThat(key.key()).contains("setof/seller-" + orgId.value() + "/product/");
            }

            @Test
            @DisplayName("CDN 접근 필요 카테고리는 uploads/ 접두사를 사용한다 - HTML")
            void cdnCategory_ShouldUseUploadsPrefix_Html() {
                // given
                UserContext context = UserContext.admin("admin@test.com");
                LocalDate date = LocalDate.of(2024, 3, 15);

                // when
                S3Key key = context.generateS3Key(UploadCategory.HTML, "detail.html", date);

                // then
                assertThat(key.key()).startsWith("uploads/");
                assertThat(key.key()).contains("connectly/html/");
            }

            @Test
            @DisplayName("내부 전용 카테고리는 internal/ 접두사를 사용한다 - EXCEL")
            void internalCategory_ShouldUseInternalPrefix_Excel() {
                // given
                UserContext context = UserContext.admin("admin@test.com");
                LocalDate date = LocalDate.of(2024, 3, 15);

                // when
                S3Key key = context.generateS3Key(UploadCategory.EXCEL, "data.xlsx", date);

                // then
                assertThat(key.key()).startsWith("internal/");
                assertThat(key.key()).contains("connectly/excel/");
            }

            @Test
            @DisplayName("내부 전용 카테고리는 internal/ 접두사를 사용한다 - SALES_MATERIAL")
            void internalCategory_ShouldUseInternalPrefix_SalesMaterial() {
                // given
                OrganizationId orgId = OrganizationId.generate();
                UserContext context = UserContext.seller(orgId, "Company", "seller@test.com");
                LocalDate date = LocalDate.of(2024, 3, 15);

                // when
                S3Key key =
                        context.generateS3Key(UploadCategory.SALES_MATERIAL, "catalog.pdf", date);

                // then
                assertThat(key.key()).startsWith("internal/");
                assertThat(key.key()).contains("setof/seller-" + orgId.value() + "/sales/");
            }

            @Test
            @DisplayName("내부 전용 카테고리는 internal/ 접두사를 사용한다 - DOCUMENT")
            void internalCategory_ShouldUseInternalPrefix_Document() {
                // given
                UserContext context = UserContext.admin("admin@test.com");
                LocalDate date = LocalDate.of(2024, 3, 15);

                // when
                S3Key key = context.generateS3Key(UploadCategory.DOCUMENT, "contract.pdf", date);

                // then
                assertThat(key.key()).startsWith("internal/");
                assertThat(key.key()).contains("connectly/document/");
            }

            @Test
            @DisplayName("Customer도 CDN 접근 조건에 따라 경로가 분기된다")
            void customer_ShouldUseUploadsPrefix() {
                // given
                UserId userId = UserId.generate();
                UserContext context = UserContext.customer(userId);
                LocalDate date = LocalDate.of(2024, 3, 15);

                // when - Customer는 카테고리 없이 사용
                S3Key key = context.generateS3Key(null, "photo.jpg", date);

                // then - Customer는 기본적으로 uploads/ 사용 (공개 이미지)
                assertThat(key.key()).startsWith("uploads/");
                assertThat(key.key()).contains("setof/customer/");
            }
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
            UserContext context = UserContext.seller(organizationId, "Company", "seller@test.com");

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
        @DisplayName("getOrganizationIdAsVO는 조직 ID Value Object를 반환한다")
        void getOrganizationIdAsVO_ShouldReturnOrgIdVO() {
            // given
            OrganizationId orgId = OrganizationId.generate();
            UserId userId = UserId.generate();

            // when & then
            assertThat(UserContext.admin("admin@test.com").getOrganizationIdAsVO()).isNull();
            assertThat(UserContext.seller(orgId, "Company", "seller@test.com").getOrganizationIdAsVO())
                    .isEqualTo(orgId);
            assertThat(UserContext.customer(userId).getOrganizationIdAsVO()).isNull();
        }

        @Test
        @DisplayName("getOrganizationId는 조직 ID 문자열을 반환한다")
        void getOrganizationId_ShouldReturnOrgIdString() {
            // given
            OrganizationId orgId = OrganizationId.generate();
            UserId userId = UserId.generate();

            // when & then
            assertThat(UserContext.admin("admin@test.com").getOrganizationId()).isNull();
            assertThat(UserContext.seller(orgId, "Company", "seller@test.com").getOrganizationId())
                    .isEqualTo(orgId.value());
            assertThat(UserContext.customer(userId).getOrganizationId()).isNull();
        }
    }

    @Nested
    @DisplayName("roles/permissions 테스트")
    class RolesPermissionsTest {

        @Test
        @DisplayName("팩토리 메서드로 생성된 컨텍스트는 기본 역할을 가진다")
        void factoryMethod_ShouldHaveDefaultRoles() {
            // given
            UserContext admin = UserContext.admin("admin@test.com");
            UserContext seller =
                    UserContext.seller(OrganizationId.generate(), "Company", "seller@test.com");
            UserContext customer = UserContext.customer(UserId.generate());

            // then
            assertThat(admin.roles()).containsExactly("ADMIN");
            assertThat(seller.roles()).containsExactly("SELLER");
            assertThat(customer.roles()).containsExactly("DEFAULT");
        }

        @Test
        @DisplayName("roles()는 불변 리스트를 반환한다")
        void roles_ShouldReturnUnmodifiableList() {
            // given
            UserContext context = UserContext.admin("admin@test.com");

            // when & then
            assertThatThrownBy(() -> context.roles().add("SELLER"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("permissions()는 불변 리스트를 반환한다")
        void permissions_ShouldReturnUnmodifiableList() {
            // given
            UserContext context = UserContext.admin("admin@test.com");

            // when & then
            assertThatThrownBy(() -> context.permissions().add("read"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("hasRole 테스트")
    class HasRoleTest {

        @Test
        @DisplayName("hasRole은 역할 존재 여부를 반환한다")
        void hasRole_ShouldReturnTrueIfRoleExists() {
            // given
            UserContext context =
                    UserContext.of(
                            Tenant.connectly(),
                            Organization.admin(),
                            "admin@test.com",
                            null,
                            List.of("ADMIN", "SELLER"),
                            Collections.emptyList());

            // then
            assertThat(context.hasRole("ADMIN")).isTrue();
            assertThat(context.hasRole("SELLER")).isTrue();
            assertThat(context.hasRole("DEFAULT")).isFalse();
        }

        @Test
        @DisplayName("hasRole은 대소문자를 구분하지 않는다")
        void hasRole_ShouldBeCaseInsensitive() {
            // given
            UserContext context = UserContext.admin("admin@test.com");

            // then
            assertThat(context.hasRole("admin")).isTrue();
            assertThat(context.hasRole("Admin")).isTrue();
            assertThat(context.hasRole("ADMIN")).isTrue();
        }

        @Test
        @DisplayName("hasRole에 null이나 빈 문자열을 전달하면 false를 반환한다")
        void hasRole_ShouldReturnFalseForNullOrEmpty() {
            // given
            UserContext context = UserContext.admin("admin@test.com");

            // then
            assertThat(context.hasRole(null)).isFalse();
            assertThat(context.hasRole("")).isFalse();
            assertThat(context.hasRole("   ")).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPermission 테스트")
    class HasPermissionTest {

        @Test
        @DisplayName("hasPermission은 권한 존재 여부를 반환한다")
        void hasPermission_ShouldReturnTrueIfPermissionExists() {
            // given
            UserContext context =
                    UserContext.of(
                            Tenant.connectly(),
                            Organization.admin(),
                            "admin@test.com",
                            null,
                            List.of("ADMIN"),
                            List.of("read", "write", "delete"));

            // then
            assertThat(context.hasPermission("read")).isTrue();
            assertThat(context.hasPermission("write")).isTrue();
            assertThat(context.hasPermission("delete")).isTrue();
            assertThat(context.hasPermission("execute")).isFalse();
        }

        @Test
        @DisplayName("hasPermission은 대소문자를 구분한다")
        void hasPermission_ShouldBeCaseSensitive() {
            // given
            UserContext context =
                    UserContext.of(
                            Tenant.connectly(),
                            Organization.admin(),
                            "admin@test.com",
                            null,
                            List.of("ADMIN"),
                            List.of("Read", "WRITE"));

            // then
            assertThat(context.hasPermission("Read")).isTrue();
            assertThat(context.hasPermission("read")).isFalse();
            assertThat(context.hasPermission("WRITE")).isTrue();
            assertThat(context.hasPermission("write")).isFalse();
        }

        @Test
        @DisplayName("hasPermission에 null이나 빈 문자열을 전달하면 false를 반환한다")
        void hasPermission_ShouldReturnFalseForNullOrEmpty() {
            // given
            UserContext context =
                    UserContext.of(
                            Tenant.connectly(),
                            Organization.admin(),
                            "admin@test.com",
                            null,
                            List.of("ADMIN"),
                            List.of("read"));

            // then
            assertThat(context.hasPermission(null)).isFalse();
            assertThat(context.hasPermission("")).isFalse();
            assertThat(context.hasPermission("   ")).isFalse();
        }
    }

    @Nested
    @DisplayName("hasAnyRole 테스트")
    class HasAnyRoleTest {

        @Test
        @DisplayName("hasAnyRole은 하나라도 매칭되면 true를 반환한다")
        void hasAnyRole_ShouldReturnTrueIfAnyMatches() {
            // given
            UserContext context = UserContext.admin("admin@test.com");

            // then
            assertThat(context.hasAnyRole("SELLER", "ADMIN", "DEFAULT")).isTrue();
            assertThat(context.hasAnyRole("SELLER", "DEFAULT")).isFalse();
        }

        @Test
        @DisplayName("hasAnyRole에 빈 배열을 전달하면 false를 반환한다")
        void hasAnyRole_ShouldReturnFalseForEmptyArray() {
            // given
            UserContext context = UserContext.admin("admin@test.com");

            // then
            assertThat(context.hasAnyRole()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasAnyPermission 테스트")
    class HasAnyPermissionTest {

        @Test
        @DisplayName("hasAnyPermission은 하나라도 매칭되면 true를 반환한다")
        void hasAnyPermission_ShouldReturnTrueIfAnyMatches() {
            // given
            UserContext context =
                    UserContext.of(
                            Tenant.connectly(),
                            Organization.admin(),
                            "admin@test.com",
                            null,
                            List.of("ADMIN"),
                            List.of("read", "write"));

            // then
            assertThat(context.hasAnyPermission("delete", "read")).isTrue();
            assertThat(context.hasAnyPermission("delete", "execute")).isFalse();
        }

        @Test
        @DisplayName("hasAnyPermission에 빈 배열을 전달하면 false를 반환한다")
        void hasAnyPermission_ShouldReturnFalseForEmptyArray() {
            // given
            UserContext context = UserContext.admin("admin@test.com");

            // then
            assertThat(context.hasAnyPermission()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSuperAdmin 테스트")
    class IsSuperAdminTest {

        @Test
        @DisplayName("isSuperAdmin은 SUPER_ADMIN 역할이 있을 때만 true를 반환한다")
        void isSuperAdmin_ShouldReturnTrueOnlyForSuperAdminRole() {
            // given
            UserContext superAdmin =
                    UserContext.of(
                            Tenant.connectly(),
                            Organization.admin(),
                            "super@test.com",
                            null,
                            List.of("SUPER_ADMIN"),
                            Collections.emptyList());
            UserContext admin = UserContext.admin("admin@test.com");
            UserContext seller =
                    UserContext.seller(OrganizationId.generate(), "Company", "seller@test.com");

            // then
            assertThat(superAdmin.isSuperAdmin()).isTrue();
            assertThat(admin.isSuperAdmin()).isFalse();
            assertThat(seller.isSuperAdmin()).isFalse();
        }
    }

    @Nested
    @DisplayName("generateS3KeyWithCustomPath 테스트")
    class GenerateS3KeyWithCustomPathTest {

        @Test
        @DisplayName("SYSTEM 토큰으로 customPath 사용 시 internal prefix가 추가된다")
        void systemToken_withCustomPath_shouldAddInternalPrefix() {
            // given
            UserContext systemContext = UserContext.system();
            String customPath = "applications/seller-123/documents";
            String fileName = "business-license.pdf";

            // when
            S3Key result = systemContext.generateS3KeyWithCustomPath(customPath, fileName);

            // then
            assertThat(result.key())
                    .isEqualTo("internal/applications/seller-123/documents/business-license.pdf");
        }

        @Test
        @DisplayName("customPath가 슬래시로 끝나도 정상 처리된다")
        void customPath_withTrailingSlash_shouldNormalize() {
            // given
            UserContext systemContext = UserContext.system();
            String customPath = "applications/seller-123/documents/";
            String fileName = "business-license.pdf";

            // when
            S3Key result = systemContext.generateS3KeyWithCustomPath(customPath, fileName);

            // then
            assertThat(result.key())
                    .isEqualTo("internal/applications/seller-123/documents/business-license.pdf");
        }

        @Test
        @DisplayName("SYSTEM이 아닌 토큰으로 customPath 사용 시 예외 발생")
        void nonSystemToken_withCustomPath_shouldThrowException() {
            // given
            UserContext adminContext = UserContext.admin("admin@test.com");

            // when & then
            assertThatThrownBy(() -> adminContext.generateS3KeyWithCustomPath("path", "file.pdf"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("SYSTEM");
        }

        @Test
        @DisplayName("Seller 토큰으로 customPath 사용 시 예외 발생")
        void sellerToken_withCustomPath_shouldThrowException() {
            // given
            OrganizationId orgId = OrganizationId.generate();
            UserContext sellerContext = UserContext.seller(orgId, "Company", "seller@test.com");

            // when & then
            assertThatThrownBy(() -> sellerContext.generateS3KeyWithCustomPath("path", "file.pdf"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("SYSTEM");
        }

        @Test
        @DisplayName("customPath가 null이면 예외 발생")
        void nullCustomPath_shouldThrowException() {
            // given
            UserContext systemContext = UserContext.system();

            // when & then
            assertThatThrownBy(() -> systemContext.generateS3KeyWithCustomPath(null, "file.pdf"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("customPath");
        }

        @Test
        @DisplayName("customPath가 빈 문자열이면 예외 발생")
        void emptyCustomPath_shouldThrowException() {
            // given
            UserContext systemContext = UserContext.system();

            // when & then
            assertThatThrownBy(() -> systemContext.generateS3KeyWithCustomPath("", "file.pdf"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("customPath");
        }

        @Test
        @DisplayName("customPath가 공백 문자열이면 예외 발생")
        void blankCustomPath_shouldThrowException() {
            // given
            UserContext systemContext = UserContext.system();

            // when & then
            assertThatThrownBy(() -> systemContext.generateS3KeyWithCustomPath("   ", "file.pdf"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("customPath");
        }

        @Test
        @DisplayName("customPath에 '..'이 포함되면 예외 발생")
        void pathTraversal_shouldThrowException() {
            // given
            UserContext systemContext = UserContext.system();

            // when & then
            assertThatThrownBy(
                            () ->
                                    systemContext.generateS3KeyWithCustomPath(
                                            "applications/../secrets", "file.pdf"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("..");
        }

        @Test
        @DisplayName("customPath가 '/'로 시작하면 예외 발생")
        void absolutePath_shouldThrowException() {
            // given
            UserContext systemContext = UserContext.system();

            // when & then
            assertThatThrownBy(
                            () ->
                                    systemContext.generateS3KeyWithCustomPath(
                                            "/applications/seller-123", "file.pdf"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("/");
        }

        @Test
        @DisplayName("fileName이 null이면 예외 발생")
        void nullFileName_shouldThrowException() {
            // given
            UserContext systemContext = UserContext.system();

            // when & then
            assertThatThrownBy(
                            () ->
                                    systemContext.generateS3KeyWithCustomPath(
                                            "applications/seller-123", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일명");
        }

        @Test
        @DisplayName("fileName이 빈 문자열이면 예외 발생")
        void emptyFileName_shouldThrowException() {
            // given
            UserContext systemContext = UserContext.system();

            // when & then
            assertThatThrownBy(
                            () ->
                                    systemContext.generateS3KeyWithCustomPath(
                                            "applications/seller-123", ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일명");
        }
    }

    @Nested
    @DisplayName("getPrimaryRole 테스트")
    class GetPrimaryRoleTest {

        @Test
        @DisplayName("getPrimaryRole은 역할 목록 중 가장 높은 우선순위를 반환한다")
        void getPrimaryRole_ShouldReturnHighestPriority() {
            // given
            UserContext context =
                    UserContext.of(
                            Tenant.connectly(),
                            Organization.admin(),
                            "admin@test.com",
                            null,
                            List.of("SELLER", "ADMIN", "DEFAULT"),
                            Collections.emptyList());

            // then
            assertThat(context.getPrimaryRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("SUPER_ADMIN이 있으면 getPrimaryRole은 SUPER_ADMIN을 반환한다")
        void getPrimaryRole_ShouldReturnSuperAdminIfPresent() {
            // given
            UserContext context =
                    UserContext.of(
                            Tenant.connectly(),
                            Organization.admin(),
                            "admin@test.com",
                            null,
                            List.of("ADMIN", "SUPER_ADMIN"),
                            Collections.emptyList());

            // then
            assertThat(context.getPrimaryRole()).isEqualTo(UserRole.SUPER_ADMIN);
        }

        @Test
        @DisplayName("팩토리 메서드로 생성된 컨텍스트의 getPrimaryRole은 조직 역할과 일치한다")
        void getPrimaryRole_ShouldMatchOrganizationRoleForFactoryMethods() {
            // given
            UserContext admin = UserContext.admin("admin@test.com");
            UserContext seller =
                    UserContext.seller(OrganizationId.generate(), "Company", "seller@test.com");
            UserContext customer = UserContext.customer(UserId.generate());

            // then
            assertThat(admin.getPrimaryRole()).isEqualTo(UserRole.ADMIN);
            assertThat(seller.getPrimaryRole()).isEqualTo(UserRole.SELLER);
            assertThat(customer.getPrimaryRole()).isEqualTo(UserRole.DEFAULT);
        }
    }

    @Nested
    @DisplayName("serviceName 필드 테스트")
    class ServiceNameTest {

        @Test
        @DisplayName("system() 메서드는 serviceName이 null인 SYSTEM 컨텍스트를 생성한다")
        void system_ShouldCreateSystemContextWithNullServiceName() {
            // when
            UserContext context = UserContext.system();

            // then
            assertThat(context.organization().isSystem()).isTrue();
            assertThat(context.email()).isEqualTo("master@connectly.co.kr");
            assertThat(context.userId()).isNotNull();
            assertThat(context.userId().value()).isEqualTo("019b2b35-3979-75ba-a981-84ae15f0572a");
            assertThat(context.getServiceName()).isNull();
            assertThat(context.isServiceCall()).isFalse();
        }

        @Test
        @DisplayName("system(serviceName) 메서드는 serviceName이 포함된 SYSTEM 컨텍스트를 생성한다")
        void systemWithServiceName_ShouldCreateSystemContextWithServiceName() {
            // given
            String serviceName = "setof-server";

            // when
            UserContext context = UserContext.system(serviceName);

            // then
            assertThat(context.organization().isSystem()).isTrue();
            assertThat(context.email()).isEqualTo("master@connectly.co.kr");
            assertThat(context.userId()).isNotNull();
            assertThat(context.userId().value()).isEqualTo("019b2b35-3979-75ba-a981-84ae15f0572a");
            assertThat(context.getServiceName()).isEqualTo("setof-server");
            assertThat(context.isServiceCall()).isTrue();
        }

        @Test
        @DisplayName("isServiceCall은 serviceName이 있을 때만 true를 반환한다")
        void isServiceCall_ShouldReturnTrueOnlyWhenServiceNameExists() {
            // given
            UserContext withService = UserContext.system("batch-worker");
            UserContext withoutService = UserContext.system();
            UserContext admin = UserContext.admin("admin@test.com");

            // then
            assertThat(withService.isServiceCall()).isTrue();
            assertThat(withoutService.isServiceCall()).isFalse();
            assertThat(admin.isServiceCall()).isFalse();
        }

        @Test
        @DisplayName("getServiceName은 serviceName 값을 반환한다")
        void getServiceName_ShouldReturnServiceNameValue() {
            // given
            UserContext context1 = UserContext.system("partner-admin");
            UserContext context2 = UserContext.system();

            // then
            assertThat(context1.getServiceName()).isEqualTo("partner-admin");
            assertThat(context2.getServiceName()).isNull();
        }

        @Test
        @DisplayName("다른 팩토리 메서드로 생성된 컨텍스트는 serviceName이 null이다")
        void otherFactoryMethods_ShouldHaveNullServiceName() {
            // given
            UserContext admin = UserContext.admin("admin@test.com");
            UserContext seller =
                    UserContext.seller(OrganizationId.generate(), "Company", "seller@test.com");
            UserContext customer = UserContext.customer(UserId.generate());

            // then
            assertThat(admin.getServiceName()).isNull();
            assertThat(admin.isServiceCall()).isFalse();

            assertThat(seller.getServiceName()).isNull();
            assertThat(seller.isServiceCall()).isFalse();

            assertThat(customer.getServiceName()).isNull();
            assertThat(customer.isServiceCall()).isFalse();
        }

        @Test
        @DisplayName("serviceName이 빈 문자열이면 isServiceCall은 false를 반환한다")
        void emptyServiceName_ShouldNotBeConsideredServiceCall() {
            // given - 빈 문자열로 system 생성
            UserContext context = UserContext.system("");

            // then
            assertThat(context.isServiceCall()).isFalse();
            assertThat(context.email()).isEqualTo("master@connectly.co.kr");
        }

        @Test
        @DisplayName("serviceName이 공백 문자열이면 isServiceCall은 false를 반환한다")
        void blankServiceName_ShouldNotBeConsideredServiceCall() {
            // given - 공백 문자열로 system 생성
            UserContext context = UserContext.system("   ");

            // then
            assertThat(context.isServiceCall()).isFalse();
            assertThat(context.email()).isEqualTo("master@connectly.co.kr");
        }
    }
}

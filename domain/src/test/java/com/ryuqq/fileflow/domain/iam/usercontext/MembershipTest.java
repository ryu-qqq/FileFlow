package com.ryuqq.fileflow.domain.iam.usercontext;

import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Membership Value Object 유효성 검증 테스트
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("Membership 테스트")
class MembershipTest {

    private TenantId tenantId;
    private OrganizationId organizationId;

    @BeforeEach
    void setUp() {
        tenantId = new TenantId("tenant-001");
        organizationId = OrganizationId.of(101L);
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 값으로 Membership을 생성할 수 있다")
        void createWithValidValues() {
            // when
            Membership membership = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);

            // then
            assertThat(membership.tenantId()).isEqualTo(tenantId);
            assertThat(membership.organizationId()).isEqualTo(organizationId);
            assertThat(membership.type()).isEqualTo(MembershipType.EMPLOYEE);
        }

        @Test
        @DisplayName("모든 MembershipType으로 Membership을 생성할 수 있다")
        void createWithAllTypes() {
            // EMPLOYEE
            Membership employee = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);
            assertThat(employee.type()).isEqualTo(MembershipType.EMPLOYEE);

            // SELLER_MEMBER
            Membership seller = Membership.of(tenantId, organizationId, MembershipType.SELLER_MEMBER);
            assertThat(seller.type()).isEqualTo(MembershipType.SELLER_MEMBER);

            // GUEST
            Membership guest = Membership.of(tenantId, organizationId, MembershipType.GUEST);
            assertThat(guest.type()).isEqualTo(MembershipType.GUEST);

            // SYSTEM
            Membership system = Membership.of(tenantId, organizationId, MembershipType.SYSTEM);
            assertThat(system.type()).isEqualTo(MembershipType.SYSTEM);
        }

        @Test
        @DisplayName("TenantId가 null이면 예외가 발생한다")
        void createWithNullTenantId() {
            // when & then
            assertThatThrownBy(() -> Membership.of(null, organizationId, MembershipType.EMPLOYEE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant ID는 필수입니다");
        }

        @Test
        @DisplayName("OrganizationId가 null이면 예외가 발생한다")
        void createWithNullOrganizationId() {
            // when & then
            assertThatThrownBy(() -> Membership.of(tenantId, null, MembershipType.EMPLOYEE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization ID는 필수입니다");
        }

        @Test
        @DisplayName("MembershipType이 null이면 예외가 발생한다")
        void createWithNullType() {
            // when & then
            assertThatThrownBy(() -> Membership.of(tenantId, organizationId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Membership 타입은 필수입니다");
        }
    }

    @Nested
    @DisplayName("소속 확인 테스트 (Law of Demeter)")
    class BelongsToTest {

        @Test
        @DisplayName("특정 테넌트에 소속되어 있는지 확인할 수 있다")
        void belongsToTenant() {
            // given
            Membership membership = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);
            TenantId anotherTenantId = new TenantId("tenant-002");

            // when & then
            assertThat(membership.belongsToTenant(tenantId)).isTrue();
            assertThat(membership.belongsToTenant(anotherTenantId)).isFalse();
        }

        @Test
        @DisplayName("특정 조직에 소속되어 있는지 확인할 수 있다")
        void belongsToOrganization() {
            // given
            Membership membership = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);
            OrganizationId anotherOrganizationId = OrganizationId.of(102L);

            // when & then
            assertThat(membership.belongsToOrganization(organizationId)).isTrue();
            assertThat(membership.belongsToOrganization(anotherOrganizationId)).isFalse();
        }

        @Test
        @DisplayName("특정 테넌트와 조직에 소속되어 있는지 확인할 수 있다")
        void belongsTo() {
            // given
            Membership membership = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);
            TenantId anotherTenantId = new TenantId("tenant-002");
            OrganizationId anotherOrganizationId = OrganizationId.of(102L);

            // when & then
            assertThat(membership.belongsTo(tenantId, organizationId)).isTrue();
            assertThat(membership.belongsTo(anotherTenantId, organizationId)).isFalse();
            assertThat(membership.belongsTo(tenantId, anotherOrganizationId)).isFalse();
            assertThat(membership.belongsTo(anotherTenantId, anotherOrganizationId)).isFalse();
        }
    }

    @Nested
    @DisplayName("타입 확인 테스트 (Law of Demeter)")
    class TypeCheckTest {

        @Test
        @DisplayName("시스템 멤버십인지 확인할 수 있다")
        void isSystem() {
            // given
            Membership systemMembership = Membership.of(tenantId, organizationId, MembershipType.SYSTEM);
            Membership employeeMembership = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);

            // when & then
            assertThat(systemMembership.isSystem()).isTrue();
            assertThat(employeeMembership.isSystem()).isFalse();
        }

        @Test
        @DisplayName("게스트 멤버십인지 확인할 수 있다")
        void isGuest() {
            // given
            Membership guestMembership = Membership.of(tenantId, organizationId, MembershipType.GUEST);
            Membership employeeMembership = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);

            // when & then
            assertThat(guestMembership.isGuest()).isTrue();
            assertThat(employeeMembership.isGuest()).isFalse();
        }

        @Test
        @DisplayName("판매자 멤버십인지 확인할 수 있다")
        void isSellerMember() {
            // given
            Membership sellerMembership = Membership.of(tenantId, organizationId, MembershipType.SELLER_MEMBER);
            Membership employeeMembership = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);

            // when & then
            assertThat(sellerMembership.isSellerMember()).isTrue();
            assertThat(employeeMembership.isSellerMember()).isFalse();
        }
    }

    @Nested
    @DisplayName("원시 값 추출 테스트 (Law of Demeter)")
    class ValueExtractionTest {

        @Test
        @DisplayName("Tenant ID 원시 값을 추출할 수 있다")
        void getTenantIdValue() {
            // given
            Membership membership = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);

            // when
            String tenantIdValue = membership.getTenantIdValue();

            // then
            assertThat(tenantIdValue).isEqualTo("tenant-001");
        }

        @Test
        @DisplayName("Organization ID 원시 값을 추출할 수 있다")
        void getOrganizationIdValue() {
            // given
            Membership membership = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);

            // when
            Long organizationIdValue = membership.getOrganizationIdValue();

            // then
            assertThat(organizationIdValue).isEqualTo(101L);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값의 Membership은 동등하다")
        void equalityWithSameValue() {
            // given
            Membership m1 = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);
            Membership m2 = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);

            // when & then
            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("TenantId가 다르면 동등하지 않다")
        void inequalityWithDifferentTenantId() {
            // given
            TenantId anotherTenantId = new TenantId("tenant-002");
            Membership m1 = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);
            Membership m2 = Membership.of(anotherTenantId, organizationId, MembershipType.EMPLOYEE);

            // when & then
            assertThat(m1).isNotEqualTo(m2);
        }

        @Test
        @DisplayName("OrganizationId가 다르면 동등하지 않다")
        void inequalityWithDifferentOrganizationId() {
            // given
            OrganizationId anotherOrganizationId = OrganizationId.of(102L);
            Membership m1 = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);
            Membership m2 = Membership.of(tenantId, anotherOrganizationId, MembershipType.EMPLOYEE);

            // when & then
            assertThat(m1).isNotEqualTo(m2);
        }

        @Test
        @DisplayName("MembershipType이 다르면 동등하지 않다")
        void inequalityWithDifferentType() {
            // given
            Membership m1 = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);
            Membership m2 = Membership.of(tenantId, organizationId, MembershipType.GUEST);

            // when & then
            assertThat(m1).isNotEqualTo(m2);
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("Record로 구현되어 불변 객체이다")
        void isImmutable() {
            // given
            Membership membership = Membership.of(tenantId, organizationId, MembershipType.EMPLOYEE);

            // when & then - 모든 필드는 final이므로 변경 불가능
            assertThat(membership.tenantId()).isEqualTo(tenantId);
            assertThat(membership.organizationId()).isEqualTo(organizationId);
            assertThat(membership.type()).isEqualTo(MembershipType.EMPLOYEE);
        }
    }
}

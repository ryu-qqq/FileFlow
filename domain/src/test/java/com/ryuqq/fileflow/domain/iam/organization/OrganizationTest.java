package com.ryuqq.fileflow.domain.iam.organization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Organization Aggregate Root 비즈니스 로직 테스트
 *
 * @author Claude
 * @since 2025-10-22
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("Organization 테스트")
class OrganizationTest {

    private OrganizationId organizationId;
    private String tenantId;
    private OrgCode orgCode;
    private String orgName;

    @BeforeEach
    void setUp() {
        organizationId = new OrganizationId(1L);
        tenantId = "TENANT-100";
        orgCode = new OrgCode("SALES-KR");
        orgName = "Sales Korea";
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 값으로 Organization을 생성할 수 있다")
        void createWithValidValues() {
            // when
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // then
            assertThat(organization.getId()).isEqualTo(organizationId);
            assertThat(organization.getTenantId()).isEqualTo(tenantId);
            assertThat(organization.getOrgCode()).isEqualTo(orgCode);
            assertThat(organization.getName()).isEqualTo(orgName);
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
            assertThat(organization.isDeleted()).isFalse();
            assertThat(organization.getCreatedAt()).isNotNull();
            assertThat(organization.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("생성 직후 Organization은 활성 상태이다")
        void initiallyActive() {
            // when
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // then
            assertThat(organization.isActive()).isTrue();
        }

        @Test
        @DisplayName("이름 앞뒤 공백이 자동으로 제거된다")
        void trimOrgName() {
            // given
            String nameWithWhitespace = "  Sales Korea  ";

            // when
            Organization organization = new Organization(organizationId, tenantId, orgCode, nameWithWhitespace);

            // then
            assertThat(organization.getName()).isEqualTo("Sales Korea");
        }

        @Test
        @DisplayName("OrganizationId가 null이면 예외가 발생한다")
        void createWithNullId() {
            // when & then
            assertThatThrownBy(() -> new Organization(null, tenantId, orgCode, orgName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization ID는 필수입니다");
        }

        @Test
        @DisplayName("TenantId가 null이면 예외가 발생한다")
        void createWithNullTenantId() {
            // when & then
            assertThatThrownBy(() -> new Organization(organizationId, null, orgCode, orgName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant ID는 필수입니다");
        }

        @Test
        @DisplayName("TenantId가 빈 문자열이면 예외가 발생한다")
        void createWithInvalidTenantId() {
            // when & then
            assertThatThrownBy(() -> new Organization(organizationId, "", orgCode, orgName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant ID는 필수입니다");

            assertThatThrownBy(() -> new Organization(organizationId, "   ", orgCode, orgName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant ID는 필수입니다");
        }

        @Test
        @DisplayName("OrgCode가 null이면 예외가 발생한다")
        void createWithNullOrgCode() {
            // when & then
            assertThatThrownBy(() -> new Organization(organizationId, tenantId, null, orgName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 코드는 필수입니다");
        }

        @Test
        @DisplayName("이름이 null이면 예외가 발생한다")
        void createWithNullName() {
            // when & then
            assertThatThrownBy(() -> new Organization(organizationId, tenantId, orgCode, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 이름은 필수입니다");
        }

        @Test
        @DisplayName("이름이 빈 문자열이면 예외가 발생한다")
        void createWithEmptyName() {
            // when & then
            assertThatThrownBy(() -> new Organization(organizationId, tenantId, orgCode, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 이름은 필수입니다");
        }

        @Test
        @DisplayName("이름이 공백 문자열이면 예외가 발생한다")
        void createWithBlankName() {
            // when & then
            assertThatThrownBy(() -> new Organization(organizationId, tenantId, orgCode, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 이름은 필수입니다");
        }
    }

    @Nested
    @DisplayName("이름 변경 테스트")
    class UpdateNameTest {

        @Test
        @DisplayName("활성 상태의 Organization 이름을 변경할 수 있다")
        void updateName() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            String newName = "Sales Korea Division";

            // when
            organization.updateName(newName);

            // then
            assertThat(organization.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("이름 변경 시 updatedAt이 갱신된다")
        void updateNameUpdatesTimestamp() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            var originalUpdatedAt = organization.getUpdatedAt();

            // when
            organization.updateName("New Name");

            // then
            assertThat(organization.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("이름 변경 시 앞뒤 공백이 제거된다")
        void updateNameTrimsWhitespace() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // when
            organization.updateName("  New Name  ");

            // then
            assertThat(organization.getName()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("삭제된 Organization의 이름은 변경할 수 없다")
        void cannotUpdateNameWhenDeleted() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            organization.softDelete();

            // when & then
            assertThatThrownBy(() -> organization.updateName("New Name"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 Organization은 수정할 수 없습니다");
        }

        @Test
        @DisplayName("null로 이름을 변경하면 예외가 발생한다")
        void cannotUpdateWithNull() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // when & then
            assertThatThrownBy(() -> organization.updateName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 이름은 필수입니다");
        }

        @Test
        @DisplayName("빈 문자열로 이름을 변경하면 예외가 발생한다")
        void cannotUpdateWithEmpty() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // when & then
            assertThatThrownBy(() -> organization.updateName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 이름은 필수입니다");
        }
    }

    @Nested
    @DisplayName("비활성화 테스트")
    class DeactivateTest {

        @Test
        @DisplayName("활성 Organization을 비활성화할 수 있다")
        void deactivate() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // when
            organization.deactivate();

            // then
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
            assertThat(organization.isActive()).isFalse();
        }

        @Test
        @DisplayName("비활성화 시 updatedAt이 갱신된다")
        void deactivateUpdatesTimestamp() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            var originalUpdatedAt = organization.getUpdatedAt();

            // when
            organization.deactivate();

            // then
            assertThat(organization.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("이미 비활성화된 Organization은 다시 비활성화할 수 없다")
        void cannotDeactivateTwice() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            organization.deactivate();

            // when & then
            assertThatThrownBy(organization::deactivate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 비활성화된 Organization입니다");
        }

        @Test
        @DisplayName("삭제된 Organization은 비활성화할 수 없다")
        void cannotDeactivateWhenDeleted() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            organization.softDelete();

            // when & then
            assertThatThrownBy(organization::deactivate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 Organization은 비활성화할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("소프트 삭제 테스트")
    class SoftDeleteTest {

        @Test
        @DisplayName("Organization을 소프트 삭제할 수 있다")
        void softDelete() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // when
            organization.softDelete();

            // then
            assertThat(organization.isDeleted()).isTrue();
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
            assertThat(organization.isActive()).isFalse();
        }

        @Test
        @DisplayName("소프트 삭제 시 updatedAt이 갱신된다")
        void softDeleteUpdatesTimestamp() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            var originalUpdatedAt = organization.getUpdatedAt();

            // when
            organization.softDelete();

            // then
            assertThat(organization.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("소프트 삭제 시 자동으로 INACTIVE 상태로 전환된다")
        void softDeleteSetsInactive() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);

            // when
            organization.softDelete();

            // then
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
        }

        @Test
        @DisplayName("이미 삭제된 Organization은 다시 삭제할 수 없다")
        void cannotDeleteTwice() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            organization.softDelete();

            // when & then
            assertThatThrownBy(organization::softDelete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 삭제된 Organization입니다");
        }
    }

    @Nested
    @DisplayName("isActive 테스트 (Law of Demeter)")
    class IsActiveTest {

        @Test
        @DisplayName("ACTIVE 상태이고 삭제되지 않으면 true를 반환한다")
        void activeAndNotDeleted() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // when & then
            assertThat(organization.isActive()).isTrue();
        }

        @Test
        @DisplayName("INACTIVE 상태이면 false를 반환한다")
        void inactive() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            organization.deactivate();

            // when & then
            assertThat(organization.isActive()).isFalse();
        }

        @Test
        @DisplayName("삭제되었으면 false를 반환한다")
        void deleted() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            organization.softDelete();

            // when & then
            assertThat(organization.isActive()).isFalse();
        }

    }

    @Nested
    @DisplayName("belongsToTenant 테스트 (Law of Demeter)")
    class BelongsToTenantTest {

        @Test
        @DisplayName("같은 TenantId이면 true를 반환한다")
        void belongsToSameTenant() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // when & then
            assertThat(organization.belongsToTenant("TENANT-100")).isTrue();
        }

        @Test
        @DisplayName("다른 TenantId이면 false를 반환한다")
        void doesNotBelongToDifferentTenant() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // when & then
            assertThat(organization.belongsToTenant("TENANT-200")).isFalse();
        }

        @Test
        @DisplayName("null TenantId이면 false를 반환한다")
        void doesNotBelongToNullTenant() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // when & then
            assertThat(organization.belongsToTenant(null)).isFalse();
        }

        @Test
        @DisplayName("String FK 전략: Tenant 객체가 아닌 String ID로 확인한다")
        void stringFkStrategy() {
            // given
            Organization organization = new Organization(organizationId, "TENANT-100", orgCode, orgName);

            // when & then - String 타입으로 직접 비교
            assertThat(organization.getTenantId()).isEqualTo("TENANT-100");
            assertThat(organization.belongsToTenant("TENANT-100")).isTrue();
        }
    }

    @Nested
    @DisplayName("생명주기 통합 테스트")
    class LifecycleTest {

        @Test
        @DisplayName("생성 → 이름변경 → 비활성화 시나리오")
        void createUpdateDeactivate() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // when & then - 초기 활성 상태
            assertThat(organization.isActive()).isTrue();

            // when & then - 이름 변경
            organization.updateName("New Name");
            assertThat(organization.getName()).isEqualTo("New Name");
            assertThat(organization.isActive()).isTrue();

            // when & then - 비활성화
            organization.deactivate();
            assertThat(organization.isActive()).isFalse();
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
        }

        @Test
        @DisplayName("생성 → 이름변경 → 삭제 시나리오")
        void createUpdateDelete() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);

            // when & then - 이름 변경
            organization.updateName("New Organization");
            assertThat(organization.getName()).isEqualTo("New Organization");
            assertThat(organization.isActive()).isTrue();

            // when & then - 소프트 삭제
            organization.softDelete();
            assertThat(organization.isDeleted()).isTrue();
            assertThat(organization.isActive()).isFalse();
        }

        @Test
        @DisplayName("삭제 후에는 모든 수정 작업이 불가능하다")
        void cannotModifyAfterDeletion() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            organization.softDelete();

            // when & then
            assertThatThrownBy(() -> organization.updateName("New Name"))
                .isInstanceOf(IllegalStateException.class);

            assertThatThrownBy(organization::deactivate)
                .isInstanceOf(IllegalStateException.class);

            assertThatThrownBy(organization::softDelete)
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Clock 제어 테스트")
    class ClockControlTest {

        @Test
        @DisplayName("고정된 시간으로 Organization을 생성할 수 있다")
        void createWithFixedTime() {
            // given
            Clock fixedClock = Clock.fixed(
                java.time.Instant.parse("2025-01-01T00:00:00Z"),
                java.time.ZoneId.of("UTC")
            );
            java.time.LocalDateTime expectedTime = java.time.LocalDateTime.ofInstant(
                fixedClock.instant(),
                fixedClock.getZone()
            );

            // when
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName, fixedClock);

            // then
            assertThat(organization.getCreatedAt()).isEqualTo(expectedTime);
            assertThat(organization.getUpdatedAt()).isEqualTo(expectedTime);
        }

        @Test
        @DisplayName("비활성화 시 updatedAt이 갱신된다")
        void deactivateUpdatesTime() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            java.time.LocalDateTime beforeDeactivate = organization.getUpdatedAt();

            // when
            organization.deactivate();

            // then
            assertThat(organization.getUpdatedAt()).isAfterOrEqualTo(beforeDeactivate);
        }

        @Test
        @DisplayName("소프트 삭제 시 updatedAt이 갱신된다")
        void softDeleteUpdatesTime() {
            // given
            Organization organization = new Organization(organizationId, tenantId, orgCode, orgName);
            java.time.LocalDateTime beforeDelete = organization.getUpdatedAt();

            // when
            organization.softDelete();

            // then
            assertThat(organization.getUpdatedAt()).isAfterOrEqualTo(beforeDelete);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 ID를 가진 Organization은 동등하다")
        void equalityWithSameId() {
            // given
            Organization org1 = new Organization(organizationId, tenantId, orgCode, orgName);
            Organization org2 = new Organization(organizationId, "TENANT-200", new OrgCode("OTHER"), "Other Name");

            // when & then
            assertThat(org1).isEqualTo(org2);
            assertThat(org1.hashCode()).isEqualTo(org2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 Organization은 동등하지 않다")
        void inequalityWithDifferentId() {
            // given
            OrganizationId otherId = new OrganizationId(2L);
            Organization org1 = new Organization(organizationId, tenantId, orgCode, orgName);
            Organization org2 = new Organization(otherId, tenantId, orgCode, orgName);

            // when & then
            assertThat(org1).isNotEqualTo(org2);
        }
    }
}

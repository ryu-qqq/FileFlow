package com.ryuqq.fileflow.domain.iam.usercontext;

import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserContext Aggregate Root 비즈니스 로직 테스트
 *
 * <p>특히 다중 조직 소속 시나리오를 중점적으로 테스트합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("UserContext 테스트")
class UserContextTest {

    private UserContextId userContextId;
    private ExternalUserId externalUserId;
    private Email email;
    private TenantId tenantId1;
    private TenantId tenantId2;
    private OrganizationId organizationId1;
    private OrganizationId organizationId2;
    private OrganizationId organizationId3;

    @BeforeEach
    void setUp() {
        userContextId = UserContextId.of(1L);
        externalUserId = ExternalUserId.of("auth0|507f1f77bcf86cd799439011");
        email = Email.of("user@example.com");
        tenantId1 = new TenantId("tenant-001");
        tenantId2 = new TenantId("tenant-002");
        organizationId1 = OrganizationId.of(101L);
        organizationId2 = OrganizationId.of(102L);
        organizationId3 = OrganizationId.of(103L);
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 값으로 UserContext를 생성할 수 있다")
        void createWithValidValues() {
            // when
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // then
            assertThat(userContext.getIdValue()).isEqualTo(1L);
            assertThat(userContext.getExternalUserIdValue()).isEqualTo("auth0|507f1f77bcf86cd799439011");
            assertThat(userContext.getEmailValue()).isEqualTo("user@example.com");
            assertThat(userContext.isDeleted()).isFalse();
            assertThat(userContext.getMembershipCount()).isEqualTo(0);
            assertThat(userContext.getCreatedAt()).isNotNull();
            assertThat(userContext.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("생성 직후 UserContext는 삭제되지 않은 상태이다")
        void initiallyNotDeleted() {
            // when
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // then
            assertThat(userContext.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("생성 직후 멤버십이 없는 상태이다")
        void initiallyNoMemberships() {
            // when
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // then
            assertThat(userContext.getMembershipCount()).isEqualTo(0);
            assertThat(userContext.getMemberships()).isEmpty();
        }

        @Test
        @DisplayName("UserContextId가 null이면 예외가 발생한다")
        void createWithNullId() {
            // when & then
            assertThatThrownBy(() -> UserContext.of(null, externalUserId, email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserContext ID는 필수입니다");
        }

        @Test
        @DisplayName("ExternalUserId가 null이면 예외가 발생한다")
        void createWithNullExternalUserId() {
            // when & then
            assertThatThrownBy(() -> UserContext.of(userContextId, null, email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("External User ID는 필수입니다");
        }

        @Test
        @DisplayName("Email이 null이면 예외가 발생한다")
        void createWithNullEmail() {
            // when & then
            assertThatThrownBy(() -> UserContext.of(userContextId, externalUserId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 주소는 필수입니다");
        }
    }

    @Nested
    @DisplayName("멤버십 추가 테스트 (다중 조직 소속 시나리오)")
    class AddMembershipTest {

        @Test
        @DisplayName("UserContext에 멤버십을 추가할 수 있다")
        void addMembership() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);

            // when
            userContext.addMembership(membership);

            // then
            assertThat(userContext.getMembershipCount()).isEqualTo(1);
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId1)).isTrue();
        }

        @Test
        @DisplayName("동일 사용자가 여러 테넌트의 조직에 소속될 수 있다")
        void addMultipleMembershipsAcrossTenants() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Membership membership1 = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            Membership membership2 = Membership.of(tenantId2, organizationId2, MembershipType.SELLER_MEMBER);

            // when
            userContext.addMembership(membership1);
            userContext.addMembership(membership2);

            // then
            assertThat(userContext.getMembershipCount()).isEqualTo(2);
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId1)).isTrue();
            assertThat(userContext.hasMembershipIn(tenantId2, organizationId2)).isTrue();
        }

        @Test
        @DisplayName("동일 테넌트 내 여러 조직에 소속될 수 있다")
        void addMultipleMembershipsWithinSameTenant() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Membership membership1 = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            Membership membership2 = Membership.of(tenantId1, organizationId2, MembershipType.EMPLOYEE);
            Membership membership3 = Membership.of(tenantId1, organizationId3, MembershipType.GUEST);

            // when
            userContext.addMembership(membership1);
            userContext.addMembership(membership2);
            userContext.addMembership(membership3);

            // then
            assertThat(userContext.getMembershipCount()).isEqualTo(3);
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId1)).isTrue();
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId2)).isTrue();
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId3)).isTrue();
        }

        @Test
        @DisplayName("복잡한 다중 테넌트 다중 조직 시나리오")
        void complexMultiTenantMultiOrganizationScenario() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // tenant-001의 조직 2개
            Membership m1 = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            Membership m2 = Membership.of(tenantId1, organizationId2, MembershipType.GUEST);

            // tenant-002의 조직 2개
            Membership m3 = Membership.of(tenantId2, organizationId1, MembershipType.SELLER_MEMBER);
            Membership m4 = Membership.of(tenantId2, organizationId3, MembershipType.SYSTEM);

            // when
            userContext.addMembership(m1);
            userContext.addMembership(m2);
            userContext.addMembership(m3);
            userContext.addMembership(m4);

            // then
            assertThat(userContext.getMembershipCount()).isEqualTo(4);

            // tenant-001 검증
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId1)).isTrue();
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId2)).isTrue();

            // tenant-002 검증
            assertThat(userContext.hasMembershipIn(tenantId2, organizationId1)).isTrue();
            assertThat(userContext.hasMembershipIn(tenantId2, organizationId3)).isTrue();

            // 존재하지 않는 조합 검증
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId3)).isFalse();
            assertThat(userContext.hasMembershipIn(tenantId2, organizationId2)).isFalse();
        }

        @Test
        @DisplayName("중복된 멤버십을 추가하면 예외가 발생한다")
        void cannotAddDuplicateMembership() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            userContext.addMembership(membership);

            // when & then
            Membership duplicateMembership = Membership.of(tenantId1, organizationId1, MembershipType.GUEST);
            assertThatThrownBy(() -> userContext.addMembership(duplicateMembership))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 해당 테넌트와 조직에 멤버십이 존재합니다");
        }

        @Test
        @DisplayName("null 멤버십을 추가하면 예외가 발생한다")
        void cannotAddNullMembership() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // when & then
            assertThatThrownBy(() -> userContext.addMembership(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("멤버십은 필수입니다");
        }

        @Test
        @DisplayName("삭제된 UserContext에 멤버십을 추가할 수 없다")
        void cannotAddMembershipWhenDeleted() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            userContext.softDelete();
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);

            // when & then
            assertThatThrownBy(() -> userContext.addMembership(membership))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 UserContext에 멤버십을 추가할 수 없습니다");
        }

        @Test
        @DisplayName("멤버십 추가 시 updatedAt이 갱신된다")
        void addMembershipUpdatesTimestamp() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            LocalDateTime originalUpdatedAt = userContext.getUpdatedAt();
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);

            // when
            userContext.addMembership(membership);

            // then
            assertThat(userContext.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("멤버십 철회 테스트")
    class RevokeMembershipTest {

        @Test
        @DisplayName("UserContext에서 멤버십을 철회할 수 있다")
        void revokeMembership() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            userContext.addMembership(membership);

            // when
            userContext.revokeMembership(tenantId1, organizationId1);

            // then
            assertThat(userContext.getMembershipCount()).isEqualTo(0);
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId1)).isFalse();
        }

        @Test
        @DisplayName("여러 멤버십 중 특정 멤버십만 철회할 수 있다")
        void revokeSpecificMembership() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Membership m1 = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            Membership m2 = Membership.of(tenantId1, organizationId2, MembershipType.GUEST);
            Membership m3 = Membership.of(tenantId2, organizationId1, MembershipType.SELLER_MEMBER);
            userContext.addMembership(m1);
            userContext.addMembership(m2);
            userContext.addMembership(m3);

            // when - organizationId2만 철회
            userContext.revokeMembership(tenantId1, organizationId2);

            // then
            assertThat(userContext.getMembershipCount()).isEqualTo(2);
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId1)).isTrue();
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId2)).isFalse(); // 철회됨
            assertThat(userContext.hasMembershipIn(tenantId2, organizationId1)).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 멤버십을 철회하면 예외가 발생한다")
        void cannotRevokeNonExistentMembership() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // when & then
            assertThatThrownBy(() -> userContext.revokeMembership(tenantId1, organizationId1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("해당 테넌트와 조직의 멤버십이 존재하지 않습니다");
        }

        @Test
        @DisplayName("null TenantId로 철회하면 예외가 발생한다")
        void cannotRevokeWithNullTenantId() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // when & then
            assertThatThrownBy(() -> userContext.revokeMembership(null, organizationId1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant ID는 필수입니다");
        }

        @Test
        @DisplayName("null OrganizationId로 철회하면 예외가 발생한다")
        void cannotRevokeWithNullOrganizationId() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // when & then
            assertThatThrownBy(() -> userContext.revokeMembership(tenantId1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization ID는 필수입니다");
        }

        @Test
        @DisplayName("삭제된 UserContext에서 멤버십을 철회할 수 없다")
        void cannotRevokeMembershipWhenDeleted() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            userContext.addMembership(membership);
            userContext.softDelete();

            // when & then
            assertThatThrownBy(() -> userContext.revokeMembership(tenantId1, organizationId1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 UserContext의 멤버십은 철회할 수 없습니다");
        }

        @Test
        @DisplayName("멤버십 철회 시 updatedAt이 갱신된다")
        void revokeMembershipUpdatesTimestamp() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            userContext.addMembership(membership);
            LocalDateTime originalUpdatedAt = userContext.getUpdatedAt();

            // when
            userContext.revokeMembership(tenantId1, organizationId1);

            // then
            assertThat(userContext.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("이메일 변경 테스트")
    class UpdateEmailTest {

        @Test
        @DisplayName("UserContext의 이메일을 변경할 수 있다")
        void updateEmail() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Email newEmail = Email.of("newemail@example.com");

            // when
            userContext.updateEmail(newEmail);

            // then
            assertThat(userContext.getEmailValue()).isEqualTo("newemail@example.com");
        }

        @Test
        @DisplayName("이메일 변경 시 updatedAt이 갱신된다")
        void updateEmailUpdatesTimestamp() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            LocalDateTime originalUpdatedAt = userContext.getUpdatedAt();
            Email newEmail = Email.of("newemail@example.com");

            // when
            userContext.updateEmail(newEmail);

            // then
            assertThat(userContext.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("null로 이메일을 변경하면 예외가 발생한다")
        void cannotUpdateWithNull() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // when & then
            assertThatThrownBy(() -> userContext.updateEmail(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 이메일 주소는 필수입니다");
        }

        @Test
        @DisplayName("삭제된 UserContext의 이메일은 변경할 수 없다")
        void cannotUpdateEmailWhenDeleted() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            userContext.softDelete();
            Email newEmail = Email.of("newemail@example.com");

            // when & then
            assertThatThrownBy(() -> userContext.updateEmail(newEmail))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 UserContext의 이메일은 변경할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("소프트 삭제 테스트")
    class SoftDeleteTest {

        @Test
        @DisplayName("UserContext를 소프트 삭제할 수 있다")
        void softDelete() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // when
            userContext.softDelete();

            // then
            assertThat(userContext.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("소프트 삭제 시 updatedAt이 갱신된다")
        void softDeleteUpdatesTimestamp() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            LocalDateTime originalUpdatedAt = userContext.getUpdatedAt();

            // when
            userContext.softDelete();

            // then
            assertThat(userContext.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("이미 삭제된 UserContext는 다시 삭제할 수 없다")
        void cannotDeleteTwice() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            userContext.softDelete();

            // when & then
            assertThatThrownBy(userContext::softDelete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 삭제된 UserContext입니다");
        }

        @Test
        @DisplayName("삭제 후에도 기존 멤버십 정보는 조회 가능하다")
        void membershipAccessibleAfterDeletion() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            userContext.addMembership(membership);

            // when
            userContext.softDelete();

            // then - 조회는 가능
            assertThat(userContext.getMembershipCount()).isEqualTo(1);
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId1)).isTrue();
        }
    }

    @Nested
    @DisplayName("멤버십 조회 테스트 (Law of Demeter)")
    class MembershipQueryTest {

        @Test
        @DisplayName("특정 테넌트와 조직의 멤버십 존재 여부를 확인할 수 있다")
        void hasMembershipIn() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            userContext.addMembership(membership);

            // when & then
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId1)).isTrue();
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId2)).isFalse();
            assertThat(userContext.hasMembershipIn(tenantId2, organizationId1)).isFalse();
        }

        @Test
        @DisplayName("멤버십 개수를 조회할 수 있다")
        void getMembershipCount() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // when & then - 초기 상태
            assertThat(userContext.getMembershipCount()).isEqualTo(0);

            // when & then - 멤버십 추가
            userContext.addMembership(Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE));
            assertThat(userContext.getMembershipCount()).isEqualTo(1);

            userContext.addMembership(Membership.of(tenantId1, organizationId2, MembershipType.GUEST));
            assertThat(userContext.getMembershipCount()).isEqualTo(2);

            userContext.addMembership(Membership.of(tenantId2, organizationId1, MembershipType.SELLER_MEMBER));
            assertThat(userContext.getMembershipCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("멤버십 리스트는 방어적 복사로 반환된다")
        void getMembershipsIsDefensivelyCopied() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            userContext.addMembership(membership);

            // when
            var memberships = userContext.getMemberships();

            // then - 외부에서 수정 불가능
            assertThatThrownBy(() -> memberships.add(Membership.of(tenantId2, organizationId2, MembershipType.GUEST)))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("생명주기 통합 테스트")
    class LifecycleTest {

        @Test
        @DisplayName("생성 → 멤버십 추가 → 멤버십 철회 → 삭제 시나리오")
        void createAddRevokDelete() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // when & then - 초기 상태
            assertThat(userContext.getMembershipCount()).isEqualTo(0);

            // when & then - 멤버십 추가
            Membership m1 = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            Membership m2 = Membership.of(tenantId1, organizationId2, MembershipType.GUEST);
            userContext.addMembership(m1);
            userContext.addMembership(m2);
            assertThat(userContext.getMembershipCount()).isEqualTo(2);

            // when & then - 멤버십 철회
            userContext.revokeMembership(tenantId1, organizationId2);
            assertThat(userContext.getMembershipCount()).isEqualTo(1);
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId1)).isTrue();

            // when & then - 소프트 삭제
            userContext.softDelete();
            assertThat(userContext.isDeleted()).isTrue();
            assertThat(userContext.getMembershipCount()).isEqualTo(1); // 멤버십은 유지
        }

        @Test
        @DisplayName("다중 테넌트 멤버십 추가/철회 복합 시나리오")
        void complexMultiTenantLifecycle() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email);

            // tenant-001에 3개 조직 추가
            userContext.addMembership(Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE));
            userContext.addMembership(Membership.of(tenantId1, organizationId2, MembershipType.GUEST));
            userContext.addMembership(Membership.of(tenantId1, organizationId3, MembershipType.SYSTEM));

            // tenant-002에 2개 조직 추가
            userContext.addMembership(Membership.of(tenantId2, organizationId1, MembershipType.SELLER_MEMBER));
            userContext.addMembership(Membership.of(tenantId2, organizationId2, MembershipType.EMPLOYEE));

            // when & then - 총 5개 멤버십
            assertThat(userContext.getMembershipCount()).isEqualTo(5);

            // tenant-001의 organizationId2 철회
            userContext.revokeMembership(tenantId1, organizationId2);
            assertThat(userContext.getMembershipCount()).isEqualTo(4);
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId2)).isFalse();

            // tenant-002의 organizationId1 철회
            userContext.revokeMembership(tenantId2, organizationId1);
            assertThat(userContext.getMembershipCount()).isEqualTo(3);
            assertThat(userContext.hasMembershipIn(tenantId2, organizationId1)).isFalse();

            // 이메일 변경
            Email newEmail = Email.of("newemail@example.com");
            userContext.updateEmail(newEmail);
            assertThat(userContext.getEmailValue()).isEqualTo("newemail@example.com");

            // 최종 상태 검증
            assertThat(userContext.getMembershipCount()).isEqualTo(3);
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId1)).isTrue();
            assertThat(userContext.hasMembershipIn(tenantId1, organizationId3)).isTrue();
            assertThat(userContext.hasMembershipIn(tenantId2, organizationId2)).isTrue();
        }
    }

    @Nested
    @DisplayName("시간 제어 테스트 (Clock)")
    class ClockTest {

        private Clock fixedClock;
        private LocalDateTime fixedTime;

        @BeforeEach
        void setUp() {
            fixedClock = Clock.fixed(Instant.parse("2025-10-24T00:00:00Z"), ZoneId.of("UTC"));
            fixedTime = LocalDateTime.ofInstant(fixedClock.instant(), ZoneId.of("UTC"));
        }

        @Test
        @DisplayName("고정된 시간으로 UserContext를 생성할 수 있다")
        void createWithFixedClock() {
            // when
            UserContext userContext = UserContext.of(userContextId, externalUserId, email, fixedClock);

            // then
            assertThat(userContext.getCreatedAt()).isEqualTo(fixedTime);
            assertThat(userContext.getUpdatedAt()).isEqualTo(fixedTime);
        }

        @Test
        @DisplayName("고정된 시간으로 멤버십 추가 시 예측 가능한 시간이 기록된다")
        void addMembershipWithFixedClock() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email, fixedClock);
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);

            // when
            userContext.addMembership(membership);

            // then
            assertThat(userContext.getUpdatedAt()).isEqualTo(fixedTime);
        }

        @Test
        @DisplayName("고정된 시간으로 멤버십 철회 시 예측 가능한 시간이 기록된다")
        void revokeMembershipWithFixedClock() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email, fixedClock);
            Membership membership = Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE);
            userContext.addMembership(membership);

            // when
            userContext.revokeMembership(tenantId1, organizationId1);

            // then
            assertThat(userContext.getUpdatedAt()).isEqualTo(fixedTime);
        }

        @Test
        @DisplayName("고정된 시간으로 이메일 변경 시 예측 가능한 시간이 기록된다")
        void updateEmailWithFixedClock() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email, fixedClock);
            Email newEmail = Email.of("newemail@example.com");

            // when
            userContext.updateEmail(newEmail);

            // then
            assertThat(userContext.getUpdatedAt()).isEqualTo(fixedTime);
        }

        @Test
        @DisplayName("고정된 시간으로 softDelete 시 예측 가능한 시간이 기록된다")
        void softDeleteWithFixedClock() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email, fixedClock);

            // when
            userContext.softDelete();

            // then
            assertThat(userContext.isDeleted()).isTrue();
            assertThat(userContext.getUpdatedAt()).isEqualTo(fixedTime);
        }

        @Test
        @DisplayName("시간이 변경되어도 동일한 Clock을 사용하면 시간이 일관되게 유지된다")
        void consistentTimeWithSameClock() {
            // given
            UserContext userContext = UserContext.of(userContextId, externalUserId, email, fixedClock);
            LocalDateTime createdAt = userContext.getCreatedAt();

            // when - 여러 작업 수행
            userContext.addMembership(Membership.of(tenantId1, organizationId1, MembershipType.EMPLOYEE));
            userContext.addMembership(Membership.of(tenantId1, organizationId2, MembershipType.GUEST));
            userContext.updateEmail(Email.of("newemail@example.com"));
            userContext.revokeMembership(tenantId1, organizationId2);

            // then - 모든 시간이 고정된 시간과 동일
            assertThat(userContext.getCreatedAt()).isEqualTo(fixedTime);
            assertThat(userContext.getUpdatedAt()).isEqualTo(fixedTime);
            assertThat(createdAt).isEqualTo(fixedTime);
        }
    }
}

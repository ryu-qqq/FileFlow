package com.ryuqq.fileflow.domain.iam.usercontext;

import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.usercontext.fixture.UserContextFixture;
import com.ryuqq.fileflow.domain.iam.usercontext.fixture.MembershipFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * UserContext Domain Aggregate 단위 테스트
 *
 * <p>테스트 범위:</p>
 * <ul>
 *   <li>Happy Path: 정상 생성 및 비즈니스 메서드</li>
 *   <li>Edge Cases: 경계값 테스트</li>
 *   <li>Exception Cases: 예외 상황 처리</li>
 *   <li>Invariant Validation: 불변식 검증</li>
 *   <li>Law of Demeter 준수 확인</li>
 *   <li>Membership 관리 테스트</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@DisplayName("UserContext Domain 단위 테스트")
class UserContextTest {

    // ===== Happy Path Tests =====

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreationTests {

        @Test
        @DisplayName("신규 UserContext 생성 성공 (forNew)")
        void shouldCreateNewUserContext() {
            // Given
            String externalUserId = "auth0|test-user-001";
            String email = "test@example.com";

            // When
            UserContext userContext = UserContextFixture.createNew(externalUserId, email);

            // Then
            assertThat(userContext.getIdValue()).isNull(); // 신규 생성은 ID 없음
            assertThat(userContext.getExternalUserIdValue()).isEqualTo(externalUserId);
            assertThat(userContext.getEmailValue()).isEqualTo(email);
            assertThat(userContext.isDeleted()).isFalse();
            assertThat(userContext.getMembershipCount()).isZero();
        }

        @Test
        @DisplayName("ID가 있는 UserContext 생성 성공 (of)")
        void shouldCreateUserContextWithId() {
            // Given
            Long id = 100L;
            String externalUserId = "auth0|test-user-001";
            String email = "test@example.com";

            // When
            UserContext userContext = UserContextFixture.createWithId(id, externalUserId, email);

            // Then
            assertThat(userContext.getIdValue()).isEqualTo(id);
            assertThat(userContext.getExternalUserIdValue()).isEqualTo(externalUserId);
            assertThat(userContext.getEmailValue()).isEqualTo(email);
        }

        @Test
        @DisplayName("Fixture를 통한 여러 UserContext 생성")
        void shouldCreateMultipleUserContexts() {
            // When
            var userContexts = UserContextFixture.createMultiple(3);

            // Then
            assertThat(userContexts).hasSize(3);
            assertThat(userContexts.get(0).getExternalUserIdValue()).isEqualTo("auth0|test-user-001");
            assertThat(userContexts.get(1).getExternalUserIdValue()).isEqualTo("auth0|test-user-002");
            assertThat(userContexts.get(2).getExternalUserIdValue()).isEqualTo("auth0|test-user-003");
        }
    }

    @Nested
    @DisplayName("이메일 변경 테스트 (Happy Path)")
    class EmailUpdateTests {

        @Test
        @DisplayName("이메일 변경 성공")
        void shouldUpdateEmail() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);
            Email newEmail = Email.of("updated@example.com");
            LocalDateTime beforeUpdate = userContext.getUpdatedAt();

            // When
            userContext.updateEmail(newEmail);

            // Then
            assertThat(userContext.getEmailValue()).isEqualTo("updated@example.com");
            assertThat(userContext.getUpdatedAt()).isAfter(beforeUpdate);
        }
    }

    @Nested
    @DisplayName("멤버십 관리 테스트 (Happy Path)")
    class MembershipManagementTests {

        @Test
        @DisplayName("멤버십 추가 성공")
        void shouldAddMembership() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);
            Membership membership = MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE);

            // When
            userContext.addMembership(membership);

            // Then
            assertThat(userContext.getMembershipCount()).isEqualTo(1);
            assertThat(userContext.hasMembershipIn(TenantId.of(1L), OrganizationId.of(1L))).isTrue();
        }

        @Test
        @DisplayName("여러 멤버십 추가 성공")
        void shouldAddMultipleMemberships() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);
            Membership membership1 = MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE);
            Membership membership2 = MembershipFixture.create(1L, 2L, MembershipType.EMPLOYEE);
            Membership membership3 = MembershipFixture.create(2L, 1L, MembershipType.GUEST);

            // When
            userContext.addMembership(membership1);
            userContext.addMembership(membership2);
            userContext.addMembership(membership3);

            // Then
            assertThat(userContext.getMembershipCount()).isEqualTo(3);
            assertThat(userContext.hasMembershipIn(TenantId.of(1L))).isTrue();
            assertThat(userContext.hasMembershipIn(TenantId.of(2L))).isTrue();
        }

        @Test
        @DisplayName("멤버십 해지 성공")
        void shouldRevokeMembership() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);
            Membership membership = MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE);
            userContext.addMembership(membership);

            // When
            userContext.revokeMembership(TenantId.of(1L), OrganizationId.of(1L));

            // Then
            assertThat(userContext.getMembershipCount()).isZero();
            assertThat(userContext.hasMembershipIn(TenantId.of(1L), OrganizationId.of(1L))).isFalse();
        }

        @Test
        @DisplayName("특정 Tenant의 멤버십 확인")
        void shouldCheckMembershipInTenant() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);
            userContext.addMembership(MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE));
            userContext.addMembership(MembershipFixture.create(1L, 2L, MembershipType.EMPLOYEE));

            // When
            boolean hasMembershipInTenant1 = userContext.hasMembershipIn(TenantId.of(1L));
            boolean hasMembershipInTenant2 = userContext.hasMembershipIn(TenantId.of(2L));

            // Then
            assertThat(hasMembershipInTenant1).isTrue();
            assertThat(hasMembershipInTenant2).isFalse();
        }

        @Test
        @DisplayName("특정 조직의 멤버십 확인")
        void shouldCheckMembershipInOrganization() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);
            userContext.addMembership(MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE));

            // When
            boolean hasMembership = userContext.hasMembershipIn(TenantId.of(1L), OrganizationId.of(1L));
            boolean noMembership = userContext.hasMembershipIn(TenantId.of(1L), OrganizationId.of(2L));

            // Then
            assertThat(hasMembership).isTrue();
            assertThat(noMembership).isFalse();
        }
    }

    @Nested
    @DisplayName("소프트 삭제 테스트 (Happy Path)")
    class SoftDeleteTests {

        @Test
        @DisplayName("UserContext 소프트 삭제 성공")
        void shouldSoftDeleteUserContext() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);

            // When
            userContext.softDelete();

            // Then
            assertThat(userContext.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue()로 ID 직접 접근 (체이닝 방지)")
        void shouldGetIdValueDirectly() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);

            // When
            Long idValue = userContext.getIdValue();

            // Then
            assertThat(idValue).isEqualTo(1L);
            // ✅ Good: userContext.getIdValue()
            // ❌ Bad: userContext.getId().value()
        }

        @Test
        @DisplayName("getExternalUserIdValue()로 외부 사용자 ID 직접 접근 (체이닝 방지)")
        void shouldGetExternalUserIdValueDirectly() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);

            // When
            String externalUserIdValue = userContext.getExternalUserIdValue();

            // Then
            assertThat(externalUserIdValue).isEqualTo("auth0|test-user-001");
            // ✅ Good: userContext.getExternalUserIdValue()
            // ❌ Bad: userContext.getExternalUserId().value()
        }

        @Test
        @DisplayName("getEmailValue()로 이메일 직접 접근 (체이닝 방지)")
        void shouldGetEmailValueDirectly() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);

            // When
            String emailValue = userContext.getEmailValue();

            // Then
            assertThat(emailValue).isEqualTo("test@example.com");
            // ✅ Good: userContext.getEmailValue()
            // ❌ Bad: userContext.getEmail().value()
        }

        @Test
        @DisplayName("getMembershipCount()로 멤버십 개수 확인 (컬렉션 노출 방지)")
        void shouldGetMembershipCountDirectly() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);
            userContext.addMembership(MembershipFixture.create());

            // When
            int count = userContext.getMembershipCount();

            // Then
            assertThat(count).isEqualTo(1);
            // ✅ Good: userContext.getMembershipCount()
            // ❌ Bad: userContext.getMemberships().size()
        }

        @Test
        @DisplayName("hasMembershipIn()로 멤버십 확인 (Tell, Don't Ask)")
        void shouldCheckMembershipDirectly() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);
            userContext.addMembership(MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE));

            // When
            boolean hasMembership = userContext.hasMembershipIn(TenantId.of(1L), OrganizationId.of(1L));

            // Then
            assertThat(hasMembership).isTrue();
            // ✅ Good: userContext.hasMembershipIn(tenantId, organizationId)
            // ❌ Bad: userContext.getMemberships().stream().anyMatch(...)
        }

        @Test
        @DisplayName("getMemberships()는 불변 리스트 반환 (방어적 복사)")
        void shouldReturnUnmodifiableListOfMemberships() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);
            userContext.addMembership(MembershipFixture.create());

            // When
            List<Membership> memberships = userContext.getMemberships();

            // Then
            assertThatThrownBy(() -> memberships.add(MembershipFixture.create(2L, 2L, MembershipType.GUEST)))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ===== Edge Cases Tests =====

    @Nested
    @DisplayName("경계값 테스트 (Edge Cases)")
    class EdgeCaseTests {

        @Test
        @DisplayName("ID 없는 신규 UserContext는 getIdValue()가 null 반환")
        void shouldReturnNullIdValueForNewUserContext() {
            // When
            UserContext userContext = UserContextFixture.createNew();

            // Then
            assertThat(userContext.getIdValue()).isNull();
        }

        @Test
        @DisplayName("멤버십이 없는 UserContext는 getMembershipCount()가 0 반환")
        void shouldReturnZeroMembershipCountWhenNoMemberships() {
            // When
            UserContext userContext = UserContextFixture.createWithId(1L);

            // Then
            assertThat(userContext.getMembershipCount()).isZero();
            assertThat(userContext.getMemberships()).isEmpty();
        }

        @Test
        @DisplayName("동일한 Tenant의 여러 Organization 멤버십 허용")
        void shouldAllowMultipleMembershipsInSameTenant() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);

            // When
            userContext.addMembership(MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE));
            userContext.addMembership(MembershipFixture.create(1L, 2L, MembershipType.EMPLOYEE));
            userContext.addMembership(MembershipFixture.create(1L, 3L, MembershipType.GUEST));

            // Then
            assertThat(userContext.getMembershipCount()).isEqualTo(3);
            assertThat(userContext.hasMembershipIn(TenantId.of(1L))).isTrue();
        }

        @Test
        @DisplayName("여러 Tenant의 멤버십 허용")
        void shouldAllowMembershipsInMultipleTenants() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);

            // When
            userContext.addMembership(MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE));
            userContext.addMembership(MembershipFixture.create(2L, 1L, MembershipType.GUEST));
            userContext.addMembership(MembershipFixture.create(3L, 1L, MembershipType.SYSTEM));

            // Then
            assertThat(userContext.getMembershipCount()).isEqualTo(3);
            assertThat(userContext.hasMembershipIn(TenantId.of(1L))).isTrue();
            assertThat(userContext.hasMembershipIn(TenantId.of(2L))).isTrue();
            assertThat(userContext.hasMembershipIn(TenantId.of(3L))).isTrue();
        }
    }

    // ===== Exception Cases Tests =====

    @Nested
    @DisplayName("예외 처리 테스트 (Exception Cases)")
    class ExceptionTests {

        @Test
        @DisplayName("ExternalUserId가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenExternalUserIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> UserContext.forNew(null, Email.of("test@example.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("External User ID는 필수");
        }

        @Test
        @DisplayName("Email이 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenEmailIsNull() {
            // When & Then
            assertThatThrownBy(() -> UserContext.forNew(ExternalUserId.of("auth0|test"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일 주소는 필수");
        }

        @Test
        @DisplayName("updateEmail에서 null을 전달하면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenUpdatingWithNullEmail() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);

            // When & Then
            assertThatThrownBy(() -> userContext.updateEmail(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("새로운 이메일 주소는 필수");
        }

        @Test
        @DisplayName("삭제된 UserContext 이메일 변경 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenUpdatingEmailOfDeletedUserContext() {
            // Given
            UserContext userContext = UserContextFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> userContext.updateEmail(Email.of("new@example.com")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 UserContext의 이메일은 변경할 수 없습니다");
        }

        @Test
        @DisplayName("null 멤버십 추가 시 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenAddingNullMembership() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);

            // When & Then
            assertThatThrownBy(() -> userContext.addMembership(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("멤버십은 필수");
        }

        @Test
        @DisplayName("중복 멤버십 추가 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenAddingDuplicateMembership() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);
            Membership membership = MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE);
            userContext.addMembership(membership);

            // When & Then
            Membership duplicateMembership = MembershipFixture.create(1L, 1L, MembershipType.GUEST);
            assertThatThrownBy(() -> userContext.addMembership(duplicateMembership))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 해당 테넌트와 조직에 멤버십이 존재합니다");
        }

        @Test
        @DisplayName("삭제된 UserContext에 멤버십 추가 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenAddingMembershipToDeletedUserContext() {
            // Given
            UserContext userContext = UserContextFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> userContext.addMembership(MembershipFixture.create()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 UserContext에 멤버십을 추가할 수 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 멤버십 해지 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenRevokingNonExistentMembership() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);

            // When & Then
            assertThatThrownBy(() -> userContext.revokeMembership(TenantId.of(1L), OrganizationId.of(1L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("해당 테넌트와 조직의 멤버십이 존재하지 않습니다");
        }

        @Test
        @DisplayName("삭제된 UserContext의 멤버십 해지 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenRevokingMembershipFromDeletedUserContext() {
            // Given
            UserContext userContext = UserContextFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> userContext.revokeMembership(TenantId.of(1L), OrganizationId.of(1L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 UserContext의 멤버십은 철회할 수 없습니다");
        }

        @Test
        @DisplayName("이미 삭제된 UserContext를 재삭제하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenDeletingDeletedUserContext() {
            // Given
            UserContext userContext = UserContextFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> userContext.softDelete())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 삭제된 UserContext");
        }

        @Test
        @DisplayName("of() 메서드에서 ID가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCreatingWithNullId() {
            // When & Then
            assertThatThrownBy(() -> UserContext.of(
                null, ExternalUserId.of("auth0|test"), Email.of("test@example.com")
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserContext ID는 필수");
        }

        @Test
        @DisplayName("reconstitute에서 ID가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenReconstituteWithNullId() {
            // When & Then
            assertThatThrownBy(() -> UserContext.reconstitute(
                null, ExternalUserId.of("auth0|test"), Email.of("test@example.com"),
                List.of(), LocalDateTime.now(), LocalDateTime.now(), false
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수");
        }
    }

    // ===== Invariant Validation Tests =====

    @Nested
    @DisplayName("불변식 검증 테스트 (Invariant Validation)")
    class InvariantTests {

        @Test
        @DisplayName("UserContext는 항상 유효한 상태를 유지 (생성 직후)")
        void shouldMaintainInvariantsAfterCreation() {
            // When
            UserContext userContext = UserContextFixture.createWithId(1L);

            // Then
            assertThat(userContext.getIdValue()).isNotNull();
            assertThat(userContext.getExternalUserIdValue()).isNotBlank();
            assertThat(userContext.getEmailValue()).isNotBlank();
            assertThat(userContext.getCreatedAt()).isNotNull();
            assertThat(userContext.getUpdatedAt()).isNotNull();
            assertThat(userContext.getMemberships()).isNotNull();
        }

        @Test
        @DisplayName("UserContext는 항상 유효한 상태를 유지 (멤버십 추가 후)")
        void shouldMaintainInvariantsAfterAddingMembership() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);

            // When
            userContext.addMembership(MembershipFixture.create());

            // Then
            assertThat(userContext.getMembershipCount()).isPositive();
            assertThat(userContext.getMemberships()).isNotEmpty();
        }

        @Test
        @DisplayName("삭제된 UserContext는 모든 상태 변경 불가")
        void shouldPreventAllStateChangesWhenDeleted() {
            // Given
            UserContext userContext = UserContextFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> userContext.updateEmail(Email.of("new@example.com")))
                .isInstanceOf(IllegalStateException.class);

            assertThatThrownBy(() -> userContext.addMembership(MembershipFixture.create()))
                .isInstanceOf(IllegalStateException.class);

            assertThatThrownBy(() -> userContext.revokeMembership(TenantId.of(1L), OrganizationId.of(1L)))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Fixture Builder 테스트")
    class FixtureBuilderTests {

        @Test
        @DisplayName("Builder로 커스텀 UserContext 생성")
        void shouldCreateCustomUserContextWithBuilder() {
            // Given
            List<Membership> memberships = List.of(
                MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE),
                MembershipFixture.create(2L, 1L, MembershipType.GUEST)
            );

            // When
            UserContext userContext = UserContextFixture.builder()
                .id(999L)
                .externalUserId("custom-auth-id")
                .email("custom@example.com")
                .memberships(memberships)
                .deleted(true)
                .build();

            // Then
            assertThat(userContext.getIdValue()).isEqualTo(999L);
            assertThat(userContext.getExternalUserIdValue()).isEqualTo("custom-auth-id");
            assertThat(userContext.getEmailValue()).isEqualTo("custom@example.com");
            assertThat(userContext.getMembershipCount()).isEqualTo(2);
            assertThat(userContext.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Builder로 ID 없는 UserContext 생성")
        void shouldCreateNewUserContextWithBuilder() {
            // When
            UserContext userContext = UserContextFixture.builder()
                .externalUserId("new-auth-id")
                .email("new@example.com")
                .build();

            // Then
            assertThat(userContext.getIdValue()).isNull();
            assertThat(userContext.getExternalUserIdValue()).isEqualTo("new-auth-id");
            assertThat(userContext.getMembershipCount()).isZero();
        }
    }

    @Nested
    @DisplayName("멤버십 시나리오 테스트")
    class MembershipScenarioTests {

        @Test
        @DisplayName("멤버십 추가 → 확인 → 해지 시나리오")
        void shouldHandleAddCheckRevokeMembershipScenario() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);
            TenantId tenantId = TenantId.of(1L);
            OrganizationId organizationId = OrganizationId.of(1L);

            // When - 추가
            userContext.addMembership(MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE));

            // Then - 확인
            assertThat(userContext.hasMembershipIn(tenantId, organizationId)).isTrue();
            assertThat(userContext.getMembershipCount()).isEqualTo(1);

            // When - 해지
            userContext.revokeMembership(tenantId, organizationId);

            // Then - 해지 확인
            assertThat(userContext.hasMembershipIn(tenantId, organizationId)).isFalse();
            assertThat(userContext.getMembershipCount()).isZero();
        }

        @Test
        @DisplayName("다중 Tenant, 다중 Organization 멤버십 관리")
        void shouldHandleMultipleTenantAndOrganizationMemberships() {
            // Given
            UserContext userContext = UserContextFixture.createWithId(1L);

            // When
            userContext.addMembership(MembershipFixture.create(1L, 1L, MembershipType.EMPLOYEE));
            userContext.addMembership(MembershipFixture.create(1L, 2L, MembershipType.EMPLOYEE));
            userContext.addMembership(MembershipFixture.create(2L, 1L, MembershipType.GUEST));
            userContext.addMembership(MembershipFixture.create(3L, 1L, MembershipType.SYSTEM));

            // Then
            assertThat(userContext.getMembershipCount()).isEqualTo(4);
            assertThat(userContext.hasMembershipIn(TenantId.of(1L))).isTrue();
            assertThat(userContext.hasMembershipIn(TenantId.of(2L))).isTrue();
            assertThat(userContext.hasMembershipIn(TenantId.of(3L))).isTrue();
            assertThat(userContext.hasMembershipIn(TenantId.of(1L), OrganizationId.of(1L))).isTrue();
            assertThat(userContext.hasMembershipIn(TenantId.of(1L), OrganizationId.of(2L))).isTrue();
        }
    }
}

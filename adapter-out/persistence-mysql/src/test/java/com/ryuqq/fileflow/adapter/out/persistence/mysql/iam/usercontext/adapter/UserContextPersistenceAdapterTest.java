package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.config.IntegrationTestBase;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.usercontext.Email;
import com.ryuqq.fileflow.domain.iam.usercontext.ExternalUserId;
import com.ryuqq.fileflow.domain.iam.usercontext.Membership;
import com.ryuqq.fileflow.domain.iam.usercontext.MembershipType;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContextId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserContextPersistenceAdapter Integration Test
 *
 * <p><strong>테스트 대상</strong>: {@link UserContextPersistenceAdapter}</p>
 * <p><strong>테스트 환경</strong>: TestContainers MySQL 8.0</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ UserContext 저장 (신규/수정 + Membership 연결)</li>
 *   <li>✅ ID로 UserContext 조회 (Aggregate 재구성)</li>
 *   <li>✅ ExternalUserId로 UserContext 조회</li>
 *   <li>✅ ExternalUserId 중복 확인</li>
 *   <li>✅ ID로 UserContext 삭제 (Hard Delete + Membership Cascade)</li>
 *   <li>✅ 예외 케이스 (null 검증)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Import(UserContextPersistenceAdapter.class)
@DisplayName("UserContextPersistenceAdapter 통합 테스트")
class UserContextPersistenceAdapterTest extends IntegrationTestBase {

    @Autowired
    private UserContextPersistenceAdapter userContextPersistenceAdapter;

    @Nested
    @DisplayName("UserContext 저장 테스트")
    class SaveTests {

        @Test
        @DisplayName("신규 UserContext를 저장하면 정상적으로 저장된다")
        void save_NewUserContext_SavesSuccessfully() {
            // given
            ExternalUserId externalUserId = ExternalUserId.of("auth0|user1");
            Email email = Email.of("user1@example.com");

            // ID가 null인 Domain 객체 생성 → Mapper가 JPA Entity.create() 호출 → DB가 ID 자동 생성
            UserContext newUserContext = UserContext.of(
                UserContextId.of(null),  // null ID → 신규 엔티티
                externalUserId,
                email
            );

            Membership membership = Membership.of(
                TenantId.of("550e8400-e29b-41d4-a716-446655440001"),  // TenantId는 String UUID 형식
                OrganizationId.of(1L),
                MembershipType.EMPLOYEE
            );
            newUserContext.addMembership(membership);

            // when
            UserContext savedUserContext = userContextPersistenceAdapter.save(newUserContext);

            // then
            assertThat(savedUserContext).isNotNull();
            assertThat(savedUserContext.getId()).isNotNull();
            assertThat(savedUserContext.getExternalUserIdValue()).isEqualTo("auth0|user1");
            assertThat(savedUserContext.getEmailValue()).isEqualTo("user1@example.com");
            assertThat(savedUserContext.getMembershipCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("기존 UserContext를 수정하면 Membership이 업데이트된다")
        void save_ExistingUserContext_UpdatesMemberships() {
            // given
            ExternalUserId externalUserId = ExternalUserId.of("auth0|user2");
            Email email = Email.of("user2@example.com");

            UserContext originalUserContext = userContextPersistenceAdapter.save(
                UserContext.of(UserContextId.of(null), externalUserId, email)
            );

            Membership originalMembership = Membership.of(
                TenantId.of("550e8400-e29b-41d4-a716-446655440002"),
                OrganizationId.of(1L),
                MembershipType.EMPLOYEE
            );
            originalUserContext.addMembership(originalMembership);
            userContextPersistenceAdapter.save(originalUserContext);

            // Membership 변경
            UserContext updatedUserContext = userContextPersistenceAdapter.findById(
                originalUserContext.getId()
            ).orElseThrow();

            updatedUserContext.revokeMembership(TenantId.of("550e8400-e29b-41d4-a716-446655440002"), OrganizationId.of(1L));
            updatedUserContext.addMembership(Membership.of(
                TenantId.of("550e8400-e29b-41d4-a716-446655440003"),
                OrganizationId.of(2L),
                MembershipType.SELLER_MEMBER
            ));

            // when
            UserContext savedUserContext = userContextPersistenceAdapter.save(updatedUserContext);

            // then
            assertThat(savedUserContext.getId()).isEqualTo(originalUserContext.getId());
            assertThat(savedUserContext.getMembershipCount()).isEqualTo(1);
            assertThat(savedUserContext.hasMembershipIn(TenantId.of("550e8400-e29b-41d4-a716-446655440003"), OrganizationId.of(2L))).isTrue();
            assertThat(savedUserContext.hasMembershipIn(TenantId.of("550e8400-e29b-41d4-a716-446655440002"), OrganizationId.of(1L))).isFalse();
        }

        @Test
        @DisplayName("null UserContext 저장 시도 시 IllegalArgumentException이 발생한다")
        void save_NullUserContext_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> userContextPersistenceAdapter.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserContext must not be null");
        }
    }

    @Nested
    @DisplayName("UserContext ID 조회 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("ID로 UserContext를 조회하면 Membership과 함께 반환된다")
        void findById_ReturnsUserContextWithMemberships() {
            // given
            ExternalUserId externalUserId = ExternalUserId.of("auth0|user3");
            Email email = Email.of("user3@example.com");

            UserContext userContext = UserContext.of(
                UserContextId.of(null),
                externalUserId,
                email
            );
            userContext.addMembership(Membership.of(
                TenantId.of("550e8400-e29b-41d4-a716-446655440004"),
                OrganizationId.of(10L),
                MembershipType.EMPLOYEE
            ));

            UserContext savedUserContext = userContextPersistenceAdapter.save(userContext);

            // when
            Optional<UserContext> foundUserContext = userContextPersistenceAdapter.findById(
                savedUserContext.getId()
            );

            // then
            assertThat(foundUserContext).isPresent();
            assertThat(foundUserContext.get().getId()).isEqualTo(savedUserContext.getId());
            assertThat(foundUserContext.get().getExternalUserIdValue()).isEqualTo("auth0|user3");
            assertThat(foundUserContext.get().getMembershipCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional이 반환된다")
        void findById_NonExistentId_ReturnsEmptyOptional() {
            // when
            Optional<UserContext> foundUserContext = userContextPersistenceAdapter.findById(
                UserContextId.of(99999L)
            );

            // then
            assertThat(foundUserContext).isEmpty();
        }

        @Test
        @DisplayName("null UserContextId로 조회 시도 시 IllegalArgumentException이 발생한다")
        void findById_NullUserContextId_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> userContextPersistenceAdapter.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserContextId must not be null");
        }
    }

    @Nested
    @DisplayName("UserContext ExternalUserId 조회 테스트")
    class FindByExternalUserIdTests {

        @Test
        @DisplayName("ExternalUserId로 UserContext를 조회하면 Membership과 함께 반환된다")
        void findByExternalUserId_ReturnsUserContextWithMemberships() {
            // given
            ExternalUserId externalUserId = ExternalUserId.of("auth0|user4");
            Email email = Email.of("user4@example.com");

            UserContext userContext = UserContext.of(
                UserContextId.of(null),
                externalUserId,
                email
            );
            userContext.addMembership(Membership.of(
                TenantId.of("550e8400-e29b-41d4-a716-446655440005"),
                OrganizationId.of(20L),
                MembershipType.GUEST
            ));

            userContextPersistenceAdapter.save(userContext);

            // when
            Optional<UserContext> foundUserContext = userContextPersistenceAdapter.findByExternalUserId(
                ExternalUserId.of("auth0|user4")
            );

            // then
            assertThat(foundUserContext).isPresent();
            assertThat(foundUserContext.get().getExternalUserIdValue()).isEqualTo("auth0|user4");
            assertThat(foundUserContext.get().getMembershipCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("존재하지 않는 ExternalUserId로 조회하면 빈 Optional이 반환된다")
        void findByExternalUserId_NonExistent_ReturnsEmptyOptional() {
            // when
            Optional<UserContext> foundUserContext = userContextPersistenceAdapter.findByExternalUserId(
                ExternalUserId.of("auth0|nonexistent")
            );

            // then
            assertThat(foundUserContext).isEmpty();
        }

        @Test
        @DisplayName("null ExternalUserId로 조회 시도 시 IllegalArgumentException이 발생한다")
        void findByExternalUserId_NullExternalUserId_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> userContextPersistenceAdapter.findByExternalUserId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ExternalUserId must not be null");
        }
    }

    @Nested
    @DisplayName("ExternalUserId 중복 확인 테스트")
    class ExistsByExternalUserIdTests {

        @Test
        @DisplayName("ExternalUserId가 존재하면 true를 반환한다")
        void existsByExternalUserId_Existing_ReturnsTrue() {
            // given
            ExternalUserId externalUserId = ExternalUserId.of("auth0|user5");
            Email email = Email.of("user5@example.com");

            UserContext userContext = UserContext.of(
                UserContextId.of(null),
                externalUserId,
                email
            );
            userContextPersistenceAdapter.save(userContext);

            // when
            boolean exists = userContextPersistenceAdapter.existsByExternalUserId(
                ExternalUserId.of("auth0|user5")
            );

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("ExternalUserId가 존재하지 않으면 false를 반환한다")
        void existsByExternalUserId_NonExistent_ReturnsFalse() {
            // when
            boolean exists = userContextPersistenceAdapter.existsByExternalUserId(
                ExternalUserId.of("auth0|nonexistent2")
            );

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("null ExternalUserId로 중복 확인 시도 시 IllegalArgumentException이 발생한다")
        void existsByExternalUserId_NullExternalUserId_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> userContextPersistenceAdapter.existsByExternalUserId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ExternalUserId must not be null");
        }
    }

    @Nested
    @DisplayName("UserContext 삭제 테스트")
    class DeleteByIdTests {

        @Test
        @DisplayName("ID로 UserContext를 Hard Delete하면 Membership도 함께 삭제된다")
        void deleteById_RemovesUserContextAndMemberships() {
            // given
            ExternalUserId externalUserId = ExternalUserId.of("auth0|user6");
            Email email = Email.of("user6@example.com");

            UserContext userContext = UserContext.of(
                UserContextId.of(null),
                externalUserId,
                email
            );
            userContext.addMembership(Membership.of(
                TenantId.of("550e8400-e29b-41d4-a716-446655440006"),
                OrganizationId.of(40L),
                MembershipType.SYSTEM
            ));

            UserContext savedUserContext = userContextPersistenceAdapter.save(userContext);

            // when
            userContextPersistenceAdapter.deleteById(savedUserContext.getId());

            // then
            Optional<UserContext> foundUserContext = userContextPersistenceAdapter.findById(
                savedUserContext.getId()
            );
            assertThat(foundUserContext).isEmpty();

            // ExternalUserId로도 조회 불가능해야 함
            Optional<UserContext> foundByExternalId = userContextPersistenceAdapter.findByExternalUserId(
                externalUserId
            );
            assertThat(foundByExternalId).isEmpty();
        }

        @Test
        @DisplayName("null UserContextId로 삭제 시도 시 IllegalArgumentException이 발생한다")
        void deleteById_NullUserContextId_ThrowsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> userContextPersistenceAdapter.deleteById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserContextId must not be null");
        }
    }
}

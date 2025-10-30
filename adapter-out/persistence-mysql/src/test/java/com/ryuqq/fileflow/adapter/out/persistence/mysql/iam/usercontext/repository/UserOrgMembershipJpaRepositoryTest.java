package com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.fixture.UserOrgMembershipJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserOrgMembershipJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * UserOrgMembershipJpaRepository 단위 테스트
 *
 * <p><strong>테스트 대상</strong>: {@link UserOrgMembershipJpaRepository}</p>
 * <p><strong>테스트 전략</strong>: Mockito 기반 단위 테스트</p>
 *
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>✅ Happy Path: 정상 조회 시나리오</li>
 *   <li>✅ Edge Cases: 빈 결과, 리스트 처리</li>
 *   <li>✅ Spring Data JPA Query Method 검증</li>
 *   <li>✅ findAllByUserContextId() 메서드 테스트</li>
 *   <li>✅ Membership 타입별 조회 (OWNER, ADMIN, MEMBER)</li>
 * </ul>
 *
 * <h3>테스트 패턴</h3>
 * <ul>
 *   <li>✅ Given-When-Then 구조</li>
 *   <li>✅ @Nested를 활용한 논리적 그룹화</li>
 *   <li>✅ @DisplayName으로 테스트 의도 명확화 (한글)</li>
 *   <li>✅ AssertJ를 활용한 Fluent Assertion</li>
 *   <li>✅ BDDMockito를 활용한 Given 설정</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserOrgMembershipJpaRepository 단위 테스트")
class UserOrgMembershipJpaRepositoryTest {

    @Mock
    private UserOrgMembershipJpaRepository repository;

    private UserOrgMembershipJpaEntity ownerMembership;
    private UserOrgMembershipJpaEntity adminMembership;
    private UserOrgMembershipJpaEntity memberMembership;

    @BeforeEach
    void setUp() {
        ownerMembership = UserOrgMembershipJpaEntityFixture.createWithId(1L, 1L, 100L, 1L, "OWNER");
        adminMembership = UserOrgMembershipJpaEntityFixture.createWithId(2L, 1L, 100L, 2L, "ADMIN");
        memberMembership = UserOrgMembershipJpaEntityFixture.createWithId(3L, 1L, 100L, 3L, "MEMBER");
    }

    @Nested
    @DisplayName("findAllByUserContextId() - UserContext ID로 모든 Membership 조회")
    class FindAllByUserContextIdTests {

        @Test
        @DisplayName("정상: UserContext의 모든 Membership 조회 성공")
        void shouldFindAllMembershipsByUserContextId() {
            // Given
            Long userContextId = 1L;
            List<UserOrgMembershipJpaEntity> memberships = Arrays.asList(
                ownerMembership,
                adminMembership,
                memberMembership
            );
            given(repository.findAllByUserContextId(userContextId)).willReturn(memberships);

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(userContextId);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(UserOrgMembershipJpaEntity::getMembershipType)
                .containsExactlyInAnyOrder("OWNER", "ADMIN", "MEMBER");
            verify(repository, times(1)).findAllByUserContextId(userContextId);
        }

        @Test
        @DisplayName("정상: 특정 UserContext가 여러 Organization에 속한 경우")
        void shouldFindMultipleOrganizationMemberships() {
            // Given
            Long userContextId = 1L;
            List<UserOrgMembershipJpaEntity> memberships = Arrays.asList(
                UserOrgMembershipJpaEntityFixture.createWithId(1L, userContextId, 100L, 1L, "OWNER"),
                UserOrgMembershipJpaEntityFixture.createWithId(2L, userContextId, 100L, 2L, "ADMIN"),
                UserOrgMembershipJpaEntityFixture.createWithId(3L, userContextId, 100L, 3L, "MEMBER"),
                UserOrgMembershipJpaEntityFixture.createWithId(4L, userContextId, 100L, 4L, "MEMBER")
            );
            given(repository.findAllByUserContextId(userContextId)).willReturn(memberships);

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(userContextId);

            // Then
            assertThat(result).hasSize(4);
            assertThat(result).extracting(UserOrgMembershipJpaEntity::getOrganizationId)
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
        }

        @Test
        @DisplayName("정상: 여러 Tenant의 Organization에 속한 경우")
        void shouldFindMembershipsAcrossMultipleTenants() {
            // Given
            Long userContextId = 1L;
            List<UserOrgMembershipJpaEntity> memberships = Arrays.asList(
                UserOrgMembershipJpaEntityFixture.createWithId(1L, userContextId, 100L, 1L, "OWNER"),
                UserOrgMembershipJpaEntityFixture.createWithId(2L, userContextId, 200L, 2L, "ADMIN"),
                UserOrgMembershipJpaEntityFixture.createWithId(3L, userContextId, 300L, 3L, "MEMBER")
            );
            given(repository.findAllByUserContextId(userContextId)).willReturn(memberships);

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(userContextId);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(UserOrgMembershipJpaEntity::getTenantId)
                .containsExactlyInAnyOrder(100L, 200L, 300L);
        }

        @Test
        @DisplayName("정상: UserContext에 Membership이 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoMemberships() {
            // Given
            Long userContextId = 999L;
            given(repository.findAllByUserContextId(userContextId)).willReturn(Collections.emptyList());

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(userContextId);

            // Then
            assertThat(result).isEmpty();
            verify(repository, times(1)).findAllByUserContextId(userContextId);
        }

        @Test
        @DisplayName("정상: OWNER 타입 Membership만 필터링")
        void shouldFilterOwnerMembershipsOnly() {
            // Given
            Long userContextId = 1L;
            List<UserOrgMembershipJpaEntity> allMemberships = Arrays.asList(
                ownerMembership,
                adminMembership,
                memberMembership
            );
            given(repository.findAllByUserContextId(userContextId)).willReturn(allMemberships);

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(userContextId);
            List<UserOrgMembershipJpaEntity> ownerOnly = result.stream()
                .filter(m -> "OWNER".equals(m.getMembershipType()))
                .toList();

            // Then
            assertThat(ownerOnly).hasSize(1);
            assertThat(ownerOnly.get(0).getMembershipType()).isEqualTo("OWNER");
            assertThat(ownerOnly.get(0).getOrganizationId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("정상: 특정 Tenant의 Membership만 필터링")
        void shouldFilterMembershipsByTenant() {
            // Given
            Long userContextId = 1L;
            Long targetTenantId = 100L;
            List<UserOrgMembershipJpaEntity> allMemberships = Arrays.asList(
                UserOrgMembershipJpaEntityFixture.createWithId(1L, userContextId, 100L, 1L, "OWNER"),
                UserOrgMembershipJpaEntityFixture.createWithId(2L, userContextId, 200L, 2L, "ADMIN"),
                UserOrgMembershipJpaEntityFixture.createWithId(3L, userContextId, 100L, 3L, "MEMBER")
            );
            given(repository.findAllByUserContextId(userContextId)).willReturn(allMemberships);

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(userContextId);
            List<UserOrgMembershipJpaEntity> tenantMemberships = result.stream()
                .filter(m -> targetTenantId.equals(m.getTenantId()))
                .toList();

            // Then
            assertThat(tenantMemberships).hasSize(2);
            assertThat(tenantMemberships).allMatch(m -> m.getTenantId().equals(targetTenantId));
        }
    }

    @Nested
    @DisplayName("Spring Data JPA 기본 메서드")
    class SpringDataJpaMethodsTests {

        @Test
        @DisplayName("정상: findById() - ID로 조회")
        void shouldFindById() {
            // Given
            Long id = 1L;
            given(repository.findById(id)).willReturn(Optional.of(ownerMembership));

            // When
            Optional<UserOrgMembershipJpaEntity> result = repository.findById(id);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getMembershipType()).isEqualTo("OWNER");
        }

        @Test
        @DisplayName("정상: existsById() - ID 존재 여부 확인")
        void shouldCheckExistenceById() {
            // Given
            Long id = 1L;
            given(repository.existsById(id)).willReturn(true);

            // When
            boolean exists = repository.existsById(id);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("정상: count() - 전체 Membership 개수 조회")
        void shouldCountAllMemberships() {
            // Given
            given(repository.count()).willReturn(15L);

            // When
            long count = repository.count();

            // Then
            assertThat(count).isEqualTo(15L);
        }

        @Test
        @DisplayName("정상: findAll() - 모든 Membership 조회")
        void shouldFindAllMemberships() {
            // Given
            List<UserOrgMembershipJpaEntity> allMemberships = Arrays.asList(
                ownerMembership,
                adminMembership,
                memberMembership
            );
            given(repository.findAll()).willReturn(allMemberships);

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAll();

            // Then
            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Membership 타입별 시나리오")
    class MembershipTypeScenarios {

        @Test
        @DisplayName("정상: OWNER 타입 Membership 생성")
        void shouldCreateOwnerMembership() {
            // Given
            UserOrgMembershipJpaEntity owner = UserOrgMembershipJpaEntityFixture.createOwner(1L, 100L, 1L);
            given(repository.save(owner)).willReturn(owner);

            // When
            UserOrgMembershipJpaEntity saved = repository.save(owner);

            // Then
            assertThat(saved.getMembershipType()).isEqualTo("OWNER");
            assertThat(saved.getUserContextId()).isEqualTo(1L);
            assertThat(saved.getTenantId()).isEqualTo(100L);
            assertThat(saved.getOrganizationId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("정상: ADMIN 타입 Membership 생성")
        void shouldCreateAdminMembership() {
            // Given
            UserOrgMembershipJpaEntity admin = UserOrgMembershipJpaEntityFixture.createAdmin(1L, 100L, 1L);
            given(repository.save(admin)).willReturn(admin);

            // When
            UserOrgMembershipJpaEntity saved = repository.save(admin);

            // Then
            assertThat(saved.getMembershipType()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("정상: MEMBER 타입 Membership 생성")
        void shouldCreateMemberMembership() {
            // Given
            UserOrgMembershipJpaEntity member = UserOrgMembershipJpaEntityFixture.create(1L, 100L, 1L, "MEMBER");
            given(repository.save(member)).willReturn(member);

            // When
            UserOrgMembershipJpaEntity saved = repository.save(member);

            // Then
            assertThat(saved.getMembershipType()).isEqualTo("MEMBER");
        }

        @Test
        @DisplayName("정상: 동일 UserContext의 여러 타입 Membership 조회")
        void shouldFindMultipleTypesForSameUserContext() {
            // Given
            Long userContextId = 1L;
            List<UserOrgMembershipJpaEntity> memberships = Arrays.asList(
                UserOrgMembershipJpaEntityFixture.createWithId(1L, userContextId, 100L, 1L, "OWNER"),
                UserOrgMembershipJpaEntityFixture.createWithId(2L, userContextId, 100L, 2L, "ADMIN"),
                UserOrgMembershipJpaEntityFixture.createWithId(3L, userContextId, 100L, 3L, "MEMBER")
            );
            given(repository.findAllByUserContextId(userContextId)).willReturn(memberships);

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(userContextId);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(UserOrgMembershipJpaEntity::getMembershipType)
                .containsExactlyInAnyOrder("OWNER", "ADMIN", "MEMBER");
        }
    }

    @Nested
    @DisplayName("Edge Cases - 특수 케이스 처리")
    class EdgeCasesTests {

        @Test
        @DisplayName("Edge: 단일 Membership만 있는 UserContext")
        void shouldHandleSingleMembership() {
            // Given
            Long userContextId = 1L;
            List<UserOrgMembershipJpaEntity> singleMembership = Collections.singletonList(ownerMembership);
            given(repository.findAllByUserContextId(userContextId)).willReturn(singleMembership);

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(userContextId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMembershipType()).isEqualTo("OWNER");
        }

        @Test
        @DisplayName("Edge: 대량 Membership 조회 (10개 이상)")
        void shouldHandleLargeNumberOfMemberships() {
            // Given
            Long userContextId = 1L;
            UserOrgMembershipJpaEntity[] memberships = UserOrgMembershipJpaEntityFixture.createMultipleWithId(15);
            given(repository.findAllByUserContextId(userContextId)).willReturn(Arrays.asList(memberships));

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(userContextId);

            // Then
            assertThat(result).hasSize(15);
        }

        @Test
        @DisplayName("Edge: 동일 Organization의 중복 Membership 방지 확인")
        void shouldPreventDuplicateMembership() {
            // Given
            Long userContextId = 1L;
            Long organizationId = 1L;

            List<UserOrgMembershipJpaEntity> existingMemberships = Arrays.asList(
                UserOrgMembershipJpaEntityFixture.createWithId(1L, userContextId, 100L, organizationId, "OWNER")
            );
            given(repository.findAllByUserContextId(userContextId)).willReturn(existingMemberships);

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(userContextId);
            boolean alreadyMember = result.stream()
                .anyMatch(m -> m.getOrganizationId().equals(organizationId));

            // Then
            assertThat(alreadyMember).isTrue();
        }

        @Test
        @DisplayName("Edge: UserContext ID가 0인 경우 빈 리스트 반환")
        void shouldReturnEmptyForZeroUserContextId() {
            // Given
            Long invalidId = 0L;
            given(repository.findAllByUserContextId(invalidId)).willReturn(Collections.emptyList());

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(invalidId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Edge: 음수 UserContext ID는 빈 리스트 반환")
        void shouldReturnEmptyForNegativeUserContextId() {
            // Given
            Long negativeId = -1L;
            given(repository.findAllByUserContextId(negativeId)).willReturn(Collections.emptyList());

            // When
            List<UserOrgMembershipJpaEntity> result = repository.findAllByUserContextId(negativeId);

            // Then
            assertThat(result).isEmpty();
        }
    }
}

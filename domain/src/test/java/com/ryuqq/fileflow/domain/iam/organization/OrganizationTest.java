package com.ryuqq.fileflow.domain.iam.organization;

import com.ryuqq.fileflow.domain.iam.organization.fixture.OrganizationFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Organization Domain Aggregate 단위 테스트
 *
 * <p>테스트 범위:</p>
 * <ul>
 *   <li>Happy Path: 정상 생성 및 비즈니스 메서드</li>
 *   <li>Edge Cases: 경계값 테스트</li>
 *   <li>Exception Cases: 예외 상황 처리</li>
 *   <li>Invariant Validation: 불변식 검증</li>
 *   <li>Law of Demeter 준수 확인</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@DisplayName("Organization Domain 단위 테스트")
class OrganizationTest {

    // ===== Happy Path Tests =====

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreationTests {

        @Test
        @DisplayName("신규 Organization 생성 성공 (forNew)")
        void shouldCreateNewOrganization() {
            // Given
            Long tenantId = 1L;
            String orgCodeValue = "ORG-001";
            String name = "Test Organization";

            // When
            Organization organization = OrganizationFixture.createNew(tenantId, orgCodeValue, name);

            // Then
            assertThat(organization.getIdValue()).isNull(); // 신규 생성은 ID 없음
            assertThat(organization.getTenantId()).isEqualTo(tenantId);
            assertThat(organization.getOrgCodeValue()).isEqualTo(orgCodeValue);
            assertThat(organization.getName()).isEqualTo(name);
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
            assertThat(organization.isDeleted()).isFalse();
            assertThat(organization.isActive()).isTrue();
        }

        @Test
        @DisplayName("ID가 있는 Organization 생성 성공 (of)")
        void shouldCreateOrganizationWithId() {
            // Given
            Long id = 100L;

            // When
            Organization organization = OrganizationFixture.createWithId(id);

            // Then
            assertThat(organization.getIdValue()).isEqualTo(id);
            assertThat(organization.getTenantId()).isEqualTo(1L); // Fixture 기본값
            assertThat(organization.getOrgCodeValue()).isEqualTo("ORG-001"); // Fixture 기본값
            assertThat(organization.isActive()).isTrue();
        }

        @Test
        @DisplayName("Fixture를 통한 여러 Organization 생성")
        void shouldCreateMultipleOrganizations() {
            // When
            var organizations = OrganizationFixture.createMultiple(3);

            // Then
            assertThat(organizations).hasSize(3);
            assertThat(organizations.get(0).getOrgCodeValue()).isEqualTo("ORG-001");
            assertThat(organizations.get(1).getOrgCodeValue()).isEqualTo("ORG-002");
            assertThat(organizations.get(2).getOrgCodeValue()).isEqualTo("ORG-003");
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트 (Happy Path)")
    class BusinessMethodTests {

        @Test
        @DisplayName("Organization 이름 변경 성공")
        void shouldUpdateName() {
            // Given
            Organization organization = OrganizationFixture.createWithId(1L);
            String newName = "Updated Organization";
            LocalDateTime beforeUpdate = organization.getUpdatedAt();

            // When
            organization.updateName(newName);

            // Then
            assertThat(organization.getName()).isEqualTo(newName);
            assertThat(organization.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("Organization 비활성화 성공")
        void shouldDeactivateOrganization() {
            // Given
            Organization organization = OrganizationFixture.createWithId(1L);

            // When
            organization.deactivate();

            // Then
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
            assertThat(organization.isActive()).isFalse();
        }

        @Test
        @DisplayName("Organization 소프트 삭제 성공")
        void shouldSoftDeleteOrganization() {
            // Given
            Organization organization = OrganizationFixture.createWithId(1L);

            // When
            organization.softDelete();

            // Then
            assertThat(organization.isDeleted()).isTrue();
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
            assertThat(organization.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue()로 ID 직접 접근 (체이닝 방지)")
        void shouldGetIdValueDirectly() {
            // Given
            Organization organization = OrganizationFixture.createWithId(1L);

            // When
            Long idValue = organization.getIdValue();

            // Then
            assertThat(idValue).isEqualTo(1L);
            // ✅ Good: organization.getIdValue()
            // ❌ Bad: organization.getId().value()
        }

        @Test
        @DisplayName("getOrgCodeValue()로 OrgCode 직접 접근 (체이닝 방지)")
        void shouldGetOrgCodeValueDirectly() {
            // Given
            Organization organization = OrganizationFixture.createWithId(1L);

            // When
            String orgCodeValue = organization.getOrgCodeValue();

            // Then
            assertThat(orgCodeValue).isEqualTo("ORG-001");
            // ✅ Good: organization.getOrgCodeValue()
            // ❌ Bad: organization.getOrgCode().getValue()
        }

        @Test
        @DisplayName("isActive()로 상태 확인 (Tell, Don't Ask)")
        void shouldCheckIsActiveDirectly() {
            // Given
            Organization organization = OrganizationFixture.createWithId(1L);

            // When
            boolean active = organization.isActive();

            // Then
            assertThat(active).isTrue();
            // ✅ Good: organization.isActive()
            // ❌ Bad: organization.getStatus() == ACTIVE && !organization.isDeleted()
        }

        @Test
        @DisplayName("belongsToTenant()로 소속 확인 (Tell, Don't Ask)")
        void shouldCheckBelongsToTenantDirectly() {
            // Given
            Organization organization = OrganizationFixture.createWithId(1L, 100L, "ORG-001", "Test");

            // When
            boolean belongsToTenant = organization.belongsToTenant(100L);

            // Then
            assertThat(belongsToTenant).isTrue();
            // ✅ Good: organization.belongsToTenant(tenantId)
            // ❌ Bad: organization.getTenantId().equals(tenantId)
        }
    }

    // ===== Edge Cases Tests =====

    @Nested
    @DisplayName("경계값 테스트 (Edge Cases)")
    class EdgeCaseTests {

        @Test
        @DisplayName("최소 Tenant ID (1L) 허용")
        void shouldAcceptMinimumTenantId() {
            // When
            Organization organization = OrganizationFixture.createNew(1L, "ORG-001", "Test");

            // Then
            assertThat(organization.getTenantId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("최대 Tenant ID (Long.MAX_VALUE) 허용")
        void shouldAcceptMaximumTenantId() {
            // Given
            Long maxTenantId = Long.MAX_VALUE;

            // When
            Organization organization = OrganizationFixture.createNew(maxTenantId, "ORG-001", "Test");

            // Then
            assertThat(organization.getTenantId()).isEqualTo(maxTenantId);
        }

        @Test
        @DisplayName("Organization 이름 앞뒤 공백 자동 제거")
        void shouldTrimOrganizationName() {
            // Given
            String nameWithSpaces = "  Test Organization  ";

            // When
            Organization organization = OrganizationFixture.createNew(1L, "ORG-001", nameWithSpaces);

            // Then
            assertThat(organization.getName()).isEqualTo("Test Organization");
        }

        @Test
        @DisplayName("ID 없는 신규 Organization은 getIdValue()가 null 반환")
        void shouldReturnNullIdValueForNewOrganization() {
            // When
            Organization organization = OrganizationFixture.createNew();

            // Then
            assertThat(organization.getIdValue()).isNull();
        }
    }

    // ===== Exception Cases Tests =====

    @Nested
    @DisplayName("예외 처리 테스트 (Exception Cases)")
    class ExceptionTests {

        @Test
        @DisplayName("Tenant ID가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenTenantIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> Organization.forNew(null, OrgCode.of("ORG-001"), "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 필수");
        }

        @Test
        @DisplayName("Tenant ID가 0 이하이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenTenantIdIsZeroOrNegative() {
            // When & Then
            assertThatThrownBy(() -> Organization.forNew(0L, OrgCode.of("ORG-001"), "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("양수");

            assertThatThrownBy(() -> Organization.forNew(-1L, OrgCode.of("ORG-001"), "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("양수");
        }

        @Test
        @DisplayName("OrgCode가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenOrgCodeIsNull() {
            // When & Then
            assertThatThrownBy(() -> Organization.forNew(1L, null, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("조직 코드는 필수");
        }

        @Test
        @DisplayName("Organization 이름이 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenNameIsNull() {
            // When & Then
            assertThatThrownBy(() -> Organization.forNew(1L, OrgCode.of("ORG-001"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("조직 이름은 필수");
        }

        @Test
        @DisplayName("Organization 이름이 빈 문자열이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenNameIsBlank() {
            // When & Then
            assertThatThrownBy(() -> Organization.forNew(1L, OrgCode.of("ORG-001"), "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("조직 이름은 필수");
        }

        @Test
        @DisplayName("삭제된 Organization 이름 변경 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenUpdatingNameOfDeletedOrganization() {
            // Given
            Organization organization = OrganizationFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> organization.updateName("New Name"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 Organization은 수정할 수 없습니다");
        }

        @Test
        @DisplayName("이미 INACTIVE인 Organization을 비활성화하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenDeactivatingInactiveOrganization() {
            // Given
            Organization organization = OrganizationFixture.createInactive(1L);

            // When & Then
            assertThatThrownBy(() -> organization.deactivate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 비활성화된 Organization");
        }

        @Test
        @DisplayName("이미 삭제된 Organization을 재삭제하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenDeletingDeletedOrganization() {
            // Given
            Organization organization = OrganizationFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> organization.softDelete())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 삭제된 Organization");
        }

        @Test
        @DisplayName("of() 메서드에서 ID가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCreatingWithNullId() {
            // When & Then
            assertThatThrownBy(() -> Organization.of(null, 1L, OrgCode.of("ORG-001"), "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organization ID는 필수");
        }

        @Test
        @DisplayName("reconstitute에서 ID가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenReconstituteWithNullId() {
            // When & Then
            assertThatThrownBy(() -> Organization.reconstitute(
                null, 1L, OrgCode.of("ORG-001"), "Test",
                OrganizationStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now(), false
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
        @DisplayName("Organization은 항상 유효한 상태를 유지 (생성 직후)")
        void shouldMaintainInvariantsAfterCreation() {
            // When
            Organization organization = OrganizationFixture.createWithId(1L);

            // Then
            assertThat(organization.getIdValue()).isNotNull();
            assertThat(organization.getTenantId()).isNotNull().isPositive();
            assertThat(organization.getOrgCodeValue()).isNotBlank();
            assertThat(organization.getName()).isNotBlank();
            assertThat(organization.getStatus()).isIn(OrganizationStatus.ACTIVE, OrganizationStatus.INACTIVE);
            assertThat(organization.getCreatedAt()).isNotNull();
            assertThat(organization.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Organization은 항상 유효한 상태를 유지 (비활성화 후)")
        void shouldMaintainInvariantsAfterDeactivation() {
            // Given
            Organization organization = OrganizationFixture.createWithId(1L);

            // When
            organization.deactivate();

            // Then
            assertThat(organization.getIdValue()).isNotNull();
            assertThat(organization.getTenantId()).isNotNull().isPositive();
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
            assertThat(organization.isActive()).isFalse();
        }

        @Test
        @DisplayName("소프트 삭제 시 자동으로 INACTIVE 상태로 전환")
        void shouldAutomaticallyDeactivateWhenSoftDeleted() {
            // Given
            Organization organization = OrganizationFixture.createWithId(1L);

            // When
            organization.softDelete();

            // Then
            assertThat(organization.isDeleted()).isTrue();
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
        }

        @Test
        @DisplayName("삭제된 Organization은 모든 상태 변경 불가")
        void shouldPreventAllStateChangesWhenDeleted() {
            // Given
            Organization organization = OrganizationFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> organization.updateName("New Name"))
                .isInstanceOf(IllegalStateException.class);

            assertThatThrownBy(() -> organization.deactivate())
                .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Fixture Builder 테스트")
    class FixtureBuilderTests {

        @Test
        @DisplayName("Builder로 커스텀 Organization 생성")
        void shouldCreateCustomOrganizationWithBuilder() {
            // When
            Organization organization = OrganizationFixture.builder()
                .id(999L)
                .tenantId(100L)
                .orgCode("CUSTOM-ORG")
                .name("Custom Organization")
                .status(OrganizationStatus.INACTIVE)
                .deleted(true)
                .build();

            // Then
            assertThat(organization.getIdValue()).isEqualTo(999L);
            assertThat(organization.getTenantId()).isEqualTo(100L);
            assertThat(organization.getOrgCodeValue()).isEqualTo("CUSTOM-ORG");
            assertThat(organization.getName()).isEqualTo("Custom Organization");
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.INACTIVE);
            assertThat(organization.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Builder로 ID 없는 Organization 생성")
        void shouldCreateNewOrganizationWithBuilder() {
            // When
            Organization organization = OrganizationFixture.builder()
                .tenantId(100L)
                .orgCode("NEW-ORG")
                .name("New Organization")
                .build();

            // Then
            assertThat(organization.getIdValue()).isNull();
            assertThat(organization.getTenantId()).isEqualTo(100L);
            assertThat(organization.isActive()).isTrue();
        }
    }
}

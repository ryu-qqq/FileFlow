package com.ryuqq.fileflow.domain.iam.tenant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Tenant Domain Aggregate 단위 테스트
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
@DisplayName("Tenant Domain 단위 테스트")
class TenantTest {

    // ===== Happy Path Tests =====

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreationTests {

        @Test
        @DisplayName("신규 Tenant 생성 성공 (forNew)")
        void shouldCreateNewTenant() {
            // Given
            String name = "Test Tenant";

            // When
            Tenant tenant = TenantFixture.createNew(name);

            // Then
            assertThat(tenant.getIdValue()).isNull(); // 신규 생성은 ID 없음
            assertThat(tenant.getNameValue()).isEqualTo(name);
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
            assertThat(tenant.isDeleted()).isFalse();
            assertThat(tenant.isActive()).isTrue();
        }

        @Test
        @DisplayName("ID가 있는 Tenant 생성 성공 (of)")
        void shouldCreateTenantWithId() {
            // Given
            Long id = 100L;
            String name = "Test Tenant";

            // When
            Tenant tenant = TenantFixture.createWithId(id, name);

            // Then
            assertThat(tenant.getIdValue()).isEqualTo(id);
            assertThat(tenant.getNameValue()).isEqualTo(name);
            assertThat(tenant.isActive()).isTrue();
        }

        @Test
        @DisplayName("Fixture를 통한 여러 Tenant 생성")
        void shouldCreateMultipleTenants() {
            // When
            var tenants = TenantFixture.createMultiple(3);

            // Then
            assertThat(tenants).hasSize(3);
            assertThat(tenants.get(0).getNameValue()).isEqualTo("Test Tenant 1");
            assertThat(tenants.get(1).getNameValue()).isEqualTo("Test Tenant 2");
            assertThat(tenants.get(2).getNameValue()).isEqualTo("Test Tenant 3");
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트 (Happy Path)")
    class BusinessMethodTests {

        @Test
        @DisplayName("Tenant 이름 변경 성공")
        void shouldUpdateName() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L);
            TenantName newName = TenantName.of("Updated Tenant");
            LocalDateTime beforeUpdate = tenant.getUpdatedAt();

            // When
            tenant.updateName(newName);

            // Then
            assertThat(tenant.getNameValue()).isEqualTo("Updated Tenant");
            assertThat(tenant.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("Tenant 일시 정지 성공")
        void shouldSuspendTenant() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L);

            // When
            tenant.suspend();

            // Then
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(tenant.isActive()).isFalse();
        }

        @Test
        @DisplayName("일시 정지된 Tenant 활성화 성공")
        void shouldActivateSuspendedTenant() {
            // Given
            Tenant tenant = TenantFixture.createSuspended(1L);

            // When
            tenant.activate();

            // Then
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
            assertThat(tenant.isActive()).isTrue();
        }

        @Test
        @DisplayName("Tenant 소프트 삭제 성공")
        void shouldSoftDeleteTenant() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L);

            // When
            tenant.softDelete();

            // Then
            assertThat(tenant.isDeleted()).isTrue();
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(tenant.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue()로 ID 직접 접근 (체이닝 방지)")
        void shouldGetIdValueDirectly() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L);

            // When
            Long idValue = tenant.getIdValue();

            // Then
            assertThat(idValue).isEqualTo(1L);
            // ✅ Good: tenant.getIdValue()
            // ❌ Bad: tenant.getId().value()
        }

        @Test
        @DisplayName("getNameValue()로 이름 직접 접근 (체이닝 방지)")
        void shouldGetNameValueDirectly() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L, "Test Tenant");

            // When
            String nameValue = tenant.getNameValue();

            // Then
            assertThat(nameValue).isEqualTo("Test Tenant");
            // ✅ Good: tenant.getNameValue()
            // ❌ Bad: tenant.getName().getValue()
        }

        @Test
        @DisplayName("isActive()로 상태 확인 (Tell, Don't Ask)")
        void shouldCheckIsActiveDirectly() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L);

            // When
            boolean active = tenant.isActive();

            // Then
            assertThat(active).isTrue();
            // ✅ Good: tenant.isActive()
            // ❌ Bad: tenant.getStatus() == ACTIVE && !tenant.isDeleted()
        }
    }

    // ===== Edge Cases Tests =====

    @Nested
    @DisplayName("경계값 테스트 (Edge Cases)")
    class EdgeCaseTests {

        @Test
        @DisplayName("ID 없는 신규 Tenant는 getIdValue()가 null 반환")
        void shouldReturnNullIdValueForNewTenant() {
            // When
            Tenant tenant = TenantFixture.createNew();

            // Then
            assertThat(tenant.getIdValue()).isNull();
        }

        @Test
        @DisplayName("최소 Tenant ID (1L) 허용")
        void shouldAcceptMinimumTenantId() {
            // When
            Tenant tenant = TenantFixture.createWithId(1L);

            // Then
            assertThat(tenant.getIdValue()).isEqualTo(1L);
        }

        @Test
        @DisplayName("최대 Tenant ID (Long.MAX_VALUE) 허용")
        void shouldAcceptMaximumTenantId() {
            // Given
            Long maxId = Long.MAX_VALUE;

            // When
            Tenant tenant = TenantFixture.createWithId(maxId);

            // Then
            assertThat(tenant.getIdValue()).isEqualTo(maxId);
        }
    }

    // ===== Exception Cases Tests =====

    @Nested
    @DisplayName("예외 처리 테스트 (Exception Cases)")
    class ExceptionTests {

        @Test
        @DisplayName("Tenant 이름이 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenNameIsNull() {
            // When & Then
            assertThatThrownBy(() -> Tenant.forNew(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant 이름은 필수");
        }

        @Test
        @DisplayName("updateName에서 null을 전달하면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenUpdatingWithNullName() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L);

            // When & Then
            assertThatThrownBy(() -> tenant.updateName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("새로운 Tenant 이름은 필수");
        }

        @Test
        @DisplayName("삭제된 Tenant 이름 변경 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenUpdatingNameOfDeletedTenant() {
            // Given
            Tenant tenant = TenantFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> tenant.updateName(TenantName.of("New Name")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 Tenant의 이름은 변경할 수 없습니다");
        }

        @Test
        @DisplayName("이미 SUSPENDED인 Tenant를 일시 정지하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenSuspendingSuspendedTenant() {
            // Given
            Tenant tenant = TenantFixture.createSuspended(1L);

            // When & Then
            assertThatThrownBy(() -> tenant.suspend())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 일시 정지된 Tenant");
        }

        @Test
        @DisplayName("삭제된 Tenant를 일시 정지하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenSuspendingDeletedTenant() {
            // Given
            Tenant tenant = TenantFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> tenant.suspend())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 Tenant는 일시 정지할 수 없습니다");
        }

        @Test
        @DisplayName("이미 ACTIVE인 Tenant를 활성화하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenActivatingActiveTenant() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L);

            // When & Then
            assertThatThrownBy(() -> tenant.activate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 활성 상태인 Tenant");
        }

        @Test
        @DisplayName("삭제된 Tenant를 활성화하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenActivatingDeletedTenant() {
            // Given
            Tenant tenant = TenantFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> tenant.activate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 Tenant는 활성화할 수 없습니다");
        }

        @Test
        @DisplayName("이미 삭제된 Tenant를 재삭제하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenDeletingDeletedTenant() {
            // Given
            Tenant tenant = TenantFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> tenant.softDelete())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 삭제된 Tenant");
        }

        @Test
        @DisplayName("of() 메서드에서 ID가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCreatingWithNullId() {
            // When & Then
            assertThatThrownBy(() -> Tenant.of(null, TenantName.of("Test")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 필수");
        }

        @Test
        @DisplayName("reconstitute에서 ID가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenReconstituteWithNullId() {
            // When & Then
            assertThatThrownBy(() -> Tenant.reconstitute(
                null, TenantName.of("Test"), TenantStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), false
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
        @DisplayName("Tenant는 항상 유효한 상태를 유지 (생성 직후)")
        void shouldMaintainInvariantsAfterCreation() {
            // When
            Tenant tenant = TenantFixture.createWithId(1L);

            // Then
            assertThat(tenant.getIdValue()).isNotNull();
            assertThat(tenant.getNameValue()).isNotBlank();
            assertThat(tenant.getStatus()).isIn(TenantStatus.ACTIVE, TenantStatus.SUSPENDED);
            assertThat(tenant.getCreatedAt()).isNotNull();
            assertThat(tenant.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Tenant는 항상 유효한 상태를 유지 (일시 정지 후)")
        void shouldMaintainInvariantsAfterSuspension() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L);

            // When
            tenant.suspend();

            // Then
            assertThat(tenant.getIdValue()).isNotNull();
            assertThat(tenant.getNameValue()).isNotBlank();
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(tenant.isActive()).isFalse();
        }

        @Test
        @DisplayName("소프트 삭제 시 자동으로 SUSPENDED 상태로 전환")
        void shouldAutomaticallySuspendWhenSoftDeleted() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L);

            // When
            tenant.softDelete();

            // Then
            assertThat(tenant.isDeleted()).isTrue();
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
        }

        @Test
        @DisplayName("삭제된 Tenant는 모든 상태 변경 불가")
        void shouldPreventAllStateChangesWhenDeleted() {
            // Given
            Tenant tenant = TenantFixture.createDeleted(1L);

            // When & Then
            assertThatThrownBy(() -> tenant.updateName(TenantName.of("New Name")))
                .isInstanceOf(IllegalStateException.class);

            assertThatThrownBy(() -> tenant.suspend())
                .isInstanceOf(IllegalStateException.class);

            assertThatThrownBy(() -> tenant.activate())
                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("SUSPENDED 상태의 Tenant는 다시 ACTIVE로 전환 가능")
        void shouldAllowActivationOfSuspendedTenant() {
            // Given
            Tenant tenant = TenantFixture.createSuspended(1L);

            // When
            tenant.activate();

            // Then
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
            assertThat(tenant.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Fixture Builder 테스트")
    class FixtureBuilderTests {

        @Test
        @DisplayName("Builder로 커스텀 Tenant 생성")
        void shouldCreateCustomTenantWithBuilder() {
            // When
            Tenant tenant = TenantFixture.builder()
                .id(999L)
                .name("Custom Tenant")
                .status(TenantStatus.SUSPENDED)
                .deleted(true)
                .build();

            // Then
            assertThat(tenant.getIdValue()).isEqualTo(999L);
            assertThat(tenant.getNameValue()).isEqualTo("Custom Tenant");
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(tenant.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Builder로 ID 없는 Tenant 생성")
        void shouldCreateNewTenantWithBuilder() {
            // When
            Tenant tenant = TenantFixture.builder()
                .name("New Tenant")
                .build();

            // Then
            assertThat(tenant.getIdValue()).isNull();
            assertThat(tenant.getNameValue()).isEqualTo("New Tenant");
            assertThat(tenant.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("상태 전환 시나리오 테스트")
    class StateTransitionTests {

        @Test
        @DisplayName("ACTIVE → SUSPENDED → ACTIVE 상태 전환")
        void shouldTransitionFromActiveToSuspendedToActive() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L);

            // When & Then
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);

            tenant.suspend();
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);

            tenant.activate();
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        }

        @Test
        @DisplayName("ACTIVE → 소프트 삭제 (SUSPENDED)")
        void shouldTransitionFromActiveToDeleted() {
            // Given
            Tenant tenant = TenantFixture.createWithId(1L);

            // When
            tenant.softDelete();

            // Then
            assertThat(tenant.isDeleted()).isTrue();
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
            assertThat(tenant.isActive()).isFalse();
        }
    }
}

package com.ryuqq.fileflow.application.iam.tenant.assembler;

import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;
import com.ryuqq.fileflow.domain.iam.tenant.TenantStatus;
import com.ryuqq.fileflow.fixtures.TenantFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TenantAssembler 단위 테스트
 *
 * <p>TenantAssembler의 Domain ↔ DTO 변환 로직을 검증합니다.</p>
 *
 * <p><strong>테스트 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ @Tag("unit"), @Tag("application"), @Tag("fast") 사용</li>
 *   <li>✅ @Nested 그룹으로 테스트 조직화</li>
 *   <li>✅ AAA 패턴 (Arrange-Act-Assert)</li>
 *   <li>✅ TenantFixtures 사용으로 테스트 데이터 표준화</li>
 *   <li>✅ DisplayName으로 한글 설명 제공</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("TenantAssembler 테스트")
class TenantAssemblerTest {

    @Nested
    @DisplayName("toDomain 메서드 테스트")
    class ToDomainTests {

        @Test
        @DisplayName("TenantName으로 새로운 Tenant Domain 생성 성공")
        void shouldCreateTenantDomainFromTenantName() {
            // Arrange
            TenantName tenantName = TenantName.of("My Company");

            // Act
            Tenant tenant = TenantAssembler.toDomain(tenantName);

            // Assert
            assertThat(tenant).isNotNull();
            assertThat(tenant.getNameValue()).isEqualTo("My Company");
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
            assertThat(tenant.isDeleted()).isFalse();
            assertThat(tenant.getIdValue()).isNotBlank(); // UUID 자동 생성
            assertThat(tenant.getCreatedAt()).isNotNull();
            assertThat(tenant.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("null TenantName 전달 시 예외 발생")
        void shouldThrowExceptionWhenTenantNameIsNull() {
            // Arrange
            TenantName tenantName = null;

            // Act & Assert
            assertThatThrownBy(() -> TenantAssembler.toDomain(tenantName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TenantName은 필수입니다");
        }

        @Test
        @DisplayName("생성된 Tenant는 자동으로 UUID 기반 ID를 가짐")
        void shouldGenerateUuidBasedTenantId() {
            // Arrange
            TenantName tenantName = TenantName.of("Test Company");

            // Act
            Tenant tenant1 = TenantAssembler.toDomain(tenantName);
            Tenant tenant2 = TenantAssembler.toDomain(tenantName);

            // Assert
            assertThat(tenant1.getIdValue()).isNotEqualTo(tenant2.getIdValue()); // 다른 UUID 생성
            assertThat(tenant1.getIdValue()).hasSize(36); // UUID 표준 길이
        }
    }

    @Nested
    @DisplayName("toResponse 메서드 테스트 - Domain to Response 변환")
    class ToResponseTests {

        @Test
        @DisplayName("활성 Tenant를 TenantResponse로 변환 성공")
        void shouldConvertActiveTenantToResponse() {
            // Arrange
            Tenant tenant = TenantFixtures.activeTenantWithName("Test Company");

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenant.getIdValue());
            assertThat(response.name()).isEqualTo("Test Company");
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.deleted()).isFalse();
            assertThat(response.createdAt()).isEqualTo(tenant.getCreatedAt());
            assertThat(response.updatedAt()).isEqualTo(tenant.getUpdatedAt());
        }

        @Test
        @DisplayName("일시 정지된 Tenant를 TenantResponse로 변환 성공")
        void shouldConvertSuspendedTenantToResponse() {
            // Arrange
            Tenant tenant = TenantFixtures.suspendedTenantWithName("Suspended Company");

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenant.getIdValue());
            assertThat(response.name()).isEqualTo("Suspended Company");
            assertThat(response.status()).isEqualTo("SUSPENDED");
            assertThat(response.deleted()).isFalse();
        }

        @Test
        @DisplayName("삭제된 Tenant를 TenantResponse로 변환 성공")
        void shouldConvertDeletedTenantToResponse() {
            // Arrange
            Tenant tenant = TenantFixtures.deletedTenantWithName("Deleted Company");

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenant.getIdValue());
            assertThat(response.name()).isEqualTo("Deleted Company");
            assertThat(response.status()).isEqualTo("SUSPENDED"); // 삭제 시 자동 SUSPENDED
            assertThat(response.deleted()).isTrue();
        }

        @Test
        @DisplayName("null Tenant 전달 시 예외 발생")
        void shouldThrowExceptionWhenTenantIsNull() {
            // Arrange
            Tenant tenant = null;

            // Act & Assert
            assertThatThrownBy(() -> TenantAssembler.toResponse(tenant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant는 필수입니다");
        }
    }

    @Nested
    @DisplayName("필드 매핑 검증 테스트")
    class FieldMappingTests {

        @Test
        @DisplayName("TenantId String 변환 검증 (Law of Demeter 준수)")
        void shouldCorrectlyMapTenantIdToString() {
            // Arrange
            String expectedId = "test-tenant-123";
            Tenant tenant = TenantFixtures.activeTenantWithId(expectedId);

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert - Law of Demeter: tenant.getIdValue() 사용
            assertThat(response.tenantId()).isEqualTo(expectedId);
            assertThat(response.tenantId()).isEqualTo(tenant.getIdValue());
        }

        @Test
        @DisplayName("TenantName String 변환 검증 (Law of Demeter 준수)")
        void shouldCorrectlyMapTenantNameToString() {
            // Arrange
            String expectedName = "ABC Corporation";
            Tenant tenant = TenantFixtures.activeTenantWithName(expectedName);

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert - Law of Demeter: tenant.getNameValue() 사용
            assertThat(response.name()).isEqualTo(expectedName);
            assertThat(response.name()).isEqualTo(tenant.getNameValue());
        }

        @Test
        @DisplayName("TenantStatus Enum을 String으로 변환 검증")
        void shouldConvertTenantStatusEnumToString() {
            // Arrange
            Tenant activeTenant = TenantFixtures.activeTenant();
            Tenant suspendedTenant = TenantFixtures.suspendedTenant();

            // Act
            TenantResponse activeResponse = TenantAssembler.toResponse(activeTenant);
            TenantResponse suspendedResponse = TenantAssembler.toResponse(suspendedTenant);

            // Assert
            assertThat(activeResponse.status()).isEqualTo("ACTIVE");
            assertThat(suspendedResponse.status()).isEqualTo("SUSPENDED");
        }

        @Test
        @DisplayName("LocalDateTime 객체가 그대로 전달되는지 검증")
        void shouldPreserveLocalDateTimeValues() {
            // Arrange
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 2, 15, 30, 0);
            Tenant tenant = TenantFixtures.customTenant(
                "test-id",
                "Test Company",
                TenantStatus.ACTIVE,
                createdAt,
                updatedAt,
                false
            );

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("boolean deleted 필드가 정확히 매핑되는지 검증")
        void shouldCorrectlyMapDeletedFlag() {
            // Arrange
            Tenant activeTenant = TenantFixtures.activeTenant();
            Tenant deletedTenant = TenantFixtures.deletedTenant();

            // Act
            TenantResponse activeResponse = TenantAssembler.toResponse(activeTenant);
            TenantResponse deletedResponse = TenantAssembler.toResponse(deletedTenant);

            // Assert
            assertThat(activeResponse.deleted()).isFalse();
            assertThat(deletedResponse.deleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Cases 및 경계값 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("다양한 TenantStatus 값에 대한 변환 검증")
        void shouldHandleAllTenantStatusValues() {
            // Arrange & Act & Assert
            for (TenantStatus status : TenantStatus.values()) {
                Tenant tenant = TenantFixtures.customTenant(
                    "test-id",
                    "Test Company",
                    status,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    false
                );

                TenantResponse response = TenantAssembler.toResponse(tenant);

                assertThat(response.status()).isEqualTo(status.name());
            }
        }

        @Test
        @DisplayName("삭제된 상태에서도 모든 필드가 정확히 매핑되는지 검증")
        void shouldCorrectlyMapAllFieldsForDeletedTenant() {
            // Arrange
            String tenantId = "deleted-tenant-001";
            String tenantName = "Deleted Company";
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 10, 23, 12, 0);

            Tenant deletedTenant = TenantFixtures.customTenant(
                tenantId,
                tenantName,
                TenantStatus.SUSPENDED,
                createdAt,
                updatedAt,
                true
            );

            // Act
            TenantResponse response = TenantAssembler.toResponse(deletedTenant);

            // Assert - 모든 필드 검증
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.name()).isEqualTo(tenantName);
            assertThat(response.status()).isEqualTo("SUSPENDED");
            assertThat(response.deleted()).isTrue();
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("특수 문자가 포함된 Tenant 이름 변환 검증")
        void shouldHandleSpecialCharactersInTenantName() {
            // Arrange
            String specialName = "ABC & 123 Co., Ltd. (주)";
            Tenant tenant = TenantFixtures.activeTenantWithName(specialName);

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert
            assertThat(response.name()).isEqualTo(specialName);
        }

        @Test
        @DisplayName("UUID 형식의 TenantId 변환 검증")
        void shouldHandleUuidFormatTenantId() {
            // Arrange
            String uuidId = "550e8400-e29b-41d4-a716-446655440000";
            Tenant tenant = TenantFixtures.activeTenantWithIdAndName(uuidId, "UUID Company");

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert
            assertThat(response.tenantId()).isEqualTo(uuidId);
            assertThat(response.tenantId()).hasSize(36); // UUID 표준 길이
        }

        @Test
        @DisplayName("최소 길이 Tenant 이름 변환 검증")
        void shouldHandleMinimumLengthTenantName() {
            // Arrange - TenantName의 최소 길이는 2자
            String minName = "AB";
            Tenant tenant = TenantFixtures.activeTenantWithName(minName);

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert
            assertThat(response.name()).isEqualTo(minName);
            assertThat(response.name()).hasSize(2);
        }

        @Test
        @DisplayName("최대 길이 Tenant 이름 변환 검증")
        void shouldHandleMaximumLengthTenantName() {
            // Arrange - TenantName의 최대 길이는 50자
            String maxName = "A".repeat(50);
            Tenant tenant = TenantFixtures.activeTenantWithName(maxName);

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert
            assertThat(response.name()).isEqualTo(maxName);
            assertThat(response.name()).hasSize(50);
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 검증")
    class LawOfDemeterTests {

        @Test
        @DisplayName("Assembler가 tenant.getIdValue() 사용 확인 (Getter 체이닝 금지)")
        void shouldUseLawOfDemeterForTenantId() {
            // Arrange
            Tenant tenant = TenantFixtures.activeTenant();

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert
            // ❌ Bad: tenant.getId().value()
            // ✅ Good: tenant.getIdValue()
            assertThat(response.tenantId()).isEqualTo(tenant.getIdValue());
            assertThat(response.tenantId()).isNotNull();
        }

        @Test
        @DisplayName("Assembler가 tenant.getNameValue() 사용 확인 (Getter 체이닝 금지)")
        void shouldUseLawOfDemeterForTenantName() {
            // Arrange
            Tenant tenant = TenantFixtures.activeTenantWithName("LawOfDemeter Company");

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert
            // ❌ Bad: tenant.getName().getValue()
            // ✅ Good: tenant.getNameValue()
            assertThat(response.name()).isEqualTo(tenant.getNameValue());
            assertThat(response.name()).isEqualTo("LawOfDemeter Company");
        }

        @Test
        @DisplayName("Assembler가 tenant.getStatus().name() 직접 사용 확인")
        void shouldDirectlyAccessStatusEnumName() {
            // Arrange
            Tenant tenant = TenantFixtures.activeTenant();

            // Act
            TenantResponse response = TenantAssembler.toResponse(tenant);

            // Assert
            // Enum은 .name() 직접 사용 (Law of Demeter 예외)
            assertThat(response.status()).isEqualTo(tenant.getStatus().name());
        }
    }

    @Nested
    @DisplayName("유틸리티 클래스 검증")
    class UtilityClassTests {

        @Test
        @DisplayName("TenantAssembler는 인스턴스를 생성할 수 없음")
        void shouldNotAllowInstantiation() {
            // Act & Assert
            assertThatThrownBy(() -> {
                java.lang.reflect.Constructor<?> constructor =
                    TenantAssembler.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            })
            .isInstanceOf(java.lang.reflect.InvocationTargetException.class)
            .hasCauseInstanceOf(AssertionError.class);
        }
    }
}

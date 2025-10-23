package com.ryuqq.fileflow.application.iam.organization.assembler;

import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.domain.iam.organization.OrgCode;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus;
import com.ryuqq.fileflow.fixtures.OrganizationFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

/**
 * OrganizationAssembler 단위 테스트
 *
 * <p>OrganizationAssembler의 Domain ↔ DTO 변환 로직을 검증합니다.</p>
 *
 * <p><strong>테스트 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ @Tag("unit"), @Tag("application"), @Tag("fast") 사용</li>
 *   <li>✅ @Nested 그룹으로 테스트 조직화</li>
 *   <li>✅ AAA 패턴 (Arrange-Act-Assert)</li>
 *   <li>✅ OrganizationFixtures 사용으로 테스트 데이터 표준화</li>
 *   <li>✅ DisplayName으로 한글 설명 제공</li>
 *   <li>✅ Law of Demeter 엄격 준수 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("OrganizationAssembler 테스트")
class OrganizationAssemblerTest {

    @Nested
    @DisplayName("toDomain 메서드")
    class ToDomainTests {

        @Test
        @DisplayName("CreateOrganizationCommand를 Organization Domain으로 변환 성공")
        void toDomain_withValidCommand_shouldCreateOrganization() {
            // Arrange
            String tenantId = "tenant-123";
            String orgCode = "SALES";
            String name = "Sales Department";
            CreateOrganizationCommand command = new CreateOrganizationCommand(tenantId, orgCode, name);
            OrgCode orgCodeVO = OrgCode.of(orgCode);

            // Act
            Organization organization = OrganizationAssembler.toDomain(command, orgCodeVO);

            // Assert
            assertThat(organization).isNotNull();
            assertThat(organization.getId()).isNull();  // ID는 DB 저장 후 생성됨
            assertThat(organization.getTenantId()).isEqualTo(tenantId);
            assertThat(organization.getOrgCodeValue()).isEqualTo(orgCode);
            assertThat(organization.getName()).isEqualTo(name);
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
            assertThat(organization.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("null Command 전달 시 IllegalArgumentException 발생")
        void toDomain_withNullCommand_shouldThrowException() {
            // Arrange
            OrgCode orgCode = OrgCode.of("SALES");

            // Act & Assert
            assertThatThrownBy(() -> OrganizationAssembler.toDomain(null, orgCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CreateOrganizationCommand는 필수입니다");
        }

        @Test
        @DisplayName("null OrgCode 전달 시 IllegalArgumentException 발생")
        void toDomain_withNullOrgCode_shouldThrowException() {
            // Arrange
            CreateOrganizationCommand command = new CreateOrganizationCommand("tenant-123", "SALES", "Sales Dept");

            // Act & Assert
            assertThatThrownBy(() -> OrganizationAssembler.toDomain(command, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OrgCode는 필수입니다");
        }

        @Test
        @DisplayName("모든 필드가 정확히 매핑되어야 함")
        void toDomain_shouldMapAllFieldsCorrectly() {
            // Arrange
            String tenantId = "tenant-456";
            String orgCodeValue = "IT";
            String name = "IT Department";
            CreateOrganizationCommand command = new CreateOrganizationCommand(tenantId, orgCodeValue, name);
            OrgCode orgCode = OrgCode.of(orgCodeValue);

            // Act
            Organization organization = OrganizationAssembler.toDomain(command, orgCode);

            // Assert
            assertThat(organization.getTenantId()).isEqualTo(tenantId);
            assertThat(organization.getOrgCodeValue()).isEqualTo(orgCodeValue);
            assertThat(organization.getName()).isEqualTo(name);
            assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
            assertThat(organization.isDeleted()).isFalse();
            assertThat(organization.getCreatedAt()).isNotNull();
            assertThat(organization.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toResponse 메서드")
    class ToResponseTests {

        @Test
        @DisplayName("활성 Organization을 OrganizationResponse로 변환 성공")
        void toResponse_withActiveOrganization_shouldCreateResponse() {
            // Arrange
            Long organizationId = 1L;
            String tenantId = "tenant-123";
            Organization organization = OrganizationFixtures.salesOrganizationWithId(organizationId, tenantId);

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(organization);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isEqualTo(organizationId);
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.orgCode()).isEqualTo("SALES");
            assertThat(response.name()).isEqualTo("Sales Department");
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.deleted()).isFalse();
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("비활성 Organization 변환 성공")
        void toResponse_withInactiveOrganization_shouldCreateResponse() {
            // Arrange
            Organization organization = OrganizationFixtures.inactiveOrganization();

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(organization);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("INACTIVE");
            assertThat(response.deleted()).isFalse();
        }

        @Test
        @DisplayName("삭제된 Organization 변환 성공")
        void toResponse_withDeletedOrganization_shouldCreateResponse() {
            // Arrange
            Organization organization = OrganizationFixtures.deletedOrganization("tenant-123");

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(organization);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.deleted()).isTrue();
            assertThat(response.status()).isEqualTo("INACTIVE");
        }

        @Test
        @DisplayName("null Organization 전달 시 IllegalArgumentException 발생")
        void toResponse_withNullOrganization_shouldThrowException() {
            // Act & Assert
            assertThatThrownBy(() -> OrganizationAssembler.toResponse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization은 필수입니다");
        }
    }

    @Nested
    @DisplayName("필드 매핑 검증")
    class FieldMappingTests {

        @Test
        @DisplayName("OrganizationId (Long) -> Long 변환 검증")
        void toResponse_shouldMapOrganizationIdAsLong() {
            // Arrange
            Long expectedId = 12345L;
            Organization organization = OrganizationFixtures.salesOrganizationWithId(expectedId, "tenant-123");

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(organization);

            // Assert
            assertThat(response.organizationId()).isEqualTo(expectedId);
            assertThat(response.organizationId()).isInstanceOf(Long.class);
        }

        @Test
        @DisplayName("TenantId (String) -> String 변환 검증")
        void toResponse_shouldMapTenantIdAsString() {
            // Arrange
            String tenantId = "tenant-abc-123";
            Organization organization = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(organization);

            // Assert
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.tenantId()).isInstanceOf(String.class);
        }

        @Test
        @DisplayName("OrgCode Value Object -> String 변환 검증")
        void toResponse_shouldMapOrgCodeAsString() {
            // Arrange
            String orgCodeValue = "IT-DEPT";
            Organization organization = OrganizationFixtures.organizationWithCode(
                "tenant-123", orgCodeValue, "IT Department"
            );
            Organization withId = Organization.reconstitute(
                OrganizationId.of(1L),
                organization.getTenantId(),
                OrgCode.of(orgCodeValue),
                organization.getName(),
                organization.getStatus(),
                organization.getCreatedAt(),
                organization.getUpdatedAt(),
                organization.isDeleted()
            );

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(withId);

            // Assert
            assertThat(response.orgCode()).isEqualTo(orgCodeValue);
            assertThat(response.orgCode()).isInstanceOf(String.class);
        }

        @Test
        @DisplayName("OrganizationName -> String 변환 검증")
        void toResponse_shouldMapNameAsString() {
            // Arrange
            String name = "Human Resources Department";
            Organization organization = OrganizationFixtures.organizationWithCode(
                "tenant-123", "HR", name
            );
            Organization withId = Organization.reconstitute(
                OrganizationId.of(1L),
                organization.getTenantId(),
                organization.getOrgCode(),
                name,
                organization.getStatus(),
                organization.getCreatedAt(),
                organization.getUpdatedAt(),
                organization.isDeleted()
            );

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(withId);

            // Assert
            assertThat(response.name()).isEqualTo(name);
            assertThat(response.name()).isInstanceOf(String.class);
        }

        @Test
        @DisplayName("OrganizationStatus Enum -> String 변환 검증")
        void toResponse_shouldMapStatusEnumToString() {
            // Arrange - ACTIVE
            Organization activeOrg = OrganizationFixtures.salesOrganizationWithId(1L, "tenant-123");

            // Act
            OrganizationResponse activeResponse = OrganizationAssembler.toResponse(activeOrg);

            // Assert
            assertThat(activeResponse.status()).isEqualTo("ACTIVE");
            assertThat(activeResponse.status()).isInstanceOf(String.class);

            // Arrange - INACTIVE
            Organization inactiveOrg = OrganizationFixtures.inactiveOrganization();

            // Act
            OrganizationResponse inactiveResponse = OrganizationAssembler.toResponse(inactiveOrg);

            // Assert
            assertThat(inactiveResponse.status()).isEqualTo("INACTIVE");
        }

        @Test
        @DisplayName("LocalDateTime 객체가 그대로 전달되어야 함")
        void toResponse_shouldPreserveLocalDateTimeObjects() {
            // Arrange
            Clock fixedClock = Clock.fixed(
                Instant.parse("2025-10-23T10:00:00Z"),
                ZoneId.of("UTC")
            );
            Organization organization = OrganizationFixtures.organizationWithClock(
                "tenant-123", "SALES", "Sales Dept", fixedClock
            );
            Organization withId = Organization.reconstitute(
                OrganizationId.of(1L),
                organization.getTenantId(),
                organization.getOrgCode(),
                organization.getName(),
                organization.getStatus(),
                organization.getCreatedAt(),
                organization.getUpdatedAt(),
                organization.isDeleted()
            );

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(withId);

            // Assert
            assertThat(response.createdAt()).isInstanceOf(LocalDateTime.class);
            assertThat(response.updatedAt()).isInstanceOf(LocalDateTime.class);
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("모든 OrganizationStatus 값 변환 검증 (ACTIVE, INACTIVE)")
        void toResponse_shouldHandleAllStatusValues() {
            // ACTIVE
            Organization activeOrg = OrganizationFixtures.customOrganization(
                1L, "tenant-123", "ACTIVE-ORG", "Active Org",
                OrganizationStatus.ACTIVE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                false
            );
            OrganizationResponse activeResponse = OrganizationAssembler.toResponse(activeOrg);
            assertThat(activeResponse.status()).isEqualTo("ACTIVE");

            // INACTIVE
            Organization inactiveOrg = OrganizationFixtures.customOrganization(
                2L, "tenant-123", "INACTIVE-ORG", "Inactive Org",
                OrganizationStatus.INACTIVE,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now(),
                false
            );
            OrganizationResponse inactiveResponse = OrganizationAssembler.toResponse(inactiveOrg);
            assertThat(inactiveResponse.status()).isEqualTo("INACTIVE");
        }

        @Test
        @DisplayName("특수문자 포함 orgCode와 name 변환 성공")
        void toResponse_shouldHandleSpecialCharacters() {
            // Arrange
            String specialOrgCode = "DEPT-IT_001";
            String specialName = "IT Department (R&D)";
            Organization organization = OrganizationFixtures.customOrganization(
                1L, "tenant-123", specialOrgCode, specialName,
                OrganizationStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
            );

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(organization);

            // Assert
            assertThat(response.orgCode()).isEqualTo(specialOrgCode);
            assertThat(response.name()).isEqualTo(specialName);
        }

        @Test
        @DisplayName("Long ID 경계값 테스트 (1L, 999999L)")
        void toResponse_shouldHandleLongIdBoundaryValues() {
            // 최소 ID
            Organization minIdOrg = OrganizationFixtures.salesOrganizationWithId(1L, "tenant-123");
            OrganizationResponse minResponse = OrganizationAssembler.toResponse(minIdOrg);
            assertThat(minResponse.organizationId()).isEqualTo(1L);

            // 큰 ID
            Organization maxIdOrg = OrganizationFixtures.salesOrganizationWithId(999999L, "tenant-123");
            OrganizationResponse maxResponse = OrganizationAssembler.toResponse(maxIdOrg);
            assertThat(maxResponse.organizationId()).isEqualTo(999999L);
        }

        @Test
        @DisplayName("최소/최대 길이 name 변환 성공")
        void toResponse_shouldHandleMinMaxNameLength() {
            // 최소 길이 (1자 name, OrgCode는 최소 2자)
            Organization minOrg = OrganizationFixtures.customOrganization(
                1L, "tenant-123", "AB", "A",
                OrganizationStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
            );
            OrganizationResponse minResponse = OrganizationAssembler.toResponse(minOrg);
            assertThat(minResponse.name()).isEqualTo("A");

            // 긴 이름
            String longName = "Very Long Organization Name ".repeat(10);
            Organization maxOrg = OrganizationFixtures.customOrganization(
                2L, "tenant-123", "LONG", longName,
                OrganizationStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
            );
            OrganizationResponse maxResponse = OrganizationAssembler.toResponse(maxOrg);
            assertThat(maxResponse.name()).isEqualTo(longName);
        }

        @Test
        @DisplayName("삭제된 상태에서 모든 필드 정확히 매핑되어야 함")
        void toResponse_withDeletedOrganization_shouldMapAllFieldsCorrectly() {
            // Arrange
            Long id = 999L;
            String tenantId = "tenant-deleted";
            String orgCode = "DELETED-ORG";
            String name = "Deleted Organization";
            LocalDateTime createdAt = LocalDateTime.now().minusDays(60);
            LocalDateTime updatedAt = LocalDateTime.now().minusDays(30);

            Organization organization = OrganizationFixtures.customOrganization(
                id, tenantId, orgCode, name,
                OrganizationStatus.INACTIVE,
                createdAt, updatedAt, true
            );

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(organization);

            // Assert
            assertThat(response.organizationId()).isEqualTo(id);
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.orgCode()).isEqualTo(orgCode);
            assertThat(response.name()).isEqualTo(name);
            assertThat(response.status()).isEqualTo("INACTIVE");
            assertThat(response.deleted()).isTrue();
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.updatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 검증")
    class LawOfDemeterTests {

        @Test
        @DisplayName("organization.getIdValue() 사용 확인 (Getter 체이닝 금지)")
        void toResponse_shouldUseGetIdValue_notGetterChaining() {
            // Arrange
            Long expectedId = 100L;
            Organization organization = OrganizationFixtures.salesOrganizationWithId(expectedId, "tenant-123");

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(organization);

            // Assert - Law of Demeter: organization.getIdValue() 사용 (not getId().value())
            assertThat(response.organizationId()).isEqualTo(expectedId);
            assertThat(organization.getIdValue()).isEqualTo(expectedId);
        }

        @Test
        @DisplayName("organization.getTenantId() 직접 사용 확인")
        void toResponse_shouldUseGetTenantIdDirectly() {
            // Arrange
            String tenantId = "tenant-law-of-demeter";
            Organization organization = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(organization);

            // Assert - Law of Demeter: 직접 접근
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(organization.getTenantId()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("organization.getOrgCodeValue() 사용 확인")
        void toResponse_shouldUseGetOrgCodeValue() {
            // Arrange
            String orgCodeValue = "LOD-TEST";
            Organization organization = OrganizationFixtures.organizationWithCode(
                "tenant-123", orgCodeValue, "LOD Test"
            );
            Organization withId = Organization.reconstitute(
                OrganizationId.of(1L),
                organization.getTenantId(),
                organization.getOrgCode(),
                organization.getName(),
                organization.getStatus(),
                organization.getCreatedAt(),
                organization.getUpdatedAt(),
                organization.isDeleted()
            );

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(withId);

            // Assert - Law of Demeter
            assertThat(response.orgCode()).isEqualTo(orgCodeValue);
            assertThat(withId.getOrgCodeValue()).isEqualTo(orgCodeValue);
        }

        @Test
        @DisplayName("organization.getName() 직접 사용 확인")
        void toResponse_shouldUseGetNameDirectly() {
            // Arrange
            String name = "Law of Demeter Test Org";
            Organization organization = OrganizationFixtures.organizationWithCode(
                "tenant-123", "LOD", name
            );
            Organization withId = Organization.reconstitute(
                OrganizationId.of(1L),
                organization.getTenantId(),
                organization.getOrgCode(),
                name,
                organization.getStatus(),
                organization.getCreatedAt(),
                organization.getUpdatedAt(),
                organization.isDeleted()
            );

            // Act
            OrganizationResponse response = OrganizationAssembler.toResponse(withId);

            // Assert
            assertThat(response.name()).isEqualTo(name);
            assertThat(withId.getName()).isEqualTo(name);
        }
    }

    @Nested
    @DisplayName("Utility Class 검증")
    class UtilityClassTests {

        @Test
        @DisplayName("OrganizationAssembler 인스턴스 생성 불가 검증")
        void utilityClass_shouldThrowAssertionError_whenInstantiated() throws Exception {
            // Arrange
            Constructor<OrganizationAssembler> constructor = OrganizationAssembler.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            // Act & Assert
            assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(AssertionError.class)
                .cause()
                .hasMessage("Cannot instantiate utility class");
        }
    }
}

package com.ryuqq.fileflow.adapter.rest.iam.organization.mapper;

import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request.CreateOrganizationApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.response.OrganizationApiResponse;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request.UpdateOrganizationApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request.UpdateOrganizationStatusApiRequest;
import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OrganizationDtoMapperTest - OrganizationDtoMapper 단위 테스트
 *
 * <p>OrganizationDtoMapper의 REST DTO ↔ Application DTO 변환 로직을 검증합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ {@code toCommand()}: Request → Command 변환 검증</li>
 *   <li>✅ {@code toApiResponse()}: Response → ApiResponse 변환 검증</li>
 *   <li>✅ null 파라미터 예외 처리 검증</li>
 *   <li>✅ Validation 어노테이션 검증 (Request 객체)</li>
 *   <li>✅ 필드 매핑 정확성 검증</li>
 *   <li>✅ String FK 전략 검증 (tenantId는 String 타입)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("adapter")
@Tag("fast")
@DisplayName("OrganizationDtoMapper 테스트")
class OrganizationApiMapperTest {

    private static final String DEFAULT_TENANT_ID = "tenant-uuid-123";

    @Nested
    @DisplayName("toCommand() - CreateOrganizationRequest → CreateOrganizationCommand 변환")
    class ToCreateCommandTests {

        /**
         * 정상: CreateOrganizationRequest를 CreateOrganizationCommand로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("CreateOrganizationRequest를 CreateOrganizationCommand로 변환한다")
        void toCommand_CreateOrganizationRequest_ReturnsCommand() {
            // Given
            CreateOrganizationApiRequest request = new CreateOrganizationApiRequest(
                DEFAULT_TENANT_ID,
                "ORG001",
                "Engineering Department"
            );

            // When
            CreateOrganizationCommand command = OrganizationApiMapper.toCommand(request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.tenantId()).isEqualTo(DEFAULT_TENANT_ID);  // String FK
            assertThat(command.orgCode()).isEqualTo("ORG001");
            assertThat(command.name()).isEqualTo("Engineering Department");
        }

        /**
         * 예외: null Request 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null CreateOrganizationRequest 전달 시 IllegalArgumentException이 발생한다")
        void toCommand_NullCreateOrganizationRequest_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> OrganizationApiMapper.toCommand((CreateOrganizationApiRequest) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreateOrganizationRequest는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("toCommand() - UpdateOrganizationRequest → UpdateOrganizationCommand 변환")
    class ToUpdateCommandTests {

        /**
         * 정상: UpdateOrganizationRequest를 UpdateOrganizationCommand로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("UpdateOrganizationRequest를 UpdateOrganizationCommand로 변환한다")
        void toCommand_UpdateOrganizationRequest_ReturnsCommand() {
            // Given
            Long organizationId = 1L;
            UpdateOrganizationApiRequest request = new UpdateOrganizationApiRequest("Updated Department Name");

            // When
            UpdateOrganizationCommand command = OrganizationApiMapper.toCommand(organizationId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.organizationId()).isEqualTo(organizationId);
            assertThat(command.name()).isEqualTo("Updated Department Name");
        }

        /**
         * 예외: null organizationId 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null organizationId 전달 시 IllegalArgumentException이 발생한다")
        void toCommand_NullOrganizationId_ThrowsException() {
            // Given
            UpdateOrganizationApiRequest request = new UpdateOrganizationApiRequest("Updated Department Name");

            // When & Then
            assertThatThrownBy(() -> OrganizationApiMapper.toCommand(null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organization ID는 null일 수 없으며 양수여야 합니다");
        }

        /**
         * 예외: null Request 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null UpdateOrganizationRequest 전달 시 IllegalArgumentException이 발생한다")
        void toCommand_NullUpdateOrganizationRequest_ThrowsException() {
            // Given
            Long organizationId = 1L;

            // When & Then
            assertThatThrownBy(() -> OrganizationApiMapper.toCommand(organizationId, (UpdateOrganizationApiRequest) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateOrganizationRequest는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("toCommand() - UpdateOrganizationStatusRequest → UpdateOrganizationStatusCommand 변환")
    class ToUpdateStatusCommandTests {

        /**
         * 정상: UpdateOrganizationStatusRequest를 UpdateOrganizationStatusCommand로 변환 (ACTIVE)
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("UpdateOrganizationStatusRequest를 UpdateOrganizationStatusCommand로 변환한다 (ACTIVE)")
        void toCommand_UpdateOrganizationStatusRequestActive_ReturnsCommand() {
            // Given
            Long organizationId = 1L;
            UpdateOrganizationStatusApiRequest request = new UpdateOrganizationStatusApiRequest("ACTIVE");

            // When
            UpdateOrganizationStatusCommand command = OrganizationApiMapper.toCommand(organizationId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.organizationId()).isEqualTo(organizationId);
            assertThat(command.status()).isEqualTo("ACTIVE");
        }

        /**
         * 정상: UpdateOrganizationStatusRequest를 UpdateOrganizationStatusCommand로 변환 (INACTIVE)
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("UpdateOrganizationStatusRequest를 UpdateOrganizationStatusCommand로 변환한다 (INACTIVE)")
        void toCommand_UpdateOrganizationStatusRequestInactive_ReturnsCommand() {
            // Given
            Long organizationId = 2L;
            UpdateOrganizationStatusApiRequest request = new UpdateOrganizationStatusApiRequest("INACTIVE");

            // When
            UpdateOrganizationStatusCommand command = OrganizationApiMapper.toCommand(organizationId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.organizationId()).isEqualTo(organizationId);
            assertThat(command.status()).isEqualTo("INACTIVE");
        }

        /**
         * 예외: null organizationId 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null organizationId 전달 시 IllegalArgumentException이 발생한다")
        void toCommand_NullOrganizationId_ThrowsException() {
            // Given
            UpdateOrganizationStatusApiRequest request = new UpdateOrganizationStatusApiRequest("ACTIVE");

            // When & Then
            assertThatThrownBy(() -> OrganizationApiMapper.toCommand(null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organization ID는 null일 수 없으며 양수여야 합니다");
        }

        /**
         * 예외: null Request 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null UpdateOrganizationStatusRequest 전달 시 IllegalArgumentException이 발생한다")
        void toCommand_NullUpdateOrganizationStatusRequest_ThrowsException() {
            // Given
            Long organizationId = 1L;

            // When & Then
            assertThatThrownBy(() -> OrganizationApiMapper.toCommand(organizationId, (UpdateOrganizationStatusApiRequest) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateOrganizationStatusRequest는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("toApiResponse() - OrganizationResponse → OrganizationApiResponse 변환")
    class ToApiResponseTests {

        /**
         * 정상: ACTIVE 상태 OrganizationResponse를 OrganizationApiResponse로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("ACTIVE 상태 OrganizationResponse를 OrganizationApiResponse로 변환한다")
        void toApiResponse_ActiveOrganizationResponse_ReturnsApiResponse() {
            // Given
            Long organizationId = 1L;
            String tenantId = DEFAULT_TENANT_ID;
            String orgCode = "ORG001";
            String name = "Engineering Department";
            String status = "ACTIVE";
            LocalDateTime createdAt = LocalDateTime.now().minusDays(30);
            LocalDateTime updatedAt = LocalDateTime.now();

            OrganizationResponse response = new OrganizationResponse(
                organizationId, tenantId, orgCode, name, status, false, createdAt, updatedAt
            );

            // When
            OrganizationApiResponse apiResponse = OrganizationApiMapper.toApiResponse(response);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.organizationId()).isEqualTo(organizationId);
            assertThat(apiResponse.tenantId()).isEqualTo(tenantId);  // String FK
            assertThat(apiResponse.orgCode()).isEqualTo(orgCode);
            assertThat(apiResponse.name()).isEqualTo(name);
            assertThat(apiResponse.deleted()).isFalse();
            assertThat(apiResponse.createdAt()).isEqualTo(createdAt);
            assertThat(apiResponse.updatedAt()).isEqualTo(updatedAt);
        }

        /**
         * 정상: INACTIVE 상태 OrganizationResponse를 OrganizationApiResponse로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("INACTIVE 상태 OrganizationResponse를 OrganizationApiResponse로 변환한다")
        void toApiResponse_InactiveOrganizationResponse_ReturnsApiResponse() {
            // Given
            Long organizationId = 2L;
            String tenantId = DEFAULT_TENANT_ID;
            String orgCode = "HR";
            String name = "HR Department";
            String status = "INACTIVE";
            LocalDateTime createdAt = LocalDateTime.now().minusDays(60);
            LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

            OrganizationResponse response = new OrganizationResponse(
                organizationId, tenantId, orgCode, name, status, false, createdAt, updatedAt
            );

            // When
            OrganizationApiResponse apiResponse = OrganizationApiMapper.toApiResponse(response);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.deleted()).isFalse();
        }

        /**
         * 정상: 삭제된 OrganizationResponse를 OrganizationApiResponse로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("deleted=true인 OrganizationResponse를 OrganizationApiResponse로 변환한다")
        void toApiResponse_DeletedOrganizationResponse_ReturnsApiResponse() {
            // Given
            Long organizationId = 3L;
            String tenantId = DEFAULT_TENANT_ID;
            String orgCode = "IT";
            String name = "IT Department";
            String status = "INACTIVE";
            LocalDateTime createdAt = LocalDateTime.now().minusDays(90);
            LocalDateTime updatedAt = LocalDateTime.now().minusDays(30);

            OrganizationResponse response = new OrganizationResponse(
                organizationId, tenantId, orgCode, name, status, true, createdAt, updatedAt
            );

            // When
            OrganizationApiResponse apiResponse = OrganizationApiMapper.toApiResponse(response);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.deleted()).isTrue();
        }

        /**
         * 예외: null Response 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null OrganizationResponse 전달 시 IllegalArgumentException이 발생한다")
        void toApiResponse_NullOrganizationResponse_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> OrganizationApiMapper.toApiResponse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OrganizationResponse는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("Utility 클래스 검증")
    class UtilityClassTests {

        /**
         * Utility 클래스: 인스턴스 생성 불가 검증
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("OrganizationDtoMapper는 인스턴스화할 수 없다")
        void utilityClass_CannotBeInstantiated() {
            // When & Then - Reflection으로 인스턴스 생성 시도 시 UnsupportedOperationException 발생
            assertThatThrownBy(() -> {
                java.lang.reflect.Constructor<OrganizationApiMapper> constructor =
                    OrganizationApiMapper.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            })
            .isInstanceOf(java.lang.reflect.InvocationTargetException.class)
            .hasCauseInstanceOf(UnsupportedOperationException.class)
            .hasRootCauseMessage("Utility class cannot be instantiated");
        }
    }
}

package com.ryuqq.fileflow.adapter.rest.iam.tenant.mapper;

import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.CreateTenantApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.response.TenantApiResponse;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.UpdateTenantApiRequest;
import com.ryuqq.fileflow.adapter.rest.iam.tenant.dto.request.UpdateTenantStatusApiRequest;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantStatusCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TenantDtoMapperTest - TenantDtoMapper 단위 테스트
 *
 * <p>TenantDtoMapper의 REST DTO ↔ Application DTO 변환 로직을 검증합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ {@code toCommand()}: Request → Command 변환 검증</li>
 *   <li>✅ {@code toApiResponse()}: Response → ApiResponse 변환 검증</li>
 *   <li>✅ null 파라미터 예외 처리 검증</li>
 *   <li>✅ Validation 어노테이션 검증 (Request 객체)</li>
 *   <li>✅ 필드 매핑 정확성 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("adapter")
@Tag("fast")
@DisplayName("TenantDtoMapper 테스트")
class TenantApiMapperTest {

    @Nested
    @DisplayName("toCommand() - CreateTenantRequest → CreateTenantCommand 변환")
    class ToCreateCommandTests {

        /**
         * 정상: CreateTenantRequest를 CreateTenantCommand로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("CreateTenantRequest를 CreateTenantCommand로 변환한다")
        void toCommand_CreateTenantRequest_ReturnsCommand() {
            // Given
            CreateTenantApiRequest request = new CreateTenantApiRequest("Test Company");

            // When
            CreateTenantCommand command = TenantApiMapper.toCommand(request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.name()).isEqualTo("Test Company");
        }

        /**
         * 예외: null Request 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null CreateTenantRequest 전달 시 IllegalArgumentException이 발생한다")
        void toCommand_NullCreateTenantRequest_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> TenantApiMapper.toCommand((CreateTenantApiRequest) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreateTenantRequest는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("toCommand() - UpdateTenantRequest → UpdateTenantCommand 변환")
    class ToUpdateCommandTests {

        /**
         * 정상: UpdateTenantRequest를 UpdateTenantCommand로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("UpdateTenantRequest를 UpdateTenantCommand로 변환한다")
        void toCommand_UpdateTenantRequest_ReturnsCommand() {
            // Given
            String tenantId = "tenant-uuid-123";
            UpdateTenantApiRequest request = new UpdateTenantApiRequest("Updated Company Name");

            // When
            UpdateTenantCommand command = TenantApiMapper.toCommand(tenantId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.tenantId()).isEqualTo(tenantId);
            assertThat(command.name()).isEqualTo("Updated Company Name");
        }

        /**
         * 예외: null tenantId 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null tenantId 전달 시 IllegalArgumentException이 발생한다")
        void toCommand_NullTenantId_ThrowsException() {
            // Given
            UpdateTenantApiRequest request = new UpdateTenantApiRequest("Updated Company Name");

            // When & Then
            assertThatThrownBy(() -> TenantApiMapper.toCommand(null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 null일 수 없습니다");
        }

        /**
         * 예외: null Request 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null UpdateTenantRequest 전달 시 IllegalArgumentException이 발생한다")
        void toCommand_NullUpdateTenantRequest_ThrowsException() {
            // Given
            String tenantId = "tenant-uuid-123";

            // When & Then
            assertThatThrownBy(() -> TenantApiMapper.toCommand(tenantId, (UpdateTenantApiRequest) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateTenantRequest는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("toCommand() - UpdateTenantStatusRequest → UpdateTenantStatusCommand 변환")
    class ToUpdateStatusCommandTests {

        /**
         * 정상: UpdateTenantStatusRequest를 UpdateTenantStatusCommand로 변환 (ACTIVE)
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("UpdateTenantStatusRequest를 UpdateTenantStatusCommand로 변환한다 (ACTIVE)")
        void toCommand_UpdateTenantStatusRequestActive_ReturnsCommand() {
            // Given
            String tenantId = "tenant-uuid-123";
            UpdateTenantStatusApiRequest request = new UpdateTenantStatusApiRequest("ACTIVE");

            // When
            UpdateTenantStatusCommand command = TenantApiMapper.toCommand(tenantId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.tenantId()).isEqualTo(tenantId);
            assertThat(command.status()).isEqualTo("ACTIVE");
        }

        /**
         * 정상: UpdateTenantStatusRequest를 UpdateTenantStatusCommand로 변환 (SUSPENDED)
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("UpdateTenantStatusRequest를 UpdateTenantStatusCommand로 변환한다 (SUSPENDED)")
        void toCommand_UpdateTenantStatusRequestSuspended_ReturnsCommand() {
            // Given
            String tenantId = "tenant-uuid-456";
            UpdateTenantStatusApiRequest request = new UpdateTenantStatusApiRequest("SUSPENDED");

            // When
            UpdateTenantStatusCommand command = TenantApiMapper.toCommand(tenantId, request);

            // Then
            assertThat(command).isNotNull();
            assertThat(command.tenantId()).isEqualTo(tenantId);
            assertThat(command.status()).isEqualTo("SUSPENDED");
        }

        /**
         * 예외: null tenantId 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null tenantId 전달 시 IllegalArgumentException이 발생한다")
        void toCommand_NullTenantId_ThrowsException() {
            // Given
            UpdateTenantStatusApiRequest request = new UpdateTenantStatusApiRequest("ACTIVE");

            // When & Then
            assertThatThrownBy(() -> TenantApiMapper.toCommand(null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 null일 수 없습니다");
        }

        /**
         * 예외: null Request 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null UpdateTenantStatusRequest 전달 시 IllegalArgumentException이 발생한다")
        void toCommand_NullUpdateTenantStatusRequest_ThrowsException() {
            // Given
            String tenantId = "tenant-uuid-123";

            // When & Then
            assertThatThrownBy(() -> TenantApiMapper.toCommand(tenantId, (UpdateTenantStatusApiRequest) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateTenantStatusRequest는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("toApiResponse() - TenantResponse → TenantApiResponse 변환")
    class ToApiResponseTests {

        /**
         * 정상: ACTIVE 상태 TenantResponse를 TenantApiResponse로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("ACTIVE 상태 TenantResponse를 TenantApiResponse로 변환한다")
        void toApiResponse_ActiveTenantResponse_ReturnsApiResponse() {
            // Given
            String tenantId = "tenant-uuid-123";
            String name = "Test Company";
            String status = "ACTIVE";
            LocalDateTime createdAt = LocalDateTime.now().minusDays(30);
            LocalDateTime updatedAt = LocalDateTime.now();

            TenantResponse response = new TenantResponse(
                tenantId, name, status, false, createdAt, updatedAt
            );

            // When
            TenantApiResponse apiResponse = TenantApiMapper.toApiResponse(response);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.tenantId()).isEqualTo(tenantId);
            assertThat(apiResponse.name()).isEqualTo(name);
            assertThat(apiResponse.status()).isEqualTo(status);
            assertThat(apiResponse.deleted()).isFalse();
            assertThat(apiResponse.createdAt()).isEqualTo(createdAt);
            assertThat(apiResponse.updatedAt()).isEqualTo(updatedAt);
        }

        /**
         * 정상: SUSPENDED 상태 TenantResponse를 TenantApiResponse로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("SUSPENDED 상태 TenantResponse를 TenantApiResponse로 변환한다")
        void toApiResponse_SuspendedTenantResponse_ReturnsApiResponse() {
            // Given
            String tenantId = "tenant-uuid-456";
            String name = "Suspended Company";
            String status = "SUSPENDED";
            LocalDateTime createdAt = LocalDateTime.now().minusDays(60);
            LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

            TenantResponse response = new TenantResponse(
                tenantId, name, status, false, createdAt, updatedAt
            );

            // When
            TenantApiResponse apiResponse = TenantApiMapper.toApiResponse(response);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.status()).isEqualTo("SUSPENDED");
            assertThat(apiResponse.deleted()).isFalse();
        }

        /**
         * 정상: 삭제된 TenantResponse를 TenantApiResponse로 변환
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("deleted=true인 TenantResponse를 TenantApiResponse로 변환한다")
        void toApiResponse_DeletedTenantResponse_ReturnsApiResponse() {
            // Given
            String tenantId = "tenant-uuid-789";
            String name = "Deleted Company";
            String status = "SUSPENDED";
            LocalDateTime createdAt = LocalDateTime.now().minusDays(90);
            LocalDateTime updatedAt = LocalDateTime.now().minusDays(30);

            TenantResponse response = new TenantResponse(
                tenantId, name, status, true, createdAt, updatedAt
            );

            // When
            TenantApiResponse apiResponse = TenantApiMapper.toApiResponse(response);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.deleted()).isTrue();
            assertThat(apiResponse.status()).isEqualTo("SUSPENDED");
        }

        /**
         * 예외: null Response 전달 시 IllegalArgumentException 발생
         *
         * @author ryu-qqq
         * @since 2025-10-23
         */
        @Test
        @DisplayName("null TenantResponse 전달 시 IllegalArgumentException이 발생한다")
        void toApiResponse_NullTenantResponse_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> TenantApiMapper.toApiResponse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TenantResponse는 null일 수 없습니다");
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
        @DisplayName("TenantDtoMapper는 인스턴스화할 수 없다")
        void utilityClass_CannotBeInstantiated() {
            // When & Then - Reflection으로 인스턴스 생성 시도 시 UnsupportedOperationException 발생
            assertThatThrownBy(() -> {
                java.lang.reflect.Constructor<TenantApiMapper> constructor =
                    TenantApiMapper.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            })
            .isInstanceOf(java.lang.reflect.InvocationTargetException.class)
            .hasCauseInstanceOf(UnsupportedOperationException.class)
            .hasRootCauseMessage("Utility class cannot be instantiated");
        }
    }
}

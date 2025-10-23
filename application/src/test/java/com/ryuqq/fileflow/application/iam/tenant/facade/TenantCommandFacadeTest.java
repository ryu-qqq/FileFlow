package com.ryuqq.fileflow.application.iam.tenant.facade;

import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantStatusCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.in.CreateTenantUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.in.UpdateTenantStatusUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.in.UpdateTenantUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TenantCommandFacadeTest - TenantCommandFacade 단위 테스트
 *
 * <p><strong>테스트 목적:</strong></p>
 * <ul>
 *   <li>Facade의 UseCase 위임 검증</li>
 *   <li>여러 Command UseCase의 통합 진입점 검증</li>
 *   <li>예외 전파 검증 (Facade는 예외를 그대로 전파)</li>
 * </ul>
 *
 * <p><strong>테스트 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ AAA 패턴 (Arrange-Act-Assert)</li>
 *   <li>✅ @Nested를 이용한 테스트 그룹화</li>
 *   <li>✅ Mock을 이용한 의존성 격리</li>
 *   <li>✅ @Tag("unit") @Tag("application") @Tag("fast")</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("TenantCommandFacade 테스트")
class TenantCommandFacadeTest {

    private CreateTenantUseCase createTenantUseCase;
    private UpdateTenantUseCase updateTenantUseCase;
    private UpdateTenantStatusUseCase updateTenantStatusUseCase;
    private TenantCommandFacade tenantCommandFacade;

    @BeforeEach
    void setUp() {
        // Arrange - Mock 의존성 생성
        createTenantUseCase = mock(CreateTenantUseCase.class);
        updateTenantUseCase = mock(UpdateTenantUseCase.class);
        updateTenantStatusUseCase = mock(UpdateTenantStatusUseCase.class);

        // Arrange - Facade 생성 (Constructor Injection)
        tenantCommandFacade = new TenantCommandFacade(
            createTenantUseCase,
            updateTenantUseCase,
            updateTenantStatusUseCase
        );
    }

    @Nested
    @DisplayName("createTenant - Tenant 생성")
    class CreateTenantTests {

        @Test
        @DisplayName("CreateTenantUseCase로 위임하고 결과를 그대로 반환한다")
        void shouldDelegateToCreateTenantUseCase() {
            // Arrange
            CreateTenantCommand command = new CreateTenantCommand("test-tenant");
            TenantResponse expectedResponse = new TenantResponse(
                "tenant-id-123",
                "test-tenant",
                "ACTIVE",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            when(createTenantUseCase.execute(command)).thenReturn(expectedResponse);

            // Act
            TenantResponse actualResponse = tenantCommandFacade.createTenant(command);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).isEqualTo(expectedResponse);
            assertThat(actualResponse.tenantId()).isEqualTo("tenant-id-123");
            assertThat(actualResponse.name()).isEqualTo("test-tenant");
            assertThat(actualResponse.status()).isEqualTo("ACTIVE");

            verify(createTenantUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("UseCase에서 발생한 예외를 그대로 전파한다")
        void shouldPropagateExceptionFromUseCase() {
            // Arrange
            CreateTenantCommand command = new CreateTenantCommand("duplicate-tenant");
            IllegalStateException expectedException = new IllegalStateException("동일한 이름의 Tenant가 이미 존재합니다");

            when(createTenantUseCase.execute(command)).thenThrow(expectedException);

            // Act & Assert
            assertThatThrownBy(() -> tenantCommandFacade.createTenant(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("동일한 이름의 Tenant가 이미 존재합니다");

            verify(createTenantUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("Command가 null이어도 UseCase로 위임한다 (UseCase에서 검증)")
        void shouldDelegateEvenWhenCommandIsNull() {
            // Arrange
            IllegalArgumentException expectedException = new IllegalArgumentException("CreateTenantCommand는 필수입니다");

            when(createTenantUseCase.execute(null)).thenThrow(expectedException);

            // Act & Assert
            assertThatThrownBy(() -> tenantCommandFacade.createTenant(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreateTenantCommand는 필수입니다");

            verify(createTenantUseCase, times(1)).execute(null);
        }
    }

    @Nested
    @DisplayName("updateTenant - Tenant 수정")
    class UpdateTenantTests {

        @Test
        @DisplayName("UpdateTenantUseCase로 위임하고 결과를 그대로 반환한다")
        void shouldDelegateToUpdateTenantUseCase() {
            // Arrange
            UpdateTenantCommand command = new UpdateTenantCommand("tenant-id-123", "updated-name");
            TenantResponse expectedResponse = new TenantResponse(
                "tenant-id-123",
                "updated-name",
                "ACTIVE",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            when(updateTenantUseCase.execute(command)).thenReturn(expectedResponse);

            // Act
            TenantResponse actualResponse = tenantCommandFacade.updateTenant(command);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).isEqualTo(expectedResponse);
            assertThat(actualResponse.tenantId()).isEqualTo("tenant-id-123");
            assertThat(actualResponse.name()).isEqualTo("updated-name");

            verify(updateTenantUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("UseCase에서 발생한 예외를 그대로 전파한다 (Tenant 미존재)")
        void shouldPropagateExceptionWhenTenantNotFound() {
            // Arrange
            UpdateTenantCommand command = new UpdateTenantCommand("non-existent-id", "new-name");
            IllegalStateException expectedException = new IllegalStateException("Tenant를 찾을 수 없습니다");

            when(updateTenantUseCase.execute(command)).thenThrow(expectedException);

            // Act & Assert
            assertThatThrownBy(() -> tenantCommandFacade.updateTenant(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다");

            verify(updateTenantUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("Command가 null이어도 UseCase로 위임한다 (UseCase에서 검증)")
        void shouldDelegateEvenWhenCommandIsNull() {
            // Arrange
            IllegalArgumentException expectedException = new IllegalArgumentException("UpdateTenantCommand는 필수입니다");

            when(updateTenantUseCase.execute(null)).thenThrow(expectedException);

            // Act & Assert
            assertThatThrownBy(() -> tenantCommandFacade.updateTenant(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateTenantCommand는 필수입니다");

            verify(updateTenantUseCase, times(1)).execute(null);
        }
    }

    @Nested
    @DisplayName("updateTenantStatus - Tenant 상태 변경")
    class UpdateTenantStatusTests {

        @Test
        @DisplayName("UpdateTenantStatusUseCase로 위임하고 결과를 그대로 반환한다 (ACTIVE → SUSPENDED)")
        void shouldDelegateToUpdateTenantStatusUseCaseForSuspend() {
            // Arrange
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand("tenant-id-123", "SUSPENDED");
            TenantResponse expectedResponse = new TenantResponse(
                "tenant-id-123",
                "test-tenant",
                "SUSPENDED",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            when(updateTenantStatusUseCase.execute(command)).thenReturn(expectedResponse);

            // Act
            TenantResponse actualResponse = tenantCommandFacade.updateTenantStatus(command);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).isEqualTo(expectedResponse);
            assertThat(actualResponse.tenantId()).isEqualTo("tenant-id-123");
            assertThat(actualResponse.status()).isEqualTo("SUSPENDED");

            verify(updateTenantStatusUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("UpdateTenantStatusUseCase로 위임하고 결과를 그대로 반환한다 (SUSPENDED → ACTIVE)")
        void shouldDelegateToUpdateTenantStatusUseCaseForActivate() {
            // Arrange
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand("tenant-id-123", "ACTIVE");
            TenantResponse expectedResponse = new TenantResponse(
                "tenant-id-123",
                "test-tenant",
                "ACTIVE",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            when(updateTenantStatusUseCase.execute(command)).thenReturn(expectedResponse);

            // Act
            TenantResponse actualResponse = tenantCommandFacade.updateTenantStatus(command);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).isEqualTo(expectedResponse);
            assertThat(actualResponse.status()).isEqualTo("ACTIVE");

            verify(updateTenantStatusUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("UseCase에서 발생한 예외를 그대로 전파한다 (잘못된 상태값)")
        void shouldPropagateExceptionWhenInvalidStatus() {
            // Arrange
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand("tenant-id-123", "INVALID_STATUS");
            IllegalArgumentException expectedException = new IllegalArgumentException("지원하지 않는 상태값입니다");

            when(updateTenantStatusUseCase.execute(command)).thenThrow(expectedException);

            // Act & Assert
            assertThatThrownBy(() -> tenantCommandFacade.updateTenantStatus(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 상태값입니다");

            verify(updateTenantStatusUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("UseCase에서 발생한 예외를 그대로 전파한다 (Tenant 미존재)")
        void shouldPropagateExceptionWhenTenantNotFound() {
            // Arrange
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand("non-existent-id", "ACTIVE");
            IllegalStateException expectedException = new IllegalStateException("Tenant를 찾을 수 없습니다");

            when(updateTenantStatusUseCase.execute(command)).thenThrow(expectedException);

            // Act & Assert
            assertThatThrownBy(() -> tenantCommandFacade.updateTenantStatus(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다");

            verify(updateTenantStatusUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("Command가 null이어도 UseCase로 위임한다 (UseCase에서 검증)")
        void shouldDelegateEvenWhenCommandIsNull() {
            // Arrange
            IllegalArgumentException expectedException = new IllegalArgumentException("UpdateTenantStatusCommand는 필수입니다");

            when(updateTenantStatusUseCase.execute(null)).thenThrow(expectedException);

            // Act & Assert
            assertThatThrownBy(() -> tenantCommandFacade.updateTenantStatus(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateTenantStatusCommand는 필수입니다");

            verify(updateTenantStatusUseCase, times(1)).execute(null);
        }
    }

    @Nested
    @DisplayName("Facade 통합 테스트 - 여러 Command UseCase 조율")
    class FacadeOrchestrationTests {

        @Test
        @DisplayName("3개의 독립적인 UseCase를 모두 올바르게 위임한다")
        void shouldOrchestrateMulitpleUseCases() {
            // Arrange
            CreateTenantCommand createCommand = new CreateTenantCommand("new-tenant");
            UpdateTenantCommand updateCommand = new UpdateTenantCommand("tenant-id-123", "updated-name");
            UpdateTenantStatusCommand statusCommand = new UpdateTenantStatusCommand("tenant-id-123", "SUSPENDED");

            TenantResponse createResponse = new TenantResponse(
                "tenant-id-123",
                "new-tenant",
                "ACTIVE",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            TenantResponse updateResponse = new TenantResponse(
                "tenant-id-123",
                "updated-name",
                "ACTIVE",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            TenantResponse statusResponse = new TenantResponse(
                "tenant-id-123",
                "updated-name",
                "SUSPENDED",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            when(createTenantUseCase.execute(createCommand)).thenReturn(createResponse);
            when(updateTenantUseCase.execute(updateCommand)).thenReturn(updateResponse);
            when(updateTenantStatusUseCase.execute(statusCommand)).thenReturn(statusResponse);

            // Act
            TenantResponse result1 = tenantCommandFacade.createTenant(createCommand);
            TenantResponse result2 = tenantCommandFacade.updateTenant(updateCommand);
            TenantResponse result3 = tenantCommandFacade.updateTenantStatus(statusCommand);

            // Assert
            assertThat(result1.name()).isEqualTo("new-tenant");
            assertThat(result2.name()).isEqualTo("updated-name");
            assertThat(result3.status()).isEqualTo("SUSPENDED");

            verify(createTenantUseCase, times(1)).execute(createCommand);
            verify(updateTenantUseCase, times(1)).execute(updateCommand);
            verify(updateTenantStatusUseCase, times(1)).execute(statusCommand);
        }

        @Test
        @DisplayName("Facade는 추가 로직 없이 순수 위임만 수행한다")
        void shouldOnlyDelegateWithoutAdditionalLogic() {
            // Arrange
            CreateTenantCommand command = new CreateTenantCommand("test-tenant");
            TenantResponse expectedResponse = new TenantResponse(
                "tenant-id-123",
                "test-tenant",
                "ACTIVE",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            when(createTenantUseCase.execute(command)).thenReturn(expectedResponse);

            // Act
            TenantResponse actualResponse = tenantCommandFacade.createTenant(command);

            // Assert - UseCase의 결과를 그대로 반환 (변형 없음)
            assertThat(actualResponse).isSameAs(expectedResponse);

            verify(createTenantUseCase, times(1)).execute(command);
            verifyNoMoreInteractions(createTenantUseCase);
        }
    }
}

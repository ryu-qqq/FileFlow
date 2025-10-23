package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantStatusCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;
import com.ryuqq.fileflow.fixtures.TenantFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("TenantCommandService 테스트")
class TenantCommandServiceTest {

    private TenantRepositoryPort tenantRepositoryPort;
    private TenantCommandService tenantCommandService;

    @BeforeEach
    void setUp() {
        tenantRepositoryPort = mock(TenantRepositoryPort.class);
        tenantCommandService = new TenantCommandService(tenantRepositoryPort);
    }

    @Nested
    @DisplayName("CreateTenantUseCase 구현")
    class CreateTenantTests {

        @Test
        @DisplayName("Tenant를 성공적으로 생성한다")
        void shouldCreateTenantSuccessfully() {
            // Arrange
            CreateTenantCommand command = new CreateTenantCommand("My Company");
            Tenant newTenant = TenantFixtures.activeTenantWithName("My Company");

            when(tenantRepositoryPort.existsByName(any(TenantName.class)))
                .thenReturn(false);
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenReturn(newTenant);

            // Act
            TenantResponse response = tenantCommandService.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("My Company");
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.deleted()).isFalse();

            verify(tenantRepositoryPort).existsByName(any(TenantName.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            assertThatThrownBy(() -> tenantCommandService.execute((CreateTenantCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreateTenantCommand는 필수입니다");

            verify(tenantRepositoryPort, never()).existsByName(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("동일한 이름의 Tenant가 이미 존재하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenTenantNameAlreadyExists() {
            // Arrange
            CreateTenantCommand command = new CreateTenantCommand("Existing Company");

            when(tenantRepositoryPort.existsByName(any(TenantName.class)))
                .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> tenantCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("동일한 이름의 Tenant가 이미 존재합니다");

            verify(tenantRepositoryPort).existsByName(any(TenantName.class));
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("TenantRepositoryPort.save()가 호출되었는지 검증")
        void shouldVerifyRepositorySaveCall() {
            // Arrange
            CreateTenantCommand command = new CreateTenantCommand("New Company");
            Tenant savedTenant = TenantFixtures.activeTenantWithName("New Company");

            when(tenantRepositoryPort.existsByName(any(TenantName.class)))
                .thenReturn(false);
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenReturn(savedTenant);

            // Act
            tenantCommandService.execute(command);

            // Assert
            verify(tenantRepositoryPort, times(1)).save(any(Tenant.class));
        }
    }

    @Nested
    @DisplayName("UpdateTenantUseCase 구현")
    class UpdateTenantTests {

        @Test
        @DisplayName("Tenant 이름을 성공적으로 수정한다")
        void shouldUpdateTenantNameSuccessfully() {
            // Arrange
            String tenantId = "tenant-123";
            UpdateTenantCommand command = new UpdateTenantCommand(tenantId, "Updated Company");
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = tenantCommandService.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.name()).isEqualTo("Updated Company");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            assertThatThrownBy(() -> tenantCommandService.execute((UpdateTenantCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateTenantCommand는 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Tenant가 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenTenantNotFound() {
            // Arrange
            String nonExistentId = "tenant-999";
            UpdateTenantCommand command = new UpdateTenantCommand(nonExistentId, "Updated Company");

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> tenantCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Domain의 updateName() 메서드가 호출되었는지 검증")
        void shouldVerifyDomainUpdateNameCall() {
            // Arrange
            String tenantId = "tenant-123";
            UpdateTenantCommand command = new UpdateTenantCommand(tenantId, "New Name");
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = tenantCommandService.execute(command);

            // Assert
            assertThat(response.name()).isEqualTo("New Name");
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }
    }

    @Nested
    @DisplayName("UpdateTenantStatusUseCase 구현")
    class UpdateTenantStatusTests {

        @Test
        @DisplayName("ACTIVE → SUSPENDED 상태 전환에 성공한다")
        void shouldSuspendActiveTenant() {
            // Arrange
            String tenantId = "tenant-123";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "SUSPENDED");
            Tenant activeTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(activeTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = tenantCommandService.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.status()).isEqualTo("SUSPENDED");
            assertThat(response.deleted()).isFalse();

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("SUSPENDED → ACTIVE 상태 전환에 성공한다")
        void shouldActivateSuspendedTenant() {
            // Arrange
            String tenantId = "tenant-123";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "ACTIVE");
            Tenant suspendedTenant = TenantFixtures.suspendedTenant();

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(suspendedTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = tenantCommandService.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("ACTIVE");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            assertThatThrownBy(() -> tenantCommandService.execute((UpdateTenantStatusCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateTenantStatusCommand는 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Tenant가 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenTenantNotFound() {
            // Arrange
            String nonExistentId = "tenant-999";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(nonExistentId, "ACTIVE");

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> tenantCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("잘못된 상태값이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenStatusIsInvalid() {
            // Arrange
            String tenantId = "tenant-123";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "INVALID_STATUS");
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));

            // Act & Assert
            assertThatThrownBy(() -> tenantCommandService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 상태값입니다");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Domain의 activate() 메서드가 호출되었는지 검증")
        void shouldVerifyDomainActivateCall() {
            // Arrange
            String tenantId = "tenant-123";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "ACTIVE");
            Tenant suspendedTenant = TenantFixtures.suspendedTenant();

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(suspendedTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = tenantCommandService.execute(command);

            // Assert
            assertThat(response.status()).isEqualTo("ACTIVE");
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("Domain의 suspend() 메서드가 호출되었는지 검증")
        void shouldVerifyDomainSuspendCall() {
            // Arrange
            String tenantId = "tenant-123";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "SUSPENDED");
            Tenant activeTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(activeTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = tenantCommandService.execute(command);

            // Assert
            assertThat(response.status()).isEqualTo("SUSPENDED");
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }
    }
}

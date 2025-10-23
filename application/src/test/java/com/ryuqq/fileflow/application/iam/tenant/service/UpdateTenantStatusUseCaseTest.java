package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantStatusCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.in.UpdateTenantStatusUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.fixtures.TenantFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UpdateTenantStatusUseCaseTest - UpdateTenantStatusUseCase 단위 테스트
 *
 * <p>Mockito를 사용한 UseCase 계층 단위 테스트입니다.
 * Repository는 Mocking하여 UseCase 로직만 검증합니다.</p>
 *
 * <p><strong>테스트 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ @DisplayName으로 테스트 의도 명확화</li>
 *   <li>✅ @Nested로 테스트 그룹화</li>
 *   <li>✅ AAA 패턴 (Arrange-Act-Assert)</li>
 *   <li>✅ Mockito ArgumentCaptor 활용</li>
 *   <li>✅ test-fixtures 사용</li>
 * </ul>
 *
 * <p><strong>테스트 대상:</strong></p>
 * <ul>
 *   <li>구현체: {@link TenantCommandService#execute(UpdateTenantStatusCommand)}</li>
 *   <li>인터페이스: {@link UpdateTenantStatusUseCase}</li>
 * </ul>
 *
 * <p><strong>상태 전환 테스트:</strong></p>
 * <ul>
 *   <li>ACTIVE → SUSPENDED: tenant.suspend() 호출</li>
 *   <li>SUSPENDED → ACTIVE: tenant.activate() 호출</li>
 *   <li>잘못된 상태 전환: 예외 발생 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("UpdateTenantStatusUseCase 테스트")
class UpdateTenantStatusUseCaseTest {

    private TenantRepositoryPort tenantRepositoryPort;
    private UpdateTenantStatusUseCase updateTenantStatusUseCase;

    @BeforeEach
    void setUp() {
        tenantRepositoryPort = mock(TenantRepositoryPort.class);
        updateTenantStatusUseCase = new TenantCommandService(tenantRepositoryPort);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("ACTIVE 상태의 Tenant를 SUSPENDED로 변경한다")
        void shouldSuspendActiveTenant() {
            // Arrange
            String tenantId = "tenant-id-123";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "SUSPENDED");
            Tenant activeTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(activeTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = updateTenantStatusUseCase.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.status()).isEqualTo("SUSPENDED");
            assertThat(response.deleted()).isFalse();

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("SUSPENDED 상태의 Tenant를 ACTIVE로 변경한다")
        void shouldActivateSuspendedTenant() {
            // Arrange
            String tenantId = "tenant-id-456";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "ACTIVE");
            Tenant suspendedTenant = TenantFixtures.suspendedTenantWithName("Suspended Company");

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(suspendedTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = updateTenantStatusUseCase.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.deleted()).isFalse();

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("상태 문자열은 대소문자를 구분하지 않는다")
        void shouldHandleCaseInsensitiveStatus() {
            // Arrange
            String tenantId = "tenant-id-789";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "suspended");
            Tenant activeTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(activeTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = updateTenantStatusUseCase.execute(command);

            // Assert
            assertThat(response.status()).isEqualTo("SUSPENDED");
        }

        @Test
        @DisplayName("Repository에서 올바른 TenantId로 조회한다")
        void shouldFindByCorrectTenantId() {
            // Arrange
            String tenantId = "tenant-id-999";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "SUSPENDED");
            Tenant activeTenant = TenantFixtures.activeTenantWithId(tenantId);
            ArgumentCaptor<TenantId> tenantIdCaptor = ArgumentCaptor.forClass(TenantId.class);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(activeTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            updateTenantStatusUseCase.execute(command);

            // Assert
            verify(tenantRepositoryPort).findById(tenantIdCaptor.capture());
            TenantId capturedId = tenantIdCaptor.getValue();

            assertThat(capturedId.value()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("Domain 메서드 호출 후 updatedAt이 갱신된다")
        void shouldUpdateTimestampAfterStatusChange() {
            // Arrange
            String tenantId = "tenant-id-111";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "SUSPENDED");
            Tenant activeTenant = TenantFixtures.activeTenantWithId(tenantId);
            var originalUpdatedAt = activeTenant.getUpdatedAt();

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(activeTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            updateTenantStatusUseCase.execute(command);

            // Assert
            ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);
            verify(tenantRepositoryPort).save(tenantCaptor.capture());
            Tenant savedTenant = tenantCaptor.getValue();

            assertThat(savedTenant.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> updateTenantStatusUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateTenantStatusCommand는 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Tenant가 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenTenantNotFound() {
            // Arrange
            String nonExistentId = "non-existent-id";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(nonExistentId, "SUSPENDED");

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> updateTenantStatusUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다")
                .hasMessageContaining(nonExistentId);

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("잘못된 상태값이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenInvalidStatus() {
            // Arrange
            String tenantId = "tenant-id-123";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "INVALID_STATUS");
            Tenant activeTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(activeTenant));

            // Act & Assert
            assertThatThrownBy(() -> updateTenantStatusUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 상태값입니다")
                .hasMessageContaining("INVALID_STATUS");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("삭제된 Tenant의 상태 변경 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenUpdatingDeletedTenant() {
            // Arrange
            String tenantId = "deleted-tenant-id";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "ACTIVE");
            Tenant deletedTenant = TenantFixtures.deletedTenantWithName("Deleted Company");

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(deletedTenant));

            // Act & Assert
            assertThatThrownBy(() -> updateTenantStatusUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 Tenant는 활성화할 수 없습니다");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("이미 ACTIVE인 Tenant를 ACTIVE로 변경하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenAlreadyActive() {
            // Arrange
            String tenantId = "tenant-id-active";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "ACTIVE");
            Tenant activeTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(activeTenant));

            // Act & Assert
            assertThatThrownBy(() -> updateTenantStatusUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 활성 상태인 Tenant입니다");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("이미 SUSPENDED인 Tenant를 SUSPENDED로 변경하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenAlreadySuspended() {
            // Arrange
            String tenantId = "tenant-id-suspended";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "SUSPENDED");
            Tenant suspendedTenant = TenantFixtures.suspendedTenantWithName("Suspended Company");

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(suspendedTenant));

            // Act & Assert
            assertThatThrownBy(() -> updateTenantStatusUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 일시 정지된 Tenant입니다");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Command 검증")
    class CommandValidationScenarios {

        @Test
        @DisplayName("Command의 tenantId가 null이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenTenantIdIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new UpdateTenantStatusCommand(null, "SUSPENDED"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Command의 tenantId가 빈 문자열이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenTenantIdIsBlank() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new UpdateTenantStatusCommand("", "SUSPENDED"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Command의 status가 null이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenStatusIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new UpdateTenantStatusCommand("tenant-id-123", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상태는 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Command의 status가 빈 문자열이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenStatusIsBlank() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new UpdateTenantStatusCommand("tenant-id-123", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상태는 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Command의 status가 공백 문자열이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenStatusIsWhitespace() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new UpdateTenantStatusCommand("tenant-id-123", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상태는 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Domain 로직 검증")
    class DomainLogicScenarios {

        @Test
        @DisplayName("ACTIVE → SUSPENDED 변경 시 tenant.suspend() 메서드가 호출된다")
        void shouldCallSuspendMethodWhenActivatingToSuspended() {
            // Arrange
            String tenantId = "tenant-id-123";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "SUSPENDED");
            Tenant activeTenant = TenantFixtures.activeTenantWithId(tenantId);
            ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(activeTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            updateTenantStatusUseCase.execute(command);

            // Assert - save 호출 시 전달된 Tenant의 status가 변경되었는지 확인
            verify(tenantRepositoryPort).save(tenantCaptor.capture());
            Tenant savedTenant = tenantCaptor.getValue();

            assertThat(savedTenant.getStatus().name()).isEqualTo("SUSPENDED");
            assertThat(savedTenant.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("SUSPENDED → ACTIVE 변경 시 tenant.activate() 메서드가 호출된다")
        void shouldCallActivateMethodWhenSuspendedToActive() {
            // Arrange
            String tenantId = "tenant-id-456";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "ACTIVE");
            Tenant suspendedTenant = TenantFixtures.suspendedTenantWithName("Suspended Company");
            ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(suspendedTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            updateTenantStatusUseCase.execute(command);

            // Assert - save 호출 시 전달된 Tenant의 status가 변경되었는지 확인
            verify(tenantRepositoryPort).save(tenantCaptor.capture());
            Tenant savedTenant = tenantCaptor.getValue();

            assertThat(savedTenant.getStatus().name()).isEqualTo("ACTIVE");
            assertThat(savedTenant.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("상태 변경 시 deleted 플래그는 변경되지 않는다")
        void shouldNotChangeDeletedFlagWhenUpdatingStatus() {
            // Arrange
            String tenantId = "tenant-id-789";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "SUSPENDED");
            Tenant activeTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(activeTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = updateTenantStatusUseCase.execute(command);

            // Assert
            assertThat(response.deleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("Transaction 검증")
    class TransactionScenarios {

        @Test
        @DisplayName("상태 변경은 Repository를 통해 영속화된다")
        void shouldPersistStatusChangeThroughRepository() {
            // Arrange
            String tenantId = "tenant-id-123";
            UpdateTenantStatusCommand command = new UpdateTenantStatusCommand(tenantId, "SUSPENDED");
            Tenant activeTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(activeTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            updateTenantStatusUseCase.execute(command);

            // Assert - Repository의 save가 정확히 1번 호출되었는지 확인
            verify(tenantRepositoryPort, times(1)).findById(any(TenantId.class));
            verify(tenantRepositoryPort, times(1)).save(any(Tenant.class));
        }
    }
}

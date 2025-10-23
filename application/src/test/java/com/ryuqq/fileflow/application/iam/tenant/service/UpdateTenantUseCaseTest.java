package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.in.UpdateTenantUseCase;
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
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UpdateTenantUseCaseTest - UpdateTenantUseCase 단위 테스트
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
 *   <li>구현체: {@link TenantCommandService#execute(UpdateTenantCommand)}</li>
 *   <li>인터페이스: {@link UpdateTenantUseCase}</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("UpdateTenantUseCase 테스트")
class UpdateTenantUseCaseTest {

    private TenantRepositoryPort tenantRepositoryPort;
    private UpdateTenantUseCase updateTenantUseCase;

    @BeforeEach
    void setUp() {
        tenantRepositoryPort = mock(TenantRepositoryPort.class);
        updateTenantUseCase = new TenantCommandService(tenantRepositoryPort);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("Tenant 이름을 성공적으로 수정한다")
        void shouldUpdateTenantNameSuccessfully() {
            // Arrange
            String tenantId = "tenant-id-123";
            String originalName = "Original Company";
            String newName = "Updated Company";

            UpdateTenantCommand command = new UpdateTenantCommand(tenantId, newName);
            Tenant existingTenant = TenantFixtures.activeTenantWithIdAndName(tenantId, originalName);

            // Mock: Repository에서 기존 Tenant 조회
            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));

            // Mock: 수정된 Tenant 저장 (실제로는 동일 객체지만 명시)
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = updateTenantUseCase.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.name()).isEqualTo(newName);
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.deleted()).isFalse();

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("Repository에서 올바른 TenantId로 조회한다")
        void shouldFindByCorrectTenantId() {
            // Arrange
            String tenantId = "tenant-id-456";
            UpdateTenantCommand command = new UpdateTenantCommand(tenantId, "New Name");
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);
            ArgumentCaptor<TenantId> tenantIdCaptor = ArgumentCaptor.forClass(TenantId.class);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            updateTenantUseCase.execute(command);

            // Assert
            verify(tenantRepositoryPort).findById(tenantIdCaptor.capture());
            TenantId capturedId = tenantIdCaptor.getValue();

            assertThat(capturedId.value()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("Domain 객체의 updateName 메서드를 호출한다")
        void shouldCallUpdateNameOnDomainObject() {
            // Arrange
            String tenantId = "tenant-id-789";
            String originalName = "Original";
            String newName = "Updated";

            UpdateTenantCommand command = new UpdateTenantCommand(tenantId, newName);
            Tenant existingTenant = TenantFixtures.activeTenantWithIdAndName(tenantId, originalName);
            ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            updateTenantUseCase.execute(command);

            // Assert - save 호출 시 전달된 Tenant의 name이 변경되었는지 확인
            verify(tenantRepositoryPort).save(tenantCaptor.capture());
            Tenant savedTenant = tenantCaptor.getValue();

            assertThat(savedTenant.getNameValue()).isEqualTo(newName);
            assertThat(savedTenant.isActive()).isTrue();
            assertThat(savedTenant.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("수정 후 변경된 Tenant를 Repository에 저장한다")
        void shouldSaveUpdatedTenant() {
            // Arrange
            String tenantId = "tenant-id-999";
            UpdateTenantCommand command = new UpdateTenantCommand(tenantId, "Modified Name");
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            updateTenantUseCase.execute(command);

            // Assert
            verify(tenantRepositoryPort, times(1)).save(any(Tenant.class));
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> updateTenantUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateTenantCommand는 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Tenant가 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenTenantNotFound() {
            // Arrange
            String nonExistentId = "non-existent-id";
            UpdateTenantCommand command = new UpdateTenantCommand(nonExistentId, "New Name");

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> updateTenantUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다")
                .hasMessageContaining(nonExistentId);

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("삭제된 Tenant의 이름 수정 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenUpdatingDeletedTenant() {
            // Arrange
            String tenantId = "deleted-tenant-id";
            UpdateTenantCommand command = new UpdateTenantCommand(tenantId, "New Name");
            Tenant deletedTenant = TenantFixtures.deletedTenantWithName("Deleted Company");

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(deletedTenant));

            // Act & Assert
            assertThatThrownBy(() -> updateTenantUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 Tenant의 이름은 변경할 수 없습니다");

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
            assertThatThrownBy(() -> new UpdateTenantCommand(null, "New Name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Command의 tenantId가 빈 문자열이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenTenantIdIsBlank() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new UpdateTenantCommand("", "New Name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Command의 name이 null이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenNameIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new UpdateTenantCommand("tenant-id-123", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant 이름은 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Command의 name이 빈 문자열이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenNameIsBlank() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new UpdateTenantCommand("tenant-id-123", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant 이름은 필수입니다");

            verify(tenantRepositoryPort, never()).findById(any());
            verify(tenantRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Domain 로직 검증")
    class DomainLogicScenarios {

        @Test
        @DisplayName("Tenant 이름 변경 시 updatedAt이 갱신된다")
        void shouldUpdateTimestampWhenNameChanged() {
            // Arrange
            String tenantId = "tenant-id-123";
            UpdateTenantCommand command = new UpdateTenantCommand(tenantId, "New Name");
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);
            var originalUpdatedAt = existingTenant.getUpdatedAt();

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            updateTenantUseCase.execute(command);

            // Assert
            ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);
            verify(tenantRepositoryPort).save(tenantCaptor.capture());
            Tenant savedTenant = tenantCaptor.getValue();

            assertThat(savedTenant.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("이름 변경 시 Tenant 상태는 유지된다")
        void shouldPreserveTenantStatusWhenUpdatingName() {
            // Arrange
            String tenantId = "tenant-id-456";
            UpdateTenantCommand command = new UpdateTenantCommand(tenantId, "New Name");
            Tenant suspendedTenant = TenantFixtures.suspendedTenantWithName("Suspended Company");

            when(tenantRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(suspendedTenant));
            when(tenantRepositoryPort.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TenantResponse response = updateTenantUseCase.execute(command);

            // Assert
            assertThat(response.status()).isEqualTo("SUSPENDED");
            assertThat(response.deleted()).isFalse();
        }
    }
}

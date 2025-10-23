package com.ryuqq.fileflow.application.iam.organization.service;

import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.in.UpdateOrganizationStatusUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.fixtures.OrganizationFixtures;
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
@DisplayName("UpdateOrganizationStatusUseCase 테스트")
class UpdateOrganizationStatusUseCaseTest {

    private OrganizationRepositoryPort organizationRepositoryPort;
    private UpdateOrganizationStatusUseCase updateOrganizationStatusUseCase;

    @BeforeEach
    void setUp() {
        organizationRepositoryPort = mock(OrganizationRepositoryPort.class);
        updateOrganizationStatusUseCase = new OrganizationCommandService(organizationRepositoryPort);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("ACTIVE → INACTIVE 상태 전환에 성공한다")
        void shouldInactivateActiveOrganization() {
            // Arrange
            Long organizationId = 1L;
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(organizationId, "INACTIVE");
            Organization activeOrganization = OrganizationFixtures.salesOrganizationWithId(organizationId, "tenant-123");

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(activeOrganization));
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OrganizationResponse response = updateOrganizationStatusUseCase.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isEqualTo(organizationId);
            assertThat(response.status()).isEqualTo("INACTIVE");
            assertThat(response.deleted()).isFalse();

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            assertThatThrownBy(() -> updateOrganizationStatusUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateOrganizationStatusCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("Organization이 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenOrganizationNotFound() {
            Long nonExistentId = 999L;
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(nonExistentId, "INACTIVE");

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> updateOrganizationStatusUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("INACTIVE → ACTIVE 복원 시도 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenActivatingInactiveOrganization() {
            // Arrange
            Long organizationId = 1L;
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(organizationId, "ACTIVE");
            Organization inactiveOrganization = OrganizationFixtures.inactiveOrganization(organizationId, "tenant-123");

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(inactiveOrganization));

            // Act & Assert
            assertThatThrownBy(() -> updateOrganizationStatusUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization은 INACTIVE에서 ACTIVE로 복원할 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("삭제된 Organization의 상태 변경 시 예외 발생")
        void shouldThrowExceptionWhenUpdatingDeletedOrganizationStatus() {
            // Arrange
            Long organizationId = 1L;
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(organizationId, "INACTIVE");
            Organization deletedOrganization = OrganizationFixtures.deletedOrganization("tenant-123");

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(deletedOrganization));

            // Act & Assert
            assertThatThrownBy(() -> updateOrganizationStatusUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class);

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Command 검증")
    class CommandValidationScenarios {

        @Test
        @DisplayName("organizationId가 null이면 예외 발생")
        void shouldThrowExceptionWhenOrganizationIdIsNull() {
            assertThatThrownBy(() -> new UpdateOrganizationStatusCommand(null, "ACTIVE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organization ID는 필수이며 양수여야 합니다");
        }

        @Test
        @DisplayName("status가 null이면 예외 발생")
        void shouldThrowExceptionWhenStatusIsNull() {
            assertThatThrownBy(() -> new UpdateOrganizationStatusCommand(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상태는 필수입니다");
        }

        @Test
        @DisplayName("status가 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenStatusIsBlank() {
            assertThatThrownBy(() -> new UpdateOrganizationStatusCommand(1L, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상태는 필수입니다");
        }
    }
}

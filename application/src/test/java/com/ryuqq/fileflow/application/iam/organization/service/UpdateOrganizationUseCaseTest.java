package com.ryuqq.fileflow.application.iam.organization.service;

import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.in.UpdateOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.fixtures.OrganizationFixtures;
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

@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("UpdateOrganizationUseCase 테스트")
class UpdateOrganizationUseCaseTest {

    private OrganizationRepositoryPort organizationRepositoryPort;
    private UpdateOrganizationUseCase updateOrganizationUseCase;

    @BeforeEach
    void setUp() {
        organizationRepositoryPort = mock(OrganizationRepositoryPort.class);
        updateOrganizationUseCase = new OrganizationCommandService(organizationRepositoryPort);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("Organization 이름을 성공적으로 수정한다")
        void shouldUpdateOrganizationNameSuccessfully() {
            // Arrange
            Long organizationId = 1L;
            String newName = "Updated Sales Department";
            UpdateOrganizationCommand command = new UpdateOrganizationCommand(organizationId, newName);
            Organization existingOrganization = OrganizationFixtures.salesOrganizationWithId(organizationId, "tenant-123");

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(existingOrganization));
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OrganizationResponse response = updateOrganizationUseCase.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isEqualTo(organizationId);
            assertThat(response.name()).isEqualTo(newName);
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
            assertThatThrownBy(() -> updateOrganizationUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateOrganizationCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("Organization이 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenOrganizationNotFound() {
            Long nonExistentId = 999L;
            UpdateOrganizationCommand command = new UpdateOrganizationCommand(nonExistentId, "New Name");

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> updateOrganizationUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("삭제된 Organization 수정 시 IllegalStateException 발생")
        void shouldThrowExceptionWhenUpdatingDeletedOrganization() {
            Long organizationId = 1L;
            UpdateOrganizationCommand command = new UpdateOrganizationCommand(organizationId, "New Name");
            Organization deletedOrganization = OrganizationFixtures.deletedOrganization("tenant-123");

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(deletedOrganization));

            assertThatThrownBy(() -> updateOrganizationUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("삭제된 Organization은 수정할 수 없습니다");

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
            assertThatThrownBy(() -> new UpdateOrganizationCommand(null, "New Name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organization ID는 필수이며 양수여야 합니다");
        }

        @Test
        @DisplayName("organizationId가 0이면 예외 발생")
        void shouldThrowExceptionWhenOrganizationIdIsZero() {
            assertThatThrownBy(() -> new UpdateOrganizationCommand(0L, "New Name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organization ID는 필수이며 양수여야 합니다");
        }

        @Test
        @DisplayName("name이 null이면 예외 발생")
        void shouldThrowExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> new UpdateOrganizationCommand(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("조직 이름은 필수입니다");
        }

        @Test
        @DisplayName("name이 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenNameIsBlank() {
            assertThatThrownBy(() -> new UpdateOrganizationCommand(1L, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("조직 이름은 필수입니다");
        }
    }
}

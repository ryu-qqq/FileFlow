package com.ryuqq.fileflow.application.iam.organization.service;

import com.ryuqq.fileflow.application.iam.organization.dto.command.SoftDeleteOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.port.in.DeleteOrganizationUseCase;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("DeleteOrganizationUseCase 테스트")
class DeleteOrganizationUseCaseTest {

    private OrganizationRepositoryPort organizationRepositoryPort;
    private DeleteOrganizationUseCase deleteOrganizationUseCase;

    @BeforeEach
    void setUp() {
        organizationRepositoryPort = mock(OrganizationRepositoryPort.class);
        deleteOrganizationUseCase = new OrganizationCommandService(organizationRepositoryPort);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("Organization을 성공적으로 Soft Delete한다")
        void shouldSoftDeleteOrganizationSuccessfully() {
            // Arrange
            Long organizationId = 1L;
            SoftDeleteOrganizationCommand command = new SoftDeleteOrganizationCommand(organizationId);
            Organization existingOrganization = OrganizationFixtures.salesOrganizationWithId(organizationId, "tenant-123");

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(existingOrganization));
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            deleteOrganizationUseCase.execute(command);

            // Assert
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
            assertThatThrownBy(() -> deleteOrganizationUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SoftDeleteOrganizationCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("Organization이 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenOrganizationNotFound() {
            Long nonExistentId = 999L;
            SoftDeleteOrganizationCommand command = new SoftDeleteOrganizationCommand(nonExistentId);

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> deleteOrganizationUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("이미 삭제된 Organization을 삭제하려고 하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenDeletingAlreadyDeletedOrganization() {
            Long organizationId = 1L;
            SoftDeleteOrganizationCommand command = new SoftDeleteOrganizationCommand(organizationId);
            Organization deletedOrganization = OrganizationFixtures.deletedOrganization("tenant-123");

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(deletedOrganization));

            assertThatThrownBy(() -> deleteOrganizationUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 삭제된 Organization입니다");

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
            assertThatThrownBy(() -> new SoftDeleteOrganizationCommand(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organization ID는 필수이며 양수여야 합니다");
        }

        @Test
        @DisplayName("organizationId가 0이면 예외 발생")
        void shouldThrowExceptionWhenOrganizationIdIsZero() {
            assertThatThrownBy(() -> new SoftDeleteOrganizationCommand(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organization ID는 필수이며 양수여야 합니다");
        }
    }
}

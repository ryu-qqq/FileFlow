package com.ryuqq.fileflow.application.iam.organization.service;

import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.SoftDeleteOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.OrgCode;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("OrganizationCommandService 테스트")
class OrganizationCommandServiceTest {

    private OrganizationRepositoryPort organizationRepositoryPort;
    private OrganizationCommandService organizationCommandService;

    @BeforeEach
    void setUp() {
        organizationRepositoryPort = mock(OrganizationRepositoryPort.class);
        organizationCommandService = new OrganizationCommandService(organizationRepositoryPort);
    }

    @Nested
    @DisplayName("CreateOrganizationUseCase 구현")
    class CreateOrganizationTests {

        @Test
        @DisplayName("Organization을 성공적으로 생성한다")
        void shouldCreateOrganizationSuccessfully() {
            // Arrange
            String tenantId = "tenant-123";
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                tenantId,
                "ORG-001",
                "Engineering Department"
            );
            // save 후 ID가 생성된 Organization 반환
            Organization newOrganization = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);

            when(organizationRepositoryPort.existsByTenantIdAndOrgCode(any(String.class), any(OrgCode.class)))
                .thenReturn(false);
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenReturn(newOrganization);

            // Act
            OrganizationResponse response = organizationCommandService.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isNotNull();
            assertThat(response.name()).isEqualTo("Sales Department");
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.deleted()).isFalse();

            verify(organizationRepositoryPort).existsByTenantIdAndOrgCode(any(String.class), any(OrgCode.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            assertThatThrownBy(() -> organizationCommandService.execute((CreateOrganizationCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreateOrganizationCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).existsByTenantIdAndOrgCode(any(), any());
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("동일한 Tenant 내 동일한 조직 코드가 이미 존재하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenOrgCodeAlreadyExists() {
            // Arrange
            String tenantId = "tenant-123";
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                tenantId,
                "ORG-001",
                "Engineering Department"
            );

            when(organizationRepositoryPort.existsByTenantIdAndOrgCode(any(String.class), any(OrgCode.class)))
                .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("동일한 Tenant 내에 동일한 조직 코드가 이미 존재합니다");

            verify(organizationRepositoryPort).existsByTenantIdAndOrgCode(any(String.class), any(OrgCode.class));
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("OrganizationRepositoryPort.save()가 호출되었는지 검증")
        void shouldVerifyRepositorySaveCall() {
            // Arrange
            String tenantId = "tenant-123";
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                tenantId,
                "ORG-002",
                "Sales Department"
            );
            // save 후 ID가 생성된 Organization 반환
            Organization savedOrganization = OrganizationFixtures.salesOrganizationWithId(2L, tenantId);

            when(organizationRepositoryPort.existsByTenantIdAndOrgCode(any(String.class), any(OrgCode.class)))
                .thenReturn(false);
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenReturn(savedOrganization);

            // Act
            organizationCommandService.execute(command);

            // Assert
            verify(organizationRepositoryPort, times(1)).save(any(Organization.class));
        }
    }

    @Nested
    @DisplayName("UpdateOrganizationUseCase 구현")
    class UpdateOrganizationTests {

        @Test
        @DisplayName("Organization 이름을 성공적으로 수정한다")
        void shouldUpdateOrganizationNameSuccessfully() {
            // Arrange
            Long organizationId = 123L;
            UpdateOrganizationCommand command = new UpdateOrganizationCommand(
                organizationId,
                "Updated Department"
            );
            Organization existingOrganization = OrganizationFixtures.activeOrganizationWithId(organizationId);

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(existingOrganization));
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OrganizationResponse response = organizationCommandService.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isEqualTo(organizationId);
            assertThat(response.name()).isEqualTo("Updated Department");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            assertThatThrownBy(() -> organizationCommandService.execute((UpdateOrganizationCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateOrganizationCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).findById(any());
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Organization이 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenOrganizationNotFound() {
            // Arrange
            Long nonExistentId = 999L;
            UpdateOrganizationCommand command = new UpdateOrganizationCommand(
                nonExistentId,
                "Updated Department"
            );

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Domain의 updateName() 메서드가 호출되었는지 검증")
        void shouldVerifyDomainUpdateNameCall() {
            // Arrange
            Long organizationId = 123L;
            UpdateOrganizationCommand command = new UpdateOrganizationCommand(
                organizationId,
                "New Department Name"
            );
            Organization existingOrganization = OrganizationFixtures.activeOrganizationWithId(organizationId);

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(existingOrganization));
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OrganizationResponse response = organizationCommandService.execute(command);

            // Assert
            assertThat(response.name()).isEqualTo("New Department Name");
            verify(organizationRepositoryPort).save(any(Organization.class));
        }
    }

    @Nested
    @DisplayName("DeleteOrganizationUseCase 구현")
    class SoftDeleteOrganizationTests {

        @Test
        @DisplayName("Organization을 성공적으로 소프트 삭제한다")
        void shouldSoftDeleteOrganizationSuccessfully() {
            // Arrange
            Long organizationId = 123L;
            SoftDeleteOrganizationCommand command = new SoftDeleteOrganizationCommand(organizationId);
            Organization existingOrganization = OrganizationFixtures.activeOrganizationWithId(organizationId);

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(existingOrganization));
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            organizationCommandService.execute(command);

            // Assert
            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            assertThatThrownBy(() -> organizationCommandService.execute((SoftDeleteOrganizationCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SoftDeleteOrganizationCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).findById(any());
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Organization이 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenOrganizationNotFound() {
            // Arrange
            Long nonExistentId = 999L;
            SoftDeleteOrganizationCommand command = new SoftDeleteOrganizationCommand(nonExistentId);

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Domain의 softDelete() 메서드가 호출되었는지 검증")
        void shouldVerifyDomainSoftDeleteCall() {
            // Arrange
            Long organizationId = 123L;
            SoftDeleteOrganizationCommand command = new SoftDeleteOrganizationCommand(organizationId);
            Organization existingOrganization = OrganizationFixtures.activeOrganizationWithId(organizationId);

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(existingOrganization));
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenAnswer(invocation -> {
                    Organization org = invocation.getArgument(0);
                    // softDelete() 호출 후 deleted=true, status=INACTIVE인지 검증
                    assertThat(org.isDeleted()).isTrue();
                    assertThat(org.getStatus().name()).isEqualTo("INACTIVE");
                    return org;
                });

            // Act
            organizationCommandService.execute(command);

            // Assert
            verify(organizationRepositoryPort).save(any(Organization.class));
        }
    }

    @Nested
    @DisplayName("UpdateOrganizationStatusUseCase 구현")
    class UpdateOrganizationStatusTests {

        @Test
        @DisplayName("ACTIVE → INACTIVE 상태 전환에 성공한다 (단방향)")
        void shouldDeactivateActiveOrganization() {
            // Arrange
            Long organizationId = 123L;
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(
                organizationId,
                "INACTIVE"
            );
            Organization activeOrganization = OrganizationFixtures.activeOrganizationWithId(organizationId);

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(activeOrganization));
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OrganizationResponse response = organizationCommandService.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isEqualTo(organizationId);
            assertThat(response.status()).isEqualTo("INACTIVE");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("INACTIVE → ACTIVE 상태 전환 시도 시 예외 발생 (복원 불가)")
        void shouldThrowExceptionWhenTryingToActivateInactiveOrganization() {
            // Arrange
            Long organizationId = 123L;
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(
                organizationId,
                "ACTIVE"
            );
            Organization inactiveOrganization = OrganizationFixtures.inactiveOrganization();

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(inactiveOrganization));

            // Act & Assert
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization은 INACTIVE에서 ACTIVE로 복원할 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            assertThatThrownBy(() -> organizationCommandService.execute((UpdateOrganizationStatusCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateOrganizationStatusCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).findById(any());
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Organization이 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenOrganizationNotFound() {
            // Arrange
            Long nonExistentId = 999L;
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(
                nonExistentId,
                "INACTIVE"
            );

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("잘못된 상태값이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenStatusIsInvalid() {
            // Arrange
            Long organizationId = 123L;
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(
                organizationId,
                "INVALID_STATUS"
            );
            Organization existingOrganization = OrganizationFixtures.activeOrganizationWithId(organizationId);

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(existingOrganization));

            // Act & Assert
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 상태값입니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("상태값이 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenStatusIsNull() {
            // Arrange & Act & Assert
            // Command Compact Constructor에서 검증되므로 Command 생성 시점에 예외 발생
            assertThatThrownBy(() -> new UpdateOrganizationStatusCommand(123L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상태는 필수입니다");

            verify(organizationRepositoryPort, never()).findById(any());
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Domain의 deactivate() 메서드가 호출되었는지 검증")
        void shouldVerifyDomainDeactivateCall() {
            // Arrange
            Long organizationId = 123L;
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(
                organizationId,
                "INACTIVE"
            );
            Organization activeOrganization = OrganizationFixtures.activeOrganizationWithId(organizationId);

            when(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(activeOrganization));
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OrganizationResponse response = organizationCommandService.execute(command);

            // Assert
            assertThat(response.status()).isEqualTo("INACTIVE");
            verify(organizationRepositoryPort).save(any(Organization.class));
        }
    }
}

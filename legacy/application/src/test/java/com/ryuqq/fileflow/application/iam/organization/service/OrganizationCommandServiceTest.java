package com.ryuqq.fileflow.application.iam.organization.service;

import com.ryuqq.fileflow.application.iam.organization.fixture.OrganizationCommandFixture;
import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.SoftDeleteOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.OrgCode;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.fixture.OrganizationFixture;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

/**
 * OrganizationCommandService 단위 테스트
 *
 * <p>테스트 범위:</p>
 * <ul>
 *   <li>CreateOrganizationUseCase: 조직 생성 (중복 검증, Domain 생성, 영속화)</li>
 *   <li>UpdateOrganizationUseCase: 조직 수정 (조회, Domain 로직, 영속화)</li>
 *   <li>DeleteOrganizationUseCase: 조직 소프트 삭제 (softDelete)</li>
 *   <li>UpdateOrganizationStatusUseCase: 조직 상태 변경 (ACTIVE ↔ INACTIVE)</li>
 *   <li>Transaction Boundary: @Transactional 검증</li>
 *   <li>Port Interaction: Repository Port 호출 검증</li>
 *   <li>Exception Handling: 예외 처리</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationCommandService 단위 테스트")
class OrganizationCommandServiceTest {

    @Mock
    private OrganizationRepositoryPort organizationRepositoryPort;

    @InjectMocks
    private OrganizationCommandService organizationCommandService;

    @Nested
    @DisplayName("CreateOrganizationUseCase - 조직 생성")
    class CreateOrganizationUseCaseTests {

        @Test
        @DisplayName("유효한 Command로 조직 생성 성공")
        void execute_Success() {
            // Given
            CreateOrganizationCommand command = OrganizationCommandFixture.createCommand();
            Organization expectedOrganization = OrganizationFixture.createWithId(1L);

            given(organizationRepositoryPort.existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class)))
                .willReturn(false);
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(expectedOrganization);

            // When
            OrganizationResponse response = organizationCommandService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isEqualTo(expectedOrganization.getIdValue());
            assertThat(response.name()).isEqualTo(expectedOrganization.getName());

            verify(organizationRepositoryPort).existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("Engineering Department 생성 성공")
        void execute_Success_EngineeringDept() {
            // Given
            CreateOrganizationCommand command = OrganizationCommandFixture.createEngineeringDept();
            Organization expectedOrganization = OrganizationFixture.createWithId(1L, 1L, "ENG001", "Engineering Department");

            given(organizationRepositoryPort.existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class)))
                .willReturn(false);
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(expectedOrganization);

            // When
            OrganizationResponse response = organizationCommandService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.orgCode()).isEqualTo("ENG001");
            assertThat(response.name()).isEqualTo("Engineering Department");

            verify(organizationRepositoryPort).existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("Sales Department 생성 성공")
        void execute_Success_SalesDept() {
            // Given
            CreateOrganizationCommand command = OrganizationCommandFixture.createSalesDept();
            Organization expectedOrganization = OrganizationFixture.createWithId(1L, 1L, "SALES001", "Sales Department");

            given(organizationRepositoryPort.existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class)))
                .willReturn(false);
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(expectedOrganization);

            // When
            OrganizationResponse response = organizationCommandService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.orgCode()).isEqualTo("SALES001");
            assertThat(response.name()).isEqualTo("Sales Department");

            verify(organizationRepositoryPort).existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("중복된 조직 코드로 생성 시도하면 예외 발생")
        void execute_Fail_DuplicateOrgCode() {
            // Given
            CreateOrganizationCommand command = OrganizationCommandFixture.createCommand();

            given(organizationRepositoryPort.existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class)))
                .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("동일한 Tenant 내에 동일한 조직 코드가 이미 존재합니다");

            verify(organizationRepositoryPort).existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class));
            verify(organizationRepositoryPort, never()).save(any(Organization.class));
        }

        @Test
        @DisplayName("Command가 null이면 예외 발생")
        void execute_Fail_CommandIsNull() {
            // When & Then
            assertThatThrownBy(() -> organizationCommandService.execute((CreateOrganizationCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreateOrganizationCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class));
            verify(organizationRepositoryPort, never()).save(any(Organization.class));
        }

        @Test
        @DisplayName("Port 호출 순서 검증")
        void execute_PortCallOrder() {
            // Given
            CreateOrganizationCommand command = OrganizationCommandFixture.createCommand();
            Organization expectedOrganization = OrganizationFixture.createWithId(1L);

            given(organizationRepositoryPort.existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class)))
                .willReturn(false);
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(expectedOrganization);

            // When
            organizationCommandService.execute(command);

            // Then - 호출 순서: 중복 검증 → 저장
            var inOrder = inOrder(organizationRepositoryPort);
            inOrder.verify(organizationRepositoryPort).existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class));
            inOrder.verify(organizationRepositoryPort).save(any(Organization.class));
        }
    }

    @Nested
    @DisplayName("UpdateOrganizationUseCase - 조직 수정")
    class UpdateOrganizationUseCaseTests {

        @Test
        @DisplayName("유효한 Command로 조직 수정 성공")
        void execute_Success() {
            // Given
            UpdateOrganizationCommand command = OrganizationCommandFixture.updateCommand();
            Organization existingOrganization = OrganizationFixture.createWithId(1L);

            given(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.of(existingOrganization));
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(existingOrganization);

            // When
            OrganizationResponse response = organizationCommandService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isEqualTo(existingOrganization.getIdValue());

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("조직 이름 변경 성공")
        void execute_Success_UpdateName() {
            // Given
            Long organizationId = 1L;
            String newName = "Updated Organization Name";
            UpdateOrganizationCommand command = OrganizationCommandFixture.updateCommand(organizationId, newName);

            Organization existingOrganization = OrganizationFixture.createWithId(organizationId);

            given(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.of(existingOrganization));
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(existingOrganization);

            // When
            organizationCommandService.execute(command);

            // Then
            assertThat(existingOrganization.getName()).isEqualTo(newName);

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("존재하지 않는 조직 수정 시도하면 예외 발생")
        void execute_Fail_OrganizationNotFound() {
            // Given
            UpdateOrganizationCommand command = OrganizationCommandFixture.updateCommand();

            given(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any(Organization.class));
        }

        @Test
        @DisplayName("Command가 null이면 예외 발생")
        void execute_Fail_CommandIsNull() {
            // When & Then
            assertThatThrownBy(() -> organizationCommandService.execute((UpdateOrganizationCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateOrganizationCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any(Organization.class));
        }

        @Test
        @DisplayName("Port 호출 순서 검증")
        void execute_PortCallOrder() {
            // Given
            UpdateOrganizationCommand command = OrganizationCommandFixture.updateCommand();
            Organization existingOrganization = OrganizationFixture.createWithId(1L);

            given(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.of(existingOrganization));
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(existingOrganization);

            // When
            organizationCommandService.execute(command);

            // Then - 호출 순서: 조회 → 저장
            var inOrder = inOrder(organizationRepositoryPort);
            inOrder.verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            inOrder.verify(organizationRepositoryPort).save(any(Organization.class));
        }
    }

    @Nested
    @DisplayName("DeleteOrganizationUseCase - 조직 소프트 삭제")
    class DeleteOrganizationUseCaseTests {

        @Test
        @DisplayName("유효한 Command로 조직 소프트 삭제 성공")
        void execute_Success() {
            // Given
            SoftDeleteOrganizationCommand command = OrganizationCommandFixture.softDeleteCommand();
            Organization existingOrganization = OrganizationFixture.createWithId(1L);

            given(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.of(existingOrganization));
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(existingOrganization);

            // When
            organizationCommandService.execute(command);

            // Then
            assertThat(existingOrganization.isDeleted()).isTrue();

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("존재하지 않는 조직 삭제 시도하면 예외 발생")
        void execute_Fail_OrganizationNotFound() {
            // Given
            SoftDeleteOrganizationCommand command = OrganizationCommandFixture.softDeleteCommand();

            given(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any(Organization.class));
        }

        @Test
        @DisplayName("Command가 null이면 예외 발생")
        void execute_Fail_CommandIsNull() {
            // When & Then
            assertThatThrownBy(() -> organizationCommandService.execute((SoftDeleteOrganizationCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SoftDeleteOrganizationCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any(Organization.class));
        }

        @Test
        @DisplayName("Port 호출 순서 검증")
        void execute_PortCallOrder() {
            // Given
            SoftDeleteOrganizationCommand command = OrganizationCommandFixture.softDeleteCommand();
            Organization existingOrganization = OrganizationFixture.createWithId(1L);

            given(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.of(existingOrganization));
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(existingOrganization);

            // When
            organizationCommandService.execute(command);

            // Then - 호출 순서: 조회 → 저장
            var inOrder = inOrder(organizationRepositoryPort);
            inOrder.verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            inOrder.verify(organizationRepositoryPort).save(any(Organization.class));
        }
    }

    @Nested
    @DisplayName("UpdateOrganizationStatusUseCase - 조직 상태 변경")
    class UpdateOrganizationStatusUseCaseTests {

        @Test
        @DisplayName("ACTIVE → INACTIVE 상태 변경 성공 (deactivate)")
        void execute_Success_ActivateToInactive() {
            // Given
            UpdateOrganizationStatusCommand command = OrganizationCommandFixture.inactivateCommand(1L);
            Organization existingOrganization = OrganizationFixture.createWithId(1L); // ACTIVE 상태

            given(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.of(existingOrganization));
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(existingOrganization);

            // When
            OrganizationResponse response = organizationCommandService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("INACTIVE");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("INACTIVE → ACTIVE 상태 복원 시도하면 예외 발생 (단방향 전환)")
        void execute_Fail_InactiveToActive() {
            // Given
            UpdateOrganizationStatusCommand command = OrganizationCommandFixture.activateCommand(1L);
            Organization existingOrganization = OrganizationFixture.createInactive(1L); // INACTIVE 상태

            given(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.of(existingOrganization));

            // When & Then
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization은 INACTIVE에서 ACTIVE로 복원할 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any(Organization.class));
        }

        @Test
        @DisplayName("유효하지 않은 상태값으로 변경 시도하면 예외 발생")
        void execute_Fail_InvalidStatus() {
            // Given
            Long organizationId = 1L;
            String invalidStatus = "INVALID_STATUS";
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(organizationId, invalidStatus);

            Organization existingOrganization = OrganizationFixture.createWithId(organizationId);

            given(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.of(existingOrganization));

            // When & Then
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 상태값입니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any(Organization.class));
        }

        @Test
        @DisplayName("존재하지 않는 조직 상태 변경 시도하면 예외 발생")
        void execute_Fail_OrganizationNotFound() {
            // Given
            UpdateOrganizationStatusCommand command = OrganizationCommandFixture.inactivateCommand(1L);

            given(organizationRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> organizationCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationRepositoryPort).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any(Organization.class));
        }

        @Test
        @DisplayName("Command가 null이면 예외 발생")
        void execute_Fail_CommandIsNull() {
            // When & Then
            assertThatThrownBy(() -> organizationCommandService.execute((UpdateOrganizationStatusCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateOrganizationStatusCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).findById(any(OrganizationId.class));
            verify(organizationRepositoryPort, never()).save(any(Organization.class));
        }
    }

    @Nested
    @DisplayName("Transaction Boundary 검증")
    class TransactionBoundaryTests {

        @Test
        @DisplayName("모든 UseCase 메서드는 @Transactional이 적용되어 있음")
        void allUseCaseMethodsAreTransactional() throws NoSuchMethodException {
            // CreateOrganizationUseCase
            var createMethod = OrganizationCommandService.class.getDeclaredMethod("execute", CreateOrganizationCommand.class);
            assertThat(createMethod.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class)).isTrue();

            // UpdateOrganizationUseCase
            var updateMethod = OrganizationCommandService.class.getDeclaredMethod("execute", UpdateOrganizationCommand.class);
            assertThat(updateMethod.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class)).isTrue();

            // DeleteOrganizationUseCase
            var deleteMethod = OrganizationCommandService.class.getDeclaredMethod("execute", SoftDeleteOrganizationCommand.class);
            assertThat(deleteMethod.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class)).isTrue();

            // UpdateOrganizationStatusUseCase
            var updateStatusMethod = OrganizationCommandService.class.getDeclaredMethod("execute", UpdateOrganizationStatusCommand.class);
            assertThat(updateStatusMethod.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Port Interaction 검증")
    class PortInteractionTests {

        @Test
        @DisplayName("Repository Port만 호출되고 외부 API 호출은 없음")
        void onlyRepositoryPortIsCalled() {
            // Given
            CreateOrganizationCommand command = OrganizationCommandFixture.createCommand();
            Organization expectedOrganization = OrganizationFixture.createWithId(1L);

            given(organizationRepositoryPort.existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class)))
                .willReturn(false);
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(expectedOrganization);

            // When
            organizationCommandService.execute(command);

            // Then - Repository Port만 호출됨
            verify(organizationRepositoryPort).existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
            verifyNoMoreInteractions(organizationRepositoryPort);
        }

        @Test
        @DisplayName("Transaction 내에서 외부 API 호출 없음 (Transaction 경계 준수)")
        void noExternalApiCallsWithinTransaction() {
            // Given
            CreateOrganizationCommand command = OrganizationCommandFixture.createCommand();
            Organization expectedOrganization = OrganizationFixture.createWithId(1L);

            given(organizationRepositoryPort.existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class)))
                .willReturn(false);
            given(organizationRepositoryPort.save(any(Organization.class)))
                .willReturn(expectedOrganization);

            // When
            organizationCommandService.execute(command);

            // Then - Repository Port만 호출됨 (외부 API 없음)
            verify(organizationRepositoryPort, times(1)).existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class));
            verify(organizationRepositoryPort, times(1)).save(any(Organization.class));
            verifyNoMoreInteractions(organizationRepositoryPort);
        }
    }
}

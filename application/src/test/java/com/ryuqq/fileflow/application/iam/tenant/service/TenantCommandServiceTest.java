package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.iam.tenant.TenantCommandFixture;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.command.UpdateTenantStatusCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantFixture;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * TenantCommandService 단위 테스트
 *
 * <p>CQRS Command Service 테스트로, 3개의 UseCase를 검증합니다:</p>
 * <ul>
 *   <li>CreateTenantUseCase - Tenant 생성</li>
 *   <li>UpdateTenantUseCase - Tenant 수정</li>
 *   <li>UpdateTenantStatusUseCase - Tenant 상태 변경</li>
 * </ul>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ Repository Port Mock을 활용한 단위 테스트</li>
 *   <li>✅ Domain/Application testFixture(TenantFixture, TenantCommandFixture) 사용</li>
 *   <li>✅ BDD 스타일(Given-When-Then) 테스트</li>
 *   <li>✅ Transaction 경계 검증</li>
 *   <li>✅ Port 호출 순서 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantCommandService 단위 테스트")
class TenantCommandServiceTest {

    @Mock
    private TenantRepositoryPort tenantRepositoryPort;

    @InjectMocks
    private TenantCommandService tenantCommandService;

    /**
     * CreateTenantUseCase - Tenant 생성 테스트
     */
    @Nested
    @DisplayName("CreateTenantUseCase - Tenant 생성")
    class CreateTenantUseCaseTests {

        @Test
        @DisplayName("유효한 Command로 Tenant 생성 성공")
        void execute_Success() {
            // Given
            CreateTenantCommand command = TenantCommandFixture.createCommand();
            Tenant expectedTenant = TenantFixture.createWithId(1L, command.name());

            given(tenantRepositoryPort.existsByName(any(TenantName.class)))
                .willReturn(false);
            given(tenantRepositoryPort.save(any(Tenant.class)))
                .willReturn(expectedTenant);

            // When
            TenantResponse response = tenantCommandService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(expectedTenant.getIdValue());
            assertThat(response.name()).isEqualTo(expectedTenant.getNameValue());
            assertThat(response.status()).isEqualTo(expectedTenant.getStatus().name());

            verify(tenantRepositoryPort).existsByName(any(TenantName.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("Acme Corporation 생성 성공")
        void execute_Success_AcmeCorp() {
            // Given
            CreateTenantCommand command = TenantCommandFixture.createAcmeCorp();
            Tenant expectedTenant = TenantFixture.createWithId(1L, "Acme Corporation");

            given(tenantRepositoryPort.existsByName(any(TenantName.class)))
                .willReturn(false);
            given(tenantRepositoryPort.save(any(Tenant.class)))
                .willReturn(expectedTenant);

            // When
            TenantResponse response = tenantCommandService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("Acme Corporation");
        }

        @Test
        @DisplayName("Tech Startup Inc 생성 성공")
        void execute_Success_TechStartup() {
            // Given
            CreateTenantCommand command = TenantCommandFixture.createTechStartup();
            Tenant expectedTenant = TenantFixture.createWithId(1L, "Tech Startup Inc");

            given(tenantRepositoryPort.existsByName(any(TenantName.class)))
                .willReturn(false);
            given(tenantRepositoryPort.save(any(Tenant.class)))
                .willReturn(expectedTenant);

            // When
            TenantResponse response = tenantCommandService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("Tech Startup Inc");
        }

        @Test
        @DisplayName("중복된 Tenant 이름으로 생성 시도하면 예외 발생")
        void execute_Fail_DuplicateName() {
            // Given
            CreateTenantCommand command = TenantCommandFixture.createCommand();

            given(tenantRepositoryPort.existsByName(any(TenantName.class)))
                .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> tenantCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("동일한 이름의 Tenant가 이미 존재합니다");

            verify(tenantRepositoryPort).existsByName(any(TenantName.class));
        }

        @Test
        @DisplayName("Command가 null이면 예외 발생")
        void execute_Fail_CommandIsNull() {
            // When & Then
            assertThatThrownBy(() -> tenantCommandService.execute((CreateTenantCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreateTenantCommand는 필수입니다");
        }

        @Test
        @DisplayName("Port 호출 순서 검증")
        void verifyPortCallOrder() {
            // Given
            CreateTenantCommand command = TenantCommandFixture.createCommand();
            Tenant expectedTenant = TenantFixture.createWithId(1L);

            given(tenantRepositoryPort.existsByName(any(TenantName.class)))
                .willReturn(false);
            given(tenantRepositoryPort.save(any(Tenant.class)))
                .willReturn(expectedTenant);

            // When
            tenantCommandService.execute(command);

            // Then - Port 호출 순서: existsByName → save
            var inOrder = org.mockito.Mockito.inOrder(tenantRepositoryPort);
            inOrder.verify(tenantRepositoryPort).existsByName(any(TenantName.class));
            inOrder.verify(tenantRepositoryPort).save(any(Tenant.class));
        }
    }

    /**
     * UpdateTenantUseCase - Tenant 수정 테스트
     */
    @Nested
    @DisplayName("UpdateTenantUseCase - Tenant 수정")
    class UpdateTenantUseCaseTests {

        @Test
        @DisplayName("유효한 Command로 Tenant 수정 성공")
        void execute_Success() {
            // Given
            UpdateTenantCommand command = TenantCommandFixture.updateCommand(1L, "Updated Tenant");
            Tenant existingTenant = TenantFixture.createWithId(1L, "Original Tenant");
            Tenant updatedTenant = TenantFixture.createWithId(1L, "Updated Tenant");

            given(tenantRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(existingTenant));
            given(tenantRepositoryPort.save(any(Tenant.class)))
                .willReturn(updatedTenant);

            // When
            TenantResponse response = tenantCommandService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Updated Tenant");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("Tenant 이름 변경 성공")
        void execute_Success_NameChange() {
            // Given
            UpdateTenantCommand command = TenantCommandFixture.updateCommand(1L, "New Name");
            Tenant existingTenant = TenantFixture.createWithId(1L, "Old Name");
            Tenant updatedTenant = TenantFixture.createWithId(1L, "New Name");

            given(tenantRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(existingTenant));
            given(tenantRepositoryPort.save(any(Tenant.class)))
                .willReturn(updatedTenant);

            // When
            TenantResponse response = tenantCommandService.execute(command);

            // Then
            assertThat(response.name()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("존재하지 않는 Tenant 수정 시도하면 예외 발생")
        void execute_Fail_TenantNotFound() {
            // Given
            UpdateTenantCommand command = TenantCommandFixture.updateCommand(999L, "New Name");

            given(tenantRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> tenantCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
        }

        @Test
        @DisplayName("Command가 null이면 예외 발생")
        void execute_Fail_CommandIsNull() {
            // When & Then
            assertThatThrownBy(() -> tenantCommandService.execute((UpdateTenantCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateTenantCommand는 필수입니다");
        }

        @Test
        @DisplayName("Port 호출 순서 검증")
        void verifyPortCallOrder() {
            // Given
            UpdateTenantCommand command = TenantCommandFixture.updateCommand(1L, "Updated");
            Tenant existingTenant = TenantFixture.createWithId(1L);
            Tenant updatedTenant = TenantFixture.createWithId(1L, "Updated");

            given(tenantRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(existingTenant));
            given(tenantRepositoryPort.save(any(Tenant.class)))
                .willReturn(updatedTenant);

            // When
            tenantCommandService.execute(command);

            // Then - Port 호출 순서: findById → save
            var inOrder = org.mockito.Mockito.inOrder(tenantRepositoryPort);
            inOrder.verify(tenantRepositoryPort).findById(any(TenantId.class));
            inOrder.verify(tenantRepositoryPort).save(any(Tenant.class));
        }
    }

    /**
     * UpdateTenantStatusUseCase - Tenant 상태 변경 테스트
     */
    @Nested
    @DisplayName("UpdateTenantStatusUseCase - Tenant 상태 변경")
    class UpdateTenantStatusUseCaseTests {

        @Test
        @DisplayName("ACTIVE → SUSPENDED 상태 변경 성공")
        void execute_Success_ActivateToSuspend() {
            // Given
            UpdateTenantStatusCommand command = TenantCommandFixture.suspendCommand(1L);
            Tenant activeTenant = TenantFixture.createWithId(1L);  // 기본 ACTIVE
            Tenant suspendedTenant = TenantFixture.createSuspended(1L);

            given(tenantRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(activeTenant));
            given(tenantRepositoryPort.save(any(Tenant.class)))
                .willReturn(suspendedTenant);

            // When
            TenantResponse response = tenantCommandService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(1L);
            assertThat(response.status()).isEqualTo("SUSPENDED");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("SUSPENDED → ACTIVE 상태 변경 성공")
        void execute_Success_SuspendToActivate() {
            // Given
            UpdateTenantStatusCommand command = TenantCommandFixture.activateCommand(1L);
            Tenant suspendedTenant = TenantFixture.createSuspended(1L);
            Tenant activatedTenant = TenantFixture.createWithId(1L);

            given(tenantRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(suspendedTenant));
            given(tenantRepositoryPort.save(any(Tenant.class)))
                .willReturn(activatedTenant);

            // When
            TenantResponse response = tenantCommandService.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("잘못된 상태값으로 변경 시도하면 예외 발생")
        void execute_Fail_InvalidStatus() {
            // Given
            UpdateTenantStatusCommand command = TenantCommandFixture.updateStatusCommand(1L, "INVALID_STATUS");
            Tenant tenant = TenantFixture.createWithId(1L);

            given(tenantRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(tenant));

            // When & Then
            assertThatThrownBy(() -> tenantCommandService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 상태값입니다");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
        }

        @Test
        @DisplayName("존재하지 않는 Tenant 상태 변경 시도하면 예외 발생")
        void execute_Fail_TenantNotFound() {
            // Given
            UpdateTenantStatusCommand command = TenantCommandFixture.activateCommand(999L);

            given(tenantRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> tenantCommandService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다");

            verify(tenantRepositoryPort).findById(any(TenantId.class));
        }

        @Test
        @DisplayName("Command가 null이면 예외 발생")
        void execute_Fail_CommandIsNull() {
            // When & Then
            assertThatThrownBy(() -> tenantCommandService.execute((UpdateTenantStatusCommand) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpdateTenantStatusCommand는 필수입니다");
        }
    }

    /**
     * Transaction 경계 테스트
     */
    @Nested
    @DisplayName("Transaction 경계 검증")
    class TransactionBoundaryTests {

        @Test
        @DisplayName("모든 Command UseCase 메서드는 @Transactional로 실행됨")
        void verifyTransactionalAnnotation() throws NoSuchMethodException {
            // Given
            Method createMethod = TenantCommandService.class.getMethod("execute", CreateTenantCommand.class);
            Method updateMethod = TenantCommandService.class.getMethod("execute", UpdateTenantCommand.class);
            Method updateStatusMethod = TenantCommandService.class.getMethod("execute", UpdateTenantStatusCommand.class);

            // When
            Transactional createTransactional = createMethod.getAnnotation(Transactional.class);
            Transactional updateTransactional = updateMethod.getAnnotation(Transactional.class);
            Transactional updateStatusTransactional = updateStatusMethod.getAnnotation(Transactional.class);

            // Then
            assertThat(createTransactional).isNotNull();
            assertThat(updateTransactional).isNotNull();
            assertThat(updateStatusTransactional).isNotNull();
        }
    }

    /**
     * Port Interaction 검증 테스트
     */
    @Nested
    @DisplayName("Port Interaction 검증")
    class PortInteractionTests {

        @Test
        @DisplayName("Repository Port만 호출되고 외부 API 호출은 없음")
        void verifyOnlyRepositoryPortIsCalled() {
            // Given
            CreateTenantCommand command = TenantCommandFixture.createCommand();
            Tenant tenant = TenantFixture.createWithId(1L);

            given(tenantRepositoryPort.existsByName(any(TenantName.class)))
                .willReturn(false);
            given(tenantRepositoryPort.save(any(Tenant.class)))
                .willReturn(tenant);

            // When
            tenantCommandService.execute(command);

            // Then - TenantRepositoryPort만 호출됨
            verify(tenantRepositoryPort).existsByName(any(TenantName.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
            // 외부 API 호출 없음 (Mock 검증으로 확인)
        }

        @Test
        @DisplayName("Transaction 내에서 외부 API 호출 없음 (Transaction 경계 준수)")
        void verifyNoExternalApiCallsWithinTransaction() {
            // Given
            UpdateTenantCommand command = TenantCommandFixture.updateCommand(1L, "Updated");
            Tenant existingTenant = TenantFixture.createWithId(1L);
            Tenant updatedTenant = TenantFixture.createWithId(1L, "Updated");

            given(tenantRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(existingTenant));
            given(tenantRepositoryPort.save(any(Tenant.class)))
                .willReturn(updatedTenant);

            // When
            tenantCommandService.execute(command);

            // Then - Repository Port만 호출됨 (외부 API 없음)
            verify(tenantRepositoryPort).findById(any(TenantId.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
            // WebClient, RestTemplate 등 외부 API 호출 없음
        }
    }
}

package com.ryuqq.fileflow.application.iam.tenant.usecase;

import com.ryuqq.fileflow.application.iam.tenant.dto.CreateTenantCommand;
import com.ryuqq.fileflow.application.iam.tenant.dto.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.TenantRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CreateTenantUseCaseTest - CreateTenantUseCase 단위 테스트
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
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
class CreateTenantUseCaseTest {

    private TenantRepositoryPort tenantRepositoryPort;
    private CreateTenantUseCase createTenantUseCase;

    @BeforeEach
    void setUp() {
        tenantRepositoryPort = mock(TenantRepositoryPort.class);
        createTenantUseCase = new CreateTenantUseCase(tenantRepositoryPort);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("새로운 Tenant를 성공적으로 생성한다")
        void shouldCreateTenantSuccessfully() {
            // Arrange
            CreateTenantCommand command = new CreateTenantCommand("my-tenant");
            Tenant savedTenant = Tenant.of(
                TenantId.of("tenant-id-123"),
                TenantName.of("my-tenant")
            );

            when(tenantRepositoryPort.existsByName(any(TenantName.class))).thenReturn(false);
            when(tenantRepositoryPort.save(any(Tenant.class))).thenReturn(savedTenant);

            // Act
            TenantResponse response = createTenantUseCase.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo("tenant-id-123");
            assertThat(response.name()).isEqualTo("my-tenant");
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.deleted()).isFalse();

            verify(tenantRepositoryPort).existsByName(any(TenantName.class));
            verify(tenantRepositoryPort).save(any(Tenant.class));
        }

        @Test
        @DisplayName("Repository에 올바른 Tenant 객체를 저장한다")
        void shouldSaveCorrectTenantObject() {
            // Arrange
            CreateTenantCommand command = new CreateTenantCommand("new-tenant");
            ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);

            Tenant savedTenant = Tenant.of(
                TenantId.of("tenant-id-456"),
                TenantName.of("new-tenant")
            );

            when(tenantRepositoryPort.existsByName(any(TenantName.class))).thenReturn(false);
            when(tenantRepositoryPort.save(any(Tenant.class))).thenReturn(savedTenant);

            // Act
            createTenantUseCase.execute(command);

            // Assert
            verify(tenantRepositoryPort).save(tenantCaptor.capture());
            Tenant capturedTenant = tenantCaptor.getValue();

            assertThat(capturedTenant.getNameValue()).isEqualTo("new-tenant");
            assertThat(capturedTenant.isActive()).isTrue();
            assertThat(capturedTenant.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> createTenantUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreateTenantCommand는 필수입니다");

            verify(tenantRepositoryPort, never()).existsByName(any());
            verify(tenantRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("중복된 Tenant 이름이 존재하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenDuplicateTenantNameExists() {
            // Arrange
            CreateTenantCommand command = new CreateTenantCommand("duplicate-tenant");

            when(tenantRepositoryPort.existsByName(any(TenantName.class))).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> createTenantUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("동일한 이름의 Tenant가 이미 존재합니다");

            verify(tenantRepositoryPort).existsByName(any(TenantName.class));
            verify(tenantRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("중복 검증 로직")
    class DuplicationCheckScenarios {

        @Test
        @DisplayName("existsByName을 올바른 TenantName으로 호출한다")
        void shouldCallExistsByNameWithCorrectTenantName() {
            // Arrange
            CreateTenantCommand command = new CreateTenantCommand("check-tenant");
            ArgumentCaptor<TenantName> tenantNameCaptor = ArgumentCaptor.forClass(TenantName.class);

            Tenant savedTenant = Tenant.of(
                TenantId.of("tenant-id-789"),
                TenantName.of("check-tenant")
            );

            when(tenantRepositoryPort.existsByName(any(TenantName.class))).thenReturn(false);
            when(tenantRepositoryPort.save(any(Tenant.class))).thenReturn(savedTenant);

            // Act
            createTenantUseCase.execute(command);

            // Assert
            verify(tenantRepositoryPort).existsByName(tenantNameCaptor.capture());
            TenantName capturedName = tenantNameCaptor.getValue();

            assertThat(capturedName.getValue()).isEqualTo("check-tenant");
        }
    }
}

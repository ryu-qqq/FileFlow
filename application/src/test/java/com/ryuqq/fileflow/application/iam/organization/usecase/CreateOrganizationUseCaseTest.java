package com.ryuqq.fileflow.application.iam.organization.usecase;

import com.ryuqq.fileflow.application.iam.organization.dto.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.OrganizationRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.OrgCode;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * CreateOrganizationUseCaseTest - CreateOrganizationUseCase 단위 테스트
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
 *   <li>✅ Long FK 전략 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
class CreateOrganizationUseCaseTest {

    private OrganizationRepositoryPort organizationRepositoryPort;
    private CreateOrganizationUseCase createOrganizationUseCase;

    @BeforeEach
    void setUp() {
        organizationRepositoryPort = mock(OrganizationRepositoryPort.class);
        createOrganizationUseCase = new CreateOrganizationUseCase(organizationRepositoryPort);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("새로운 Organization을 성공적으로 생성한다")
        void shouldCreateOrganizationSuccessfully() {
            // Arrange
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                1L,
                "ORG001",
                "Engineering Dept"
            );

            Organization savedOrganization = Organization.of(
                OrganizationId.of(100L),
                1L,
                OrgCode.of("ORG001"),
                "Engineering Dept"
            );

            when(organizationRepositoryPort.existsByTenantIdAndOrgCode(eq(1L), any(OrgCode.class)))
                .thenReturn(false);
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenReturn(savedOrganization);

            // Act
            OrganizationResponse response = createOrganizationUseCase.execute(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isEqualTo(100L);
            assertThat(response.tenantId()).isEqualTo(1L);
            assertThat(response.orgCode()).isEqualTo("ORG001");
            assertThat(response.name()).isEqualTo("Engineering Dept");
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.deleted()).isFalse();

            verify(organizationRepositoryPort).existsByTenantIdAndOrgCode(eq(1L), any(OrgCode.class));
            verify(organizationRepositoryPort).save(any(Organization.class));
        }

        @Test
        @DisplayName("Repository에 올바른 Organization 객체를 저장한다")
        void shouldSaveCorrectOrganizationObject() {
            // Arrange
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                2L,
                "ORG002",
                "Sales Dept"
            );
            ArgumentCaptor<Organization> organizationCaptor = ArgumentCaptor.forClass(Organization.class);

            Organization savedOrganization = Organization.of(
                OrganizationId.of(200L),
                2L,
                OrgCode.of("ORG002"),
                "Sales Dept"
            );

            when(organizationRepositoryPort.existsByTenantIdAndOrgCode(eq(2L), any(OrgCode.class)))
                .thenReturn(false);
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenReturn(savedOrganization);

            // Act
            createOrganizationUseCase.execute(command);

            // Assert
            verify(organizationRepositoryPort).save(organizationCaptor.capture());
            Organization capturedOrganization = organizationCaptor.getValue();

            assertThat(capturedOrganization.getTenantId()).isEqualTo(2L);
            assertThat(capturedOrganization.getOrgCodeValue()).isEqualTo("ORG002");
            assertThat(capturedOrganization.getName()).isEqualTo("Sales Dept");
            assertThat(capturedOrganization.isActive()).isTrue();
            assertThat(capturedOrganization.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Command가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenCommandIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> createOrganizationUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CreateOrganizationCommand는 필수입니다");

            verify(organizationRepositoryPort, never()).existsByTenantIdAndOrgCode(anyLong(), any());
            verify(organizationRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("중복된 조직 코드가 존재하면 IllegalStateException 발생")
        void shouldThrowExceptionWhenDuplicateOrgCodeExists() {
            // Arrange
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                1L,
                "DUPLICATE",
                "Duplicate Dept"
            );

            when(organizationRepositoryPort.existsByTenantIdAndOrgCode(eq(1L), any(OrgCode.class)))
                .thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> createOrganizationUseCase.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("동일한 Tenant 내에 동일한 조직 코드가 이미 존재합니다");

            verify(organizationRepositoryPort).existsByTenantIdAndOrgCode(eq(1L), any(OrgCode.class));
            verify(organizationRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("중복 검증 로직")
    class DuplicationCheckScenarios {

        @Test
        @DisplayName("existsByTenantIdAndOrgCode를 올바른 파라미터로 호출한다")
        void shouldCallExistsWithCorrectParameters() {
            // Arrange
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                3L,
                "ORG003",
                "HR Dept"
            );
            ArgumentCaptor<Long> tenantIdCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<OrgCode> orgCodeCaptor = ArgumentCaptor.forClass(OrgCode.class);

            Organization savedOrganization = Organization.of(
                OrganizationId.of(300L),
                3L,
                OrgCode.of("ORG003"),
                "HR Dept"
            );

            when(organizationRepositoryPort.existsByTenantIdAndOrgCode(anyLong(), any(OrgCode.class)))
                .thenReturn(false);
            when(organizationRepositoryPort.save(any(Organization.class)))
                .thenReturn(savedOrganization);

            // Act
            createOrganizationUseCase.execute(command);

            // Assert
            verify(organizationRepositoryPort).existsByTenantIdAndOrgCode(
                tenantIdCaptor.capture(),
                orgCodeCaptor.capture()
            );

            assertThat(tenantIdCaptor.getValue()).isEqualTo(3L);
            assertThat(orgCodeCaptor.getValue().getValue()).isEqualTo("ORG003");
        }
    }
}

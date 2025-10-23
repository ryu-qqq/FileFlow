package com.ryuqq.fileflow.application.iam.organization.facade;

import com.ryuqq.fileflow.application.iam.organization.dto.command.CreateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.SoftDeleteOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.command.UpdateOrganizationStatusCommand;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.in.CreateOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.DeleteOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.UpdateOrganizationStatusUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.UpdateOrganizationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrganizationCommandFacadeTest - OrganizationCommandFacade 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>Facade가 각 UseCase로 올바르게 위임하는지 검증</li>
 *   <li>Command 파라미터가 UseCase에 정확히 전달되는지 검증</li>
 *   <li>UseCase의 반환값이 Facade를 통해 올바르게 반환되는지 검증</li>
 * </ul>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>@Nested를 사용한 테스트 그룹화 (UseCase별)</li>
 *   <li>AAA 패턴 (Arrange-Act-Assert) 일관적 적용</li>
 *   <li>모든 의존성 Mocking (Pure Unit Test)</li>
 *   <li>Fast Test (@Tag("fast"))</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Mockito 사용 (mock(), verify())</li>
 *   <li>✅ AssertJ 사용 (assertThat())</li>
 *   <li>✅ @DisplayName으로 테스트 의도 명확화</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("OrganizationCommandFacade 테스트")
class OrganizationCommandFacadeTest {

    private CreateOrganizationUseCase createOrganizationUseCase;
    private UpdateOrganizationUseCase updateOrganizationUseCase;
    private UpdateOrganizationStatusUseCase updateOrganizationStatusUseCase;
    private DeleteOrganizationUseCase deleteOrganizationUseCase;

    private OrganizationCommandFacade organizationCommandFacade;

    /**
     * 각 테스트 실행 전 초기화
     *
     * <p>모든 UseCase를 Mock으로 생성하고, Facade에 주입합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @BeforeEach
    void setUp() {
        createOrganizationUseCase = mock(CreateOrganizationUseCase.class);
        updateOrganizationUseCase = mock(UpdateOrganizationUseCase.class);
        updateOrganizationStatusUseCase = mock(UpdateOrganizationStatusUseCase.class);
        deleteOrganizationUseCase = mock(DeleteOrganizationUseCase.class);

        organizationCommandFacade = new OrganizationCommandFacade(
            createOrganizationUseCase,
            updateOrganizationUseCase,
            updateOrganizationStatusUseCase,
            deleteOrganizationUseCase
        );
    }

    /**
     * CreateOrganizationUseCase 위임 테스트
     *
     * <p>Facade가 CreateOrganizationUseCase로 정확히 위임하는지 검증합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Nested
    @DisplayName("createOrganization() 위임 테스트")
    class CreateOrganizationDelegationTests {

        @Test
        @DisplayName("CreateOrganizationUseCase로 올바르게 위임한다")
        void shouldDelegateToCreateOrganizationUseCase() {
            // Arrange
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                "tenant-123",
                "ORG-001",
                "Engineering Department"
            );
            LocalDateTime now = LocalDateTime.now();
            OrganizationResponse expectedResponse = new OrganizationResponse(
                1L,
                "tenant-123",
                "ORG-001",
                "Engineering Department",
                "ACTIVE",
                false,
                now,
                now
            );

            when(createOrganizationUseCase.execute(any(CreateOrganizationCommand.class)))
                .thenReturn(expectedResponse);

            // Act
            OrganizationResponse actualResponse = organizationCommandFacade.createOrganization(command);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.organizationId()).isEqualTo(expectedResponse.organizationId());
            assertThat(actualResponse.tenantId()).isEqualTo(expectedResponse.tenantId());
            assertThat(actualResponse.orgCode()).isEqualTo(expectedResponse.orgCode());
            assertThat(actualResponse.name()).isEqualTo(expectedResponse.name());
            assertThat(actualResponse.status()).isEqualTo(expectedResponse.status());
            assertThat(actualResponse.deleted()).isEqualTo(expectedResponse.deleted());

            verify(createOrganizationUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("Command 파라미터가 UseCase에 정확히 전달된다")
        void shouldPassCommandToUseCaseCorrectly() {
            // Arrange
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                "tenant-456",
                "ORG-002",
                "Sales Department"
            );
            LocalDateTime now = LocalDateTime.now();
            OrganizationResponse mockResponse = new OrganizationResponse(
                2L,
                "tenant-456",
                "ORG-002",
                "Sales Department",
                "ACTIVE",
                false,
                now,
                now
            );

            when(createOrganizationUseCase.execute(command))
                .thenReturn(mockResponse);

            // Act
            organizationCommandFacade.createOrganization(command);

            // Assert - Command 객체가 정확히 전달되었는지 검증
            verify(createOrganizationUseCase).execute(command);
        }

        @Test
        @DisplayName("UseCase의 반환값을 그대로 반환한다 (추가 변환 없음)")
        void shouldReturnUseCaseResponseDirectly() {
            // Arrange
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                "tenant-789",
                "ORG-003",
                "Marketing Department"
            );
            LocalDateTime now = LocalDateTime.now();
            OrganizationResponse expectedResponse = new OrganizationResponse(
                3L,
                "tenant-789",
                "ORG-003",
                "Marketing Department",
                "ACTIVE",
                false,
                now,
                now
            );

            when(createOrganizationUseCase.execute(command))
                .thenReturn(expectedResponse);

            // Act
            OrganizationResponse actualResponse = organizationCommandFacade.createOrganization(command);

            // Assert - 반환값이 UseCase의 반환값과 동일한지 검증
            assertThat(actualResponse).isSameAs(expectedResponse);
        }
    }

    /**
     * UpdateOrganizationUseCase 위임 테스트
     *
     * <p>Facade가 UpdateOrganizationUseCase로 정확히 위임하는지 검증합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Nested
    @DisplayName("updateOrganization() 위임 테스트")
    class UpdateOrganizationDelegationTests {

        @Test
        @DisplayName("UpdateOrganizationUseCase로 올바르게 위임한다")
        void shouldDelegateToUpdateOrganizationUseCase() {
            // Arrange
            UpdateOrganizationCommand command = new UpdateOrganizationCommand(
                100L,
                "Updated Department"
            );
            LocalDateTime now = LocalDateTime.now();
            OrganizationResponse expectedResponse = new OrganizationResponse(
                100L,
                "tenant-123",
                "ORG-001",
                "Updated Department",
                "ACTIVE",
                false,
                now,
                now
            );

            when(updateOrganizationUseCase.execute(any(UpdateOrganizationCommand.class)))
                .thenReturn(expectedResponse);

            // Act
            OrganizationResponse actualResponse = organizationCommandFacade.updateOrganization(command);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.organizationId()).isEqualTo(expectedResponse.organizationId());
            assertThat(actualResponse.name()).isEqualTo(expectedResponse.name());

            verify(updateOrganizationUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("Command 파라미터가 UseCase에 정확히 전달된다")
        void shouldPassCommandToUseCaseCorrectly() {
            // Arrange
            UpdateOrganizationCommand command = new UpdateOrganizationCommand(
                200L,
                "New Name"
            );
            LocalDateTime now = LocalDateTime.now();
            OrganizationResponse mockResponse = new OrganizationResponse(
                200L,
                "tenant-456",
                "ORG-002",
                "New Name",
                "ACTIVE",
                false,
                now,
                now
            );

            when(updateOrganizationUseCase.execute(command))
                .thenReturn(mockResponse);

            // Act
            organizationCommandFacade.updateOrganization(command);

            // Assert
            verify(updateOrganizationUseCase).execute(command);
        }

        @Test
        @DisplayName("UseCase의 반환값을 그대로 반환한다")
        void shouldReturnUseCaseResponseDirectly() {
            // Arrange
            UpdateOrganizationCommand command = new UpdateOrganizationCommand(
                300L,
                "Another Name"
            );
            LocalDateTime now = LocalDateTime.now();
            OrganizationResponse expectedResponse = new OrganizationResponse(
                300L,
                "tenant-789",
                "ORG-003",
                "Another Name",
                "ACTIVE",
                false,
                now,
                now
            );

            when(updateOrganizationUseCase.execute(command))
                .thenReturn(expectedResponse);

            // Act
            OrganizationResponse actualResponse = organizationCommandFacade.updateOrganization(command);

            // Assert
            assertThat(actualResponse).isSameAs(expectedResponse);
        }
    }

    /**
     * UpdateOrganizationStatusUseCase 위임 테스트
     *
     * <p>Facade가 UpdateOrganizationStatusUseCase로 정확히 위임하는지 검증합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Nested
    @DisplayName("updateOrganizationStatus() 위임 테스트")
    class UpdateOrganizationStatusDelegationTests {

        @Test
        @DisplayName("UpdateOrganizationStatusUseCase로 올바르게 위임한다")
        void shouldDelegateToUpdateOrganizationStatusUseCase() {
            // Arrange
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(
                100L,
                "INACTIVE"
            );
            LocalDateTime now = LocalDateTime.now();
            OrganizationResponse expectedResponse = new OrganizationResponse(
                100L,
                "tenant-123",
                "ORG-001",
                "Engineering Department",
                "INACTIVE",
                false,
                now,
                now
            );

            when(updateOrganizationStatusUseCase.execute(any(UpdateOrganizationStatusCommand.class)))
                .thenReturn(expectedResponse);

            // Act
            OrganizationResponse actualResponse = organizationCommandFacade.updateOrganizationStatus(command);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.organizationId()).isEqualTo(expectedResponse.organizationId());
            assertThat(actualResponse.status()).isEqualTo(expectedResponse.status());

            verify(updateOrganizationStatusUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("Command 파라미터가 UseCase에 정확히 전달된다")
        void shouldPassCommandToUseCaseCorrectly() {
            // Arrange
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(
                200L,
                "INACTIVE"
            );
            LocalDateTime now = LocalDateTime.now();
            OrganizationResponse mockResponse = new OrganizationResponse(
                200L,
                "tenant-456",
                "ORG-002",
                "Sales Department",
                "INACTIVE",
                false,
                now,
                now
            );

            when(updateOrganizationStatusUseCase.execute(command))
                .thenReturn(mockResponse);

            // Act
            organizationCommandFacade.updateOrganizationStatus(command);

            // Assert
            verify(updateOrganizationStatusUseCase).execute(command);
        }

        @Test
        @DisplayName("UseCase의 반환값을 그대로 반환한다")
        void shouldReturnUseCaseResponseDirectly() {
            // Arrange
            UpdateOrganizationStatusCommand command = new UpdateOrganizationStatusCommand(
                300L,
                "INACTIVE"
            );
            LocalDateTime now = LocalDateTime.now();
            OrganizationResponse expectedResponse = new OrganizationResponse(
                300L,
                "tenant-789",
                "ORG-003",
                "Marketing Department",
                "INACTIVE",
                false,
                now,
                now
            );

            when(updateOrganizationStatusUseCase.execute(command))
                .thenReturn(expectedResponse);

            // Act
            OrganizationResponse actualResponse = organizationCommandFacade.updateOrganizationStatus(command);

            // Assert
            assertThat(actualResponse).isSameAs(expectedResponse);
        }
    }

    /**
     * DeleteOrganizationUseCase 위임 테스트
     *
     * <p>Facade가 DeleteOrganizationUseCase로 정확히 위임하는지 검증합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Nested
    @DisplayName("deleteOrganization() 위임 테스트")
    class DeleteOrganizationDelegationTests {

        @Test
        @DisplayName("DeleteOrganizationUseCase로 올바르게 위임한다")
        void shouldDelegateToDeleteOrganizationUseCase() {
            // Arrange
            SoftDeleteOrganizationCommand command = new SoftDeleteOrganizationCommand(100L);

            doNothing().when(deleteOrganizationUseCase).execute(any(SoftDeleteOrganizationCommand.class));

            // Act
            organizationCommandFacade.deleteOrganization(command);

            // Assert
            verify(deleteOrganizationUseCase, times(1)).execute(command);
        }

        @Test
        @DisplayName("Command 파라미터가 UseCase에 정확히 전달된다")
        void shouldPassCommandToUseCaseCorrectly() {
            // Arrange
            SoftDeleteOrganizationCommand command = new SoftDeleteOrganizationCommand(200L);

            doNothing().when(deleteOrganizationUseCase).execute(command);

            // Act
            organizationCommandFacade.deleteOrganization(command);

            // Assert
            verify(deleteOrganizationUseCase).execute(command);
        }

        @Test
        @DisplayName("void 메서드이므로 반환값이 없다")
        void shouldReturnVoidCorrectly() {
            // Arrange
            SoftDeleteOrganizationCommand command = new SoftDeleteOrganizationCommand(300L);

            doNothing().when(deleteOrganizationUseCase).execute(command);

            // Act - void 메서드이므로 반환값 검증 불필요
            organizationCommandFacade.deleteOrganization(command);

            // Assert - UseCase 호출 검증만 수행
            verify(deleteOrganizationUseCase).execute(command);
        }
    }

    /**
     * Facade 전체 통합 테스트
     *
     * <p>Facade가 모든 UseCase와 올바르게 통합되는지 검증합니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-23
     */
    @Nested
    @DisplayName("Facade 통합 테스트")
    class FacadeIntegrationTests {

        @Test
        @DisplayName("Facade는 4개의 UseCase를 모두 통합한다")
        void shouldIntegrateAllFourUseCases() {
            // Arrange
            CreateOrganizationCommand createCommand = new CreateOrganizationCommand(
                "tenant-123",
                "ORG-001",
                "Engineering"
            );
            UpdateOrganizationCommand updateCommand = new UpdateOrganizationCommand(1L, "Updated Name");
            UpdateOrganizationStatusCommand statusCommand = new UpdateOrganizationStatusCommand(1L, "INACTIVE");
            SoftDeleteOrganizationCommand deleteCommand = new SoftDeleteOrganizationCommand(1L);

            LocalDateTime now = LocalDateTime.now();
            OrganizationResponse createResponse = new OrganizationResponse(
                1L, "tenant-123", "ORG-001", "Engineering", "ACTIVE", false, now, now
            );
            OrganizationResponse updateResponse = new OrganizationResponse(
                1L, "tenant-123", "ORG-001", "Updated Name", "ACTIVE", false, now, now
            );
            OrganizationResponse statusResponse = new OrganizationResponse(
                1L, "tenant-123", "ORG-001", "Updated Name", "INACTIVE", false, now, now
            );

            when(createOrganizationUseCase.execute(createCommand)).thenReturn(createResponse);
            when(updateOrganizationUseCase.execute(updateCommand)).thenReturn(updateResponse);
            when(updateOrganizationStatusUseCase.execute(statusCommand)).thenReturn(statusResponse);
            doNothing().when(deleteOrganizationUseCase).execute(deleteCommand);

            // Act
            OrganizationResponse result1 = organizationCommandFacade.createOrganization(createCommand);
            OrganizationResponse result2 = organizationCommandFacade.updateOrganization(updateCommand);
            OrganizationResponse result3 = organizationCommandFacade.updateOrganizationStatus(statusCommand);
            organizationCommandFacade.deleteOrganization(deleteCommand);

            // Assert
            assertThat(result1).isNotNull();
            assertThat(result2).isNotNull();
            assertThat(result3).isNotNull();

            verify(createOrganizationUseCase).execute(createCommand);
            verify(updateOrganizationUseCase).execute(updateCommand);
            verify(updateOrganizationStatusUseCase).execute(statusCommand);
            verify(deleteOrganizationUseCase).execute(deleteCommand);
        }

        @Test
        @DisplayName("Facade는 UseCase에 대한 단순 위임만 수행한다 (추가 로직 없음)")
        void shouldOnlyDelegateWithoutAdditionalLogic() {
            // Arrange
            CreateOrganizationCommand command = new CreateOrganizationCommand(
                "tenant-123",
                "ORG-001",
                "Engineering"
            );
            LocalDateTime now = LocalDateTime.now();
            OrganizationResponse expectedResponse = new OrganizationResponse(
                1L, "tenant-123", "ORG-001", "Engineering", "ACTIVE", false, now, now
            );

            when(createOrganizationUseCase.execute(command)).thenReturn(expectedResponse);

            // Act
            OrganizationResponse actualResponse = organizationCommandFacade.createOrganization(command);

            // Assert - Facade는 추가 변환이나 검증 없이 단순 위임만 수행
            assertThat(actualResponse).isSameAs(expectedResponse);
            verify(createOrganizationUseCase, times(1)).execute(command);
            verifyNoMoreInteractions(createOrganizationUseCase);
        }
    }
}

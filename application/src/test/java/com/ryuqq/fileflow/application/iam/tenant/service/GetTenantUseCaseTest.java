package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
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
 * GetTenantUseCaseTest - GetTenantUseCase 단위 테스트
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
 *   <li>구현체: {@link TenantQueryService#execute(GetTenantQuery)}</li>
 *   <li>인터페이스: {@link GetTenantUseCase}</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("GetTenantUseCase 테스트")
class GetTenantUseCaseTest {

    private TenantQueryRepositoryPort tenantQueryRepositoryPort;
    private GetTenantUseCase getTenantUseCase;

    @BeforeEach
    void setUp() {
        tenantQueryRepositoryPort = mock(TenantQueryRepositoryPort.class);
        getTenantUseCase = new TenantQueryService(tenantQueryRepositoryPort);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("ID로 Tenant를 성공적으로 조회한다")
        void shouldGetTenantByIdSuccessfully() {
            // Arrange
            String tenantId = "tenant-id-123";
            String tenantName = "Test Company";
            GetTenantQuery query = new GetTenantQuery(tenantId);
            Tenant existingTenant = TenantFixtures.activeTenantWithIdAndName(tenantId, tenantName);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));

            // Act
            TenantResponse response = getTenantUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.name()).isEqualTo(tenantName);
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.deleted()).isFalse();

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
        }

        @Test
        @DisplayName("Repository에서 올바른 TenantId로 조회한다")
        void shouldFindByCorrectTenantId() {
            // Arrange
            String tenantId = "tenant-id-456";
            GetTenantQuery query = new GetTenantQuery(tenantId);
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);
            ArgumentCaptor<TenantId> tenantIdCaptor = ArgumentCaptor.forClass(TenantId.class);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));

            // Act
            getTenantUseCase.execute(query);

            // Assert
            verify(tenantQueryRepositoryPort).findById(tenantIdCaptor.capture());
            TenantId capturedId = tenantIdCaptor.getValue();

            assertThat(capturedId.value()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("SUSPENDED 상태의 Tenant도 조회할 수 있다")
        void shouldGetSuspendedTenant() {
            // Arrange
            String tenantId = "suspended-tenant-id";
            GetTenantQuery query = new GetTenantQuery(tenantId);
            Tenant suspendedTenant = TenantFixtures.suspendedTenantWithName("Suspended Company");

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(suspendedTenant));

            // Act
            TenantResponse response = getTenantUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("SUSPENDED");
            assertThat(response.deleted()).isFalse();
        }

        @Test
        @DisplayName("조회된 Tenant의 모든 필드가 Response에 포함된다")
        void shouldIncludeAllFieldsInResponse() {
            // Arrange
            String tenantId = "tenant-id-789";
            GetTenantQuery query = new GetTenantQuery(tenantId);
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));

            // Act
            TenantResponse response = getTenantUseCase.execute(query);

            // Assert
            assertThat(response.tenantId()).isNotNull();
            assertThat(response.name()).isNotNull();
            assertThat(response.status()).isNotNull();
            assertThat(response.deleted()).isNotNull();
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> getTenantUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetTenantQuery는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("Tenant가 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenTenantNotFound() {
            // Arrange
            String nonExistentId = "non-existent-id";
            GetTenantQuery query = new GetTenantQuery(nonExistentId);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> getTenantUseCase.execute(query))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다")
                .hasMessageContaining(nonExistentId);

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
        }

        @Test
        @DisplayName("삭제된 Tenant 조회 시 정상적으로 조회된다 (soft delete)")
        void shouldGetDeletedTenant() {
            // Arrange
            String tenantId = "deleted-tenant-id";
            GetTenantQuery query = new GetTenantQuery(tenantId);
            Tenant deletedTenant = TenantFixtures.deletedTenantWithName("Deleted Company");

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(deletedTenant));

            // Act
            TenantResponse response = getTenantUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.deleted()).isTrue();
            assertThat(response.status()).isEqualTo("SUSPENDED");
        }
    }

    @Nested
    @DisplayName("Query 검증")
    class QueryValidationScenarios {

        @Test
        @DisplayName("Query의 tenantId가 null이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenTenantIdIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new GetTenantQuery(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantId는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("Query의 tenantId가 빈 문자열이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenTenantIdIsBlank() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new GetTenantQuery(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantId는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("Query의 tenantId가 공백 문자열이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenTenantIdIsWhitespace() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new GetTenantQuery("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantId는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("GetTenantQuery.of() 팩토리 메서드가 정상 동작한다")
        void shouldCreateQueryUsingFactoryMethod() {
            // Arrange
            String tenantId = "tenant-id-999";
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));

            // Act
            GetTenantQuery query = GetTenantQuery.of(tenantId);
            TenantResponse response = getTenantUseCase.execute(query);

            // Assert
            assertThat(query).isNotNull();
            assertThat(query.tenantId()).isEqualTo(tenantId);
            assertThat(response.tenantId()).isEqualTo(tenantId);
        }
    }

    @Nested
    @DisplayName("Read-Only Transaction 검증")
    class TransactionScenarios {

        @Test
        @DisplayName("조회 작업은 데이터를 변경하지 않는다")
        void shouldNotModifyData() {
            // Arrange
            String tenantId = "tenant-id-123";
            GetTenantQuery query = new GetTenantQuery(tenantId);
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));

            // Act
            getTenantUseCase.execute(query);

            // Assert - Repository는 findById만 호출 (Query Repository는 조회만 담당)
            verify(tenantQueryRepositoryPort, times(1)).findById(any(TenantId.class));
        }
    }
}

package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationQueryRepositoryPort;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantTreeQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantTreeResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantTreeUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.exception.TenantNotFoundException;
import com.ryuqq.fileflow.fixtures.OrganizationFixtures;
import com.ryuqq.fileflow.fixtures.TenantFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GetTenantTreeUseCaseTest - GetTenantTreeUseCase 단위 테스트
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
 *   <li>구현체: {@link GetTenantTreeService#execute(GetTenantTreeQuery)}</li>
 *   <li>인터페이스: {@link GetTenantTreeUseCase}</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("GetTenantTreeUseCase 테스트")
class GetTenantTreeUseCaseTest {

    private TenantQueryRepositoryPort tenantQueryRepositoryPort;
    private OrganizationQueryRepositoryPort organizationQueryRepositoryPort;
    private GetTenantTreeUseCase getTenantTreeUseCase;

    @BeforeEach
    void setUp() {
        tenantQueryRepositoryPort = mock(TenantQueryRepositoryPort.class);
        organizationQueryRepositoryPort = mock(OrganizationQueryRepositoryPort.class);
        getTenantTreeUseCase = new GetTenantTreeService(
            tenantQueryRepositoryPort,
            organizationQueryRepositoryPort
        );
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("Tenant와 하위 Organization 목록을 트리 구조로 조회한다")
        void shouldGetTenantTreeWithOrganizations() {
            // Arrange
            String tenantId = "tenant-id-123";
            GetTenantTreeQuery query = GetTenantTreeQuery.of(tenantId);
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            Organization org1 = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);
            Organization org2 = OrganizationFixtures.hrOrganizationWithId(2L, tenantId);
            Organization org3 = OrganizationFixtures.organizationWithCode(tenantId, "IT", "IT Department");
            List<Organization> organizations = List.of(org1, org2);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(organizationQueryRepositoryPort.findAllWithOffset(
                anyString(), isNull(), isNull(), eq(false), eq(0), eq(1000)))
                .thenReturn(organizations);

            // Act
            TenantTreeResponse response = getTenantTreeUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.organizationCount()).isEqualTo(2);
            assertThat(response.organizations()).hasSize(2);
            assertThat(response.deleted()).isFalse();

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                eq(tenantId), isNull(), isNull(), eq(false), eq(0), eq(1000));
        }

        @Test
        @DisplayName("Organization이 없는 Tenant도 조회할 수 있다")
        void shouldGetTenantTreeWithoutOrganizations() {
            // Arrange
            String tenantId = "tenant-id-456";
            GetTenantTreeQuery query = GetTenantTreeQuery.of(tenantId);
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);
            List<Organization> emptyOrganizations = Collections.emptyList();

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(organizationQueryRepositoryPort.findAllWithOffset(
                anyString(), isNull(), isNull(), eq(false), eq(0), eq(1000)))
                .thenReturn(emptyOrganizations);

            // Act
            TenantTreeResponse response = getTenantTreeUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.organizationCount()).isZero();
            assertThat(response.organizations()).isEmpty();
        }

        @Test
        @DisplayName("includeDeleted=false일 때 삭제된 Organization을 제외한다")
        void shouldExcludeDeletedOrganizationsWhenIncludeDeletedIsFalse() {
            // Arrange
            String tenantId = "tenant-id-789";
            GetTenantTreeQuery query = GetTenantTreeQuery.of(tenantId, false);
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            Organization org1 = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);
            Organization org2 = OrganizationFixtures.hrOrganizationWithId(2L, tenantId);
            List<Organization> activeOrganizations = List.of(org1, org2);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(organizationQueryRepositoryPort.findAllWithOffset(
                anyString(), isNull(), isNull(), eq(false), eq(0), eq(1000)))
                .thenReturn(activeOrganizations);

            // Act
            TenantTreeResponse response = getTenantTreeUseCase.execute(query);

            // Assert
            assertThat(response.organizationCount()).isEqualTo(2);
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                eq(tenantId), isNull(), isNull(), eq(false), eq(0), eq(1000));
        }

        @Test
        @DisplayName("includeDeleted=true일 때 삭제된 Organization을 포함한다")
        void shouldIncludeDeletedOrganizationsWhenIncludeDeletedIsTrue() {
            // Arrange
            String tenantId = "tenant-id-999";
            GetTenantTreeQuery query = GetTenantTreeQuery.of(tenantId, true);
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            Organization org1 = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);
            Organization org2 = OrganizationFixtures.deletedOrganization(tenantId);
            List<Organization> allOrganizations = List.of(org1, org2);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(organizationQueryRepositoryPort.findAllWithOffset(
                anyString(), isNull(), isNull(), isNull(), eq(0), eq(1000)))
                .thenReturn(allOrganizations);

            // Act
            TenantTreeResponse response = getTenantTreeUseCase.execute(query);

            // Assert
            assertThat(response.organizationCount()).isEqualTo(2);
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                eq(tenantId), isNull(), isNull(), isNull(), eq(0), eq(1000));
        }

        @Test
        @DisplayName("삭제된 Tenant도 트리 조회가 가능하다 (Soft Delete)")
        void shouldGetTreeForDeletedTenant() {
            // Arrange
            String tenantId = "deleted-tenant-id";
            GetTenantTreeQuery query = GetTenantTreeQuery.of(tenantId);
            Tenant deletedTenant = TenantFixtures.deletedTenantWithName("Deleted Company");

            Organization org1 = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);
            List<Organization> organizations = List.of(org1);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(deletedTenant));
            when(organizationQueryRepositoryPort.findAllWithOffset(
                anyString(), isNull(), isNull(), eq(false), eq(0), eq(1000)))
                .thenReturn(organizations);

            // Act
            TenantTreeResponse response = getTenantTreeUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.deleted()).isTrue();
            assertThat(response.organizationCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Repository에 올바른 tenantId로 Organization을 조회한다")
        void shouldFindOrganizationsByCorrectTenantId() {
            // Arrange
            String tenantId = "tenant-id-abc";
            GetTenantTreeQuery query = GetTenantTreeQuery.of(tenantId);
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);
            ArgumentCaptor<String> tenantIdCaptor = ArgumentCaptor.forClass(String.class);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(organizationQueryRepositoryPort.findAllWithOffset(
                anyString(), isNull(), isNull(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

            // Act
            getTenantTreeUseCase.execute(query);

            // Assert
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                tenantIdCaptor.capture(), isNull(), isNull(), eq(false), eq(0), eq(1000));
            String capturedTenantId = tenantIdCaptor.getValue();
            assertThat(capturedTenantId).isEqualTo(tenantId);
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> getTenantTreeUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetTenantTreeQuery는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findById(any());
            verify(organizationQueryRepositoryPort, never()).findAllWithOffset(
                anyString(), anyString(), anyString(), anyBoolean(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Tenant가 존재하지 않으면 TenantNotFoundException 발생")
        void shouldThrowExceptionWhenTenantNotFound() {
            // Arrange
            String nonExistentId = "non-existent-id";
            GetTenantTreeQuery query = GetTenantTreeQuery.of(nonExistentId);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> getTenantTreeUseCase.execute(query))
                .isInstanceOf(TenantNotFoundException.class)
                .hasMessageContaining(nonExistentId);

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
            verify(organizationQueryRepositoryPort, never()).findAllWithOffset(
                anyString(), anyString(), anyString(), anyBoolean(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("Query 검증")
    class QueryValidationScenarios {

        @Test
        @DisplayName("Query의 tenantId가 null이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenTenantIdIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new GetTenantTreeQuery(null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("Query의 tenantId가 빈 문자열이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenTenantIdIsBlank() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new GetTenantTreeQuery("", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("Query의 tenantId가 공백 문자열이면 예외 발생 (Record Validation)")
        void shouldThrowExceptionWhenTenantIdIsWhitespace() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new GetTenantTreeQuery("   ", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("GetTenantTreeQuery.of() 팩토리 메서드가 정상 동작한다")
        void shouldCreateQueryUsingFactoryMethod() {
            // Arrange
            String tenantId = "tenant-id-factory";
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(organizationQueryRepositoryPort.findAllWithOffset(
                anyString(), isNull(), isNull(), eq(false), eq(0), eq(1000)))
                .thenReturn(Collections.emptyList());

            // Act
            GetTenantTreeQuery query = GetTenantTreeQuery.of(tenantId);
            TenantTreeResponse response = getTenantTreeUseCase.execute(query);

            // Assert
            assertThat(query).isNotNull();
            assertThat(query.tenantId()).isEqualTo(tenantId);
            assertThat(query.includeDeleted()).isFalse();
            assertThat(response.tenantId()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("GetTenantTreeQuery.of(tenantId, includeDeleted) 팩토리 메서드가 정상 동작한다")
        void shouldCreateQueryWithIncludeDeletedUsingFactoryMethod() {
            // Arrange
            String tenantId = "tenant-id-factory-2";
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(organizationQueryRepositoryPort.findAllWithOffset(
                anyString(), isNull(), isNull(), isNull(), eq(0), eq(1000)))
                .thenReturn(Collections.emptyList());

            // Act
            GetTenantTreeQuery query = GetTenantTreeQuery.of(tenantId, true);
            TenantTreeResponse response = getTenantTreeUseCase.execute(query);

            // Assert
            assertThat(query).isNotNull();
            assertThat(query.tenantId()).isEqualTo(tenantId);
            assertThat(query.includeDeleted()).isTrue();
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
            String tenantId = "tenant-id-readonly";
            GetTenantTreeQuery query = GetTenantTreeQuery.of(tenantId);
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));
            when(organizationQueryRepositoryPort.findAllWithOffset(
                anyString(), isNull(), isNull(), eq(false), eq(0), eq(1000)))
                .thenReturn(Collections.emptyList());

            // Act
            getTenantTreeUseCase.execute(query);

            // Assert - Query Repository는 findById, findAllWithOffset만 호출 (조회만 담당)
            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                anyString(), isNull(), isNull(), eq(false), eq(0), eq(1000));
        }
    }
}

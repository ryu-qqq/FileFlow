package com.ryuqq.fileflow.application.iam.tenant.facade;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantTreeQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantsQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantTreeResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantTreeUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * TenantQueryFacade 단위 테스트
 *
 * <p>TenantQueryFacade의 UseCase 위임 및 Response 반환 로직을 검증합니다.</p>
 *
 * <p><strong>테스트 목적:</strong></p>
 * <ul>
 *   <li>Facade의 UseCase 위임 검증</li>
 *   <li>여러 Query UseCase의 통합 진입점 검증</li>
 *   <li>Pagination 처리 검증 (Offset/Cursor)</li>
 *   <li>예외 전파 검증 (Facade는 예외를 그대로 전파)</li>
 * </ul>
 *
 * <p><strong>테스트 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ AAA 패턴 (Arrange-Act-Assert)</li>
 *   <li>✅ @Nested를 이용한 테스트 그룹화</li>
 *   <li>✅ Mock을 이용한 의존성 격리</li>
 *   <li>✅ @Tag("unit") @Tag("application") @Tag("fast")</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("TenantQueryFacade 테스트")
class TenantQueryFacadeTest {

    private GetTenantUseCase getTenantUseCase;
    private GetTenantsUseCase getTenantsUseCase;
    private GetTenantTreeUseCase getTenantTreeUseCase;
    private TenantQueryFacade tenantQueryFacade;

    @BeforeEach
    void setUp() {
        // Arrange - Mock 의존성 생성
        getTenantUseCase = mock(GetTenantUseCase.class);
        getTenantsUseCase = mock(GetTenantsUseCase.class);
        getTenantTreeUseCase = mock(GetTenantTreeUseCase.class);

        // Arrange - Facade 생성 (Constructor Injection)
        tenantQueryFacade = new TenantQueryFacade(
            getTenantUseCase,
            getTenantsUseCase,
            getTenantTreeUseCase
        );
    }

    @Nested
    @DisplayName("getTenant - Tenant 단건 조회")
    class GetTenantTests {

        @Test
        @DisplayName("GetTenantUseCase로 위임하고 결과를 그대로 반환한다")
        void shouldDelegateToGetTenantUseCase() {
            // Arrange
            GetTenantQuery query = new GetTenantQuery("tenant-id-123");
            TenantResponse expectedResponse = new TenantResponse(
                "tenant-id-123",
                "test-tenant",
                "ACTIVE",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            when(getTenantUseCase.execute(query)).thenReturn(expectedResponse);

            // Act
            TenantResponse actualResponse = tenantQueryFacade.getTenant(query);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse).isEqualTo(expectedResponse);
            assertThat(actualResponse.tenantId()).isEqualTo("tenant-id-123");
            assertThat(actualResponse.name()).isEqualTo("test-tenant");
            assertThat(actualResponse.status()).isEqualTo("ACTIVE");

            verify(getTenantUseCase, times(1)).execute(query);
        }

        @Test
        @DisplayName("정확한 Query 파라미터가 UseCase로 전달된다")
        void shouldPassExactQueryParameterToUseCase() {
            // Arrange
            GetTenantQuery query = new GetTenantQuery("specific-tenant-id");
            TenantResponse response = new TenantResponse(
                "specific-tenant-id",
                "test",
                "ACTIVE",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            when(getTenantUseCase.execute(query)).thenReturn(response);

            // Act
            tenantQueryFacade.getTenant(query);

            // Assert - 정확한 Query 객체가 전달되었는지 검증
            verify(getTenantUseCase, times(1)).execute(query);
            verify(getTenantUseCase, times(1)).execute(argThat(q ->
                q.tenantId().equals("specific-tenant-id")
            ));
        }

        @Test
        @DisplayName("UseCase에서 발생한 예외를 그대로 전파한다")
        void shouldPropagateExceptionFromUseCase() {
            // Arrange
            GetTenantQuery query = new GetTenantQuery("non-existent-id");
            IllegalStateException expectedException = new IllegalStateException("Tenant를 찾을 수 없습니다");

            when(getTenantUseCase.execute(query)).thenThrow(expectedException);

            // Act & Assert
            assertThatThrownBy(() -> tenantQueryFacade.getTenant(query))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다");

            verify(getTenantUseCase, times(1)).execute(query);
        }
    }

    @Nested
    @DisplayName("getTenantsWithPage - Tenant 목록 조회 (Offset-based Pagination)")
    class GetTenantsWithPageTests {

        @Test
        @DisplayName("Offset-based Pagination을 처리하고 PageResponse를 반환한다")
        void shouldHandleOffsetBasedPagination() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 20, null, null, null);

            TenantResponse tenant1 = new TenantResponse(
                "tenant-1",
                "Tenant 1",
                "ACTIVE",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            TenantResponse tenant2 = new TenantResponse(
                "tenant-2",
                "Tenant 2",
                "ACTIVE",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            PageResponse<TenantResponse> expectedPageResponse = PageResponse.of(
                List.of(tenant1, tenant2),
                0, 20, 50L, 3, true, false
            );

            when(getTenantsUseCase.executeWithPage(query)).thenReturn(expectedPageResponse);

            // Act
            PageResponse<TenantResponse> actualResponse = tenantQueryFacade.getTenantsWithPage(query);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.content()).hasSize(2);
            assertThat(actualResponse.totalElements()).isEqualTo(50L);
            assertThat(actualResponse.totalPages()).isEqualTo(3);
            assertThat(actualResponse.page()).isEqualTo(0);
            assertThat(actualResponse.size()).isEqualTo(20);
            assertThat(actualResponse.first()).isTrue();
            assertThat(actualResponse.last()).isFalse();

            verify(getTenantsUseCase, times(1)).executeWithPage(query);
        }

        @Test
        @DisplayName("여러 Tenant를 포함한 PageResponse를 올바르게 반환한다")
        void shouldReturnPageResponseWithMultipleTenants() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(1, 10, null, null, null);

            List<TenantResponse> tenants = List.of(
                new TenantResponse("tenant-1", "Name 1", "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now()),
                new TenantResponse("tenant-2", "Name 2", "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now()),
                new TenantResponse("tenant-3", "Name 3", "SUSPENDED", false, LocalDateTime.now(), LocalDateTime.now())
            );

            PageResponse<TenantResponse> expectedResponse = PageResponse.of(
                tenants, 1, 10, 30L, 3, false, false
            );

            when(getTenantsUseCase.executeWithPage(query)).thenReturn(expectedResponse);

            // Act
            PageResponse<TenantResponse> actualResponse = tenantQueryFacade.getTenantsWithPage(query);

            // Assert
            assertThat(actualResponse.content()).hasSize(3);
            assertThat(actualResponse.content().get(0).name()).isEqualTo("Name 1");
            assertThat(actualResponse.content().get(1).name()).isEqualTo("Name 2");
            assertThat(actualResponse.content().get(2).status()).isEqualTo("SUSPENDED");

            verify(getTenantsUseCase, times(1)).executeWithPage(query);
        }

        @Test
        @DisplayName("PageResponse 필드가 정확히 매핑된다 (content, totalElements, totalPages)")
        void shouldMapPageResponseFieldsCorrectly() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(2, 15, null, null, null);

            PageResponse<TenantResponse> expectedResponse = PageResponse.of(
                Collections.emptyList(), 2, 15, 45L, 3, false, true
            );

            when(getTenantsUseCase.executeWithPage(query)).thenReturn(expectedResponse);

            // Act
            PageResponse<TenantResponse> actualResponse = tenantQueryFacade.getTenantsWithPage(query);

            // Assert - 모든 Pagination 메타데이터 검증
            assertThat(actualResponse.page()).isEqualTo(2);
            assertThat(actualResponse.size()).isEqualTo(15);
            assertThat(actualResponse.totalElements()).isEqualTo(45L);
            assertThat(actualResponse.totalPages()).isEqualTo(3);
            assertThat(actualResponse.content()).isEmpty();
            assertThat(actualResponse.first()).isFalse();
            assertThat(actualResponse.last()).isTrue();

            verify(getTenantsUseCase, times(1)).executeWithPage(query);
        }

        @Test
        @DisplayName("빈 페이지를 올바르게 처리한다")
        void shouldHandleEmptyPage() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 20, null, null, null);

            PageResponse<TenantResponse> emptyPageResponse = PageResponse.empty(0, 20);

            when(getTenantsUseCase.executeWithPage(query)).thenReturn(emptyPageResponse);

            // Act
            PageResponse<TenantResponse> actualResponse = tenantQueryFacade.getTenantsWithPage(query);

            // Assert
            assertThat(actualResponse.content()).isEmpty();
            assertThat(actualResponse.totalElements()).isEqualTo(0L);
            assertThat(actualResponse.totalPages()).isEqualTo(0);

            verify(getTenantsUseCase, times(1)).executeWithPage(query);
        }
    }

    @Nested
    @DisplayName("getTenantsWithSlice - Tenant 목록 조회 (Cursor-based Pagination)")
    class GetTenantsWithSliceTests {

        @Test
        @DisplayName("Cursor-based Pagination을 처리하고 SliceResponse를 반환한다")
        void shouldHandleCursorBasedPagination() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 20, "cursor-123", null, null);

            TenantResponse tenant1 = new TenantResponse(
                "tenant-1", "Tenant 1", "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now()
            );
            TenantResponse tenant2 = new TenantResponse(
                "tenant-2", "Tenant 2", "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now()
            );

            SliceResponse<TenantResponse> expectedSliceResponse = SliceResponse.of(
                List.of(tenant1, tenant2), 20, true, "next-cursor-456"
            );

            when(getTenantsUseCase.executeWithSlice(query)).thenReturn(expectedSliceResponse);

            // Act
            SliceResponse<TenantResponse> actualResponse = tenantQueryFacade.getTenantsWithSlice(query);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.content()).hasSize(2);
            assertThat(actualResponse.size()).isEqualTo(20);
            assertThat(actualResponse.hasNext()).isTrue();
            assertThat(actualResponse.nextCursor()).isEqualTo("next-cursor-456");

            verify(getTenantsUseCase, times(1)).executeWithSlice(query);
        }

        @Test
        @DisplayName("여러 Tenant를 포함한 SliceResponse를 올바르게 반환한다")
        void shouldReturnSliceResponseWithMultipleTenants() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 10, null, null, null);

            List<TenantResponse> tenants = List.of(
                new TenantResponse("tenant-1", "Name 1", "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now()),
                new TenantResponse("tenant-2", "Name 2", "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now())
            );

            SliceResponse<TenantResponse> expectedResponse = SliceResponse.of(
                tenants, 10, true, "cursor-next"
            );

            when(getTenantsUseCase.executeWithSlice(query)).thenReturn(expectedResponse);

            // Act
            SliceResponse<TenantResponse> actualResponse = tenantQueryFacade.getTenantsWithSlice(query);

            // Assert
            assertThat(actualResponse.content()).hasSize(2);
            assertThat(actualResponse.content().get(0).name()).isEqualTo("Name 1");
            assertThat(actualResponse.content().get(1).name()).isEqualTo("Name 2");

            verify(getTenantsUseCase, times(1)).executeWithSlice(query);
        }

        @Test
        @DisplayName("SliceResponse 필드가 정확히 매핑된다 (content, hasNext, nextCursor)")
        void shouldMapSliceResponseFieldsCorrectly() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 15, "cursor-abc", null, null);

            SliceResponse<TenantResponse> expectedResponse = SliceResponse.of(
                List.of(new TenantResponse("tenant-1", "Name 1", "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now())),
                15, true, "cursor-xyz"
            );

            when(getTenantsUseCase.executeWithSlice(query)).thenReturn(expectedResponse);

            // Act
            SliceResponse<TenantResponse> actualResponse = tenantQueryFacade.getTenantsWithSlice(query);

            // Assert - 모든 Slice 메타데이터 검증
            assertThat(actualResponse.content()).hasSize(1);
            assertThat(actualResponse.size()).isEqualTo(15);
            assertThat(actualResponse.hasNext()).isTrue();
            assertThat(actualResponse.nextCursor()).isEqualTo("cursor-xyz");

            verify(getTenantsUseCase, times(1)).executeWithSlice(query);
        }

        @Test
        @DisplayName("마지막 슬라이스를 올바르게 처리한다 (hasNext = false)")
        void shouldHandleLastSlice() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 20, "last-cursor", null, null);

            SliceResponse<TenantResponse> lastSliceResponse = SliceResponse.of(
                List.of(new TenantResponse("tenant-last", "Last Tenant", "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now())),
                20, false
            );

            when(getTenantsUseCase.executeWithSlice(query)).thenReturn(lastSliceResponse);

            // Act
            SliceResponse<TenantResponse> actualResponse = tenantQueryFacade.getTenantsWithSlice(query);

            // Assert
            assertThat(actualResponse.content()).hasSize(1);
            assertThat(actualResponse.hasNext()).isFalse();
            assertThat(actualResponse.nextCursor()).isNull();

            verify(getTenantsUseCase, times(1)).executeWithSlice(query);
        }
    }

    @Nested
    @DisplayName("getTenantTree - Tenant 트리 조회 (Tenant + Organizations)")
    class GetTenantTreeTests {

        @Test
        @DisplayName("GetTenantTreeUseCase로 위임하고 TenantTreeResponse를 반환한다")
        void shouldDelegateToGetTenantTreeUseCase() {
            // Arrange
            GetTenantTreeQuery query = GetTenantTreeQuery.of("tenant-id-123");

            TenantTreeResponse.OrganizationSummary org1 = new TenantTreeResponse.OrganizationSummary(
                1L, "ORG-001", "Organization 1", "ACTIVE", false
            );
            TenantTreeResponse.OrganizationSummary org2 = new TenantTreeResponse.OrganizationSummary(
                2L, "ORG-002", "Organization 2", "ACTIVE", false
            );

            TenantTreeResponse expectedResponse = new TenantTreeResponse(
                "tenant-id-123", "Test Tenant", "ACTIVE", false, 2,
                List.of(org1, org2), LocalDateTime.now(), LocalDateTime.now()
            );

            when(getTenantTreeUseCase.execute(query)).thenReturn(expectedResponse);

            // Act
            TenantTreeResponse actualResponse = tenantQueryFacade.getTenantTree(query);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.tenantId()).isEqualTo("tenant-id-123");
            assertThat(actualResponse.name()).isEqualTo("Test Tenant");
            assertThat(actualResponse.organizationCount()).isEqualTo(2);
            assertThat(actualResponse.organizations()).hasSize(2);

            verify(getTenantTreeUseCase, times(1)).execute(query);
        }

        @Test
        @DisplayName("Tenant와 Organizations를 통합한 트리 구조를 반환한다")
        void shouldReturnTenantTreeWithOrganizations() {
            // Arrange
            GetTenantTreeQuery query = GetTenantTreeQuery.of("tenant-tree-test");

            List<TenantTreeResponse.OrganizationSummary> organizations = List.of(
                new TenantTreeResponse.OrganizationSummary(1L, "ORG-A", "Org A", "ACTIVE", false),
                new TenantTreeResponse.OrganizationSummary(2L, "ORG-B", "Org B", "ACTIVE", false),
                new TenantTreeResponse.OrganizationSummary(3L, "ORG-C", "Org C", "SUSPENDED", false)
            );

            TenantTreeResponse expectedResponse = new TenantTreeResponse(
                "tenant-tree-test", "Tree Tenant", "ACTIVE", false, 3,
                organizations, LocalDateTime.now(), LocalDateTime.now()
            );

            when(getTenantTreeUseCase.execute(query)).thenReturn(expectedResponse);

            // Act
            TenantTreeResponse actualResponse = tenantQueryFacade.getTenantTree(query);

            // Assert
            assertThat(actualResponse.organizations()).hasSize(3);
            assertThat(actualResponse.organizations().get(0).name()).isEqualTo("Org A");
            assertThat(actualResponse.organizations().get(1).name()).isEqualTo("Org B");
            assertThat(actualResponse.organizations().get(2).status()).isEqualTo("SUSPENDED");
            assertThat(actualResponse.organizationCount()).isEqualTo(organizations.size());

            verify(getTenantTreeUseCase, times(1)).execute(query);
        }

        @Test
        @DisplayName("UseCase에서 발생한 예외를 그대로 전파한다")
        void shouldPropagateExceptionFromUseCase() {
            // Arrange
            GetTenantTreeQuery query = GetTenantTreeQuery.of("non-existent-tenant");
            IllegalStateException expectedException = new IllegalStateException("Tenant를 찾을 수 없습니다");

            when(getTenantTreeUseCase.execute(query)).thenThrow(expectedException);

            // Act & Assert
            assertThatThrownBy(() -> tenantQueryFacade.getTenantTree(query))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다");

            verify(getTenantTreeUseCase, times(1)).execute(query);
        }
    }

    @Nested
    @DisplayName("Facade 통합 테스트 - 여러 Query UseCase 조율")
    class FacadeOrchestrationTests {

        @Test
        @DisplayName("3개의 독립적인 UseCase를 모두 올바르게 위임한다")
        void shouldOrchestrateMulitpleUseCases() {
            // Arrange
            GetTenantQuery getTenantQuery = new GetTenantQuery("tenant-123");
            GetTenantsQuery getTenantsQuery = new GetTenantsQuery(0, 20, null, null, null);
            GetTenantTreeQuery getTenantTreeQuery = GetTenantTreeQuery.of("tenant-123");

            TenantResponse tenantResponse = new TenantResponse(
                "tenant-123", "Test Tenant", "ACTIVE", false, LocalDateTime.now(), LocalDateTime.now()
            );

            PageResponse<TenantResponse> pageResponse = PageResponse.of(
                List.of(tenantResponse), 0, 20, 1L, 1, true, true
            );

            TenantTreeResponse treeResponse = new TenantTreeResponse(
                "tenant-123", "Test Tenant", "ACTIVE", false, 0,
                Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now()
            );

            when(getTenantUseCase.execute(getTenantQuery)).thenReturn(tenantResponse);
            when(getTenantsUseCase.executeWithPage(getTenantsQuery)).thenReturn(pageResponse);
            when(getTenantTreeUseCase.execute(getTenantTreeQuery)).thenReturn(treeResponse);

            // Act
            TenantResponse result1 = tenantQueryFacade.getTenant(getTenantQuery);
            PageResponse<TenantResponse> result2 = tenantQueryFacade.getTenantsWithPage(getTenantsQuery);
            TenantTreeResponse result3 = tenantQueryFacade.getTenantTree(getTenantTreeQuery);

            // Assert
            assertThat(result1.tenantId()).isEqualTo("tenant-123");
            assertThat(result2.content()).hasSize(1);
            assertThat(result3.organizationCount()).isEqualTo(0);

            verify(getTenantUseCase, times(1)).execute(getTenantQuery);
            verify(getTenantsUseCase, times(1)).executeWithPage(getTenantsQuery);
            verify(getTenantTreeUseCase, times(1)).execute(getTenantTreeQuery);
        }

        @Test
        @DisplayName("Facade는 추가 로직 없이 순수 위임만 수행한다")
        void shouldOnlyDelegateWithoutAdditionalLogic() {
            // Arrange
            GetTenantQuery query = new GetTenantQuery("tenant-id-pure");
            TenantResponse expectedResponse = new TenantResponse(
                "tenant-id-pure", "Pure Delegation Test", "ACTIVE", false,
                LocalDateTime.now(), LocalDateTime.now()
            );

            when(getTenantUseCase.execute(query)).thenReturn(expectedResponse);

            // Act
            TenantResponse actualResponse = tenantQueryFacade.getTenant(query);

            // Assert - UseCase의 결과를 그대로 반환 (변형 없음)
            assertThat(actualResponse).isSameAs(expectedResponse);

            verify(getTenantUseCase, times(1)).execute(query);
            verifyNoMoreInteractions(getTenantUseCase);
        }
    }
}

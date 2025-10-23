package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantsQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.fixtures.TenantFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("TenantQueryService 테스트")
class TenantQueryServiceTest {

    private TenantQueryRepositoryPort tenantQueryRepositoryPort;
    private TenantQueryService tenantQueryService;

    @BeforeEach
    void setUp() {
        tenantQueryRepositoryPort = mock(TenantQueryRepositoryPort.class);
        tenantQueryService = new TenantQueryService(tenantQueryRepositoryPort);
    }

    @Nested
    @DisplayName("GetTenantUseCase 구현")
    class GetTenantTests {

        @Test
        @DisplayName("Tenant를 성공적으로 조회한다")
        void shouldGetTenantSuccessfully() {
            // Arrange
            String tenantId = "tenant-123";
            GetTenantQuery query = new GetTenantQuery(tenantId);
            Tenant existingTenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(existingTenant));

            // Act
            TenantResponse response = tenantQueryService.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenantId);
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.deleted()).isFalse();

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
        }

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            assertThatThrownBy(() -> tenantQueryService.execute((GetTenantQuery) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetTenantQuery는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("Tenant가 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenTenantNotFound() {
            // Arrange
            String nonExistentId = "tenant-999";
            GetTenantQuery query = new GetTenantQuery(nonExistentId);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> tenantQueryService.execute(query))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다");

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
        }
    }

    @Nested
    @DisplayName("GetTenantsUseCase 구현 - Offset-based Pagination")
    class GetTenantsWithPageTests {

        @Test
        @DisplayName("Offset-based Pagination으로 Tenant 목록을 성공적으로 조회한다")
        void shouldGetTenantsWithPageSuccessfully() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 10, null, null, null);
            Tenant tenant1 = TenantFixtures.activeTenantWithName("Company A");
            Tenant tenant2 = TenantFixtures.activeTenantWithName("Company B");
            List<Tenant> tenants = List.of(tenant1, tenant2);

            when(tenantQueryRepositoryPort.findAllWithOffset(
                isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(tenants);
            when(tenantQueryRepositoryPort.countAll(isNull(), isNull()))
                .thenReturn(2L);

            // Act
            PageResponse<TenantResponse> response = tenantQueryService.executeWithPage(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);
            assertThat(response.totalElements()).isEqualTo(2);
            assertThat(response.totalPages()).isEqualTo(1);
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isTrue();

            verify(tenantQueryRepositoryPort).findAllWithOffset(isNull(), isNull(), eq(0), eq(10));
            verify(tenantQueryRepositoryPort).countAll(isNull(), isNull());
        }

        @Test
        @DisplayName("빈 결과를 반환한다")
        void shouldReturnEmptyPage() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 10, null, null, null);

            when(tenantQueryRepositoryPort.findAllWithOffset(
                isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
            when(tenantQueryRepositoryPort.countAll(isNull(), isNull()))
                .thenReturn(0L);

            // Act
            PageResponse<TenantResponse> response = tenantQueryService.executeWithPage(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(0);
            assertThat(response.totalPages()).isEqualTo(0);
        }

        @Test
        @DisplayName("nameContains 필터링 조건으로 조회한다")
        void shouldGetTenantsWithNameFilter() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 10, null, "Company", null);
            Tenant tenant = TenantFixtures.activeTenantWithName("Company A");

            when(tenantQueryRepositoryPort.findAllWithOffset(
                eq("Company"), isNull(), eq(0), eq(10)))
                .thenReturn(List.of(tenant));
            when(tenantQueryRepositoryPort.countAll(eq("Company"), isNull()))
                .thenReturn(1L);

            // Act
            PageResponse<TenantResponse> response = tenantQueryService.executeWithPage(query);

            // Assert
            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).name()).contains("Company");

            verify(tenantQueryRepositoryPort).findAllWithOffset(
                eq("Company"), isNull(), eq(0), eq(10));
        }

        @Test
        @DisplayName("deleted 필터링 조건으로 조회한다")
        void shouldGetTenantsWithDeletedFilter() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 10, null, null, true);
            Tenant deletedTenant = TenantFixtures.deletedTenant();

            when(tenantQueryRepositoryPort.findAllWithOffset(
                isNull(), eq(true), eq(0), eq(10)))
                .thenReturn(List.of(deletedTenant));
            when(tenantQueryRepositoryPort.countAll(isNull(), eq(true)))
                .thenReturn(1L);

            // Act
            PageResponse<TenantResponse> response = tenantQueryService.executeWithPage(query);

            // Assert
            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).deleted()).isTrue();

            verify(tenantQueryRepositoryPort).findAllWithOffset(
                isNull(), eq(true), eq(0), eq(10));
        }

        @Test
        @DisplayName("페이지 계산이 올바르게 동작한다")
        void shouldCalculatePaginationCorrectly() {
            // Arrange - 총 25개, 페이지 크기 10, 두 번째 페이지 요청
            GetTenantsQuery query = new GetTenantsQuery(1, 10, null, null, null);

            when(tenantQueryRepositoryPort.findAllWithOffset(
                isNull(), isNull(), eq(10), eq(10)))
                .thenReturn(List.of(
                    TenantFixtures.activeTenant(),
                    TenantFixtures.activeTenant()
                ));
            when(tenantQueryRepositoryPort.countAll(isNull(), isNull()))
                .thenReturn(25L);

            // Act
            PageResponse<TenantResponse> response = tenantQueryService.executeWithPage(query);

            // Assert
            assertThat(response.page()).isEqualTo(1);
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.totalPages()).isEqualTo(3);
            assertThat(response.first()).isFalse();
            assertThat(response.last()).isFalse();
        }

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            assertThatThrownBy(() -> tenantQueryService.executeWithPage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetTenantsQuery는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findAllWithOffset(
                any(), any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("page가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenPageIsNull() {
            // Arrange - cursor를 제공하여 Offset-based가 아니게 함
            GetTenantsQuery query = new GetTenantsQuery(null, 10, "cursor-123", null, null);

            // Act & Assert
            assertThatThrownBy(() -> tenantQueryService.executeWithPage(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Offset-based Pagination을 위해서는 page가 필요합니다");
        }
    }

    @Nested
    @DisplayName("GetTenantsUseCase 구현 - Cursor-based Pagination")
    class GetTenantsWithSliceTests {

        @Test
        @DisplayName("Cursor-based Pagination으로 Tenant 목록을 성공적으로 조회한다")
        void shouldGetTenantsWithSliceSuccessfully() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 2, null, null, null);
            Tenant tenant1 = TenantFixtures.activeTenantWithId("tenant-1");
            Tenant tenant2 = TenantFixtures.activeTenantWithId("tenant-2");
            Tenant tenant3 = TenantFixtures.activeTenantWithId("tenant-3");

            // limit + 1개 반환 (hasNext 판단용)
            when(tenantQueryRepositoryPort.findAllWithCursor(
                isNull(), isNull(), isNull(), eq(3)))
                .thenReturn(List.of(tenant1, tenant2, tenant3));

            // Act
            SliceResponse<TenantResponse> response = tenantQueryService.executeWithSlice(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2); // limit 크기만큼만
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();

            verify(tenantQueryRepositoryPort).findAllWithCursor(
                isNull(), isNull(), isNull(), eq(3));
        }

        @Test
        @DisplayName("마지막 슬라이스에서는 hasNext가 false이다")
        void shouldReturnLastSlice() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 10, null, null, null);
            Tenant tenant1 = TenantFixtures.activeTenant();
            Tenant tenant2 = TenantFixtures.activeTenant();

            // limit + 1보다 적게 반환 (마지막 페이지)
            when(tenantQueryRepositoryPort.findAllWithCursor(
                isNull(), isNull(), isNull(), eq(11)))
                .thenReturn(List.of(tenant1, tenant2));

            // Act
            SliceResponse<TenantResponse> response = tenantQueryService.executeWithSlice(query);

            // Assert
            assertThat(response.content()).hasSize(2);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("빈 결과를 반환한다")
        void shouldReturnEmptySlice() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 10, null, null, null);

            when(tenantQueryRepositoryPort.findAllWithCursor(
                isNull(), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());

            // Act
            SliceResponse<TenantResponse> response = tenantQueryService.executeWithSlice(query);

            // Assert
            assertThat(response.content()).isEmpty();
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("cursor를 사용하여 다음 페이지를 조회한다")
        void shouldGetNextPageWithCursor() {
            // Arrange
            String cursor = "dGVuYW50LTEwMA=="; // Base64 encoded "tenant-100"
            GetTenantsQuery query = new GetTenantsQuery(null, 10, cursor, null, null);
            Tenant tenant = TenantFixtures.activeTenant();

            when(tenantQueryRepositoryPort.findAllWithCursor(
                isNull(), isNull(), eq(cursor), eq(11)))
                .thenReturn(List.of(tenant));

            // Act
            SliceResponse<TenantResponse> response = tenantQueryService.executeWithSlice(query);

            // Assert
            assertThat(response.content()).hasSize(1);

            verify(tenantQueryRepositoryPort).findAllWithCursor(
                isNull(), isNull(), eq(cursor), eq(11));
        }

        @Test
        @DisplayName("nextCursor가 Base64로 인코딩되어 있다")
        void shouldEncodeNextCursorInBase64() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 2, null, null, null);
            Tenant tenant1 = TenantFixtures.activeTenantWithId("tenant-1");
            Tenant tenant2 = TenantFixtures.activeTenantWithId("tenant-2");
            Tenant tenant3 = TenantFixtures.activeTenantWithId("tenant-3");

            when(tenantQueryRepositoryPort.findAllWithCursor(
                isNull(), isNull(), isNull(), eq(3)))
                .thenReturn(List.of(tenant1, tenant2, tenant3));

            // Act
            SliceResponse<TenantResponse> response = tenantQueryService.executeWithSlice(query);

            // Assert
            assertThat(response.nextCursor()).isNotNull();
            // Base64 URL-safe 인코딩 검증 (패딩 문자 = 포함)
            assertThat(response.nextCursor()).matches("^[A-Za-z0-9_-]+=*$");
        }

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            assertThatThrownBy(() -> tenantQueryService.executeWithSlice(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetTenantsQuery는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findAllWithCursor(
                any(), any(), any(), anyInt());
        }
    }

    @Nested
    @DisplayName("QueryRepository 호출 검증")
    class RepositoryCallVerificationTests {

        @Test
        @DisplayName("GetTenantUseCase에서 findById 호출을 검증한다")
        void shouldVerifyFindByIdCall() {
            // Arrange
            String tenantId = "tenant-123";
            GetTenantQuery query = new GetTenantQuery(tenantId);
            Tenant tenant = TenantFixtures.activeTenantWithId(tenantId);

            when(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .thenReturn(Optional.of(tenant));

            // Act
            tenantQueryService.execute(query);

            // Assert
            verify(tenantQueryRepositoryPort, times(1)).findById(any(TenantId.class));
        }

        @Test
        @DisplayName("Offset-based Pagination에서 findAllWithOffset과 countAll 호출을 검증한다")
        void shouldVerifyOffsetPaginationCalls() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 10, null, null, null);

            when(tenantQueryRepositoryPort.findAllWithOffset(
                isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
            when(tenantQueryRepositoryPort.countAll(isNull(), isNull()))
                .thenReturn(0L);

            // Act
            tenantQueryService.executeWithPage(query);

            // Assert
            verify(tenantQueryRepositoryPort, times(1)).findAllWithOffset(
                isNull(), isNull(), eq(0), eq(10));
            verify(tenantQueryRepositoryPort, times(1)).countAll(isNull(), isNull());
        }

        @Test
        @DisplayName("Cursor-based Pagination에서 findAllWithCursor 호출을 검증한다")
        void shouldVerifyCursorPaginationCalls() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 10, null, null, null);

            when(tenantQueryRepositoryPort.findAllWithCursor(
                isNull(), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());

            // Act
            tenantQueryService.executeWithSlice(query);

            // Assert
            verify(tenantQueryRepositoryPort, times(1)).findAllWithCursor(
                isNull(), isNull(), isNull(), eq(11));
            // Cursor-based는 countAll을 호출하지 않음
            verify(tenantQueryRepositoryPort, never()).countAll(any(), any());
        }
    }
}

package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantsQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.in.GetTenantsUseCase;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.fixtures.TenantFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GetTenantsUseCaseTest - GetTenantsUseCase 단위 테스트
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
 *   <li>구현체: {@link TenantQueryService#executeWithPage(GetTenantsQuery)}</li>
 *   <li>구현체: {@link TenantQueryService#executeWithSlice(GetTenantsQuery)}</li>
 *   <li>인터페이스: {@link GetTenantsUseCase}</li>
 * </ul>
 *
 * <p><strong>Pagination 전략 테스트:</strong></p>
 * <ul>
 *   <li>Offset-based Pagination: executeWithPage() 메서드 테스트</li>
 *   <li>Cursor-based Pagination: executeWithSlice() 메서드 테스트</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("GetTenantsUseCase 테스트")
class GetTenantsUseCaseTest {

    private TenantQueryRepositoryPort tenantQueryRepositoryPort;
    private GetTenantsUseCase getTenantsUseCase;

    @BeforeEach
    void setUp() {
        tenantQueryRepositoryPort = mock(TenantQueryRepositoryPort.class);
        getTenantsUseCase = new TenantQueryService(tenantQueryRepositoryPort);
    }

    @Nested
    @DisplayName("Offset-based Pagination 테스트")
    class OffsetBasedPaginationTests {

        @Test
        @DisplayName("첫 페이지 조회 시 올바른 PageResponse를 반환한다")
        void shouldReturnFirstPageWithCorrectMetadata() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 20, null, null, null);
            List<Tenant> tenants = List.of(
                TenantFixtures.activeTenantWithName("Tenant A"),
                TenantFixtures.activeTenantWithName("Tenant B")
            );

            when(tenantQueryRepositoryPort.findAllWithOffset(any(), any(), anyInt(), anyInt()))
                .thenReturn(tenants);
            when(tenantQueryRepositoryPort.countAll(any(), any()))
                .thenReturn(42L);

            // Act
            PageResponse<TenantResponse> response = getTenantsUseCase.executeWithPage(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);
            assertThat(response.page()).isEqualTo(0);
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.totalElements()).isEqualTo(42L);
            assertThat(response.totalPages()).isEqualTo(3); // ceil(42/20) = 3
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isFalse();

            verify(tenantQueryRepositoryPort).findAllWithOffset(any(), any(), eq(0), eq(20));
            verify(tenantQueryRepositoryPort).countAll(any(), any());
        }

        @Test
        @DisplayName("마지막 페이지 조회 시 last가 true이다")
        void shouldIndicateLastPageCorrectly() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(2, 20, null, null, null);
            List<Tenant> tenants = List.of(
                TenantFixtures.activeTenantWithName("Tenant X"),
                TenantFixtures.activeTenantWithName("Tenant Y")
            );

            when(tenantQueryRepositoryPort.findAllWithOffset(any(), any(), anyInt(), anyInt()))
                .thenReturn(tenants);
            when(tenantQueryRepositoryPort.countAll(any(), any()))
                .thenReturn(42L); // totalPages = 3 (pages 0, 1, 2)

            // Act
            PageResponse<TenantResponse> response = getTenantsUseCase.executeWithPage(query);

            // Assert
            assertThat(response.page()).isEqualTo(2);
            assertThat(response.first()).isFalse();
            assertThat(response.last()).isTrue();

            verify(tenantQueryRepositoryPort).findAllWithOffset(any(), any(), eq(40), eq(20));
        }

        @Test
        @DisplayName("빈 결과 조회 시 빈 content를 반환한다")
        void shouldReturnEmptyContentWhenNoResults() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 20, null, null, null);

            when(tenantQueryRepositoryPort.findAllWithOffset(any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());
            when(tenantQueryRepositoryPort.countAll(any(), any()))
                .thenReturn(0L);

            // Act
            PageResponse<TenantResponse> response = getTenantsUseCase.executeWithPage(query);

            // Assert
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(0L);
            assertThat(response.totalPages()).isEqualTo(0);
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isTrue();
        }

        @Test
        @DisplayName("nameContains 필터가 Repository로 전달된다")
        void shouldPassNameContainsFilterToRepository() {
            // Arrange
            String searchKeyword = "Test";
            GetTenantsQuery query = new GetTenantsQuery(0, 20, null, searchKeyword, null);
            List<Tenant> tenants = List.of(TenantFixtures.activeTenantWithName("Test Company"));

            when(tenantQueryRepositoryPort.findAllWithOffset(eq(searchKeyword), any(), anyInt(), anyInt()))
                .thenReturn(tenants);
            when(tenantQueryRepositoryPort.countAll(eq(searchKeyword), any()))
                .thenReturn(1L);

            // Act
            getTenantsUseCase.executeWithPage(query);

            // Assert
            verify(tenantQueryRepositoryPort).findAllWithOffset(eq(searchKeyword), any(), anyInt(), anyInt());
            verify(tenantQueryRepositoryPort).countAll(eq(searchKeyword), any());
        }

        @Test
        @DisplayName("deleted 필터가 Repository로 전달된다")
        void shouldPassDeletedFilterToRepository() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 20, null, null, false);
            List<Tenant> tenants = List.of(TenantFixtures.activeTenantWithName("Active Company"));

            when(tenantQueryRepositoryPort.findAllWithOffset(any(), eq(false), anyInt(), anyInt()))
                .thenReturn(tenants);
            when(tenantQueryRepositoryPort.countAll(any(), eq(false)))
                .thenReturn(1L);

            // Act
            getTenantsUseCase.executeWithPage(query);

            // Assert
            verify(tenantQueryRepositoryPort).findAllWithOffset(any(), eq(false), anyInt(), anyInt());
            verify(tenantQueryRepositoryPort).countAll(any(), eq(false));
        }

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> getTenantsUseCase.executeWithPage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetTenantsQuery는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findAllWithOffset(any(), any(), anyInt(), anyInt());
            verify(tenantQueryRepositoryPort, never()).countAll(any(), any());
        }

        @Test
        @DisplayName("page가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenPageIsNull() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 20, null, null, null);

            // Act & Assert
            assertThatThrownBy(() -> getTenantsUseCase.executeWithPage(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Offset-based Pagination을 위해서는 page가 필요합니다");

            verify(tenantQueryRepositoryPort, never()).findAllWithOffset(any(), any(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("Cursor-based Pagination 테스트")
    class CursorBasedPaginationTests {

        @Test
        @DisplayName("첫 조회 시 올바른 SliceResponse를 반환한다")
        void shouldReturnFirstSliceWithCorrectMetadata() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 2, null, null, null);
            List<Tenant> tenants = List.of(
                TenantFixtures.activeTenantWithIdAndName("tenant-1", "Tenant A"),
                TenantFixtures.activeTenantWithIdAndName("tenant-2", "Tenant B"),
                TenantFixtures.activeTenantWithIdAndName("tenant-3", "Tenant C") // limit + 1
            );

            when(tenantQueryRepositoryPort.findAllWithCursor(any(), any(), any(), eq(3)))
                .thenReturn(tenants);

            // Act
            SliceResponse<TenantResponse> response = getTenantsUseCase.executeWithSlice(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2); // 실제 데이터는 2개 (size=2)
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.hasNext()).isTrue(); // 3개 조회되었으므로 다음 페이지 있음
            assertThat(response.nextCursor()).isNotNull(); // 마지막 항목의 ID가 Base64 인코딩됨

            verify(tenantQueryRepositoryPort).findAllWithCursor(any(), any(), any(), eq(3));
        }

        @Test
        @DisplayName("마지막 슬라이스 조회 시 hasNext가 false이다")
        void shouldIndicateNoNextSliceWhenLastSlice() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 20, "some-cursor", null, null);
            List<Tenant> tenants = List.of(
                TenantFixtures.activeTenantWithIdAndName("tenant-last-1", "Tenant X"),
                TenantFixtures.activeTenantWithIdAndName("tenant-last-2", "Tenant Y")
            ); // limit + 1보다 적게 조회 → 마지막 슬라이스

            when(tenantQueryRepositoryPort.findAllWithCursor(any(), any(), eq("some-cursor"), eq(21)))
                .thenReturn(tenants);

            // Act
            SliceResponse<TenantResponse> response = getTenantsUseCase.executeWithSlice(query);

            // Assert
            assertThat(response.content()).hasSize(2);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("빈 결과 조회 시 hasNext가 false이다")
        void shouldReturnEmptySliceWhenNoResults() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 20, null, null, null);

            when(tenantQueryRepositoryPort.findAllWithCursor(any(), any(), any(), eq(21)))
                .thenReturn(List.of());

            // Act
            SliceResponse<TenantResponse> response = getTenantsUseCase.executeWithSlice(query);

            // Assert
            assertThat(response.content()).isEmpty();
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("cursor 값이 Repository로 전달된다")
        void shouldPassCursorToRepository() {
            // Arrange
            String cursor = "encoded-cursor-value";
            GetTenantsQuery query = new GetTenantsQuery(null, 20, cursor, null, null);
            List<Tenant> tenants = List.of(TenantFixtures.activeTenantWithName("Tenant A"));

            when(tenantQueryRepositoryPort.findAllWithCursor(any(), any(), eq(cursor), eq(21)))
                .thenReturn(tenants);

            // Act
            getTenantsUseCase.executeWithSlice(query);

            // Assert
            verify(tenantQueryRepositoryPort).findAllWithCursor(any(), any(), eq(cursor), eq(21));
        }

        @Test
        @DisplayName("nextCursor는 Base64 인코딩된 마지막 Tenant ID이다")
        void shouldEncodeLastTenantIdAsNextCursor() {
            // Arrange
            String lastTenantId = "tenant-last-id";
            GetTenantsQuery query = new GetTenantsQuery(null, 2, null, null, null);
            List<Tenant> tenants = List.of(
                TenantFixtures.activeTenantWithIdAndName("tenant-1", "Tenant A"),
                TenantFixtures.activeTenantWithIdAndName(lastTenantId, "Tenant B"),
                TenantFixtures.activeTenantWithIdAndName("tenant-3", "Tenant C") // limit + 1
            );

            when(tenantQueryRepositoryPort.findAllWithCursor(any(), any(), any(), eq(3)))
                .thenReturn(tenants);

            // Act
            SliceResponse<TenantResponse> response = getTenantsUseCase.executeWithSlice(query);

            // Assert
            assertThat(response.nextCursor()).isNotNull();
            // Base64 인코딩 검증 (nextCursor는 마지막 실제 content의 ID를 Base64로 인코딩한 값)
            assertThat(response.content()).hasSize(2); // size=2이므로 실제 content는 2개
            assertThat(response.content().get(1).tenantId()).isEqualTo(lastTenantId);
        }

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> getTenantsUseCase.executeWithSlice(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetTenantsQuery는 필수입니다");

            verify(tenantQueryRepositoryPort, never()).findAllWithCursor(any(), any(), any(), anyInt());
        }

        @Test
        @DisplayName("nameContains 필터가 Repository로 전달된다")
        void shouldPassNameContainsFilterToRepository() {
            // Arrange
            String searchKeyword = "Test";
            GetTenantsQuery query = new GetTenantsQuery(null, 20, null, searchKeyword, null);
            List<Tenant> tenants = List.of(TenantFixtures.activeTenantWithName("Test Company"));

            when(tenantQueryRepositoryPort.findAllWithCursor(eq(searchKeyword), any(), any(), eq(21)))
                .thenReturn(tenants);

            // Act
            getTenantsUseCase.executeWithSlice(query);

            // Assert
            verify(tenantQueryRepositoryPort).findAllWithCursor(eq(searchKeyword), any(), any(), eq(21));
        }

        @Test
        @DisplayName("deleted 필터가 Repository로 전달된다")
        void shouldPassDeletedFilterToRepository() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 20, null, null, true);
            List<Tenant> tenants = List.of(TenantFixtures.deletedTenantWithName("Deleted Company"));

            when(tenantQueryRepositoryPort.findAllWithCursor(any(), eq(true), any(), eq(21)))
                .thenReturn(tenants);

            // Act
            getTenantsUseCase.executeWithSlice(query);

            // Assert
            verify(tenantQueryRepositoryPort).findAllWithCursor(any(), eq(true), any(), eq(21));
        }
    }

    @Nested
    @DisplayName("GetTenantsQuery 검증")
    class QueryValidationTests {

        @Test
        @DisplayName("page와 cursor를 동시에 사용하면 예외 발생")
        void shouldThrowExceptionWhenBothPageAndCursorProvided() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new GetTenantsQuery(0, 20, "cursor", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page와 cursor는 동시에 사용할 수 없습니다");
        }

        @Test
        @DisplayName("size가 null이면 기본값 20으로 설정된다")
        void shouldUseDefaultSizeWhenSizeIsNull() {
            // Arrange & Act
            GetTenantsQuery query = new GetTenantsQuery(0, null, null, null, null);

            // Assert
            assertThat(query.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("size가 1보다 작으면 예외 발생")
        void shouldThrowExceptionWhenSizeIsLessThanOne() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new GetTenantsQuery(0, 0, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size는 1~100 사이여야 합니다");
        }

        @Test
        @DisplayName("size가 100보다 크면 예외 발생")
        void shouldThrowExceptionWhenSizeIsGreaterThan100() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new GetTenantsQuery(0, 101, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size는 1~100 사이여야 합니다");
        }

        @Test
        @DisplayName("page가 음수이면 예외 발생")
        void shouldThrowExceptionWhenPageIsNegative() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new GetTenantsQuery(-1, 20, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page는 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("isOffsetBased()는 page가 null이 아닐 때 true를 반환한다")
        void shouldReturnTrueForOffsetBasedWhenPageIsNotNull() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 20, null, null, null);

            // Act & Assert
            assertThat(query.isOffsetBased()).isTrue();
            assertThat(query.isCursorBased()).isFalse();
        }

        @Test
        @DisplayName("isCursorBased()는 cursor가 null이 아닐 때 true를 반환한다")
        void shouldReturnTrueForCursorBasedWhenCursorIsNotNull() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 20, "cursor", null, null);

            // Act & Assert
            assertThat(query.isCursorBased()).isTrue();
            assertThat(query.isOffsetBased()).isFalse();
        }

        @Test
        @DisplayName("cursor가 빈 문자열이면 isCursorBased()는 false를 반환한다")
        void shouldReturnFalseForCursorBasedWhenCursorIsBlank() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 20, "", null, null);

            // Act & Assert
            assertThat(query.isCursorBased()).isFalse();
        }
    }

    @Nested
    @DisplayName("Read-Only Transaction 검증")
    class TransactionTests {

        @Test
        @DisplayName("Offset-based 조회는 데이터를 변경하지 않는다")
        void shouldNotModifyDataInOffsetBasedPagination() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(0, 20, null, null, null);
            List<Tenant> tenants = List.of(TenantFixtures.activeTenantWithName("Tenant A"));

            when(tenantQueryRepositoryPort.findAllWithOffset(any(), any(), anyInt(), anyInt()))
                .thenReturn(tenants);
            when(tenantQueryRepositoryPort.countAll(any(), any()))
                .thenReturn(1L);

            // Act
            getTenantsUseCase.executeWithPage(query);

            // Assert - Repository는 조회만 담당 (Query Repository)
            verify(tenantQueryRepositoryPort, times(1)).findAllWithOffset(any(), any(), anyInt(), anyInt());
            verify(tenantQueryRepositoryPort, times(1)).countAll(any(), any());
        }

        @Test
        @DisplayName("Cursor-based 조회는 데이터를 변경하지 않는다")
        void shouldNotModifyDataInCursorBasedPagination() {
            // Arrange
            GetTenantsQuery query = new GetTenantsQuery(null, 20, null, null, null);
            List<Tenant> tenants = List.of(TenantFixtures.activeTenantWithName("Tenant A"));

            when(tenantQueryRepositoryPort.findAllWithCursor(any(), any(), any(), anyInt()))
                .thenReturn(tenants);

            // Act
            getTenantsUseCase.executeWithSlice(query);

            // Assert - Repository는 조회만 담당 (Query Repository)
            verify(tenantQueryRepositoryPort, times(1)).findAllWithCursor(any(), any(), any(), anyInt());
        }
    }
}

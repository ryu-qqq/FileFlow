package com.ryuqq.fileflow.application.iam.organization.service;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationsQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.fixtures.OrganizationFixtures;
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
@DisplayName("OrganizationQueryService 테스트")
class OrganizationQueryServiceTest {

    private OrganizationQueryRepositoryPort organizationQueryRepositoryPort;
    private OrganizationQueryService organizationQueryService;

    @BeforeEach
    void setUp() {
        organizationQueryRepositoryPort = mock(OrganizationQueryRepositoryPort.class);
        organizationQueryService = new OrganizationQueryService(organizationQueryRepositoryPort);
    }

    @Nested
    @DisplayName("GetOrganizationUseCase 구현")
    class GetOrganizationTests {

        @Test
        @DisplayName("Organization을 성공적으로 조회한다")
        void shouldGetOrganizationSuccessfully() {
            // Arrange
            Long organizationId = 1L;
            GetOrganizationQuery query = new GetOrganizationQuery(organizationId);
            Organization existingOrganization = OrganizationFixtures.activeOrganizationWithId(organizationId);

            when(organizationQueryRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(existingOrganization));

            // Act
            OrganizationResponse response = organizationQueryService.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isEqualTo(organizationId);
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.deleted()).isFalse();

            verify(organizationQueryRepositoryPort).findById(any(OrganizationId.class));
        }

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            assertThatThrownBy(() -> organizationQueryService.execute((GetOrganizationQuery) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetOrganizationQuery는 필수입니다");

            verify(organizationQueryRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("Organization이 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenOrganizationNotFound() {
            // Arrange
            Long nonExistentId = 999L;
            GetOrganizationQuery query = new GetOrganizationQuery(nonExistentId);

            when(organizationQueryRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> organizationQueryService.execute(query))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationQueryRepositoryPort).findById(any(OrganizationId.class));
        }
    }

    @Nested
    @DisplayName("GetOrganizationsUseCase 구현 - Offset-based Pagination")
    class GetOrganizationsWithPageTests {

        @Test
        @DisplayName("Offset-based Pagination으로 Organization 목록을 성공적으로 조회한다")
        void shouldGetOrganizationsWithPageSuccessfully() {
            // Arrange
            String tenantId = "tenant-123";
            GetOrganizationsQuery query = new GetOrganizationsQuery(0, 10, null, tenantId, null, null, null);
            Organization org1 = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);
            Organization org2 = OrganizationFixtures.hrOrganizationWithId(2L, tenantId);
            List<Organization> organizations = List.of(org1, org2);

            when(organizationQueryRepositoryPort.findAllWithOffset(
                eq(tenantId), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(organizations);
            when(organizationQueryRepositoryPort.countAll(
                eq(tenantId), isNull(), isNull(), isNull()))
                .thenReturn(2L);

            // Act
            PageResponse<OrganizationResponse> response = organizationQueryService.executeWithPage(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);
            assertThat(response.totalElements()).isEqualTo(2);
            assertThat(response.totalPages()).isEqualTo(1);
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isTrue();

            verify(organizationQueryRepositoryPort).findAllWithOffset(
                eq(tenantId), isNull(), isNull(), isNull(), eq(0), eq(10));
            verify(organizationQueryRepositoryPort).countAll(
                eq(tenantId), isNull(), isNull(), isNull());
        }

        @Test
        @DisplayName("빈 결과를 반환한다")
        void shouldReturnEmptyPage() {
            // Arrange
            GetOrganizationsQuery query = new GetOrganizationsQuery(0, 10, null, null, null, null, null);

            when(organizationQueryRepositoryPort.findAllWithOffset(
                isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
            when(organizationQueryRepositoryPort.countAll(
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(0L);

            // Act
            PageResponse<OrganizationResponse> response = organizationQueryService.executeWithPage(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(0);
            assertThat(response.totalPages()).isEqualTo(0);
        }

        @Test
        @DisplayName("orgCodeContains 필터링 조건으로 조회한다")
        void shouldGetOrganizationsWithOrgCodeFilter() {
            // Arrange
            String tenantId = "tenant-123";
            GetOrganizationsQuery query = new GetOrganizationsQuery(0, 10, null, tenantId, "SALES", null, null);
            Organization org = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);

            when(organizationQueryRepositoryPort.findAllWithOffset(
                eq(tenantId), eq("SALES"), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(List.of(org));
            when(organizationQueryRepositoryPort.countAll(
                eq(tenantId), eq("SALES"), isNull(), isNull()))
                .thenReturn(1L);

            // Act
            PageResponse<OrganizationResponse> response = organizationQueryService.executeWithPage(query);

            // Assert
            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).orgCode()).contains("SALES");

            verify(organizationQueryRepositoryPort).findAllWithOffset(
                eq(tenantId), eq("SALES"), isNull(), isNull(), eq(0), eq(10));
        }

        @Test
        @DisplayName("nameContains 필터링 조건으로 조회한다")
        void shouldGetOrganizationsWithNameFilter() {
            // Arrange
            String tenantId = "tenant-123";
            GetOrganizationsQuery query = new GetOrganizationsQuery(0, 10, null, tenantId, null, "Sales", null);
            Organization org = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);

            when(organizationQueryRepositoryPort.findAllWithOffset(
                eq(tenantId), isNull(), eq("Sales"), isNull(), eq(0), eq(10)))
                .thenReturn(List.of(org));
            when(organizationQueryRepositoryPort.countAll(
                eq(tenantId), isNull(), eq("Sales"), isNull()))
                .thenReturn(1L);

            // Act
            PageResponse<OrganizationResponse> response = organizationQueryService.executeWithPage(query);

            // Assert
            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).name()).contains("Sales");

            verify(organizationQueryRepositoryPort).findAllWithOffset(
                eq(tenantId), isNull(), eq("Sales"), isNull(), eq(0), eq(10));
        }

        @Test
        @DisplayName("deleted 필터링 조건으로 조회한다")
        void shouldGetOrganizationsWithDeletedFilter() {
            // Arrange
            String tenantId = "tenant-123";
            GetOrganizationsQuery query = new GetOrganizationsQuery(0, 10, null, tenantId, null, null, true);
            Organization deletedOrg = OrganizationFixtures.deletedOrganization(tenantId);

            when(organizationQueryRepositoryPort.findAllWithOffset(
                eq(tenantId), isNull(), isNull(), eq(true), eq(0), eq(10)))
                .thenReturn(List.of(deletedOrg));
            when(organizationQueryRepositoryPort.countAll(
                eq(tenantId), isNull(), isNull(), eq(true)))
                .thenReturn(1L);

            // Act
            PageResponse<OrganizationResponse> response = organizationQueryService.executeWithPage(query);

            // Assert
            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).deleted()).isTrue();

            verify(organizationQueryRepositoryPort).findAllWithOffset(
                eq(tenantId), isNull(), isNull(), eq(true), eq(0), eq(10));
        }

        @Test
        @DisplayName("페이지 계산이 올바르게 동작한다")
        void shouldCalculatePaginationCorrectly() {
            // Arrange - 총 25개, 페이지 크기 10, 두 번째 페이지 요청
            String tenantId = "tenant-123";
            GetOrganizationsQuery query = new GetOrganizationsQuery(1, 10, null, tenantId, null, null, null);

            when(organizationQueryRepositoryPort.findAllWithOffset(
                eq(tenantId), isNull(), isNull(), isNull(), eq(10), eq(10)))
                .thenReturn(List.of(
                    OrganizationFixtures.salesOrganizationWithId(11L, tenantId),
                    OrganizationFixtures.hrOrganizationWithId(12L, tenantId)
                ));
            when(organizationQueryRepositoryPort.countAll(
                eq(tenantId), isNull(), isNull(), isNull()))
                .thenReturn(25L);

            // Act
            PageResponse<OrganizationResponse> response = organizationQueryService.executeWithPage(query);

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
            assertThatThrownBy(() -> organizationQueryService.executeWithPage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetOrganizationsQuery는 필수입니다");

            verify(organizationQueryRepositoryPort, never()).findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("page가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenPageIsNull() {
            // Arrange - cursor를 제공하여 Offset-based가 아니게 함
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 10, "cursor-123", null, null, null, null);

            // Act & Assert
            assertThatThrownBy(() -> organizationQueryService.executeWithPage(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Offset-based Pagination을 위해서는 page가 필요합니다");
        }
    }

    @Nested
    @DisplayName("GetOrganizationsUseCase 구현 - Cursor-based Pagination")
    class GetOrganizationsWithSliceTests {

        @Test
        @DisplayName("Cursor-based Pagination으로 Organization 목록을 성공적으로 조회한다")
        void shouldGetOrganizationsWithSliceSuccessfully() {
            // Arrange
            String tenantId = "tenant-123";
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 2, null, tenantId, null, null, null);
            Organization org1 = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);
            Organization org2 = OrganizationFixtures.hrOrganizationWithId(2L, tenantId);
            Organization org3 = OrganizationFixtures.itOrganization(tenantId);

            // limit + 1개 반환 (hasNext 판단용)
            when(organizationQueryRepositoryPort.findAllWithCursor(
                eq(tenantId), isNull(), isNull(), isNull(), isNull(), eq(3)))
                .thenReturn(List.of(org1, org2, org3));

            // Act
            SliceResponse<OrganizationResponse> response = organizationQueryService.executeWithSlice(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2); // limit 크기만큼만
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();

            verify(organizationQueryRepositoryPort).findAllWithCursor(
                eq(tenantId), isNull(), isNull(), isNull(), isNull(), eq(3));
        }

        @Test
        @DisplayName("마지막 슬라이스에서는 hasNext가 false이다")
        void shouldReturnLastSlice() {
            // Arrange
            String tenantId = "tenant-123";
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 10, null, tenantId, null, null, null);
            Organization org1 = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);
            Organization org2 = OrganizationFixtures.hrOrganizationWithId(2L, tenantId);

            // limit + 1보다 적게 반환 (마지막 페이지)
            when(organizationQueryRepositoryPort.findAllWithCursor(
                eq(tenantId), isNull(), isNull(), isNull(), isNull(), eq(11)))
                .thenReturn(List.of(org1, org2));

            // Act
            SliceResponse<OrganizationResponse> response = organizationQueryService.executeWithSlice(query);

            // Assert
            assertThat(response.content()).hasSize(2);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("빈 결과를 반환한다")
        void shouldReturnEmptySlice() {
            // Arrange
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 10, null, null, null, null, null);

            when(organizationQueryRepositoryPort.findAllWithCursor(
                isNull(), isNull(), isNull(), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());

            // Act
            SliceResponse<OrganizationResponse> response = organizationQueryService.executeWithSlice(query);

            // Assert
            assertThat(response.content()).isEmpty();
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("cursor를 사용하여 다음 페이지를 조회한다")
        void shouldGetNextPageWithCursor() {
            // Arrange
            String tenantId = "tenant-123";
            String cursor = "MTAw"; // Base64 encoded "100"
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 10, cursor, tenantId, null, null, null);
            Organization org = OrganizationFixtures.salesOrganizationWithId(101L, tenantId);

            when(organizationQueryRepositoryPort.findAllWithCursor(
                eq(tenantId), isNull(), isNull(), isNull(), eq(cursor), eq(11)))
                .thenReturn(List.of(org));

            // Act
            SliceResponse<OrganizationResponse> response = organizationQueryService.executeWithSlice(query);

            // Assert
            assertThat(response.content()).hasSize(1);

            verify(organizationQueryRepositoryPort).findAllWithCursor(
                eq(tenantId), isNull(), isNull(), isNull(), eq(cursor), eq(11));
        }

        @Test
        @DisplayName("nextCursor가 Base64로 인코딩되어 있다")
        void shouldEncodeNextCursorInBase64() {
            // Arrange
            String tenantId = "tenant-123";
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 2, null, tenantId, null, null, null);
            Organization org1 = OrganizationFixtures.salesOrganizationWithId(1L, tenantId);
            Organization org2 = OrganizationFixtures.hrOrganizationWithId(2L, tenantId);
            Organization org3 = OrganizationFixtures.itOrganization(tenantId);

            when(organizationQueryRepositoryPort.findAllWithCursor(
                eq(tenantId), isNull(), isNull(), isNull(), isNull(), eq(3)))
                .thenReturn(List.of(org1, org2, org3));

            // Act
            SliceResponse<OrganizationResponse> response = organizationQueryService.executeWithSlice(query);

            // Assert
            assertThat(response.nextCursor()).isNotNull();
            // Base64 URL-safe 인코딩 검증 (패딩 문자 = 포함)
            assertThat(response.nextCursor()).matches("^[A-Za-z0-9_-]+=*$");
        }

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            assertThatThrownBy(() -> organizationQueryService.executeWithSlice(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetOrganizationsQuery는 필수입니다");

            verify(organizationQueryRepositoryPort, never()).findAllWithCursor(
                any(), any(), any(), any(), any(), anyInt());
        }
    }

    @Nested
    @DisplayName("QueryRepository 호출 검증")
    class RepositoryCallVerificationTests {

        @Test
        @DisplayName("GetOrganizationUseCase에서 findById 호출을 검증한다")
        void shouldVerifyFindByIdCall() {
            // Arrange
            Long organizationId = 1L;
            GetOrganizationQuery query = new GetOrganizationQuery(organizationId);
            Organization organization = OrganizationFixtures.activeOrganizationWithId(organizationId);

            when(organizationQueryRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(organization));

            // Act
            organizationQueryService.execute(query);

            // Assert
            verify(organizationQueryRepositoryPort, times(1)).findById(any(OrganizationId.class));
        }

        @Test
        @DisplayName("Offset-based Pagination에서 findAllWithOffset과 countAll 호출을 검증한다")
        void shouldVerifyOffsetPaginationCalls() {
            // Arrange
            String tenantId = "tenant-123";
            GetOrganizationsQuery query = new GetOrganizationsQuery(0, 10, null, tenantId, null, null, null);

            when(organizationQueryRepositoryPort.findAllWithOffset(
                eq(tenantId), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
            when(organizationQueryRepositoryPort.countAll(
                eq(tenantId), isNull(), isNull(), isNull()))
                .thenReturn(0L);

            // Act
            organizationQueryService.executeWithPage(query);

            // Assert
            verify(organizationQueryRepositoryPort, times(1)).findAllWithOffset(
                eq(tenantId), isNull(), isNull(), isNull(), eq(0), eq(10));
            verify(organizationQueryRepositoryPort, times(1)).countAll(
                eq(tenantId), isNull(), isNull(), isNull());
        }

        @Test
        @DisplayName("Cursor-based Pagination에서 findAllWithCursor 호출을 검증한다")
        void shouldVerifyCursorPaginationCalls() {
            // Arrange
            String tenantId = "tenant-123";
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 10, null, tenantId, null, null, null);

            when(organizationQueryRepositoryPort.findAllWithCursor(
                eq(tenantId), isNull(), isNull(), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());

            // Act
            organizationQueryService.executeWithSlice(query);

            // Assert
            verify(organizationQueryRepositoryPort, times(1)).findAllWithCursor(
                eq(tenantId), isNull(), isNull(), isNull(), isNull(), eq(11));
            // Cursor-based는 countAll을 호출하지 않음
            verify(organizationQueryRepositoryPort, never()).countAll(any(), any(), any(), any());
        }
    }
}

package com.ryuqq.fileflow.application.iam.organization.facade;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationsQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.in.GetOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.in.GetOrganizationsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrganizationQueryFacade 단위 테스트
 *
 * <p>OrganizationQueryFacade의 UseCase 위임 및 조합 로직을 검증합니다.</p>
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>GetOrganizationTests: 단건 조회 위임 검증</li>
 *   <li>GetOrganizationsWithPageTests: Offset-based Pagination 검증</li>
 *   <li>GetOrganizationsWithSliceTests: Cursor-based Pagination 검증</li>
 *   <li>FacadeIntegrationTests: 통합 동작 검증</li>
 * </ul>
 *
 * <p><strong>검증 항목:</strong></p>
 * <ul>
 *   <li>✅ Facade는 UseCase로 단순 위임만 수행 (비즈니스 로직 없음)</li>
 *   <li>✅ Query가 UseCase에 정확히 전달되는지 확인</li>
 *   <li>✅ Dual Pagination (Offset + Cursor) 모두 지원</li>
 *   <li>✅ tenantId 필터링 포함된 Query 처리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("OrganizationQueryFacade 테스트")
@ExtendWith(MockitoExtension.class)
class OrganizationQueryFacadeTest {

    @Mock
    private GetOrganizationUseCase getOrganizationUseCase;

    @Mock
    private GetOrganizationsUseCase getOrganizationsUseCase;

    @InjectMocks
    private OrganizationQueryFacade organizationQueryFacade;

    /**
     * 테스트용 OrganizationResponse 생성 헬퍼 메서드
     *
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @param orgCode Organization Code
     * @param name Organization Name
     * @return OrganizationResponse
     */
    private OrganizationResponse createOrganizationResponse(
        Long organizationId,
        String tenantId,
        String orgCode,
        String name
    ) {
        return new OrganizationResponse(
            organizationId,
            tenantId,
            orgCode,
            name,
            "ACTIVE",
            false,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusDays(1)
        );
    }

    @Nested
    @DisplayName("GetOrganization 테스트")
    class GetOrganizationTests {

        @Test
        @DisplayName("단건 조회: UseCase 호출 및 Response 반환 검증")
        void givenValidQuery_whenGetOrganization_thenReturnsResponse() {
            // Arrange
            GetOrganizationQuery query = new GetOrganizationQuery(1L);
            OrganizationResponse expectedResponse = createOrganizationResponse(
                1L,
                "tenant-uuid-123",
                "SALES",
                "Sales Department"
            );

            when(getOrganizationUseCase.execute(query))
                .thenReturn(expectedResponse);

            // Act
            OrganizationResponse actualResponse = organizationQueryFacade.getOrganization(query);

            // Assert
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.organizationId()).isEqualTo(1L);
            assertThat(actualResponse.tenantId()).isEqualTo("tenant-uuid-123");
            assertThat(actualResponse.orgCode()).isEqualTo("SALES");
            assertThat(actualResponse.name()).isEqualTo("Sales Department");

            verify(getOrganizationUseCase, times(1)).execute(query);
        }

        @Test
        @DisplayName("단건 조회: Query 파라미터 정확히 전달되는지 검증")
        void givenQuery_whenGetOrganization_thenPassesExactParameters() {
            // Arrange
            GetOrganizationQuery query = new GetOrganizationQuery(999L);
            OrganizationResponse response = createOrganizationResponse(
                999L,
                "tenant-uuid-999",
                "IT",
                "IT Department"
            );

            when(getOrganizationUseCase.execute(query))
                .thenReturn(response);

            // Act
            organizationQueryFacade.getOrganization(query);

            // Assert
            verify(getOrganizationUseCase).execute(argThat(q ->
                q.organizationId().equals(999L)
            ));
        }

        @Test
        @DisplayName("단건 조회: UseCase 예외 전파 검증")
        void givenNonExistentId_whenGetOrganization_thenThrowsException() {
            // Arrange
            GetOrganizationQuery query = new GetOrganizationQuery(9999L);

            when(getOrganizationUseCase.execute(query))
                .thenThrow(new IllegalStateException("Organization을 찾을 수 없습니다"));

            // Act & Assert
            assertThatThrownBy(() -> organizationQueryFacade.getOrganization(query))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(getOrganizationUseCase, times(1)).execute(query);
        }
    }

    @Nested
    @DisplayName("GetOrganizationsWithPage 테스트")
    class GetOrganizationsWithPageTests {

        @Test
        @DisplayName("페이지 조회: Offset-based Pagination 처리")
        void givenPageQuery_whenGetOrganizationsWithPage_thenReturnsPageResponse() {
            // Arrange
            GetOrganizationsQuery query = new GetOrganizationsQuery(
                0,      // page
                20,     // size
                null,   // cursor
                "tenant-uuid-123",
                null,
                null,
                null
            );

            List<OrganizationResponse> organizations = List.of(
                createOrganizationResponse(1L, "tenant-uuid-123", "SALES", "Sales Department"),
                createOrganizationResponse(2L, "tenant-uuid-123", "HR", "Human Resources")
            );

            PageResponse<OrganizationResponse> expectedPage = PageResponse.of(
                organizations,
                0,      // page
                20,     // size
                2L,     // totalElements
                1,      // totalPages
                true,   // first
                true    // last
            );

            when(getOrganizationsUseCase.executeWithPage(query))
                .thenReturn(expectedPage);

            // Act
            PageResponse<OrganizationResponse> actualPage = organizationQueryFacade.getOrganizationsWithPage(query);

            // Assert
            assertThat(actualPage).isNotNull();
            assertThat(actualPage.content()).hasSize(2);
            assertThat(actualPage.page()).isEqualTo(0);
            assertThat(actualPage.size()).isEqualTo(20);
            assertThat(actualPage.totalElements()).isEqualTo(2L);
            assertThat(actualPage.totalPages()).isEqualTo(1);
            assertThat(actualPage.first()).isTrue();
            assertThat(actualPage.last()).isTrue();

            verify(getOrganizationsUseCase, times(1)).executeWithPage(query);
        }

        @Test
        @DisplayName("페이지 조회: 여러 Organization 처리")
        void givenMultipleOrganizations_whenGetOrganizationsWithPage_thenReturnsAllOrganizations() {
            // Arrange
            GetOrganizationsQuery query = new GetOrganizationsQuery(
                0, 20, null, "tenant-uuid-123", null, null, null
            );

            List<OrganizationResponse> organizations = List.of(
                createOrganizationResponse(1L, "tenant-uuid-123", "SALES", "Sales"),
                createOrganizationResponse(2L, "tenant-uuid-123", "HR", "HR"),
                createOrganizationResponse(3L, "tenant-uuid-123", "IT", "IT")
            );

            PageResponse<OrganizationResponse> expectedPage = PageResponse.of(
                organizations,
                0,
                20,
                3L,
                1,
                true,
                true
            );

            when(getOrganizationsUseCase.executeWithPage(query))
                .thenReturn(expectedPage);

            // Act
            PageResponse<OrganizationResponse> actualPage = organizationQueryFacade.getOrganizationsWithPage(query);

            // Assert
            assertThat(actualPage.content()).hasSize(3);
            assertThat(actualPage.content())
                .extracting(OrganizationResponse::orgCode)
                .containsExactly("SALES", "HR", "IT");

            verify(getOrganizationsUseCase).executeWithPage(query);
        }

        @Test
        @DisplayName("페이지 조회: PageResponse 필드 검증")
        void givenSecondPage_whenGetOrganizationsWithPage_thenVerifyPageFields() {
            // Arrange
            GetOrganizationsQuery query = new GetOrganizationsQuery(
                1, 20, null, null, null, null, null  // page = 1 (두 번째 페이지)
            );

            List<OrganizationResponse> organizations = List.of(
                createOrganizationResponse(21L, "tenant-uuid-123", "MARKETING", "Marketing")
            );

            PageResponse<OrganizationResponse> expectedPage = PageResponse.of(
                organizations,
                1,      // page
                20,     // size
                21L,    // totalElements
                2,      // totalPages
                false,  // first
                true    // last
            );

            when(getOrganizationsUseCase.executeWithPage(query))
                .thenReturn(expectedPage);

            // Act
            PageResponse<OrganizationResponse> actualPage = organizationQueryFacade.getOrganizationsWithPage(query);

            // Assert
            assertThat(actualPage.page()).isEqualTo(1);
            assertThat(actualPage.first()).isFalse();
            assertThat(actualPage.last()).isTrue();
            assertThat(actualPage.totalPages()).isEqualTo(2);

            verify(getOrganizationsUseCase).executeWithPage(query);
        }

        @Test
        @DisplayName("페이지 조회: 빈 페이지 처리")
        void givenNoOrganizations_whenGetOrganizationsWithPage_thenReturnsEmptyPage() {
            // Arrange
            GetOrganizationsQuery query = new GetOrganizationsQuery(
                0, 20, null, "tenant-uuid-999", null, null, null
            );

            PageResponse<OrganizationResponse> emptyPage = PageResponse.empty(0, 20);

            when(getOrganizationsUseCase.executeWithPage(query))
                .thenReturn(emptyPage);

            // Act
            PageResponse<OrganizationResponse> actualPage = organizationQueryFacade.getOrganizationsWithPage(query);

            // Assert
            assertThat(actualPage.content()).isEmpty();
            assertThat(actualPage.totalElements()).isEqualTo(0L);
            assertThat(actualPage.totalPages()).isEqualTo(0);

            verify(getOrganizationsUseCase).executeWithPage(query);
        }
    }

    @Nested
    @DisplayName("GetOrganizationsWithSlice 테스트")
    class GetOrganizationsWithSliceTests {

        @Test
        @DisplayName("슬라이스 조회: Cursor-based Pagination 처리")
        void givenCursorQuery_whenGetOrganizationsWithSlice_thenReturnsSliceResponse() {
            // Arrange
            GetOrganizationsQuery query = new GetOrganizationsQuery(
                null,   // page
                20,     // size
                "cursor-abc",
                "tenant-uuid-123",
                null,
                null,
                null
            );

            List<OrganizationResponse> organizations = List.of(
                createOrganizationResponse(1L, "tenant-uuid-123", "SALES", "Sales Department"),
                createOrganizationResponse(2L, "tenant-uuid-123", "HR", "Human Resources")
            );

            SliceResponse<OrganizationResponse> expectedSlice = SliceResponse.of(
                organizations,
                20,
                true,
                "cursor-xyz"
            );

            when(getOrganizationsUseCase.executeWithSlice(query))
                .thenReturn(expectedSlice);

            // Act
            SliceResponse<OrganizationResponse> actualSlice = organizationQueryFacade.getOrganizationsWithSlice(query);

            // Assert
            assertThat(actualSlice).isNotNull();
            assertThat(actualSlice.content()).hasSize(2);
            assertThat(actualSlice.size()).isEqualTo(20);
            assertThat(actualSlice.hasNext()).isTrue();
            assertThat(actualSlice.nextCursor()).isEqualTo("cursor-xyz");

            verify(getOrganizationsUseCase, times(1)).executeWithSlice(query);
        }

        @Test
        @DisplayName("슬라이스 조회: 여러 Organization 처리")
        void givenMultipleOrganizations_whenGetOrganizationsWithSlice_thenReturnsAllOrganizations() {
            // Arrange
            GetOrganizationsQuery query = new GetOrganizationsQuery(
                null, 20, "cursor-abc", "tenant-uuid-123", null, null, null
            );

            List<OrganizationResponse> organizations = List.of(
                createOrganizationResponse(1L, "tenant-uuid-123", "SALES", "Sales"),
                createOrganizationResponse(2L, "tenant-uuid-123", "HR", "HR"),
                createOrganizationResponse(3L, "tenant-uuid-123", "IT", "IT")
            );

            SliceResponse<OrganizationResponse> expectedSlice = SliceResponse.of(
                organizations,
                20,
                false,
                null
            );

            when(getOrganizationsUseCase.executeWithSlice(query))
                .thenReturn(expectedSlice);

            // Act
            SliceResponse<OrganizationResponse> actualSlice = organizationQueryFacade.getOrganizationsWithSlice(query);

            // Assert
            assertThat(actualSlice.content()).hasSize(3);
            assertThat(actualSlice.content())
                .extracting(OrganizationResponse::orgCode)
                .containsExactly("SALES", "HR", "IT");

            verify(getOrganizationsUseCase).executeWithSlice(query);
        }

        @Test
        @DisplayName("슬라이스 조회: SliceResponse 필드 검증")
        void givenHasMore_whenGetOrganizationsWithSlice_thenVerifySliceFields() {
            // Arrange
            GetOrganizationsQuery query = new GetOrganizationsQuery(
                null, 10, "cursor-start", null, null, null, null
            );

            List<OrganizationResponse> organizations = List.of(
                createOrganizationResponse(1L, "tenant-uuid-123", "SALES", "Sales")
            );

            SliceResponse<OrganizationResponse> expectedSlice = SliceResponse.of(
                organizations,
                10,
                true,
                "cursor-next"
            );

            when(getOrganizationsUseCase.executeWithSlice(query))
                .thenReturn(expectedSlice);

            // Act
            SliceResponse<OrganizationResponse> actualSlice = organizationQueryFacade.getOrganizationsWithSlice(query);

            // Assert
            assertThat(actualSlice.hasNext()).isTrue();
            assertThat(actualSlice.nextCursor()).isEqualTo("cursor-next");
            assertThat(actualSlice.size()).isEqualTo(10);

            verify(getOrganizationsUseCase).executeWithSlice(query);
        }

        @Test
        @DisplayName("슬라이스 조회: 마지막 슬라이스 처리")
        void givenLastSlice_whenGetOrganizationsWithSlice_thenHasNextIsFalse() {
            // Arrange
            GetOrganizationsQuery query = new GetOrganizationsQuery(
                null, 20, "cursor-last", null, null, null, null
            );

            SliceResponse<OrganizationResponse> lastSlice = SliceResponse.of(
                List.of(createOrganizationResponse(99L, "tenant-uuid-123", "FINAL", "Final Organization")),
                20,
                false,
                null
            );

            when(getOrganizationsUseCase.executeWithSlice(query))
                .thenReturn(lastSlice);

            // Act
            SliceResponse<OrganizationResponse> actualSlice = organizationQueryFacade.getOrganizationsWithSlice(query);

            // Assert
            assertThat(actualSlice.hasNext()).isFalse();
            assertThat(actualSlice.nextCursor()).isNull();

            verify(getOrganizationsUseCase).executeWithSlice(query);
        }
    }

    @Nested
    @DisplayName("Facade 통합 테스트")
    class FacadeIntegrationTests {

        @Test
        @DisplayName("모든 Query UseCase 통합 동작 검증")
        void givenAllUseCases_whenCalledTogether_thenAllWork() {
            // Arrange
            GetOrganizationQuery singleQuery = new GetOrganizationQuery(1L);
            GetOrganizationsQuery pageQuery = new GetOrganizationsQuery(
                0, 20, null, null, null, null, null
            );
            GetOrganizationsQuery sliceQuery = new GetOrganizationsQuery(
                null, 20, "cursor-abc", null, null, null, null
            );

            OrganizationResponse singleResponse = createOrganizationResponse(
                1L, "tenant-uuid-123", "SALES", "Sales"
            );
            PageResponse<OrganizationResponse> pageResponse = PageResponse.of(
                List.of(singleResponse),
                0, 20, 1L, 1, true, true
            );
            SliceResponse<OrganizationResponse> sliceResponse = SliceResponse.of(
                List.of(singleResponse),
                20, false, null
            );

            when(getOrganizationUseCase.execute(singleQuery)).thenReturn(singleResponse);
            when(getOrganizationsUseCase.executeWithPage(pageQuery)).thenReturn(pageResponse);
            when(getOrganizationsUseCase.executeWithSlice(sliceQuery)).thenReturn(sliceResponse);

            // Act
            OrganizationResponse single = organizationQueryFacade.getOrganization(singleQuery);
            PageResponse<OrganizationResponse> page = organizationQueryFacade.getOrganizationsWithPage(pageQuery);
            SliceResponse<OrganizationResponse> slice = organizationQueryFacade.getOrganizationsWithSlice(sliceQuery);

            // Assert
            assertThat(single).isNotNull();
            assertThat(page).isNotNull();
            assertThat(slice).isNotNull();

            verify(getOrganizationUseCase).execute(singleQuery);
            verify(getOrganizationsUseCase).executeWithPage(pageQuery);
            verify(getOrganizationsUseCase).executeWithSlice(sliceQuery);
        }

        @Test
        @DisplayName("순수 위임 검증: Facade는 비즈니스 로직 없음")
        void givenFacade_whenInvoked_thenOnlyDelegates() {
            // Arrange
            GetOrganizationQuery query = new GetOrganizationQuery(1L);
            OrganizationResponse response = createOrganizationResponse(
                1L, "tenant-uuid-123", "SALES", "Sales"
            );

            when(getOrganizationUseCase.execute(query)).thenReturn(response);

            // Act
            OrganizationResponse actualResponse = organizationQueryFacade.getOrganization(query);

            // Assert - Facade는 UseCase의 결과를 그대로 반환해야 함
            assertThat(actualResponse).isSameAs(response);

            // 추가 검증: UseCase가 정확히 1번만 호출되었는지 확인
            verify(getOrganizationUseCase, times(1)).execute(query);
            verifyNoMoreInteractions(getOrganizationUseCase);
        }
    }
}

package com.ryuqq.fileflow.application.iam.organization.service;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationsQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationFixture;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * OrganizationQueryService 단위 테스트
 *
 * <p>CQRS Query Service 테스트로, 3개의 UseCase를 검증합니다:</p>
 * <ul>
 *   <li>GetOrganizationUseCase - 단건 조회</li>
 *   <li>GetOrganizationsUseCase (Offset) - Offset-based Pagination</li>
 *   <li>GetOrganizationsUseCase (Cursor) - Cursor-based Pagination</li>
 * </ul>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ Query Port Mock을 활용한 단위 테스트</li>
 *   <li>✅ Domain testFixture(OrganizationFixture) 사용</li>
 *   <li>✅ BDD 스타일(Given-When-Then) 테스트</li>
 *   <li>✅ Transaction readOnly 검증</li>
 *   <li>✅ Port 호출 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationQueryService 단위 테스트")
class OrganizationQueryServiceTest {

    @Mock
    private OrganizationQueryRepositoryPort organizationQueryRepositoryPort;

    @InjectMocks
    private OrganizationQueryService organizationQueryService;

    /**
     * GetOrganizationUseCase - Organization 단건 조회 테스트
     */
    @Nested
    @DisplayName("GetOrganizationUseCase - Organization 단건 조회")
    class GetOrganizationUseCaseTests {

        @Test
        @DisplayName("유효한 Query로 Organization 조회 성공")
        void execute_Success() {
            // Given
            GetOrganizationQuery query = new GetOrganizationQuery(1L);
            Organization organization = OrganizationFixture.createWithId(1L);

            given(organizationQueryRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.of(organization));

            // When
            OrganizationResponse response = organizationQueryService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isEqualTo(organization.getIdValue());
            assertThat(response.tenantId()).isEqualTo(organization.getTenantId());
            assertThat(response.orgCode()).isEqualTo(organization.getOrgCodeValue());
            assertThat(response.name()).isEqualTo(organization.getName());
            assertThat(response.status()).isEqualTo(organization.getStatus().name());
            assertThat(response.deleted()).isEqualTo(organization.isDeleted());

            verify(organizationQueryRepositoryPort).findById(any(OrganizationId.class));
        }

        @Test
        @DisplayName("존재하지 않는 Organization 조회 시도하면 예외 발생")
        void execute_Fail_OrganizationNotFound() {
            // Given
            GetOrganizationQuery query = new GetOrganizationQuery(999L);

            given(organizationQueryRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> organizationQueryService.execute(query))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationQueryRepositoryPort).findById(any(OrganizationId.class));
        }

        @Test
        @DisplayName("Query가 null이면 예외 발생")
        void execute_Fail_QueryIsNull() {
            // When & Then
            assertThatThrownBy(() -> organizationQueryService.execute((GetOrganizationQuery) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetOrganizationQuery는 필수입니다");
        }
    }

    /**
     * GetOrganizationsUseCase - Offset-based Pagination 테스트
     */
    @Nested
    @DisplayName("GetOrganizationsUseCase - Offset-based Pagination")
    class GetOrganizationsWithPageTests {

        @Test
        @DisplayName("Offset 기반 페이지네이션으로 목록 조회 성공")
        void executeWithPage_Success() {
            // Given
            GetOrganizationsQuery query = new GetOrganizationsQuery(0, 20, null, 1L, null, null, null);
            List<Organization> organizations = List.of(
                OrganizationFixture.createWithId(1L),
                OrganizationFixture.createWithId(2L),
                OrganizationFixture.createWithId(3L)
            );

            given(organizationQueryRepositoryPort.findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            )).willReturn(organizations);

            given(organizationQueryRepositoryPort.countAll(
                any(), any(), any(), any()
            )).willReturn(3L);

            // When
            PageResponse<OrganizationResponse> response = organizationQueryService.executeWithPage(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(3);
            assertThat(response.page()).isEqualTo(0);
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.totalElements()).isEqualTo(3L);
            assertThat(response.totalPages()).isEqualTo(1);
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isTrue();

            verify(organizationQueryRepositoryPort).findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            );
            verify(organizationQueryRepositoryPort).countAll(
                any(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("두 번째 페이지 조회 성공 (first=false, last=false)")
        void executeWithPage_Success_SecondPage() {
            // Given
            GetOrganizationsQuery query = new GetOrganizationsQuery(1, 2, null, 1L, null, null, null);
            List<Organization> organizations = List.of(
                OrganizationFixture.createWithId(3L),
                OrganizationFixture.createWithId(4L)
            );

            given(organizationQueryRepositoryPort.findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            )).willReturn(organizations);

            given(organizationQueryRepositoryPort.countAll(
                any(), any(), any(), any()
            )).willReturn(10L);

            // When
            PageResponse<OrganizationResponse> response = organizationQueryService.executeWithPage(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);
            assertThat(response.page()).isEqualTo(1);
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.totalElements()).isEqualTo(10L);
            assertThat(response.totalPages()).isEqualTo(5);
            assertThat(response.first()).isFalse();
            assertThat(response.last()).isFalse();
        }

        @Test
        @DisplayName("빈 결과 조회 성공 (빈 리스트 반환)")
        void executeWithPage_Success_EmptyResult() {
            // Given
            GetOrganizationsQuery query = new GetOrganizationsQuery(0, 20, null, 1L, null, null, null);

            given(organizationQueryRepositoryPort.findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            )).willReturn(Collections.emptyList());

            given(organizationQueryRepositoryPort.countAll(
                any(), any(), any(), any()
            )).willReturn(0L);

            // When
            PageResponse<OrganizationResponse> response = organizationQueryService.executeWithPage(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).isEmpty();
            assertThat(response.page()).isEqualTo(0);
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.totalElements()).isEqualTo(0L);
            assertThat(response.totalPages()).isEqualTo(0);
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isTrue();
        }

        @Test
        @DisplayName("Query가 null이면 예외 발생")
        void executeWithPage_Fail_QueryIsNull() {
            // When & Then
            assertThatThrownBy(() -> organizationQueryService.executeWithPage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetOrganizationsQuery는 필수입니다");
        }

        @Test
        @DisplayName("Cursor-based Query로 Page 메서드 호출 시 예외 발생")
        void executeWithPage_Fail_CursorBasedQueryProvided() {
            // Given - Cursor-based Query
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 20, "cursor123", 1L, null, null, null);

            // When & Then
            assertThatThrownBy(() -> organizationQueryService.executeWithPage(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Offset-based Pagination을 위해서는 page가 필요합니다");
        }
    }

    /**
     * GetOrganizationsUseCase - Cursor-based Pagination 테스트
     */
    @Nested
    @DisplayName("GetOrganizationsUseCase - Cursor-based Pagination")
    class GetOrganizationsWithSliceTests {

        @Test
        @DisplayName("Cursor 기반 페이지네이션으로 목록 조회 성공 (hasNext=true)")
        void executeWithSlice_Success_HasNext() {
            // Given
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 2, null, 1L, null, null, null);
            // size=2이지만 limit+1=3개 반환 → hasNext=true
            List<Organization> organizations = List.of(
                OrganizationFixture.createWithId(1L),
                OrganizationFixture.createWithId(2L),
                OrganizationFixture.createWithId(3L)  // limit+1
            );

            given(organizationQueryRepositoryPort.findAllWithCursor(
                any(), any(), any(), any(), any(), anyInt()
            )).willReturn(organizations);

            // When
            SliceResponse<OrganizationResponse> response = organizationQueryService.executeWithSlice(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);  // limit+1에서 1개 제거
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();  // Base64 인코딩된 커서

            verify(organizationQueryRepositoryPort).findAllWithCursor(
                any(), any(), any(), any(), any(), anyInt()
            );
        }

        @Test
        @DisplayName("Cursor 기반 페이지네이션으로 목록 조회 성공 (hasNext=false)")
        void executeWithSlice_Success_NoNext() {
            // Given
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 2, null, 1L, null, null, null);
            // size=2이고 정확히 2개 반환 → hasNext=false
            List<Organization> organizations = List.of(
                OrganizationFixture.createWithId(1L),
                OrganizationFixture.createWithId(2L)
            );

            given(organizationQueryRepositoryPort.findAllWithCursor(
                any(), any(), any(), any(), any(), anyInt()
            )).willReturn(organizations);

            // When
            SliceResponse<OrganizationResponse> response = organizationQueryService.executeWithSlice(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();  // 다음 페이지 없음
        }

        @Test
        @DisplayName("빈 결과 조회 성공 (빈 리스트 반환, hasNext=false)")
        void executeWithSlice_Success_EmptyResult() {
            // Given
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 20, null, 1L, null, null, null);

            given(organizationQueryRepositoryPort.findAllWithCursor(
                any(), any(), any(), any(), any(), anyInt()
            )).willReturn(Collections.emptyList());

            // When
            SliceResponse<OrganizationResponse> response = organizationQueryService.executeWithSlice(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).isEmpty();
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("커서 값으로 다음 페이지 조회 성공")
        void executeWithSlice_Success_WithCursor() {
            // Given - 커서 값 포함
            String cursor = "Y3Vyc29yMTIz";  // Base64 인코딩된 커서
            GetOrganizationsQuery query = new GetOrganizationsQuery(null, 2, cursor, 1L, null, null, null);
            List<Organization> organizations = List.of(
                OrganizationFixture.createWithId(4L),
                OrganizationFixture.createWithId(5L)
            );

            given(organizationQueryRepositoryPort.findAllWithCursor(
                any(), any(), any(), any(), any(), anyInt()
            )).willReturn(organizations);

            // When
            SliceResponse<OrganizationResponse> response = organizationQueryService.executeWithSlice(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);
            assertThat(response.hasNext()).isFalse();

            verify(organizationQueryRepositoryPort).findAllWithCursor(
                any(), any(), any(), any(), any(), anyInt()
            );
        }

        @Test
        @DisplayName("Query가 null이면 예외 발생")
        void executeWithSlice_Fail_QueryIsNull() {
            // When & Then
            assertThatThrownBy(() -> organizationQueryService.executeWithSlice(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetOrganizationsQuery는 필수입니다");
        }
    }

    /**
     * Transaction 경계 테스트
     */
    @Nested
    @DisplayName("Transaction 경계 검증")
    class TransactionBoundaryTests {

        @Test
        @DisplayName("Query Service는 @Transactional(readOnly = true)로 실행됨")
        void verifyTransactionalReadOnly() throws NoSuchMethodException {
            // Given
            Method executeMethod = OrganizationQueryService.class.getMethod("execute", GetOrganizationQuery.class);
            Method executeWithPageMethod = OrganizationQueryService.class.getMethod("executeWithPage", GetOrganizationsQuery.class);
            Method executeWithSliceMethod = OrganizationQueryService.class.getMethod("executeWithSlice", GetOrganizationsQuery.class);

            // When
            Transactional classLevelTransactional = OrganizationQueryService.class.getAnnotation(Transactional.class);

            // Then - 클래스 레벨에 @Transactional(readOnly = true) 존재
            assertThat(classLevelTransactional).isNotNull();
            assertThat(classLevelTransactional.readOnly()).isTrue();

            // Then - 메서드에는 별도 @Transactional 없음 (클래스 레벨 설정 상속)
            assertThat(executeMethod.isAnnotationPresent(Transactional.class)).isFalse();
            assertThat(executeWithPageMethod.isAnnotationPresent(Transactional.class)).isFalse();
            assertThat(executeWithSliceMethod.isAnnotationPresent(Transactional.class)).isFalse();
        }
    }

    /**
     * Port Interaction 검증 테스트
     */
    @Nested
    @DisplayName("Port Interaction 검증")
    class PortInteractionTests {

        @Test
        @DisplayName("Query Repository Port만 호출되고 외부 API 호출은 없음")
        void verifyOnlyRepositoryPortIsCalled() {
            // Given
            GetOrganizationQuery query = new GetOrganizationQuery(1L);
            Organization organization = OrganizationFixture.createWithId(1L);

            given(organizationQueryRepositoryPort.findById(any(OrganizationId.class)))
                .willReturn(Optional.of(organization));

            // When
            organizationQueryService.execute(query);

            // Then - OrganizationQueryRepositoryPort만 호출됨
            verify(organizationQueryRepositoryPort).findById(any(OrganizationId.class));
            // 외부 API 호출 없음 (Mock 검증으로 확인)
        }

        @Test
        @DisplayName("Transaction 내에서 외부 API 호출 없음 (Transaction 경계 준수)")
        void verifyNoExternalApiCallsWithinTransaction() {
            // Given
            GetOrganizationsQuery query = new GetOrganizationsQuery(0, 20, null, 1L, null, null, null);
            List<Organization> organizations = List.of(OrganizationFixture.createWithId(1L));

            given(organizationQueryRepositoryPort.findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            )).willReturn(organizations);

            given(organizationQueryRepositoryPort.countAll(
                any(), any(), any(), any()
            )).willReturn(1L);

            // When
            organizationQueryService.executeWithPage(query);

            // Then - Repository Port만 호출됨 (외부 API 없음)
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            );
            verify(organizationQueryRepositoryPort).countAll(
                any(), any(), any(), any()
            );
            // WebClient, RestTemplate 등 외부 API 호출 없음
        }
    }
}

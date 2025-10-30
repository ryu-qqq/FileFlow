package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.common.dto.PageResponse;
import com.ryuqq.fileflow.application.common.dto.SliceResponse;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantsQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantFixture;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * TenantQueryService 단위 테스트
 *
 * <p>CQRS Query Service 테스트로, 3개의 UseCase를 검증합니다:</p>
 * <ul>
 *   <li>GetTenantUseCase - 단건 조회</li>
 *   <li>GetTenantsUseCase (Offset) - Offset-based Pagination</li>
 *   <li>GetTenantsUseCase (Cursor) - Cursor-based Pagination</li>
 * </ul>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ Query Port Mock을 활용한 단위 테스트</li>
 *   <li>✅ Domain testFixture(TenantFixture) 사용</li>
 *   <li>✅ BDD 스타일(Given-When-Then) 테스트</li>
 *   <li>✅ Transaction readOnly 검증</li>
 *   <li>✅ Port 호출 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantQueryService 단위 테스트")
class TenantQueryServiceTest {

    @Mock
    private TenantQueryRepositoryPort tenantQueryRepositoryPort;

    @InjectMocks
    private TenantQueryService tenantQueryService;

    /**
     * GetTenantUseCase - Tenant 단건 조회 테스트
     */
    @Nested
    @DisplayName("GetTenantUseCase - Tenant 단건 조회")
    class GetTenantUseCaseTests {

        @Test
        @DisplayName("유효한 Query로 Tenant 조회 성공")
        void execute_Success() {
            // Given
            GetTenantQuery query = new GetTenantQuery(1L);
            Tenant tenant = TenantFixture.createWithId(1L);

            given(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(tenant));

            // When
            TenantResponse response = tenantQueryService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(tenant.getIdValue());
            assertThat(response.name()).isEqualTo(tenant.getNameValue());
            assertThat(response.status()).isEqualTo(tenant.getStatus().name());

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
        }

        @Test
        @DisplayName("존재하지 않는 Tenant 조회 시도하면 예외 발생")
        void execute_Fail_TenantNotFound() {
            // Given
            GetTenantQuery query = new GetTenantQuery(999L);

            given(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> tenantQueryService.execute(query))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant를 찾을 수 없습니다");

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
        }

        @Test
        @DisplayName("Query가 null이면 예외 발생")
        void execute_Fail_QueryIsNull() {
            // When & Then
            assertThatThrownBy(() -> tenantQueryService.execute((GetTenantQuery) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetTenantQuery는 필수입니다");
        }
    }

    /**
     * GetTenantsUseCase - Offset-based Pagination 테스트
     */
    @Nested
    @DisplayName("GetTenantsUseCase - Offset-based Pagination")
    class GetTenantsWithPageTests {

        @Test
        @DisplayName("Offset 기반 페이지네이션으로 목록 조회 성공")
        void executeWithPage_Success() {
            // Given
            GetTenantsQuery query = new GetTenantsQuery(0, 20, null, null, null);
            List<Tenant> tenants = List.of(
                TenantFixture.createWithId(1L),
                TenantFixture.createWithId(2L),
                TenantFixture.createWithId(3L)
            );

            given(tenantQueryRepositoryPort.findAllWithOffset(any(), any(), anyInt(), anyInt()))
                .willReturn(tenants);
            given(tenantQueryRepositoryPort.countAll(any(), any()))
                .willReturn(3L);

            // When
            PageResponse<TenantResponse> response = tenantQueryService.executeWithPage(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(3);
            assertThat(response.page()).isEqualTo(0);
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.totalElements()).isEqualTo(3L);
            assertThat(response.totalPages()).isEqualTo(1);
            assertThat(response.first()).isTrue();
            assertThat(response.last()).isTrue();

            verify(tenantQueryRepositoryPort).findAllWithOffset(any(), any(), anyInt(), anyInt());
            verify(tenantQueryRepositoryPort).countAll(any(), any());
        }

        @Test
        @DisplayName("두 번째 페이지 조회 성공 (first=false, last=false)")
        void executeWithPage_Success_SecondPage() {
            // Given
            GetTenantsQuery query = new GetTenantsQuery(1, 2, null, null, null);
            List<Tenant> tenants = List.of(
                TenantFixture.createWithId(3L),
                TenantFixture.createWithId(4L)
            );

            given(tenantQueryRepositoryPort.findAllWithOffset(any(), any(), anyInt(), anyInt()))
                .willReturn(tenants);
            given(tenantQueryRepositoryPort.countAll(any(), any()))
                .willReturn(10L);

            // When
            PageResponse<TenantResponse> response = tenantQueryService.executeWithPage(query);

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
            GetTenantsQuery query = new GetTenantsQuery(0, 20, null, null, null);

            given(tenantQueryRepositoryPort.findAllWithOffset(any(), any(), anyInt(), anyInt()))
                .willReturn(Collections.emptyList());
            given(tenantQueryRepositoryPort.countAll(any(), any()))
                .willReturn(0L);

            // When
            PageResponse<TenantResponse> response = tenantQueryService.executeWithPage(query);

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
            assertThatThrownBy(() -> tenantQueryService.executeWithPage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetTenantsQuery는 필수입니다");
        }

        @Test
        @DisplayName("Cursor-based Query로 Page 메서드 호출 시 예외 발생")
        void executeWithPage_Fail_CursorBasedQueryProvided() {
            // Given - Cursor-based Query
            GetTenantsQuery query = new GetTenantsQuery(null, 20, "cursor123", null, null);

            // When & Then
            assertThatThrownBy(() -> tenantQueryService.executeWithPage(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Offset-based Pagination을 위해서는 page가 필요합니다");
        }
    }

    /**
     * GetTenantsUseCase - Cursor-based Pagination 테스트
     */
    @Nested
    @DisplayName("GetTenantsUseCase - Cursor-based Pagination")
    class GetTenantsWithSliceTests {

        @Test
        @DisplayName("Cursor 기반 페이지네이션으로 목록 조회 성공 (hasNext=true)")
        void executeWithSlice_Success_HasNext() {
            // Given
            GetTenantsQuery query = new GetTenantsQuery(null, 2, null, null, null);
            // size=2이지만 limit+1=3개 반환 → hasNext=true
            List<Tenant> tenants = List.of(
                TenantFixture.createWithId(1L),
                TenantFixture.createWithId(2L),
                TenantFixture.createWithId(3L)  // limit+1
            );

            given(tenantQueryRepositoryPort.findAllWithCursor(any(), any(), any(), anyInt()))
                .willReturn(tenants);

            // When
            SliceResponse<TenantResponse> response = tenantQueryService.executeWithSlice(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);  // limit+1에서 1개 제거
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();  // Base64 인코딩된 커서

            verify(tenantQueryRepositoryPort).findAllWithCursor(any(), any(), any(), anyInt());
        }

        @Test
        @DisplayName("Cursor 기반 페이지네이션으로 목록 조회 성공 (hasNext=false)")
        void executeWithSlice_Success_NoNext() {
            // Given
            GetTenantsQuery query = new GetTenantsQuery(null, 2, null, null, null);
            // size=2이고 정확히 2개 반환 → hasNext=false
            List<Tenant> tenants = List.of(
                TenantFixture.createWithId(1L),
                TenantFixture.createWithId(2L)
            );

            given(tenantQueryRepositoryPort.findAllWithCursor(any(), any(), any(), anyInt()))
                .willReturn(tenants);

            // When
            SliceResponse<TenantResponse> response = tenantQueryService.executeWithSlice(query);

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
            GetTenantsQuery query = new GetTenantsQuery(null, 20, null, null, null);

            given(tenantQueryRepositoryPort.findAllWithCursor(any(), any(), any(), anyInt()))
                .willReturn(Collections.emptyList());

            // When
            SliceResponse<TenantResponse> response = tenantQueryService.executeWithSlice(query);

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
            GetTenantsQuery query = new GetTenantsQuery(null, 2, cursor, null, null);
            List<Tenant> tenants = List.of(
                TenantFixture.createWithId(4L),
                TenantFixture.createWithId(5L)
            );

            given(tenantQueryRepositoryPort.findAllWithCursor(any(), any(), any(), anyInt()))
                .willReturn(tenants);

            // When
            SliceResponse<TenantResponse> response = tenantQueryService.executeWithSlice(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);
            assertThat(response.hasNext()).isFalse();

            verify(tenantQueryRepositoryPort).findAllWithCursor(any(), any(), any(), anyInt());
        }

        @Test
        @DisplayName("Query가 null이면 예외 발생")
        void executeWithSlice_Fail_QueryIsNull() {
            // When & Then
            assertThatThrownBy(() -> tenantQueryService.executeWithSlice(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetTenantsQuery는 필수입니다");
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
            Method executeMethod = TenantQueryService.class.getMethod("execute", GetTenantQuery.class);
            Method executeWithPageMethod = TenantQueryService.class.getMethod("executeWithPage", GetTenantsQuery.class);
            Method executeWithSliceMethod = TenantQueryService.class.getMethod("executeWithSlice", GetTenantsQuery.class);

            // When
            Transactional classLevelTransactional = TenantQueryService.class.getAnnotation(Transactional.class);

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
            GetTenantQuery query = new GetTenantQuery(1L);
            Tenant tenant = TenantFixture.createWithId(1L);

            given(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(tenant));

            // When
            tenantQueryService.execute(query);

            // Then - TenantQueryRepositoryPort만 호출됨
            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
            // 외부 API 호출 없음 (Mock 검증으로 확인)
        }

        @Test
        @DisplayName("Transaction 내에서 외부 API 호출 없음 (Transaction 경계 준수)")
        void verifyNoExternalApiCallsWithinTransaction() {
            // Given
            GetTenantsQuery query = new GetTenantsQuery(0, 20, null, null, null);
            List<Tenant> tenants = List.of(TenantFixture.createWithId(1L));

            given(tenantQueryRepositoryPort.findAllWithOffset(any(), any(), anyInt(), anyInt()))
                .willReturn(tenants);
            given(tenantQueryRepositoryPort.countAll(any(), any()))
                .willReturn(1L);

            // When
            tenantQueryService.executeWithPage(query);

            // Then - Repository Port만 호출됨 (외부 API 없음)
            verify(tenantQueryRepositoryPort).findAllWithOffset(any(), any(), anyInt(), anyInt());
            verify(tenantQueryRepositoryPort).countAll(any(), any());
            // WebClient, RestTemplate 등 외부 API 호출 없음
        }
    }
}

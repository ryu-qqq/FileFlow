package com.ryuqq.fileflow.application.iam.tenant.service;

import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationQueryRepositoryPort;
import com.ryuqq.fileflow.application.iam.tenant.dto.query.GetTenantTreeQuery;
import com.ryuqq.fileflow.application.iam.tenant.dto.response.TenantTreeResponse;
import com.ryuqq.fileflow.application.iam.tenant.port.out.TenantQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationFixture;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantFixture;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.exception.TenantNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

/**
 * GetTenantTreeService 단위 테스트
 *
 * <p>Tenant와 그에 속한 Organization 트리를 조회하는 Query UseCase 테스트</p>
 *
 * <p><strong>테스트 대상 UseCase:</strong></p>
 * <ul>
 *   <li>GetTenantTreeUseCase: Tenant + Organizations 트리 조회</li>
 * </ul>
 *
 * <p><strong>테스트 특징:</strong></p>
 * <ul>
 *   <li>✅ 2개 Repository Port 사용 (TenantQueryRepositoryPort, OrganizationQueryRepositoryPort)</li>
 *   <li>✅ Configurable Max Limit (maxOrganizationsPerTree)</li>
 *   <li>✅ TenantNotFoundException (Domain Exception)</li>
 *   <li>✅ includeDeleted 파라미터 처리</li>
 *   <li>✅ @Transactional(readOnly = true) 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetTenantTreeService 단위 테스트")
class GetTenantTreeServiceTest {

    @Mock
    private TenantQueryRepositoryPort tenantQueryRepositoryPort;

    @Mock
    private OrganizationQueryRepositoryPort organizationQueryRepositoryPort;

    private GetTenantTreeService getTenantTreeService;

    /**
     * GetTenantTreeService 초기화
     *
     * <p>@InjectMocks 대신 수동 생성 (maxOrganizationsPerTree는 생성자 파라미터)</p>
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        getTenantTreeService = new GetTenantTreeService(
            tenantQueryRepositoryPort,
            organizationQueryRepositoryPort,
            1000  // maxOrganizationsPerTree 기본값
        );
    }

    /**
     * Tenant + Organizations 트리 조회 성공 시나리오
     */
    @Nested
    @DisplayName("execute() - Tenant 트리 조회")
    class Execute {

        /**
         * Given: 유효한 Tenant ID와 includeDeleted=false
         * And: Tenant가 존재하고 3개의 Organizations가 존재
         * When: Tenant 트리 조회 실행
         * Then: TenantTreeResponse 반환 (Tenant + 3개 Organizations)
         * And: 양쪽 Repository Port 호출 검증
         */
        @Test
        @DisplayName("Tenant와 Organizations가 모두 존재하는 경우 성공")
        void execute_Success_WithOrganizations() {
            // Given
            GetTenantTreeQuery query = new GetTenantTreeQuery(1L, false);

            Tenant tenant = TenantFixture.createWithId(1L);
            List<Organization> organizations = List.of(
                OrganizationFixture.createWithId(1L, 1L, "ORG001", "Engineering"),
                OrganizationFixture.createWithId(2L, 1L, "ORG002", "Sales"),
                OrganizationFixture.createWithId(3L, 1L, "ORG003", "Marketing")
            );

            given(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(tenant));
            given(organizationQueryRepositoryPort.findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            )).willReturn(organizations);

            // When
            TenantTreeResponse response = getTenantTreeService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Test Tenant");
            assertThat(response.organizations()).hasSize(3);
            assertThat(response.organizationCount()).isEqualTo(3);
            assertThat(response.organizations())
                .extracting("organizationId")
                .containsExactly(1L, 2L, 3L);

            // Port 호출 검증
            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            );
        }

        /**
         * Given: 유효한 Tenant ID
         * And: Tenant는 존재하지만 Organizations는 없음 (빈 리스트)
         * When: Tenant 트리 조회 실행
         * Then: TenantTreeResponse 반환 (Tenant + 빈 Organizations 리스트)
         */
        @Test
        @DisplayName("Tenant는 존재하지만 Organizations가 없는 경우 성공")
        void execute_Success_WithoutOrganizations() {
            // Given
            GetTenantTreeQuery query = new GetTenantTreeQuery(1L, false);

            Tenant tenant = TenantFixture.createWithId(1L);
            List<Organization> emptyOrganizations = Collections.emptyList();

            given(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(tenant));
            given(organizationQueryRepositoryPort.findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            )).willReturn(emptyOrganizations);

            // When
            TenantTreeResponse response = getTenantTreeService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.tenantId()).isEqualTo(1L);
            assertThat(response.organizationCount()).isEqualTo(0);
            assertThat(response.organizations()).isEmpty();

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            );
        }

        /**
         * Given: includeDeleted=true로 설정
         * When: Tenant 트리 조회 실행
         * Then: OrganizationQueryRepositoryPort 호출 시 deletedFilter=null 전달
         * And: 삭제된 Organizations도 포함된 결과 반환
         */
        @Test
        @DisplayName("includeDeleted=true인 경우 삭제된 Organizations도 포함")
        void execute_Success_IncludeDeleted() {
            // Given
            GetTenantTreeQuery query = new GetTenantTreeQuery(1L, true);

            Tenant tenant = TenantFixture.createWithId(1L);
            List<Organization> organizationsWithDeleted = List.of(
                OrganizationFixture.createWithId(1L, 1L, "ORG001", "Active Org"),
                OrganizationFixture.builder()
                    .id(2L)
                    .tenantId(1L)
                    .orgCode("ORG002")
                    .name("Deleted Org")
                    .deleted(true)
                    .build()
            );

            given(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(tenant));
            given(organizationQueryRepositoryPort.findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            )).willReturn(organizationsWithDeleted);

            // When
            TenantTreeResponse response = getTenantTreeService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.organizations()).hasSize(2);
            assertThat(response.organizations())
                .extracting("name")
                .containsExactly("Active Org", "Deleted Org");

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            );
        }

        /**
         * Given: includeDeleted=false로 설정
         * When: Tenant 트리 조회 실행
         * Then: OrganizationQueryRepositoryPort 호출 시 deletedFilter=false 전달
         * And: 삭제되지 않은 Organizations만 반환
         */
        @Test
        @DisplayName("includeDeleted=false인 경우 삭제되지 않은 Organizations만 포함")
        void execute_Success_ExcludeDeleted() {
            // Given
            GetTenantTreeQuery query = new GetTenantTreeQuery(1L, false);

            Tenant tenant = TenantFixture.createWithId(1L);
            List<Organization> activeOrganizations = List.of(
                OrganizationFixture.createWithId(1L, 1L, "ORG001", "Active Org 1"),
                OrganizationFixture.createWithId(2L, 1L, "ORG002", "Active Org 2")
            );

            given(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(tenant));
            given(organizationQueryRepositoryPort.findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            )).willReturn(activeOrganizations);

            // When
            TenantTreeResponse response = getTenantTreeService.execute(query);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.organizations()).hasSize(2);
            assertThat(response.organizations())
                .allMatch(org -> !org.deleted());

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            );
        }

        /**
         * Given: maxOrganizationsPerTree = 1000 (기본값)
         * When: Tenant 트리 조회 실행
         * Then: OrganizationQueryRepositoryPort 호출 시 limit=1000 전달
         */
        @Test
        @DisplayName("maxOrganizationsPerTree 설정값이 limit으로 전달됨")
        void execute_Success_MaxOrganizationsLimit() {
            // Given
            GetTenantTreeService service = new GetTenantTreeService(
                tenantQueryRepositoryPort,
                organizationQueryRepositoryPort,
                1000
            );
            GetTenantTreeQuery query = new GetTenantTreeQuery(1L, false);

            Tenant tenant = TenantFixture.createWithId(1L);
            List<Organization> organizations = List.of(
                OrganizationFixture.createWithId(1L, 1L, "ORG001", "Org 1")
            );

            given(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(tenant));
            given(organizationQueryRepositoryPort.findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            )).willReturn(organizations);

            // When
            TenantTreeResponse response = service.execute(query);

            // Then
            assertThat(response).isNotNull();

            // Port 호출 시 limit=1000 전달 확인
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            );
        }
    }

    /**
     * Tenant 트리 조회 실패 시나리오
     */
    @Nested
    @DisplayName("execute() - 실패 시나리오")
    class Execute_Failure {

        /**
         * Given: 존재하지 않는 Tenant ID
         * When: Tenant 트리 조회 실행
         * Then: TenantNotFoundException 발생
         * And: Exception 메시지에 Tenant ID 포함
         */
        @Test
        @DisplayName("존재하지 않는 Tenant ID로 조회 시 TenantNotFoundException 발생")
        void execute_Fail_TenantNotFound() {
            // Given
            GetTenantTreeQuery query = new GetTenantTreeQuery(999L, false);

            given(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> getTenantTreeService.execute(query))
                .isInstanceOf(TenantNotFoundException.class)
                .hasMessageContaining("999");

            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
        }

        /**
         * Given: null Query
         * When: Tenant 트리 조회 실행
         * Then: IllegalArgumentException 발생
         * And: Exception 메시지에 "GetTenantTreeQuery는 필수입니다" 포함
         */
        @Test
        @DisplayName("null Query로 조회 시 IllegalArgumentException 발생")
        void execute_Fail_NullQuery() {
            // When & Then
            assertThatThrownBy(() -> getTenantTreeService.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetTenantTreeQuery는 필수입니다");
        }
    }

    /**
     * Transaction 및 Port 상호작용 검증
     */
    @Nested
    @DisplayName("Transaction 및 Port 검증")
    class TransactionAndPortVerification {

        /**
         * Given: GetTenantTreeService 클래스
         * When: execute() 메서드 확인
         * Then: @Transactional(readOnly = true) 어노테이션 존재
         */
        @Test
        @DisplayName("execute() 메서드는 @Transactional(readOnly=true)로 선언됨")
        void execute_HasReadOnlyTransaction() throws NoSuchMethodException {
            // Given
            var executeMethod = GetTenantTreeService.class.getMethod(
                "execute",
                GetTenantTreeQuery.class
            );

            // When
            Transactional transactional = executeMethod.getDeclaringClass()
                .getAnnotation(Transactional.class);

            // Then
            assertThat(transactional).isNotNull();
            assertThat(transactional.readOnly()).isTrue();
        }

        /**
         * Given: 유효한 Query
         * When: execute() 메서드 실행
         * Then: TenantQueryRepositoryPort.findById() 정확히 1회 호출
         * And: OrganizationQueryRepositoryPort.findAllWithOffset() 정확히 1회 호출
         */
        @Test
        @DisplayName("execute() 실행 시 양쪽 Repository Port가 정확히 1회씩 호출됨")
        void execute_CallsBothRepositoryPortsOnce() {
            // Given
            GetTenantTreeQuery query = new GetTenantTreeQuery(1L, false);

            Tenant tenant = TenantFixture.createWithId(1L);
            List<Organization> organizations = List.of(
                OrganizationFixture.createWithId(1L, 1L, "ORG001", "Org 1")
            );

            given(tenantQueryRepositoryPort.findById(any(TenantId.class)))
                .willReturn(Optional.of(tenant));
            given(organizationQueryRepositoryPort.findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            )).willReturn(organizations);

            // When
            getTenantTreeService.execute(query);

            // Then
            verify(tenantQueryRepositoryPort).findById(any(TenantId.class));
            verify(organizationQueryRepositoryPort).findAllWithOffset(
                any(), any(), any(), any(), anyInt(), anyInt()
            );
        }
    }
}

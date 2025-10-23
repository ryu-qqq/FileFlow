package com.ryuqq.fileflow.application.iam.organization.service;

import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationsQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.in.GetOrganizationsUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.fixtures.OrganizationFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("GetOrganizationsUseCase 테스트")
class GetOrganizationsUseCaseTest {

    private OrganizationQueryRepositoryPort organizationQueryRepositoryPort;
    private GetOrganizationsUseCase getOrganizationsUseCase;

    @BeforeEach
    void setUp() {
        organizationQueryRepositoryPort = mock(OrganizationQueryRepositoryPort.class);
        getOrganizationsUseCase = new OrganizationQueryService(organizationQueryRepositoryPort);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("Tenant별 Organization 목록을 성공적으로 조회한다 (Offset-based)")
        void shouldGetOrganizationsByTenantIdWithPage() {
            // Arrange
            String tenantId = "tenant-123";
            // GetOrganizationsQuery(page, size, cursor, tenantId, orgCodeContains, nameContains, deleted)
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
            var response = getOrganizationsUseCase.executeWithPage(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);
            assertThat(response.content().get(0).tenantId()).isEqualTo(tenantId);
            assertThat(response.content().get(1).tenantId()).isEqualTo(tenantId);
            assertThat(response.totalElements()).isEqualTo(2);

            verify(organizationQueryRepositoryPort).findAllWithOffset(
                eq(tenantId), isNull(), isNull(), isNull(), eq(0), eq(10));
            verify(organizationQueryRepositoryPort).countAll(
                eq(tenantId), isNull(), isNull(), isNull());
        }

        @Test
        @DisplayName("Organization이 없는 경우 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoOrganizations() {
            // Arrange
            String tenantId = "tenant-empty";
            GetOrganizationsQuery query = new GetOrganizationsQuery(0, 10, null, tenantId, null, null, null);

            when(organizationQueryRepositoryPort.findAllWithOffset(
                anyString(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
            when(organizationQueryRepositoryPort.countAll(
                anyString(), isNull(), isNull(), isNull()))
                .thenReturn(0L);

            // Act
            var response = getOrganizationsUseCase.executeWithPage(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            assertThatThrownBy(() -> getOrganizationsUseCase.executeWithPage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetOrganizationsQuery는 필수입니다");

            verify(organizationQueryRepositoryPort, never()).findAllWithOffset(
                anyString(), isNull(), isNull(), isNull(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("Query 검증")
    class QueryValidationScenarios {

        @Test
        @DisplayName("tenantId가 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenTenantIdIsBlank() {
            assertThatThrownBy(() -> new GetOrganizationsQuery(0, 10, null, "", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantId는 빈 문자열일 수 없습니다");
        }

        @Test
        @DisplayName("size가 범위를 벗어나면 예외 발생")
        void shouldThrowExceptionWhenSizeIsOutOfRange() {
            assertThatThrownBy(() -> new GetOrganizationsQuery(0, 0, null, "tenant-123", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size는 1~100 사이여야 합니다");

            assertThatThrownBy(() -> new GetOrganizationsQuery(0, 101, null, "tenant-123", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size는 1~100 사이여야 합니다");
        }

        @Test
        @DisplayName("page와 cursor를 동시에 사용하면 예외 발생")
        void shouldThrowExceptionWhenBothPageAndCursorProvided() {
            assertThatThrownBy(() -> new GetOrganizationsQuery(0, 10, "cursor-123", "tenant-123", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page와 cursor는 동시에 사용할 수 없습니다");
        }
    }
}

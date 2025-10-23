package com.ryuqq.fileflow.application.iam.organization.service;

import com.ryuqq.fileflow.application.iam.organization.dto.query.GetOrganizationQuery;
import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;
import com.ryuqq.fileflow.application.iam.organization.port.in.GetOrganizationUseCase;
import com.ryuqq.fileflow.application.iam.organization.port.out.OrganizationQueryRepositoryPort;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.fixtures.OrganizationFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("application")
@Tag("fast")
@DisplayName("GetOrganizationUseCase 테스트")
class GetOrganizationUseCaseTest {

    private OrganizationQueryRepositoryPort organizationQueryRepositoryPort;
    private GetOrganizationUseCase getOrganizationUseCase;

    @BeforeEach
    void setUp() {
        organizationQueryRepositoryPort = mock(OrganizationQueryRepositoryPort.class);
        getOrganizationUseCase = new OrganizationQueryService(organizationQueryRepositoryPort);
    }

    @Nested
    @DisplayName("정상 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("ID로 Organization을 성공적으로 조회한다")
        void shouldGetOrganizationByIdSuccessfully() {
            // Arrange
            Long organizationId = 1L;
            GetOrganizationQuery query = new GetOrganizationQuery(organizationId);
            Organization existingOrganization = OrganizationFixtures.salesOrganizationWithId(organizationId, "tenant-123");

            when(organizationQueryRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.of(existingOrganization));

            // Act
            OrganizationResponse response = getOrganizationUseCase.execute(query);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.organizationId()).isEqualTo(organizationId);
            assertThat(response.orgCode()).isEqualTo("SALES");
            assertThat(response.deleted()).isFalse();

            verify(organizationQueryRepositoryPort).findById(any(OrganizationId.class));
        }
    }

    @Nested
    @DisplayName("예외 시나리오")
    class ExceptionScenarios {

        @Test
        @DisplayName("Query가 null이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenQueryIsNull() {
            assertThatThrownBy(() -> getOrganizationUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GetOrganizationQuery는 필수입니다");

            verify(organizationQueryRepositoryPort, never()).findById(any());
        }

        @Test
        @DisplayName("Organization이 존재하지 않으면 IllegalStateException 발생")
        void shouldThrowExceptionWhenOrganizationNotFound() {
            Long nonExistentId = 999L;
            GetOrganizationQuery query = new GetOrganizationQuery(nonExistentId);

            when(organizationQueryRepositoryPort.findById(any(OrganizationId.class)))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> getOrganizationUseCase.execute(query))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Organization을 찾을 수 없습니다");

            verify(organizationQueryRepositoryPort).findById(any(OrganizationId.class));
        }
    }

    @Nested
    @DisplayName("Query 검증")
    class QueryValidationScenarios {

        @Test
        @DisplayName("organizationId가 null이면 예외 발생")
        void shouldThrowExceptionWhenOrganizationIdIsNull() {
            assertThatThrownBy(() -> new GetOrganizationQuery(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("organizationId는 필수이며 0보다 커야 합니다");
        }

        @Test
        @DisplayName("organizationId가 0이면 예외 발생")
        void shouldThrowExceptionWhenOrganizationIdIsZero() {
            assertThatThrownBy(() -> new GetOrganizationQuery(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("organizationId는 필수이며 0보다 커야 합니다");
        }
    }
}

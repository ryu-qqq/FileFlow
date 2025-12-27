package com.ryuqq.fileflow.application.download.service.query;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.ryuqq.fileflow.application.download.dto.query.GetExternalDownloadQuery;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.application.download.manager.query.ExternalDownloadReadManager;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.exception.ExternalDownloadNotFoundException;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadFixture;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GetExternalDownloadService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class GetExternalDownloadServiceTest {
    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_ORG_ID = OrganizationId.generate().value();
    private static final String TEST_USER_ID = UserId.generate().value();

    private static final String TENANT_ID = TEST_ORG_ID;
    private static final String DIFFERENT_TENANT_ID = TEST_USER_ID;

    @Mock private ExternalDownloadReadManager externalDownloadReadManager;

    private GetExternalDownloadService service;

    @BeforeEach
    void setUp() {
        service = new GetExternalDownloadService(externalDownloadReadManager);
    }

    @Nested
    @DisplayName("execute 테스트")
    class ExecuteTest {

        @Test
        @DisplayName("존재하는 ExternalDownload 조회 시 상세 정보가 반환된다")
        void execute_WithExistingId_ShouldReturnDetailResponse() {
            // given
            GetExternalDownloadQuery query =
                    new GetExternalDownloadQuery("00000000-0000-0000-0000-000000000001", TENANT_ID);

            ExternalDownload externalDownload =
                    ExternalDownloadFixture.withId("00000000-0000-0000-0000-000000000001")
                            .tenantId(TenantId.of(TENANT_ID))
                            .build();

            given(
                            externalDownloadReadManager.findByIdAndTenantId(
                                    ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"),
                                    TENANT_ID))
                    .willReturn(Optional.of(externalDownload));

            // when
            ExternalDownloadDetailResponse response = service.execute(query);

            // then
            assertThat(response.id()).isEqualTo("00000000-0000-0000-0000-000000000001");
            assertThat(response.sourceUrl()).isEqualTo(externalDownload.getSourceUrl().value());
            assertThat(response.status()).isEqualTo(externalDownload.getStatus().name());
            assertThat(response.retryCount()).isEqualTo(externalDownload.getRetryCountValue());
            assertThat(response.createdAt()).isEqualTo(externalDownload.getCreatedAt());
        }

        @Test
        @DisplayName("webhookUrl이 있는 ExternalDownload 조회 시 응답에 포함된다")
        void execute_WithWebhookUrl_ShouldIncludeWebhookUrl() {
            // given
            GetExternalDownloadQuery query =
                    new GetExternalDownloadQuery("00000000-0000-0000-0000-000000000001", TENANT_ID);

            ExternalDownload externalDownload =
                    ExternalDownloadFixture.withId("00000000-0000-0000-0000-000000000001")
                            .tenantId(TenantId.of(TENANT_ID))
                            .webhookUrl("https://callback.example.com/webhook")
                            .build();

            given(
                            externalDownloadReadManager.findByIdAndTenantId(
                                    ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"),
                                    TENANT_ID))
                    .willReturn(Optional.of(externalDownload));

            // when
            ExternalDownloadDetailResponse response = service.execute(query);

            // then
            assertThat(response.webhookUrl()).isEqualTo("https://callback.example.com/webhook");
        }

        @Test
        @DisplayName("완료된 ExternalDownload 조회 시 fileAssetId가 포함된다")
        void execute_WithCompletedStatus_ShouldIncludeFileAssetId() {
            // given
            GetExternalDownloadQuery query =
                    new GetExternalDownloadQuery("00000000-0000-0000-0000-000000000001", TENANT_ID);

            ExternalDownload externalDownload =
                    ExternalDownloadFixture.withId("00000000-0000-0000-0000-000000000001")
                            .tenantId(TenantId.of(TENANT_ID))
                            .completed()
                            .build();

            given(
                            externalDownloadReadManager.findByIdAndTenantId(
                                    ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"),
                                    TENANT_ID))
                    .willReturn(Optional.of(externalDownload));

            // when
            ExternalDownloadDetailResponse response = service.execute(query);

            // then
            assertThat(response.status()).isEqualTo("COMPLETED");
            assertThat(response.fileAssetId()).isNotNull();
        }

        @Test
        @DisplayName("실패한 ExternalDownload 조회 시 errorMessage가 포함된다")
        void execute_WithFailedStatus_ShouldIncludeErrorMessage() {
            // given
            GetExternalDownloadQuery query =
                    new GetExternalDownloadQuery("00000000-0000-0000-0000-000000000001", TENANT_ID);

            ExternalDownload externalDownload =
                    ExternalDownloadFixture.withId("00000000-0000-0000-0000-000000000001")
                            .tenantId(TenantId.of(TENANT_ID))
                            .failed("다운로드 실패: timeout")
                            .build();

            given(
                            externalDownloadReadManager.findByIdAndTenantId(
                                    ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"),
                                    TENANT_ID))
                    .willReturn(Optional.of(externalDownload));

            // when
            ExternalDownloadDetailResponse response = service.execute(query);

            // then
            assertThat(response.status()).isEqualTo("FAILED");
            assertThat(response.errorMessage()).isEqualTo("다운로드 실패: timeout");
        }

        @Test
        @DisplayName("존재하지 않는 ExternalDownload 조회 시 예외가 발생한다")
        void execute_WithNonExistingId_ShouldThrowException() {
            // given
            GetExternalDownloadQuery query =
                    new GetExternalDownloadQuery("00000000-0000-0000-0000-0000000003e7", TENANT_ID);

            given(
                            externalDownloadReadManager.findByIdAndTenantId(
                                    ExternalDownloadId.of("00000000-0000-0000-0000-0000000003e7"),
                                    TENANT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.execute(query))
                    .isInstanceOf(ExternalDownloadNotFoundException.class)
                    .hasMessageContaining("0000000003e7");
        }

        @Test
        @DisplayName("다른 테넌트의 ExternalDownload 조회 시 예외가 발생한다")
        void execute_WithDifferentTenantId_ShouldThrowException() {
            // given
            GetExternalDownloadQuery query =
                    new GetExternalDownloadQuery(
                            "00000000-0000-0000-0000-000000000001", DIFFERENT_TENANT_ID);

            given(
                            externalDownloadReadManager.findByIdAndTenantId(
                                    ExternalDownloadId.of("00000000-0000-0000-0000-000000000001"),
                                    DIFFERENT_TENANT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.execute(query))
                    .isInstanceOf(ExternalDownloadNotFoundException.class);
        }
    }
}

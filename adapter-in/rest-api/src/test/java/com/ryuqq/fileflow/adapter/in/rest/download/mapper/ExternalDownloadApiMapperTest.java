package com.ryuqq.fileflow.adapter.in.rest.download.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.download.dto.command.RequestExternalDownloadApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.ExternalDownloadApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.download.dto.response.ExternalDownloadDetailApiResponse;
import com.ryuqq.fileflow.application.download.dto.command.RequestExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.query.GetExternalDownloadQuery;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ExternalDownloadApiMapper 단위 테스트.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ExternalDownloadApiMapper 단위 테스트")
class ExternalDownloadApiMapperTest {
    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_TENANT_ID = TenantId.generate().value();
    private static final String TEST_ORG_ID = OrganizationId.generate().value();
    private static final String TEST_USER_ID = UserId.generate().value();

    private ExternalDownloadApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ExternalDownloadApiMapper();
    }

    @Nested
    @DisplayName("toCommand 테스트")
    class ToCommandTest {

        @Test
        @DisplayName("API Request를 Command로 변환할 수 있다")
        void toCommand_WithValidRequest_ShouldSucceed() {
            // given
            String idempotencyKey = UUID.randomUUID().toString();
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest(
                            idempotencyKey,
                            "https://example.com/image.jpg",
                            "https://webhook.com/notify");
            String tenantId = TEST_TENANT_ID;
            String organizationId = TEST_ORG_ID;

            // when
            RequestExternalDownloadCommand command =
                    mapper.toCommand(request, tenantId, organizationId);

            // then
            assertThat(command.idempotencyKey()).isEqualTo(idempotencyKey);
            assertThat(command.sourceUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(command.tenantId()).isEqualTo(tenantId);
            assertThat(command.organizationId()).isEqualTo(organizationId);
            assertThat(command.webhookUrl()).isEqualTo("https://webhook.com/notify");
        }

        @Test
        @DisplayName("webhookUrl이 null인 경우도 변환할 수 있다")
        void toCommand_WithNullWebhookUrl_ShouldSucceed() {
            // given
            String idempotencyKey = UUID.randomUUID().toString();
            RequestExternalDownloadApiRequest request =
                    new RequestExternalDownloadApiRequest(
                            idempotencyKey, "https://example.com/image.jpg", null);
            String tenantId = TEST_ORG_ID;
            String organizationId = TEST_USER_ID;

            // when
            RequestExternalDownloadCommand command =
                    mapper.toCommand(request, tenantId, organizationId);

            // then
            assertThat(command.idempotencyKey()).isEqualTo(idempotencyKey);
            assertThat(command.sourceUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(command.webhookUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("toQuery 테스트")
    class ToQueryTest {

        @Test
        @DisplayName("id와 tenantId로 Query를 생성할 수 있다")
        void toQuery_WithIdAndTenantId_ShouldSucceed() {
            // given
            String id = "download-123";
            String tenantId = TEST_TENANT_ID;

            // when
            GetExternalDownloadQuery query = mapper.toQuery(id, tenantId);

            // then
            assertThat(query.id()).isEqualTo(id);
            assertThat(query.tenantId()).isEqualTo(tenantId);
        }
    }

    @Nested
    @DisplayName("toApiResponse 테스트")
    class ToApiResponseTest {

        @Test
        @DisplayName("Application Response를 API Response로 변환할 수 있다")
        void toApiResponse_WithValidResponse_ShouldSucceed() {
            // given
            Instant createdAt = Instant.parse("2025-12-10T10:00:00Z");
            ExternalDownloadResponse response =
                    new ExternalDownloadResponse("download-123", "PENDING", createdAt);

            // when
            ExternalDownloadApiResponse apiResponse = mapper.toApiResponse(response);

            // then
            assertThat(apiResponse.id()).isEqualTo("download-123");
            assertThat(apiResponse.status()).isEqualTo("PENDING");
            assertThat(apiResponse.createdAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("toDetailApiResponse 테스트")
    class ToDetailApiResponseTest {

        @Test
        @DisplayName("Application Detail Response를 API Detail Response로 변환할 수 있다")
        void toDetailApiResponse_WithValidResponse_ShouldSucceed() {
            // given
            Instant createdAt = Instant.parse("2025-12-10T10:00:00Z");
            Instant updatedAt = Instant.parse("2025-12-10T10:05:00Z");
            ExternalDownloadDetailResponse response =
                    new ExternalDownloadDetailResponse(
                            "download-123",
                            "https://example.com/image.jpg",
                            "COMPLETED",
                            "asset-456",
                            null,
                            0,
                            "https://webhook.com",
                            createdAt,
                            updatedAt);

            // when
            ExternalDownloadDetailApiResponse apiResponse = mapper.toDetailApiResponse(response);

            // then
            assertThat(apiResponse.id()).isEqualTo("download-123");
            assertThat(apiResponse.sourceUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(apiResponse.status()).isEqualTo("COMPLETED");
            assertThat(apiResponse.fileAssetId()).isEqualTo("asset-456");
            assertThat(apiResponse.errorMessage()).isNull();
            assertThat(apiResponse.retryCount()).isEqualTo(0);
            assertThat(apiResponse.webhookUrl()).isEqualTo("https://webhook.com");
            assertThat(apiResponse.createdAt()).isEqualTo(createdAt);
            assertThat(apiResponse.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("실패 상태의 Response를 변환할 수 있다")
        void toDetailApiResponse_WithFailedResponse_ShouldSucceed() {
            // given
            Instant now = Instant.now();
            ExternalDownloadDetailResponse response =
                    new ExternalDownloadDetailResponse(
                            "download-123",
                            "https://example.com/image.jpg",
                            "FAILED",
                            null,
                            "Connection timeout",
                            3,
                            null,
                            now,
                            now);

            // when
            ExternalDownloadDetailApiResponse apiResponse = mapper.toDetailApiResponse(response);

            // then
            assertThat(apiResponse.status()).isEqualTo("FAILED");
            assertThat(apiResponse.fileAssetId()).isNull();
            assertThat(apiResponse.errorMessage()).isEqualTo("Connection timeout");
            assertThat(apiResponse.retryCount()).isEqualTo(3);
            assertThat(apiResponse.webhookUrl()).isNull();
        }

        @Test
        @DisplayName("모든 nullable 필드가 null인 경우도 변환할 수 있다")
        void toDetailApiResponse_WithAllNullableFieldsNull_ShouldSucceed() {
            // given
            Instant now = Instant.now();
            ExternalDownloadDetailResponse response =
                    new ExternalDownloadDetailResponse(
                            "download-123",
                            "https://example.com/image.jpg",
                            "PENDING",
                            null,
                            null,
                            0,
                            null,
                            now,
                            now);

            // when
            ExternalDownloadDetailApiResponse apiResponse = mapper.toDetailApiResponse(response);

            // then
            assertThat(apiResponse.fileAssetId()).isNull();
            assertThat(apiResponse.errorMessage()).isNull();
            assertThat(apiResponse.webhookUrl()).isNull();
        }
    }
}

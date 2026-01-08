package com.ryuqq.fileflow.sdk.client;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.ryuqq.fileflow.sdk.api.ExternalDownloadApi;
import com.ryuqq.fileflow.sdk.exception.FileFlowBadRequestException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadApi WireMock 통합 테스트")
@WireMockTest
class ExternalDownloadApiIntegrationTest extends WireMockTestSupport {

    private ExternalDownloadApi externalDownloadApi;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        FileFlowClient client = createClient(wmRuntimeInfo);
        externalDownloadApi = client.externalDownloads();
    }

    @Nested
    @DisplayName("request 메서드")
    class RequestTest {

        @Test
        @DisplayName("외부 URL 다운로드를 요청하고 ID를 반환받을 수 있다")
        void shouldRequestExternalDownloadAndReturnId() {
            // given
            String idempotencyKey = "550e8400-e29b-41d4-a716-446655440000";
            String sourceUrl = "https://example.com/file.pdf";
            String webhookUrl = "https://my-service.com/webhook";

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/external-downloads")))
                            .willReturn(
                                    successResponse(wrapSuccessResponse("\"ext-download-123\""))));

            // when
            String downloadId = externalDownloadApi.request(idempotencyKey, sourceUrl, webhookUrl);

            // then
            assertThat(downloadId).isEqualTo("ext-download-123");
        }

        @Test
        @DisplayName("webhookUrl 없이 다운로드를 요청할 수 있다")
        void shouldRequestExternalDownloadWithoutWebhook() {
            // given
            String idempotencyKey = "550e8400-e29b-41d4-a716-446655440001";
            String sourceUrl = "https://example.com/file.pdf";

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/external-downloads")))
                            .willReturn(
                                    successResponse(wrapSuccessResponse("\"ext-download-456\""))));

            // when
            String downloadId = externalDownloadApi.request(idempotencyKey, sourceUrl);

            // then
            assertThat(downloadId).isEqualTo("ext-download-456");
        }

        @Test
        @DisplayName("요청 본문에 idempotencyKey가 포함된다")
        void shouldIncludeIdempotencyKeyInRequestBody() {
            // given
            String idempotencyKey = "550e8400-e29b-41d4-a716-446655440002";
            String sourceUrl = "https://example.com/file.pdf";

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/external-downloads")))
                            .willReturn(
                                    successResponse(wrapSuccessResponse("\"ext-download-789\""))));

            // when
            externalDownloadApi.request(idempotencyKey, sourceUrl);

            // then - idempotencyKey가 요청에 포함되었는지 검증
            verify(
                    postRequestedFor(urlPathEqualTo("/api/v1/file/external-downloads"))
                            .withRequestBody(containing("\"idempotencyKey\"")));
        }

        @Test
        @DisplayName("잘못된 URL로 요청하면 BadRequestException이 발생한다")
        void shouldThrowBadRequestExceptionForInvalidUrl() {
            // given
            String idempotencyKey = "550e8400-e29b-41d4-a716-446655440003";
            String invalidUrl = "not-a-valid-url";

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/external-downloads")))
                            .willReturn(
                                    errorResponse(
                                            400,
                                            "INVALID_URL",
                                            "Source URL must be a valid HTTP/HTTPS URL")));

            // when & then
            assertThatThrownBy(() -> externalDownloadApi.request(idempotencyKey, invalidUrl))
                    .isInstanceOf(FileFlowBadRequestException.class);
        }
    }

    @Nested
    @DisplayName("재시도 안전성 테스트 (멱등성)")
    class IdempotencyTest {

        @Test
        @DisplayName("동일한 idempotencyKey로 재시도하면 같은 결과를 반환한다")
        void shouldReturnSameResultForRetryWithSameIdempotencyKey() {
            // given
            String idempotencyKey = "550e8400-e29b-41d4-a716-446655440004";
            String sourceUrl = "https://example.com/file.pdf";
            String expectedDownloadId = "ext-download-same";

            // 동일한 idempotencyKey로 요청하면 항상 같은 ID 반환
            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/external-downloads")))
                            .willReturn(
                                    successResponse(
                                            wrapSuccessResponse(
                                                    "\"" + expectedDownloadId + "\""))));

            // when - 첫 번째 요청
            String firstResult = externalDownloadApi.request(idempotencyKey, sourceUrl);

            // when - 두 번째 요청 (재시도)
            String secondResult = externalDownloadApi.request(idempotencyKey, sourceUrl);

            // then - 같은 결과 반환
            assertThat(firstResult).isEqualTo(expectedDownloadId);
            assertThat(secondResult).isEqualTo(expectedDownloadId);
        }
    }

    @Nested
    @DisplayName("인증 헤더 테스트")
    class AuthenticationTest {

        @Test
        @DisplayName("Bearer 토큰이 Authorization 헤더에 포함된다")
        void shouldIncludeBearerTokenInAuthorizationHeader() {
            // given
            String idempotencyKey = "550e8400-e29b-41d4-a716-446655440005";
            String sourceUrl = "https://example.com/file.pdf";

            // Authorization 헤더가 정확히 일치해야만 응답 반환
            stubFor(
                    post(urlPathEqualTo("/api/v1/file/external-downloads"))
                            .withHeader("Authorization", equalTo("Bearer test-service-token"))
                            .willReturn(
                                    successResponse(wrapSuccessResponse("\"ext-download-auth\""))));

            // when
            String downloadId = externalDownloadApi.request(idempotencyKey, sourceUrl);

            // then
            assertThat(downloadId).isEqualTo("ext-download-auth");
        }
    }

    @Nested
    @DisplayName("요청 본문 구조 테스트")
    class RequestBodyTest {

        @Test
        @DisplayName("webhookUrl이 있으면 요청 본문에 포함된다")
        void shouldIncludeWebhookUrlInRequestBody() {
            // given
            String idempotencyKey = "550e8400-e29b-41d4-a716-446655440006";
            String sourceUrl = "https://example.com/file.pdf";
            String webhookUrl = "https://my-service.com/webhook";

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/external-downloads")))
                            .willReturn(
                                    successResponse(
                                            wrapSuccessResponse("\"ext-download-webhook\""))));

            // when
            externalDownloadApi.request(idempotencyKey, sourceUrl, webhookUrl);

            // then
            verify(
                    postRequestedFor(urlPathEqualTo("/api/v1/file/external-downloads"))
                            .withRequestBody(containing("\"webhookUrl\"")));
        }

        @Test
        @DisplayName("webhookUrl이 null이면 요청 본문에 포함되지 않는다")
        void shouldNotIncludeWebhookUrlWhenNull() {
            // given
            String idempotencyKey = "550e8400-e29b-41d4-a716-446655440007";
            String sourceUrl = "https://example.com/file.pdf";

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/external-downloads")))
                            .willReturn(
                                    successResponse(
                                            wrapSuccessResponse("\"ext-download-no-webhook\""))));

            // when
            externalDownloadApi.request(idempotencyKey, sourceUrl, null);

            // then - 요청이 성공적으로 처리됨
            verify(postRequestedFor(urlPathEqualTo("/api/v1/file/external-downloads")));
        }
    }

    @Nested
    @DisplayName("get 메서드")
    class GetTest {

        @Test
        @DisplayName("ID로 외부 다운로드 상세 정보를 조회할 수 있다")
        void shouldGetExternalDownloadDetail() {
            // given
            String id = "ext-download-123";

            String responseData =
                    """
                    {
                        "id": "ext-download-123",
                        "sourceUrl": "https://example.com/file.pdf",
                        "status": "COMPLETED",
                        "fileAssetId": "file-asset-456",
                        "errorMessage": null,
                        "retryCount": 0,
                        "webhookUrl": "https://my-service.com/webhook",
                        "createdAt": "2025-01-15T10:00:00Z",
                        "updatedAt": "2025-01-15T10:05:00Z"
                    }
                    """;

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/external-downloads/ext-download-123")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            ExternalDownloadDetailResponse response = externalDownloadApi.get(id);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("ext-download-123");
            assertThat(response.getSourceUrl()).isEqualTo("https://example.com/file.pdf");
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getFileAssetId()).isEqualTo("file-asset-456");
            assertThat(response.getRetryCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void shouldThrowNotFoundExceptionWhenNotExists() {
            // given
            String id = "non-existent-id";

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/external-downloads/non-existent-id")))
                            .willReturn(
                                    errorResponse(
                                            404,
                                            "NOT_FOUND",
                                            "External download not found")));

            // when & then
            assertThatThrownBy(() -> externalDownloadApi.get(id))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("list 메서드")
    class ListTest {

        @Test
        @DisplayName("외부 다운로드 목록을 페이지네이션으로 조회할 수 있다")
        void shouldListExternalDownloads() {
            // given
            ExternalDownloadSearchRequest request =
                    ExternalDownloadSearchRequest.builder()
                            .page(0)
                            .size(10)
                            .build();

            String responseData =
                    """
                    {
                        "content": [
                            {
                                "id": "ext-download-1",
                                "status": "COMPLETED",
                                "createdAt": "2025-01-15T10:00:00Z"
                            },
                            {
                                "id": "ext-download-2",
                                "status": "PROCESSING",
                                "createdAt": "2025-01-15T10:05:00Z"
                            }
                        ],
                        "page": 0,
                        "size": 10,
                        "totalElements": 2,
                        "totalPages": 1,
                        "first": true,
                        "last": true
                    }
                    """;

            stubFor(
                    withAuth(get(urlEqualTo("/api/v1/file/external-downloads?page=0&size=10")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            PageResponse<ExternalDownloadResponse> response = externalDownloadApi.list(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent().get(0).getId()).isEqualTo("ext-download-1");
            assertThat(response.getContent().get(0).getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getContent().get(1).getId()).isEqualTo("ext-download-2");
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("상태 필터로 외부 다운로드를 조회할 수 있다")
        void shouldListExternalDownloadsWithStatusFilter() {
            // given
            ExternalDownloadSearchRequest request =
                    ExternalDownloadSearchRequest.builder()
                            .page(0)
                            .size(10)
                            .status("COMPLETED")
                            .build();

            String responseData =
                    """
                    {
                        "content": [
                            {
                                "id": "ext-download-1",
                                "status": "COMPLETED",
                                "createdAt": "2025-01-15T10:00:00Z"
                            }
                        ],
                        "page": 0,
                        "size": 10,
                        "totalElements": 1,
                        "totalPages": 1,
                        "first": true,
                        "last": true
                    }
                    """;

            stubFor(
                    withAuth(
                                    get(
                                            urlEqualTo(
                                                    "/api/v1/file/external-downloads?page=0&size=10&status=COMPLETED")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            PageResponse<ExternalDownloadResponse> response = externalDownloadApi.list(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getStatus()).isEqualTo("COMPLETED");
        }
    }
}

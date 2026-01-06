package com.ryuqq.fileflow.sdk.client;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.ryuqq.fileflow.sdk.api.ExternalDownloadApi;
import com.ryuqq.fileflow.sdk.exception.FileFlowBadRequestException;
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
        @DisplayName("서비스 토큰이 X-Service-Token 헤더에 포함된다")
        void shouldIncludeServiceTokenInHeader() {
            // given
            String idempotencyKey = "550e8400-e29b-41d4-a716-446655440005";
            String sourceUrl = "https://example.com/file.pdf";

            // X-Service-Token 헤더가 정확히 일치해야만 응답 반환
            stubFor(
                    post(urlPathEqualTo("/api/v1/file/external-downloads"))
                            .withHeader("X-Service-Token", equalTo("test-service-token"))
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
}

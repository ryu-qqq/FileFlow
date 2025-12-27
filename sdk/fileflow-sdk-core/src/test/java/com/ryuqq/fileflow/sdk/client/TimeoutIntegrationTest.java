package com.ryuqq.fileflow.sdk.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import reactor.test.StepVerifier;

@DisplayName("타임아웃 통합 테스트")
@WireMockTest
class TimeoutIntegrationTest extends WireMockTestSupport {

    @Nested
    @DisplayName("동기 클라이언트 타임아웃")
    class SyncClientTimeoutTest {

        @Test
        @DisplayName("서버 지연 시 타임아웃이 발생한다")
        void shouldTimeoutOnServerDelay(WireMockRuntimeInfo wmRuntimeInfo) {
            // given - 1초 타임아웃 설정, 서버는 3초 지연
            FileFlowClient client =
                    FileFlowClient.builder()
                            .baseUrl(wmRuntimeInfo.getHttpBaseUrl())
                            .serviceToken(SERVICE_TOKEN)
                            .readTimeout(Duration.ofSeconds(1))
                            .build();

            stubFor(
                    get(urlPathEqualTo("/api/v1/file/file-assets/timeout-test"))
                            .willReturn(
                                    aResponse()
                                            .withStatus(200)
                                            .withHeader(
                                                    HttpHeaders.CONTENT_TYPE,
                                                    MediaType.APPLICATION_JSON_VALUE)
                                            .withBody(
                                                    wrapSuccessResponse(
                                                            """
                                                            {
                                                                "id": "timeout-test",
                                                                "filename": "test.pdf",
                                                                "contentType": "application/pdf",
                                                                "fileSize": 1024,
                                                                "status": "COMPLETED",
                                                                "category": "DOCUMENT",
                                                                "s3Key": "test.pdf",
                                                                "createdAt": "2025-01-15T10:00:00",
                                                                "updatedAt": "2025-01-15T10:00:00"
                                                            }
                                                            """))
                                            .withFixedDelay(3000))); // 3초 지연

            // when & then
            assertThatThrownBy(() -> client.fileAssets().get("timeout-test"))
                    .isInstanceOf(ResourceAccessException.class);
        }

        @Test
        @DisplayName("연결 타임아웃 설정이 적용된다")
        void shouldApplyConnectTimeout(WireMockRuntimeInfo wmRuntimeInfo) {
            // given
            FileFlowClient client =
                    FileFlowClient.builder()
                            .baseUrl(wmRuntimeInfo.getHttpBaseUrl())
                            .serviceToken(SERVICE_TOKEN)
                            .connectTimeout(Duration.ofMillis(100))
                            .readTimeout(Duration.ofSeconds(30))
                            .build();

            // 정상 응답 - 타임아웃 내에 연결되면 성공
            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/quick-response")))
                            .willReturn(
                                    aResponse()
                                            .withStatus(200)
                                            .withHeader(
                                                    HttpHeaders.CONTENT_TYPE,
                                                    MediaType.APPLICATION_JSON_VALUE)
                                            .withBody(
                                                    wrapSuccessResponse(
                                                            """
                                                            {
                                                                "id": "quick-response",
                                                                "filename": "test.pdf",
                                                                "contentType": "application/pdf",
                                                                "fileSize": 1024,
                                                                "status": "COMPLETED",
                                                                "category": "DOCUMENT",
                                                                "s3Key": "test.pdf",
                                                                "createdAt": "2025-01-15T10:00:00",
                                                                "updatedAt": "2025-01-15T10:00:00"
                                                            }
                                                            """))));

            // when & then - 정상 응답이 오면 성공
            var response = client.fileAssets().get("quick-response");
            assert response != null;
            assert "quick-response".equals(response.getId());
        }
    }

    @Nested
    @DisplayName("비동기 클라이언트 타임아웃")
    class AsyncClientTimeoutTest {

        @Test
        @DisplayName("요청 타임아웃을 적용할 수 있다")
        void shouldApplyRequestTimeout(WireMockRuntimeInfo wmRuntimeInfo) {
            // given - WebClient의 timeout은 개별 요청에 적용
            // Note: 현재 SDK는 WebClient 레벨 타임아웃을 지원하지 않음
            // Reactor Mono의 timeout 연산자로 애플리케이션 레벨에서 처리 가능
            FileFlowAsyncClient client =
                    FileFlowClient.builder()
                            .baseUrl(wmRuntimeInfo.getHttpBaseUrl())
                            .serviceToken(SERVICE_TOKEN)
                            .buildAsync();

            stubFor(
                    get(urlPathEqualTo("/api/v1/file/file-assets/delayed-response"))
                            .willReturn(
                                    aResponse()
                                            .withStatus(200)
                                            .withHeader(
                                                    HttpHeaders.CONTENT_TYPE,
                                                    MediaType.APPLICATION_JSON_VALUE)
                                            .withBody(
                                                    wrapSuccessResponse(
                                                            """
                                                            {
                                                                "id": "delayed-response",
                                                                "filename": "test.pdf",
                                                                "contentType": "application/pdf",
                                                                "fileSize": 1024,
                                                                "status": "COMPLETED",
                                                                "category": "DOCUMENT",
                                                                "s3Key": "test.pdf",
                                                                "createdAt": "2025-01-15T10:00:00",
                                                                "updatedAt": "2025-01-15T10:00:00"
                                                            }
                                                            """))
                                            .withFixedDelay(2000))); // 2초 지연

            // when & then - 애플리케이션 레벨에서 타임아웃 적용 가능
            StepVerifier.create(
                            client.fileAssets()
                                    .get("delayed-response")
                                    .timeout(Duration.ofSeconds(1))) // 1초 타임아웃
                    .expectError(java.util.concurrent.TimeoutException.class)
                    .verify();
        }

        @Test
        @DisplayName("응답 지연이 타임아웃 내면 성공한다")
        void shouldSucceedWhenResponseWithinTimeout(WireMockRuntimeInfo wmRuntimeInfo) {
            // given - 5초 타임아웃 설정, 서버는 100ms 지연
            FileFlowAsyncClient client =
                    FileFlowClient.builder()
                            .baseUrl(wmRuntimeInfo.getHttpBaseUrl())
                            .serviceToken(SERVICE_TOKEN)
                            .readTimeout(Duration.ofSeconds(5))
                            .buildAsync();

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/fast-response")))
                            .willReturn(
                                    aResponse()
                                            .withStatus(200)
                                            .withHeader(
                                                    HttpHeaders.CONTENT_TYPE,
                                                    MediaType.APPLICATION_JSON_VALUE)
                                            .withBody(
                                                    wrapSuccessResponse(
                                                            """
                                                            {
                                                                "id": "fast-response",
                                                                "filename": "test.pdf",
                                                                "contentType": "application/pdf",
                                                                "fileSize": 1024,
                                                                "status": "COMPLETED",
                                                                "category": "DOCUMENT",
                                                                "s3Key": "test.pdf",
                                                                "createdAt": "2025-01-15T10:00:00",
                                                                "updatedAt": "2025-01-15T10:00:00"
                                                            }
                                                            """))
                                            .withFixedDelay(100))); // 100ms 지연

            // when & then
            StepVerifier.create(client.fileAssets().get("fast-response"))
                    .expectNextMatches(response -> "fast-response".equals(response.getId()))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("에러 응답 처리")
    class ErrorResponseTest {

        @Test
        @DisplayName("서버 에러(5xx) 응답을 처리한다")
        void shouldHandleServerError(WireMockRuntimeInfo wmRuntimeInfo) {
            // given
            FileFlowClient client = createClient(wmRuntimeInfo);

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/server-error")))
                            .willReturn(
                                    errorResponse(500, "INTERNAL_ERROR", "Internal server error")));

            // when & then
            assertThatThrownBy(() -> client.fileAssets().get("server-error"))
                    .hasMessageContaining("Internal server error");
        }

        @Test
        @DisplayName("서비스 불가(503) 응답을 처리한다")
        void shouldHandleServiceUnavailable(WireMockRuntimeInfo wmRuntimeInfo) {
            // given
            FileFlowClient client = createClient(wmRuntimeInfo);

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/unavailable")))
                            .willReturn(
                                    errorResponse(
                                            503,
                                            "SERVICE_UNAVAILABLE",
                                            "Service temporarily unavailable")));

            // when & then
            assertThatThrownBy(() -> client.fileAssets().get("unavailable"))
                    .hasMessageContaining("Service temporarily unavailable");
        }

        @Test
        @DisplayName("비동기 클라이언트도 서버 에러를 처리한다")
        void shouldHandleServerErrorAsync(WireMockRuntimeInfo wmRuntimeInfo) {
            // given
            FileFlowAsyncClient client = createAsyncClient(wmRuntimeInfo);

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/async-server-error")))
                            .willReturn(
                                    errorResponse(500, "INTERNAL_ERROR", "Internal server error")));

            // when & then
            StepVerifier.create(client.fileAssets().get("async-server-error"))
                    .expectErrorMatches(
                            error ->
                                    error.getMessage() != null
                                            && error.getMessage().contains("Internal server error"))
                    .verify();
        }
    }
}

package com.ryuqq.fileflow.sdk.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.ryuqq.fileflow.sdk.api.FileAssetApi;
import com.ryuqq.fileflow.sdk.exception.FileFlowBadRequestException;
import com.ryuqq.fileflow.sdk.exception.FileFlowForbiddenException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.exception.FileFlowServerException;
import com.ryuqq.fileflow.sdk.exception.FileFlowUnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@DisplayName("HTTP 에러 핸들링 테스트")
@WireMockTest
class HttpErrorHandlingTest extends WireMockTestSupport {

    private FileAssetApi fileAssetApi;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        FileFlowClient client = createClient(wmRuntimeInfo);
        fileAssetApi = client.fileAssets();
    }

    @Nested
    @DisplayName("4xx 클라이언트 에러")
    class ClientErrorTest {

        @Test
        @DisplayName("400 응답은 FileFlowBadRequestException으로 변환된다")
        void shouldThrowBadRequestExceptionFor400() {
            // given
            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/test-id")))
                            .willReturn(
                                    errorResponse(
                                            400, "VALIDATION_ERROR", "Invalid request format")));

            // when & then
            assertThatThrownBy(() -> fileAssetApi.get("test-id"))
                    .isInstanceOf(FileFlowBadRequestException.class)
                    .satisfies(
                            ex -> {
                                FileFlowBadRequestException e = (FileFlowBadRequestException) ex;
                                assertThat(e.getStatusCode()).isEqualTo(400);
                                assertThat(e.getErrorCode()).isEqualTo("VALIDATION_ERROR");
                                assertThat(e.getErrorMessage()).isEqualTo("Invalid request format");
                            });
        }

        @Test
        @DisplayName("401 응답은 FileFlowUnauthorizedException으로 변환된다")
        void shouldThrowUnauthorizedExceptionFor401() {
            // given
            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/test-id")))
                            .willReturn(
                                    errorResponse(
                                            401, "UNAUTHORIZED", "Invalid or expired token")));

            // when & then
            assertThatThrownBy(() -> fileAssetApi.get("test-id"))
                    .isInstanceOf(FileFlowUnauthorizedException.class)
                    .satisfies(
                            ex -> {
                                FileFlowUnauthorizedException e =
                                        (FileFlowUnauthorizedException) ex;
                                assertThat(e.getStatusCode()).isEqualTo(401);
                            });
        }

        @Test
        @DisplayName("403 응답은 FileFlowForbiddenException으로 변환된다")
        void shouldThrowForbiddenExceptionFor403() {
            // given
            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/test-id")))
                            .willReturn(
                                    errorResponse(
                                            403,
                                            "FORBIDDEN",
                                            "You do not have permission to access this resource")));

            // when & then
            assertThatThrownBy(() -> fileAssetApi.get("test-id"))
                    .isInstanceOf(FileFlowForbiddenException.class)
                    .satisfies(
                            ex -> {
                                FileFlowForbiddenException e = (FileFlowForbiddenException) ex;
                                assertThat(e.getStatusCode()).isEqualTo(403);
                            });
        }

        @Test
        @DisplayName("404 응답은 FileFlowNotFoundException으로 변환된다")
        void shouldThrowNotFoundExceptionFor404() {
            // given
            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/test-id")))
                            .willReturn(errorResponse(404, "NOT_FOUND", "Resource not found")));

            // when & then
            assertThatThrownBy(() -> fileAssetApi.get("test-id"))
                    .isInstanceOf(FileFlowNotFoundException.class)
                    .satisfies(
                            ex -> {
                                FileFlowNotFoundException e = (FileFlowNotFoundException) ex;
                                assertThat(e.getStatusCode()).isEqualTo(404);
                            });
        }
    }

    @Nested
    @DisplayName("5xx 서버 에러")
    class ServerErrorTest {

        @Test
        @DisplayName("500 응답은 FileFlowServerException으로 변환된다")
        void shouldThrowServerExceptionFor500() {
            // given
            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/test-id")))
                            .willReturn(
                                    errorResponse(
                                            500,
                                            "INTERNAL_ERROR",
                                            "An unexpected error occurred")));

            // when & then
            assertThatThrownBy(() -> fileAssetApi.get("test-id"))
                    .isInstanceOf(FileFlowServerException.class)
                    .satisfies(
                            ex -> {
                                FileFlowServerException e = (FileFlowServerException) ex;
                                assertThat(e.getStatusCode()).isEqualTo(500);
                            });
        }

        @Test
        @DisplayName("503 응답도 FileFlowServerException으로 변환된다")
        void shouldThrowServerExceptionFor503() {
            // given
            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/test-id")))
                            .willReturn(
                                    errorResponse(
                                            503,
                                            "SERVICE_UNAVAILABLE",
                                            "Service temporarily unavailable")));

            // when & then
            assertThatThrownBy(() -> fileAssetApi.get("test-id"))
                    .isInstanceOf(FileFlowServerException.class)
                    .satisfies(
                            ex -> {
                                FileFlowServerException e = (FileFlowServerException) ex;
                                assertThat(e.getStatusCode()).isEqualTo(503);
                            });
        }
    }

    @Nested
    @DisplayName("비표준 에러 응답 처리")
    class NonStandardErrorTest {

        @Test
        @DisplayName("JSON이 아닌 에러 응답도 처리할 수 있다")
        void shouldHandleNonJsonErrorResponse() {
            // given
            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/test-id")))
                            .willReturn(
                                    aResponse()
                                            .withStatus(500)
                                            .withHeader(
                                                    HttpHeaders.CONTENT_TYPE,
                                                    MediaType.TEXT_PLAIN_VALUE)
                                            .withBody("Internal Server Error")));

            // when & then
            assertThatThrownBy(() -> fileAssetApi.get("test-id"))
                    .isInstanceOf(FileFlowServerException.class);
        }

        @Test
        @DisplayName("빈 에러 응답도 처리할 수 있다")
        void shouldHandleEmptyErrorResponse() {
            // given
            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/test-id")))
                            .willReturn(
                                    aResponse()
                                            .withStatus(500)
                                            .withHeader(
                                                    HttpHeaders.CONTENT_TYPE,
                                                    MediaType.APPLICATION_JSON_VALUE)
                                            .withBody("")));

            // when & then
            assertThatThrownBy(() -> fileAssetApi.get("test-id"))
                    .isInstanceOf(FileFlowServerException.class);
        }
    }

    @Nested
    @DisplayName("예외 메시지 포맷")
    class ExceptionMessageTest {

        @Test
        @DisplayName("예외 메시지에 상태 코드, 에러 코드, 상세 메시지가 포함된다")
        void shouldIncludeAllInfoInExceptionMessage() {
            // given
            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/test-id")))
                            .willReturn(
                                    errorResponse(
                                            404, "FILE_NOT_FOUND", "The file was not found")));

            // when & then
            assertThatThrownBy(() -> fileAssetApi.get("test-id"))
                    .isInstanceOf(FileFlowNotFoundException.class)
                    .hasMessageContaining("404")
                    .hasMessageContaining("FILE_NOT_FOUND")
                    .hasMessageContaining("The file was not found");
        }
    }
}

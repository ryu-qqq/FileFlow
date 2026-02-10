package com.ryuqq.fileflow.sdk.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ryuqq.fileflow.sdk.config.FileFlowConfig;
import com.ryuqq.fileflow.sdk.exception.FileFlowBadRequestException;
import com.ryuqq.fileflow.sdk.exception.FileFlowForbiddenException;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.exception.FileFlowServerException;
import com.ryuqq.fileflow.sdk.exception.FileFlowUnauthorizedException;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HttpClientSupportTest {

    private MockWebServer mockWebServer;
    private HttpClientSupport httpClientSupport;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        // Remove trailing slash
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        FileFlowConfig config =
                new FileFlowConfig(baseUrl, "test-service", "test-token", null, null);
        httpClientSupport = new HttpClientSupport(config);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("GET 요청 시 Service Token 헤더가 포함된다")
    void getRequestIncludesServiceTokenHeaders() throws InterruptedException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(
                                "{\"data\": null, \"timestamp\": \"2026-01-01T00:00:00\","
                                        + " \"requestId\": \"test\"}")
                        .addHeader("Content-Type", "application/json"));

        httpClientSupport.get("/api/v1/test", new TypeReference<ApiResponse<Void>>() {});

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("X-Service-Name")).isEqualTo("test-service");
        assertThat(request.getHeader("X-Service-Token")).isEqualTo("test-token");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(request.getMethod()).isEqualTo("GET");
    }

    @Test
    @DisplayName("POST 요청 시 body와 헤더가 올바르게 전송된다")
    void postRequestSendsBodyAndHeaders() throws InterruptedException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(
                                "{\"data\": null, \"timestamp\": \"2026-01-01T00:00:00\","
                                        + " \"requestId\": \"test\"}")
                        .addHeader("Content-Type", "application/json"));

        record TestRequest(String name) {}

        httpClientSupport.post(
                "/api/v1/test",
                new TestRequest("hello"),
                new TypeReference<ApiResponse<Void>>() {});

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getBody().readUtf8()).contains("\"name\":\"hello\"");
        assertThat(request.getHeader("X-Service-Name")).isEqualTo("test-service");
        assertThat(request.getHeader("X-Service-Token")).isEqualTo("test-token");
    }

    @Test
    @DisplayName("400 응답은 FileFlowBadRequestException으로 변환된다")
    void badRequestResponse() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(400)
                        .setBody(
                                "{\"type\":\"about:blank\",\"title\":\"Bad"
                                        + " Request\",\"status\":400,\"detail\":\"Invalid"
                                        + " input\",\"code\":\"VALIDATION_ERROR\"}")
                        .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(
                        () ->
                                httpClientSupport.get(
                                        "/api/v1/test", new TypeReference<ApiResponse<Void>>() {}))
                .isInstanceOf(FileFlowBadRequestException.class)
                .satisfies(
                        ex -> {
                            FileFlowBadRequestException e = (FileFlowBadRequestException) ex;
                            assertThat(e.getStatusCode()).isEqualTo(400);
                            assertThat(e.getErrorCode()).isEqualTo("VALIDATION_ERROR");
                            assertThat(e.getErrorMessage()).isEqualTo("Invalid input");
                        });
    }

    @Test
    @DisplayName("401 응답은 FileFlowUnauthorizedException으로 변환된다")
    void unauthorizedResponse() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(401)
                        .setBody(
                                "{\"type\":\"about:blank\",\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Invalid"
                                    + " token\",\"code\":\"AUTH_FAILED\"}")
                        .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(
                        () ->
                                httpClientSupport.get(
                                        "/api/v1/test", new TypeReference<ApiResponse<Void>>() {}))
                .isInstanceOf(FileFlowUnauthorizedException.class);
    }

    @Test
    @DisplayName("403 응답은 FileFlowForbiddenException으로 변환된다")
    void forbiddenResponse() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(403)
                        .setBody(
                                "{\"type\":\"about:blank\",\"title\":\"Forbidden\",\"status\":403,\"detail\":\"Access"
                                    + " denied\",\"code\":\"ACCESS_DENIED\"}")
                        .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(
                        () ->
                                httpClientSupport.get(
                                        "/api/v1/test", new TypeReference<ApiResponse<Void>>() {}))
                .isInstanceOf(FileFlowForbiddenException.class);
    }

    @Test
    @DisplayName("404 응답은 FileFlowNotFoundException으로 변환된다")
    void notFoundResponse() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(404)
                        .setBody(
                                "{\"type\":\"about:blank\",\"title\":\"Not"
                                        + " Found\",\"status\":404,\"detail\":\"Session not"
                                        + " found\",\"code\":\"SESSION_NOT_FOUND\"}")
                        .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(
                        () ->
                                httpClientSupport.get(
                                        "/api/v1/test", new TypeReference<ApiResponse<Void>>() {}))
                .isInstanceOf(FileFlowNotFoundException.class)
                .satisfies(
                        ex -> {
                            FileFlowNotFoundException e = (FileFlowNotFoundException) ex;
                            assertThat(e.getErrorCode()).isEqualTo("SESSION_NOT_FOUND");
                        });
    }

    @Test
    @DisplayName("500 응답은 FileFlowServerException으로 변환된다")
    void serverErrorResponse() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(500)
                        .setBody(
                                "{\"type\":\"about:blank\",\"title\":\"Internal Server"
                                        + " Error\",\"status\":500,\"detail\":\"Something went"
                                        + " wrong\",\"code\":\"INTERNAL_ERROR\"}")
                        .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(
                        () ->
                                httpClientSupport.get(
                                        "/api/v1/test", new TypeReference<ApiResponse<Void>>() {}))
                .isInstanceOf(FileFlowServerException.class);
    }

    @Test
    @DisplayName("DELETE 요청이 올바르게 전송된다")
    void deleteRequest() throws InterruptedException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(204)
                        .addHeader("Content-Type", "application/json"));

        httpClientSupport.delete("/api/v1/test");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("DELETE");
    }

    @Test
    @DisplayName("postVoid는 body 없이 POST 요청을 보낸다")
    void postVoidWithoutBody() throws InterruptedException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json"));

        httpClientSupport.postVoid("/api/v1/test");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
    }
}

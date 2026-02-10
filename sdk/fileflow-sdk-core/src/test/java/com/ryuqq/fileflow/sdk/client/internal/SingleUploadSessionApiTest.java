package com.ryuqq.fileflow.sdk.client.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.sdk.FileFlowClient;
import com.ryuqq.fileflow.sdk.api.SingleUploadSessionApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.session.CompleteSingleUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.CreateSingleUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.SingleUploadSessionResponse;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SingleUploadSessionApiTest {

    private MockWebServer mockWebServer;
    private SingleUploadSessionApi api;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        FileFlowClient client =
                FileFlowClient.builder()
                        .baseUrl(baseUrl)
                        .serviceName("test-service")
                        .serviceToken("test-token")
                        .build();
        api = client.singleUploadSession();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("단건 업로드 세션을 생성한다")
    void createSession() throws InterruptedException {
        String responseBody =
                """
                {
                    "data": {
                        "sessionId": "sess_123",
                        "presignedUrl": "https://s3.example.com/upload",
                        "s3Key": "public/2026/01/test.jpg",
                        "bucket": "fileflow-bucket",
                        "accessType": "PUBLIC",
                        "fileName": "test.jpg",
                        "contentType": "image/jpeg",
                        "status": "INITIATED",
                        "expiresAt": "2026-01-23T10:30:00+09:00",
                        "createdAt": "2026-01-23T09:30:00+09:00"
                    },
                    "timestamp": "2026-01-23T09:30:00+09:00",
                    "requestId": "req_123"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json"));

        CreateSingleUploadSessionRequest request =
                new CreateSingleUploadSessionRequest(
                        "test.jpg", "image/jpeg", "PUBLIC", "PRODUCT_IMAGE", "commerce-api");

        ApiResponse<SingleUploadSessionResponse> response = api.create(request);

        assertThat(response).isNotNull();
        assertThat(response.data().sessionId()).isEqualTo("sess_123");
        assertThat(response.data().presignedUrl()).isEqualTo("https://s3.example.com/upload");
        assertThat(response.data().status()).isEqualTo("INITIATED");
        assertThat(response.data().fileName()).isEqualTo("test.jpg");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/sessions/single");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    }

    @Test
    @DisplayName("단건 업로드 세션을 조회한다")
    void getSession() {
        String responseBody =
                """
                {
                    "data": {
                        "sessionId": "sess_123",
                        "presignedUrl": "https://s3.example.com/upload",
                        "s3Key": "public/2026/01/test.jpg",
                        "bucket": "fileflow-bucket",
                        "accessType": "PUBLIC",
                        "fileName": "test.jpg",
                        "contentType": "image/jpeg",
                        "status": "COMPLETED",
                        "expiresAt": "2026-01-23T10:30:00+09:00",
                        "createdAt": "2026-01-23T09:30:00+09:00"
                    },
                    "timestamp": "2026-01-23T09:30:00+09:00",
                    "requestId": "req_456"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json"));

        ApiResponse<SingleUploadSessionResponse> response = api.get("sess_123");

        assertThat(response.data().sessionId()).isEqualTo("sess_123");
        assertThat(response.data().status()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("단건 업로드 세션을 완료한다")
    void completeSession() throws InterruptedException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json"));

        CompleteSingleUploadSessionRequest request =
                new CompleteSingleUploadSessionRequest(
                        1048576L, "\"d41d8cd98f00b204e9800998ecf8427e\"");

        api.complete("sess_123", request);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath())
                .isEqualTo("/api/v1/sessions/single/sess_123/complete");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getBody().readUtf8()).contains("1048576");
    }
}

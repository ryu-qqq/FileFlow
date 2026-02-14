package com.ryuqq.fileflow.sdk.client.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.sdk.api.DownloadTaskApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.download.CreateDownloadTaskRequest;
import com.ryuqq.fileflow.sdk.model.download.DownloadTaskResponse;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DownloadTaskApiTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private MockWebServer mockWebServer;
    private DownloadTaskApi api;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = ApiTestSupport.startMockServer();
        api = ApiTestSupport.createClient(mockWebServer).downloadTask();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("다운로드 태스크를 생성한다")
    void createDownloadTask() throws Exception {
        String responseBody =
                """
                {
                    "data": {
                        "downloadTaskId": "dt_abc123",
                        "sourceUrl": "https://external-cdn.com/image.jpg",
                        "s3Key": "products/images/image.jpg",
                        "bucket": "fileflow-bucket",
                        "accessType": "PRIVATE",
                        "purpose": "PRODUCT_IMAGE",
                        "source": "product-service",
                        "status": "PENDING",
                        "retryCount": 0,
                        "maxRetries": 3,
                        "callbackUrl": "https://my-api.com/callback",
                        "lastError": null,
                        "createdAt": "2026-02-14T10:00:00+09:00",
                        "startedAt": null,
                        "completedAt": null
                    },
                    "timestamp": "2026-02-14T10:00:00+09:00",
                    "requestId": "req_001"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json"));

        CreateDownloadTaskRequest request =
                new CreateDownloadTaskRequest(
                        "https://external-cdn.com/image.jpg",
                        "products/images/image.jpg",
                        "fileflow-bucket",
                        "PRIVATE",
                        "PRODUCT_IMAGE",
                        "product-service",
                        "https://my-api.com/callback");

        ApiResponse<DownloadTaskResponse> response = api.create(request);

        assertThat(response).isNotNull();
        assertThat(response.data().downloadTaskId()).isEqualTo("dt_abc123");
        assertThat(response.data().sourceUrl()).isEqualTo("https://external-cdn.com/image.jpg");
        assertThat(response.data().status()).isEqualTo("PENDING");
        assertThat(response.data().retryCount()).isZero();
        assertThat(response.data().maxRetries()).isEqualTo(3);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/download-tasks");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");

        CreateDownloadTaskRequest actualRequest =
                OBJECT_MAPPER.readValue(
                        recordedRequest.getBody().readUtf8(), CreateDownloadTaskRequest.class);
        assertThat(actualRequest.sourceUrl()).isEqualTo(request.sourceUrl());
        assertThat(actualRequest.s3Key()).isEqualTo(request.s3Key());
        assertThat(actualRequest.bucket()).isEqualTo(request.bucket());
        assertThat(actualRequest.source()).isEqualTo(request.source());
    }

    @Test
    @DisplayName("다운로드 태스크를 조회한다")
    void getDownloadTask() throws InterruptedException {
        String responseBody =
                """
                {
                    "data": {
                        "downloadTaskId": "dt_abc123",
                        "sourceUrl": "https://external-cdn.com/image.jpg",
                        "s3Key": "products/images/image.jpg",
                        "bucket": "fileflow-bucket",
                        "accessType": "PRIVATE",
                        "purpose": "PRODUCT_IMAGE",
                        "source": "product-service",
                        "status": "COMPLETED",
                        "retryCount": 0,
                        "maxRetries": 3,
                        "callbackUrl": "https://my-api.com/callback",
                        "lastError": null,
                        "createdAt": "2026-02-14T10:00:00+09:00",
                        "startedAt": "2026-02-14T10:00:01+09:00",
                        "completedAt": "2026-02-14T10:00:05+09:00"
                    },
                    "timestamp": "2026-02-14T10:00:05+09:00",
                    "requestId": "req_002"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json"));

        ApiResponse<DownloadTaskResponse> response = api.get("dt_abc123");

        assertThat(response.data().downloadTaskId()).isEqualTo("dt_abc123");
        assertThat(response.data().status()).isEqualTo("COMPLETED");
        assertThat(response.data().completedAt()).isEqualTo("2026-02-14T10:00:05+09:00");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/download-tasks/dt_abc123");
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
    }
}

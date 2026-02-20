package com.ryuqq.fileflow.sdk.client.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.sdk.api.TransformRequestApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.transform.CreateTransformRequestRequest;
import com.ryuqq.fileflow.sdk.model.transform.TransformRequestResponse;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransformRequestApiTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private MockWebServer mockWebServer;
    private TransformRequestApi api;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = ApiTestSupport.startMockServer();
        api = ApiTestSupport.createClient(mockWebServer).transformRequest();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("변환 요청을 생성한다")
    void createTransformRequest() throws Exception {
        String responseBody =
                """
                {
                    "data": {
                        "transformRequestId": "tr_abc123",
                        "sourceAssetId": "asset_001",
                        "sourceContentType": "image/jpeg",
                        "transformType": "RESIZE",
                        "width": 800,
                        "height": 600,
                        "quality": 85,
                        "targetFormat": "webp",
                        "status": "PENDING",
                        "resultAssetId": null,
                        "lastError": null,
                        "createdAt": "2026-02-14T10:00:00+09:00",
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

        CreateTransformRequestRequest request =
                new CreateTransformRequestRequest("asset_001", "RESIZE", 800, 600, 85, "webp");

        ApiResponse<TransformRequestResponse> response = api.create(request);

        assertThat(response).isNotNull();
        assertThat(response.data().transformRequestId()).isEqualTo("tr_abc123");
        assertThat(response.data().sourceAssetId()).isEqualTo("asset_001");
        assertThat(response.data().transformType()).isEqualTo("RESIZE");
        assertThat(response.data().width()).isEqualTo(800);
        assertThat(response.data().height()).isEqualTo(600);
        assertThat(response.data().quality()).isEqualTo(85);
        assertThat(response.data().targetFormat()).isEqualTo("webp");
        assertThat(response.data().status()).isEqualTo("PENDING");
        assertThat(response.data().resultAssetId()).isNull();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/transform-requests");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");

        CreateTransformRequestRequest actualRequest =
                OBJECT_MAPPER.readValue(
                        recordedRequest.getBody().readUtf8(), CreateTransformRequestRequest.class);
        assertThat(actualRequest.sourceAssetId()).isEqualTo(request.sourceAssetId());
        assertThat(actualRequest.transformType()).isEqualTo(request.transformType());
        assertThat(actualRequest.width()).isEqualTo(request.width());
    }

    @Test
    @DisplayName("변환 요청을 조회한다")
    void getTransformRequest() throws InterruptedException {
        String responseBody =
                """
                {
                    "data": {
                        "transformRequestId": "tr_abc123",
                        "sourceAssetId": "asset_001",
                        "sourceContentType": "image/jpeg",
                        "transformType": "RESIZE",
                        "width": 800,
                        "height": 600,
                        "quality": 85,
                        "targetFormat": "webp",
                        "status": "COMPLETED",
                        "resultAssetId": "asset_002",
                        "lastError": null,
                        "createdAt": "2026-02-14T10:00:00+09:00",
                        "completedAt": "2026-02-14T10:00:03+09:00"
                    },
                    "timestamp": "2026-02-14T10:00:03+09:00",
                    "requestId": "req_002"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json"));

        ApiResponse<TransformRequestResponse> response = api.get("tr_abc123");

        assertThat(response.data().transformRequestId()).isEqualTo("tr_abc123");
        assertThat(response.data().status()).isEqualTo("COMPLETED");
        assertThat(response.data().resultAssetId()).isEqualTo("asset_002");
        assertThat(response.data().completedAt()).isEqualTo("2026-02-14T10:00:03+09:00");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/transform-requests/tr_abc123");
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
    }
}

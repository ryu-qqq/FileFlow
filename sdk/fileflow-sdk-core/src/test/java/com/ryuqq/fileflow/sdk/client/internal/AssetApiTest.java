package com.ryuqq.fileflow.sdk.client.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.sdk.FileFlowClient;
import com.ryuqq.fileflow.sdk.api.AssetApi;
import com.ryuqq.fileflow.sdk.model.asset.AssetMetadataResponse;
import com.ryuqq.fileflow.sdk.model.asset.AssetResponse;
import com.ryuqq.fileflow.sdk.model.asset.RegisterAssetRequest;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AssetApiTest {

    private MockWebServer mockWebServer;
    private AssetApi api;

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
        api = client.asset();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("S3에 이미 존재하는 파일을 Asset으로 등록한다")
    void registerAsset() throws InterruptedException {
        String responseBody =
                """
                {
                    "data": {
                        "assetId": "asset_abc123",
                        "s3Key": "public/2026/02/product-main.jpg",
                        "bucket": "fileflow-bucket",
                        "accessType": "PUBLIC",
                        "fileName": "product-main.jpg",
                        "fileSize": 512000,
                        "contentType": "image/jpeg",
                        "etag": "\\"d41d8cd98f00b204e9800998ecf8427e\\"",
                        "extension": "jpg",
                        "origin": "REGISTER",
                        "originId": null,
                        "purpose": "PRODUCT_IMAGE",
                        "source": "product-service",
                        "createdAt": "2026-02-14T10:00:00+09:00"
                    },
                    "timestamp": "2026-02-14T10:00:00+09:00",
                    "requestId": "req_789"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json"));

        RegisterAssetRequest request =
                new RegisterAssetRequest(
                        "public/2026/02/product-main.jpg",
                        "fileflow-bucket",
                        "PUBLIC",
                        "product-main.jpg",
                        "image/jpeg",
                        "PRODUCT_IMAGE",
                        "product-service");

        ApiResponse<AssetResponse> response = api.register(request);

        assertThat(response).isNotNull();
        assertThat(response.data().assetId()).isEqualTo("asset_abc123");
        assertThat(response.data().s3Key()).isEqualTo("public/2026/02/product-main.jpg");
        assertThat(response.data().bucket()).isEqualTo("fileflow-bucket");
        assertThat(response.data().origin()).isEqualTo("REGISTER");
        assertThat(response.data().purpose()).isEqualTo("PRODUCT_IMAGE");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/assets/register");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        String body = recordedRequest.getBody().readUtf8();
        assertThat(body).contains("public/2026/02/product-main.jpg");
        assertThat(body).contains("fileflow-bucket");
    }

    @Test
    @DisplayName("Asset을 조회한다")
    void getAsset() {
        String responseBody =
                """
                {
                    "data": {
                        "assetId": "asset_abc123",
                        "s3Key": "public/2026/02/product-main.jpg",
                        "bucket": "fileflow-bucket",
                        "accessType": "PUBLIC",
                        "fileName": "product-main.jpg",
                        "fileSize": 512000,
                        "contentType": "image/jpeg",
                        "etag": "\\"d41d8cd98f00b204e9800998ecf8427e\\"",
                        "extension": "jpg",
                        "origin": "REGISTER",
                        "originId": null,
                        "purpose": "PRODUCT_IMAGE",
                        "source": "product-service",
                        "createdAt": "2026-02-14T10:00:00+09:00"
                    },
                    "timestamp": "2026-02-14T10:00:00+09:00",
                    "requestId": "req_456"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json"));

        ApiResponse<AssetResponse> response = api.get("asset_abc123");

        assertThat(response.data().assetId()).isEqualTo("asset_abc123");
        assertThat(response.data().fileName()).isEqualTo("product-main.jpg");
    }

    @Test
    @DisplayName("Asset 메타데이터를 조회한다")
    void getAssetMetadata() {
        String responseBody =
                """
                {
                    "data": {
                        "metadataId": "meta_001",
                        "assetId": "asset_abc123",
                        "width": 1920,
                        "height": 1080,
                        "transformType": "ORIGINAL",
                        "createdAt": "2026-02-14T10:00:00+09:00"
                    },
                    "timestamp": "2026-02-14T10:00:00+09:00",
                    "requestId": "req_789"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json"));

        ApiResponse<AssetMetadataResponse> response = api.getMetadata("asset_abc123");

        assertThat(response.data().metadataId()).isEqualTo("meta_001");
        assertThat(response.data().assetId()).isEqualTo("asset_abc123");
        assertThat(response.data().width()).isEqualTo(1920);
        assertThat(response.data().height()).isEqualTo(1080);
    }

    @Test
    @DisplayName("Asset을 삭제한다")
    void deleteAsset() throws InterruptedException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json"));

        api.delete("asset_abc123", "product-service");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath())
                .isEqualTo("/api/v1/assets/asset_abc123?source=product-service");
        assertThat(recordedRequest.getMethod()).isEqualTo("DELETE");
    }
}

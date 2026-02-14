package com.ryuqq.fileflow.sdk.client.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.sdk.api.MultipartUploadSessionApi;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.session.AddCompletedPartRequest;
import com.ryuqq.fileflow.sdk.model.session.CompleteMultipartUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.CreateMultipartUploadSessionRequest;
import com.ryuqq.fileflow.sdk.model.session.MultipartUploadSessionResponse;
import com.ryuqq.fileflow.sdk.model.session.PresignedPartUrlResponse;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MultipartUploadSessionApiTest {

    private MockWebServer mockWebServer;
    private MultipartUploadSessionApi api;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = ApiTestSupport.startMockServer();
        api = ApiTestSupport.createClient(mockWebServer).multipartUploadSession();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("멀티파트 업로드 세션을 생성한다")
    void createSession() throws InterruptedException {
        String responseBody =
                """
                {
                    "data": {
                        "sessionId": "msess_123",
                        "uploadId": "upload_abc",
                        "s3Key": "public/2026/02/large-file.zip",
                        "bucket": "fileflow-bucket",
                        "accessType": "PUBLIC",
                        "fileName": "large-file.zip",
                        "contentType": "application/zip",
                        "partSize": 5242880,
                        "status": "INITIATED",
                        "completedPartCount": 0,
                        "completedParts": [],
                        "expiresAt": "2026-02-14T11:00:00+09:00",
                        "createdAt": "2026-02-14T10:00:00+09:00"
                    },
                    "timestamp": "2026-02-14T10:00:00+09:00",
                    "requestId": "req_001"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json"));

        CreateMultipartUploadSessionRequest request =
                new CreateMultipartUploadSessionRequest(
                        "large-file.zip",
                        "application/zip",
                        "PUBLIC",
                        5242880L,
                        "BACKUP",
                        "backup-service");

        ApiResponse<MultipartUploadSessionResponse> response = api.create(request);

        assertThat(response).isNotNull();
        assertThat(response.data().sessionId()).isEqualTo("msess_123");
        assertThat(response.data().uploadId()).isEqualTo("upload_abc");
        assertThat(response.data().partSize()).isEqualTo(5242880L);
        assertThat(response.data().status()).isEqualTo("INITIATED");
        assertThat(response.data().completedPartCount()).isZero();
        assertThat(response.data().completedParts()).isEmpty();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/sessions/multipart");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    }

    @Test
    @DisplayName("멀티파트 업로드 세션을 조회한다")
    void getSession() throws InterruptedException {
        String responseBody =
                """
                {
                    "data": {
                        "sessionId": "msess_123",
                        "uploadId": "upload_abc",
                        "s3Key": "public/2026/02/large-file.zip",
                        "bucket": "fileflow-bucket",
                        "accessType": "PUBLIC",
                        "fileName": "large-file.zip",
                        "contentType": "application/zip",
                        "partSize": 5242880,
                        "status": "UPLOADING",
                        "completedPartCount": 2,
                        "completedParts": [
                            {"partNumber": 1, "etag": "\\"etag1\\"", "size": 5242880},
                            {"partNumber": 2, "etag": "\\"etag2\\"", "size": 5242880}
                        ],
                        "expiresAt": "2026-02-14T11:00:00+09:00",
                        "createdAt": "2026-02-14T10:00:00+09:00"
                    },
                    "timestamp": "2026-02-14T10:05:00+09:00",
                    "requestId": "req_002"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json"));

        ApiResponse<MultipartUploadSessionResponse> response = api.get("msess_123");

        assertThat(response.data().sessionId()).isEqualTo("msess_123");
        assertThat(response.data().status()).isEqualTo("UPLOADING");
        assertThat(response.data().completedPartCount()).isEqualTo(2);
        assertThat(response.data().completedParts()).hasSize(2);
        assertThat(response.data().completedParts().get(0).partNumber()).isEqualTo(1);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/sessions/multipart/msess_123");
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
    }

    @Test
    @DisplayName("파트별 presigned URL을 발급한다")
    void getPresignedPartUrl() throws InterruptedException {
        String responseBody =
                """
                {
                    "data": {
                        "presignedUrl": "https://s3.example.com/upload-part?partNumber=3",
                        "partNumber": 3,
                        "expiresInSeconds": 3600
                    },
                    "timestamp": "2026-02-14T10:05:00+09:00",
                    "requestId": "req_003"
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(responseBody)
                        .addHeader("Content-Type", "application/json"));

        ApiResponse<PresignedPartUrlResponse> response = api.getPresignedPartUrl("msess_123", 3);

        assertThat(response.data().presignedUrl()).contains("upload-part");
        assertThat(response.data().partNumber()).isEqualTo(3);
        assertThat(response.data().expiresInSeconds()).isEqualTo(3600L);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath())
                .isEqualTo("/api/v1/sessions/multipart/msess_123/parts/3/presigned-url");
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
    }

    @Test
    @DisplayName("완료된 파트를 등록한다")
    void addCompletedPart() throws InterruptedException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json"));

        AddCompletedPartRequest request = new AddCompletedPartRequest(3, "\"etag3\"", 5242880L);

        api.addCompletedPart("msess_123", request);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath())
                .isEqualTo("/api/v1/sessions/multipart/msess_123/parts");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        String body = recordedRequest.getBody().readUtf8();
        assertThat(body).contains("\"partNumber\":3");
        assertThat(body).contains("etag3");
    }

    @Test
    @DisplayName("멀티파트 업로드 세션을 완료한다")
    void completeSession() throws InterruptedException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json"));

        CompleteMultipartUploadSessionRequest request =
                new CompleteMultipartUploadSessionRequest(15728640L, "\"combined-etag\"");

        api.complete("msess_123", request);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath())
                .isEqualTo("/api/v1/sessions/multipart/msess_123/complete");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getBody().readUtf8()).contains("15728640");
    }

    @Test
    @DisplayName("멀티파트 업로드 세션을 중단한다")
    void abortSession() throws InterruptedException {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json"));

        api.abort("msess_123");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath())
                .isEqualTo("/api/v1/sessions/multipart/msess_123/abort");
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    }
}

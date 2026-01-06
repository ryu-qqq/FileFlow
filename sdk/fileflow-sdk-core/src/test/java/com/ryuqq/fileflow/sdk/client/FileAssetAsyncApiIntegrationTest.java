package com.ryuqq.fileflow.sdk.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.ryuqq.fileflow.sdk.api.FileAssetAsyncApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@DisplayName("FileAssetAsyncApi WireMock 통합 테스트")
@WireMockTest
class FileAssetAsyncApiIntegrationTest extends WireMockTestSupport {

    private FileAssetAsyncApi fileAssetApi;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        FileFlowAsyncClient client = createAsyncClient(wmRuntimeInfo);
        fileAssetApi = client.fileAssets();
    }

    @Nested
    @DisplayName("generateDownloadUrl 메서드")
    class GenerateDownloadUrlTest {

        @Test
        @DisplayName("다운로드 URL을 비동기로 생성할 수 있다")
        void shouldGenerateDownloadUrlAsync() {
            // given
            String fileAssetId = "file-asset-123";
            String responseData =
                    """
                    {
                        "fileAssetId": "file-asset-123",
                        "downloadUrl": "https://s3.amazonaws.com/bucket/file.pdf?signed=abc123",
                        "expiresAt": "2025-01-15T11:00:00"
                    }
                    """;

            stubFor(
                    withAuth(
                                    post(
                                            urlPathEqualTo(
                                                    "/api/v1/file/file-assets/file-asset-123/download-url")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when & then
            StepVerifier.create(fileAssetApi.generateDownloadUrl(fileAssetId))
                    .expectNextMatches(
                            response ->
                                    "file-asset-123".equals(response.getFileAssetId())
                                            && response.getDownloadUrl() != null)
                    .verifyComplete();
        }

        @Test
        @DisplayName("존재하지 않는 파일에 대해 에러가 발생한다")
        void shouldEmitErrorForNonExistentFile() {
            // given
            String fileAssetId = "non-existent-id";

            stubFor(
                    withAuth(
                                    post(
                                            urlPathEqualTo(
                                                    "/api/v1/file/file-assets/non-existent-id/download-url")))
                            .willReturn(
                                    errorResponse(404, "FILE_NOT_FOUND", "File asset not found")));

            // when & then
            StepVerifier.create(fileAssetApi.generateDownloadUrl(fileAssetId))
                    .expectError()
                    .verify();
        }
    }

    @Nested
    @DisplayName("get 메서드")
    class GetTest {

        @Test
        @DisplayName("파일 에셋 정보를 비동기로 조회할 수 있다")
        void shouldGetFileAssetAsync() {
            // given
            String fileAssetId = "file-asset-456";
            String responseData =
                    """
                    {
                        "id": "file-asset-456",
                        "filename": "test.pdf",
                        "contentType": "application/pdf",
                        "fileSize": 2048,
                        "status": "COMPLETED",
                        "category": "DOCUMENT",
                        "s3Key": "uploads/test.pdf",
                        "createdAt": "2025-01-15T10:00:00",
                        "updatedAt": "2025-01-15T10:00:00"
                    }
                    """;

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/file-asset-456")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when & then
            StepVerifier.create(fileAssetApi.get(fileAssetId))
                    .expectNextMatches(
                            response ->
                                    "file-asset-456".equals(response.getId())
                                            && "test.pdf".equals(response.getFilename())
                                            && 2048L == response.getFileSize())
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class DeleteTest {

        @Test
        @DisplayName("파일 에셋을 비동기로 삭제할 수 있다")
        void shouldDeleteFileAssetAsync() {
            // given
            String fileAssetId = "async-file-to-delete";

            // Note: Async PATCH uses WebClient which sends Authorization header correctly
            stubFor(
                    withAuth(
                                    patch(
                                            urlPathEqualTo(
                                                    "/api/v1/file/file-assets/async-file-to-delete/delete")))
                            .willReturn(successResponse(wrapSuccessResponse("null"))));

            // when & then
            StepVerifier.create(fileAssetApi.delete(fileAssetId)).verifyComplete();
        }
    }

    @Nested
    @DisplayName("list 메서드")
    class ListTest {

        @Test
        @DisplayName("파일 에셋 목록을 비동기로 조회할 수 있다")
        void shouldListFileAssetsAsync() {
            // given
            String responseData =
                    """
                    {
                        "content": [
                            {
                                "id": "file-1",
                                "filename": "doc1.pdf",
                                "contentType": "application/pdf",
                                "fileSize": 1024,
                                "status": "COMPLETED",
                                "category": "DOCUMENT",
                                "s3Key": "uploads/doc1.pdf",
                                "createdAt": "2025-01-15T10:00:00",
                                "updatedAt": "2025-01-15T10:00:00"
                            }
                        ],
                        "page": 0,
                        "size": 10,
                        "totalElements": 1,
                        "totalPages": 1
                    }
                    """;

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when & then
            StepVerifier.create(fileAssetApi.list(0, 10))
                    .expectNextMatches(
                            response ->
                                    response.getContent().size() == 1
                                            && response.getPage() == 0
                                            && response.getTotalElements() == 1)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("인증 헤더 테스트")
    class AuthenticationTest {

        @Test
        @DisplayName("서비스 토큰이 X-Service-Token 헤더에 포함된다")
        void shouldIncludeServiceTokenInHeader() {
            // given
            String fileAssetId = "file-asset-auth";
            String responseData =
                    """
                    {
                        "id": "file-asset-auth",
                        "filename": "auth.pdf",
                        "contentType": "application/pdf",
                        "fileSize": 100,
                        "status": "COMPLETED",
                        "category": "DOCUMENT",
                        "s3Key": "auth.pdf",
                        "createdAt": "2025-01-15T10:00:00",
                        "updatedAt": "2025-01-15T10:00:00"
                    }
                    """;

            // X-Service-Token 헤더가 정확히 일치해야만 응답 반환
            stubFor(
                    get(urlPathEqualTo("/api/v1/file/file-assets/file-asset-auth"))
                            .withHeader("X-Service-Token", equalTo("test-service-token"))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when & then
            StepVerifier.create(fileAssetApi.get(fileAssetId))
                    .expectNextMatches(response -> "file-asset-auth".equals(response.getId()))
                    .verifyComplete();
        }
    }
}

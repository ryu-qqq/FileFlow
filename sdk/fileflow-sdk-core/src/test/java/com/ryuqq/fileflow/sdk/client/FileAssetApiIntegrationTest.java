package com.ryuqq.fileflow.sdk.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.ryuqq.fileflow.sdk.api.FileAssetApi;
import com.ryuqq.fileflow.sdk.exception.FileFlowNotFoundException;
import com.ryuqq.fileflow.sdk.model.asset.DownloadUrlResponse;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetResponse;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetStatisticsResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetApi WireMock 통합 테스트")
@WireMockTest
class FileAssetApiIntegrationTest extends WireMockTestSupport {

    private FileAssetApi fileAssetApi;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        FileFlowClient client = createClient(wmRuntimeInfo);
        fileAssetApi = client.fileAssets();
    }

    @Nested
    @DisplayName("generateDownloadUrl 메서드")
    class GenerateDownloadUrlTest {

        @Test
        @DisplayName("다운로드 URL을 생성할 수 있다")
        void shouldGenerateDownloadUrl() {
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

            // when
            DownloadUrlResponse response = fileAssetApi.generateDownloadUrl(fileAssetId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getFileAssetId()).isEqualTo("file-asset-123");
            assertThat(response.getDownloadUrl())
                    .isEqualTo("https://s3.amazonaws.com/bucket/file.pdf?signed=abc123");
            assertThat(response.getExpiresAt()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 파일 ID로 요청하면 FileFlowNotFoundException이 발생한다")
        void shouldThrowNotFoundExceptionWhenFileNotExists() {
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
            assertThatThrownBy(() -> fileAssetApi.generateDownloadUrl(fileAssetId))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("batchGenerateDownloadUrl 메서드")
    class BatchGenerateDownloadUrlTest {

        @Test
        @DisplayName("여러 파일의 다운로드 URL을 일괄 생성할 수 있다")
        void shouldBatchGenerateDownloadUrls() {
            // given
            List<String> fileAssetIds = List.of("file-1", "file-2");
            String responseData =
                    """
                    [
                        {
                            "fileAssetId": "file-1",
                            "downloadUrl": "https://s3.amazonaws.com/bucket/file1.pdf",
                            "expiresAt": "2025-01-15T11:00:00"
                        },
                        {
                            "fileAssetId": "file-2",
                            "downloadUrl": "https://s3.amazonaws.com/bucket/file2.pdf",
                            "expiresAt": "2025-01-15T11:00:00"
                        }
                    ]
                    """;

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/file-assets/batch-download-url")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            List<DownloadUrlResponse> responses =
                    fileAssetApi.batchGenerateDownloadUrl(fileAssetIds);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getFileAssetId()).isEqualTo("file-1");
            assertThat(responses.get(1).getFileAssetId()).isEqualTo("file-2");
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class DeleteTest {

        @Test
        @DisplayName("파일 에셋을 삭제할 수 있다")
        void shouldDeleteFileAsset() {
            // given
            String fileAssetId = "file-to-delete";

            stubFor(
                    withAuth(
                                    patch(
                                            urlPathEqualTo(
                                                    "/api/v1/file/file-assets/file-to-delete/delete")))
                            .willReturn(successResponse(wrapSuccessResponse("null"))));

            // when & then - 예외가 발생하지 않으면 성공
            fileAssetApi.delete(fileAssetId);
        }
    }

    @Nested
    @DisplayName("batchDelete 메서드")
    class BatchDeleteTest {

        @Test
        @DisplayName("여러 파일 에셋을 일괄 삭제할 수 있다")
        void shouldBatchDeleteFileAssets() {
            // given
            List<String> fileAssetIds = List.of("file-1", "file-2", "file-3");

            stubFor(
                    withAuth(post(urlPathEqualTo("/api/v1/file/file-assets/batch-delete")))
                            .willReturn(successResponse(wrapSuccessResponse("null"))));

            // when & then - 예외가 발생하지 않으면 성공
            fileAssetApi.batchDelete(fileAssetIds);
        }
    }

    @Nested
    @DisplayName("list 메서드")
    class ListTest {

        @Test
        @DisplayName("파일 에셋 목록을 페이징하여 조회할 수 있다")
        void shouldListFileAssetsWithPagination() {
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
                            },
                            {
                                "id": "file-2",
                                "filename": "doc2.pdf",
                                "contentType": "application/pdf",
                                "fileSize": 2048,
                                "status": "COMPLETED",
                                "category": "DOCUMENT",
                                "s3Key": "uploads/doc2.pdf",
                                "createdAt": "2025-01-15T10:00:00",
                                "updatedAt": "2025-01-15T10:00:00"
                            }
                        ],
                        "page": 0,
                        "size": 10,
                        "totalElements": 2,
                        "totalPages": 1
                    }
                    """;

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            var response = fileAssetApi.list(0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getPage()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(10);
            assertThat(response.getTotalElements()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("get 메서드")
    class GetTest {

        @Test
        @DisplayName("파일 에셋 정보를 조회할 수 있다")
        void shouldGetFileAsset() {
            // given
            String fileAssetId = "file-asset-123";
            String responseData =
                    """
                    {
                        "id": "file-asset-123",
                        "filename": "document.pdf",
                        "contentType": "application/pdf",
                        "fileSize": 1024,
                        "status": "COMPLETED",
                        "category": "DOCUMENT",
                        "s3Key": "uploads/document.pdf",
                        "createdAt": "2025-01-15T10:00:00",
                        "updatedAt": "2025-01-15T10:00:00"
                    }
                    """;

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/file-asset-123")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            FileAssetResponse response = fileAssetApi.get(fileAssetId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("file-asset-123");
            assertThat(response.getFilename()).isEqualTo("document.pdf");
            assertThat(response.getContentType()).isEqualTo("application/pdf");
            assertThat(response.getFileSize()).isEqualTo(1024L);
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("존재하지 않는 파일 ID로 조회하면 예외가 발생한다")
        void shouldThrowNotFoundExceptionWhenGetNonExistent() {
            // given
            String fileAssetId = "non-existent";

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/non-existent")))
                            .willReturn(
                                    errorResponse(404, "FILE_NOT_FOUND", "File asset not found")));

            // when & then
            assertThatThrownBy(() -> fileAssetApi.get(fileAssetId))
                    .isInstanceOf(FileFlowNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("retry 메서드")
    class RetryTest {

        @Test
        @DisplayName("실패한 파일 처리를 재시도할 수 있다")
        void shouldRetryFailedFileAsset() {
            // given
            String fileAssetId = "failed-file-asset";

            stubFor(
                    withAuth(
                                    post(
                                            urlPathEqualTo(
                                                    "/api/v1/file/file-assets/failed-file-asset/retry")))
                            .willReturn(successResponse(wrapSuccessResponse("null"))));

            // when & then - 예외가 발생하지 않으면 성공
            fileAssetApi.retry(fileAssetId);
        }
    }

    @Nested
    @DisplayName("getStatistics 메서드")
    class GetStatisticsTest {

        @Test
        @DisplayName("파일 에셋 통계를 조회할 수 있다")
        void shouldGetFileAssetStatistics() {
            // given
            String responseData =
                    """
                    {
                        "totalCount": 150,
                        "statusCounts": {
                            "PENDING": 10,
                            "PROCESSING": 5,
                            "COMPLETED": 120,
                            "FAILED": 15
                        },
                        "categoryCounts": {
                            "IMAGE": 80,
                            "VIDEO": 30,
                            "DOCUMENT": 25,
                            "AUDIO": 15
                        }
                    }
                    """;

            stubFor(
                    withAuth(get(urlPathEqualTo("/api/v1/file/file-assets/statistics")))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            FileAssetStatisticsResponse response = fileAssetApi.getStatistics();

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTotalCount()).isEqualTo(150);
            assertThat(response.getStatusCounts()).containsEntry("COMPLETED", 120L);
            assertThat(response.getStatusCounts()).containsEntry("FAILED", 15L);
            assertThat(response.getCategoryCounts()).containsEntry("IMAGE", 80L);
            assertThat(response.getCategoryCounts()).containsEntry("DOCUMENT", 25L);
        }
    }

    @Nested
    @DisplayName("인증 헤더 테스트")
    class AuthenticationTest {

        @Test
        @DisplayName("Bearer 토큰이 Authorization 헤더에 포함된다")
        void shouldIncludeBearerTokenInAuthorizationHeader() {
            // given
            String fileAssetId = "file-asset-123";
            String responseData =
                    """
                    {
                        "id": "file-asset-123",
                        "filename": "test.pdf",
                        "contentType": "application/pdf",
                        "fileSize": 100,
                        "status": "COMPLETED",
                        "category": "DOCUMENT",
                        "s3Key": "test.pdf",
                        "createdAt": "2025-01-15T10:00:00",
                        "updatedAt": "2025-01-15T10:00:00"
                    }
                    """;

            // Authorization 헤더가 정확히 일치해야만 응답 반환
            stubFor(
                    get(urlPathEqualTo("/api/v1/file/file-assets/file-asset-123"))
                            .withHeader("Authorization", equalTo("Bearer test-service-token"))
                            .willReturn(successResponse(wrapSuccessResponse(responseData))));

            // when
            FileAssetResponse response = fileAssetApi.get(fileAssetId);

            // then - 인증 헤더가 올바르게 전송되었음을 확인
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("file-asset-123");
        }
    }
}

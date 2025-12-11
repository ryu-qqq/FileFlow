package com.ryuqq.fileflow.adapter.in.rest.asset.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.BatchGenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.DeleteFileAssetApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.command.GenerateDownloadUrlApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.query.FileAssetSearchApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.query.FileAssetSearchApiRequest.FileAssetStatusFilter;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.query.FileAssetSearchApiRequest.FileCategoryFilter;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.BatchDownloadUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.DeleteFileAssetApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.DownloadUrlApiResponse;
import com.ryuqq.fileflow.adapter.in.rest.asset.dto.response.FileAssetApiResponse;
import com.ryuqq.fileflow.application.asset.dto.command.BatchGenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.command.DeleteFileAssetCommand;
import com.ryuqq.fileflow.application.asset.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.query.GetFileAssetQuery;
import com.ryuqq.fileflow.application.asset.dto.query.ListFileAssetsQuery;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.dto.response.DeleteFileAssetResponse;
import com.ryuqq.fileflow.application.asset.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.dto.response.FileAssetResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetApiMapper 단위 테스트")
class FileAssetApiMapperTest {

    private FileAssetApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FileAssetApiMapper();
    }

    @Nested
    @DisplayName("toGetFileAssetQuery 테스트")
    class ToGetFileAssetQueryTest {

        @Test
        @DisplayName("단건 조회 Query를 생성할 수 있다")
        void toGetFileAssetQuery_ShouldCreateQuery() {
            // given
            String id = "file-asset-123";
            String organizationId = "01912345-6789-7abc-def0-123456789100";
            String tenantId = "01912345-6789-7abc-def0-123456789001";

            // when
            GetFileAssetQuery query = mapper.toGetFileAssetQuery(id, organizationId, tenantId);

            // then
            assertThat(query.fileAssetId()).isEqualTo(id);
            assertThat(query.organizationId()).isEqualTo(organizationId);
            assertThat(query.tenantId()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("다양한 ID 형식으로 Query를 생성할 수 있다")
        void toGetFileAssetQuery_WithVariousIdFormats_ShouldCreateQuery() {
            // given - UUID 형식
            String uuidId = "550e8400-e29b-41d4-a716-446655440000";

            // when
            GetFileAssetQuery query = mapper.toGetFileAssetQuery(uuidId, "01912345-6789-7abc-def0-123456789001", "01912345-6789-7abc-def0-123456789001");

            // then
            assertThat(query.fileAssetId()).isEqualTo(uuidId);
        }
    }

    @Nested
    @DisplayName("toListFileAssetsQuery 테스트")
    class ToListFileAssetsQueryTest {

        @Test
        @DisplayName("모든 필터 조건으로 목록 조회 Query를 생성할 수 있다")
        void toListFileAssetsQuery_WithAllFilters_ShouldCreateQuery() {
            // given
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(
                            FileAssetStatusFilter.COMPLETED, FileCategoryFilter.IMAGE, 0, 20);
            String organizationId = "01912345-6789-7abc-def0-123456789100";
            String tenantId = "01912345-6789-7abc-def0-123456789001";

            // when
            ListFileAssetsQuery query =
                    mapper.toListFileAssetsQuery(request, organizationId, tenantId);

            // then
            assertThat(query.organizationId()).isEqualTo(organizationId);
            assertThat(query.tenantId()).isEqualTo(tenantId);
            assertThat(query.status()).isEqualTo("COMPLETED");
            assertThat(query.category()).isEqualTo("IMAGE");
            assertThat(query.page()).isEqualTo(0);
            assertThat(query.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("필터 없이 목록 조회 Query를 생성할 수 있다")
        void toListFileAssetsQuery_WithoutFilters_ShouldCreateQuery() {
            // given
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, null, null, null);
            String organizationId = "01912345-6789-7abc-def0-123456789100";
            String tenantId = "01912345-6789-7abc-def0-123456789001";

            // when
            ListFileAssetsQuery query =
                    mapper.toListFileAssetsQuery(request, organizationId, tenantId);

            // then
            assertThat(query.organizationId()).isEqualTo(organizationId);
            assertThat(query.tenantId()).isEqualTo(tenantId);
            assertThat(query.status()).isNull();
            assertThat(query.category()).isNull();
            assertThat(query.page()).isEqualTo(0); // 기본값
            assertThat(query.size()).isEqualTo(20); // 기본값
        }

        @Test
        @DisplayName("상태 필터만으로 Query를 생성할 수 있다")
        void toListFileAssetsQuery_WithStatusOnly_ShouldCreateQuery() {
            // given
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(FileAssetStatusFilter.PROCESSING, null, 1, 10);

            // when
            ListFileAssetsQuery query = mapper.toListFileAssetsQuery(request, "01912345-6789-7abc-def0-123456789001", "01912345-6789-7abc-def0-123456789001");

            // then
            assertThat(query.status()).isEqualTo("PROCESSING");
            assertThat(query.category()).isNull();
            assertThat(query.page()).isEqualTo(1);
            assertThat(query.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("카테고리 필터만으로 Query를 생성할 수 있다")
        void toListFileAssetsQuery_WithCategoryOnly_ShouldCreateQuery() {
            // given
            FileAssetSearchApiRequest request =
                    new FileAssetSearchApiRequest(null, FileCategoryFilter.VIDEO, 2, 50);

            // when
            ListFileAssetsQuery query = mapper.toListFileAssetsQuery(request, "01912345-6789-7abc-def0-123456789001", "01912345-6789-7abc-def0-123456789001");

            // then
            assertThat(query.status()).isNull();
            assertThat(query.category()).isEqualTo("VIDEO");
            assertThat(query.page()).isEqualTo(2);
            assertThat(query.size()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("toApiResponse 테스트")
    class ToApiResponseTest {

        @Test
        @DisplayName("FileAssetResponse를 API Response로 변환할 수 있다")
        void toApiResponse_ShouldConvertCorrectly() {
            // given
            Instant createdAt = Instant.parse("2025-11-26T10:00:00Z");
            Instant processedAt = Instant.parse("2025-11-26T10:05:00Z");

            FileAssetResponse response =
                    new FileAssetResponse(
                            "file-asset-123",
                            "session-456",
                            "document.pdf",
                            1024 * 1024L,
                            "application/pdf",
                            "DOCUMENT",
                            "test-bucket",
                            "uploads/document.pdf",
                            "etag-abc123",
                            "COMPLETED",
                            createdAt,
                            processedAt);

            // when
            FileAssetApiResponse apiResponse = mapper.toApiResponse(response);

            // then
            assertThat(apiResponse.id()).isEqualTo("file-asset-123");
            assertThat(apiResponse.sessionId()).isEqualTo("session-456");
            assertThat(apiResponse.fileName()).isEqualTo("document.pdf");
            assertThat(apiResponse.fileSize()).isEqualTo(1024 * 1024L);
            assertThat(apiResponse.contentType()).isEqualTo("application/pdf");
            assertThat(apiResponse.category()).isEqualTo("DOCUMENT");
            assertThat(apiResponse.bucket()).isEqualTo("test-bucket");
            assertThat(apiResponse.s3Key()).isEqualTo("uploads/document.pdf");
            assertThat(apiResponse.etag()).isEqualTo("etag-abc123");
            assertThat(apiResponse.status()).isEqualTo("COMPLETED");
            assertThat(apiResponse.createdAt()).isEqualTo(createdAt);
            assertThat(apiResponse.processedAt()).isEqualTo(processedAt);
        }

        @Test
        @DisplayName("processedAt이 null인 경우에도 변환할 수 있다")
        void toApiResponse_WithNullProcessedAt_ShouldConvertCorrectly() {
            // given
            Instant createdAt = Instant.parse("2025-11-26T10:00:00Z");

            FileAssetResponse response =
                    new FileAssetResponse(
                            "file-pending",
                            "session-789",
                            "image.jpg",
                            512 * 1024L,
                            "image/jpeg",
                            "IMAGE",
                            "bucket",
                            "key",
                            null,
                            "PENDING",
                            createdAt,
                            null);

            // when
            FileAssetApiResponse apiResponse = mapper.toApiResponse(response);

            // then
            assertThat(apiResponse.status()).isEqualTo("PENDING");
            assertThat(apiResponse.etag()).isNull();
            assertThat(apiResponse.processedAt()).isNull();
        }

        @Test
        @DisplayName("다양한 카테고리의 파일을 변환할 수 있다")
        void toApiResponse_WithVariousCategories_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();

            FileAssetResponse imageAsset =
                    createFileAssetResponse("img-1", "IMAGE", now);
            FileAssetResponse videoAsset =
                    createFileAssetResponse("vid-1", "VIDEO", now);
            FileAssetResponse audioAsset =
                    createFileAssetResponse("aud-1", "AUDIO", now);
            FileAssetResponse docAsset =
                    createFileAssetResponse("doc-1", "DOCUMENT", now);

            // when
            FileAssetApiResponse imageApi = mapper.toApiResponse(imageAsset);
            FileAssetApiResponse videoApi = mapper.toApiResponse(videoAsset);
            FileAssetApiResponse audioApi = mapper.toApiResponse(audioAsset);
            FileAssetApiResponse docApi = mapper.toApiResponse(docAsset);

            // then
            assertThat(imageApi.category()).isEqualTo("IMAGE");
            assertThat(videoApi.category()).isEqualTo("VIDEO");
            assertThat(audioApi.category()).isEqualTo("AUDIO");
            assertThat(docApi.category()).isEqualTo("DOCUMENT");
        }
    }

    private FileAssetResponse createFileAssetResponse(
            String id, String category, Instant createdAt) {
        return new FileAssetResponse(
                id,
                "session-1",
                "file.ext",
                1024L,
                "application/octet-stream",
                category,
                "bucket",
                "key/" + id,
                "etag",
                "COMPLETED",
                createdAt,
                createdAt);
    }

    @Nested
    @DisplayName("toDeleteFileAssetCommand 테스트")
    class ToDeleteFileAssetCommandTest {

        @Test
        @DisplayName("삭제 사유와 함께 Command를 생성할 수 있다")
        void toDeleteFileAssetCommand_WithReason_ShouldCreateCommand() {
            // given
            String fileAssetId = "file-asset-123";
            DeleteFileAssetApiRequest request = new DeleteFileAssetApiRequest("사용하지 않는 파일");
            String tenantId = "01912345-6789-7abc-def0-123456789001";
            String organizationId = "01912345-6789-7abc-def0-123456789100";

            // when
            DeleteFileAssetCommand command =
                    mapper.toDeleteFileAssetCommand(fileAssetId, request, tenantId, organizationId);

            // then
            assertThat(command.fileAssetId()).isEqualTo(fileAssetId);
            assertThat(command.tenantId()).isEqualTo(tenantId);
            assertThat(command.organizationId()).isEqualTo(organizationId);
            assertThat(command.reason()).isEqualTo("사용하지 않는 파일");
        }

        @Test
        @DisplayName("삭제 사유 없이 Command를 생성할 수 있다")
        void toDeleteFileAssetCommand_WithoutReason_ShouldCreateCommand() {
            // given
            String fileAssetId = "file-asset-456";
            DeleteFileAssetApiRequest request = DeleteFileAssetApiRequest.empty();
            String tenantId = "01912345-6789-7abc-def0-123456789001";
            String organizationId = "01912345-6789-7abc-def0-123456789100";

            // when
            DeleteFileAssetCommand command =
                    mapper.toDeleteFileAssetCommand(fileAssetId, request, tenantId, organizationId);

            // then
            assertThat(command.fileAssetId()).isEqualTo(fileAssetId);
            assertThat(command.reason()).isNull();
        }

        @Test
        @DisplayName("request가 null인 경우에도 Command를 생성할 수 있다")
        void toDeleteFileAssetCommand_WithNullRequest_ShouldCreateCommand() {
            // given
            String fileAssetId = "file-asset-789";

            // when
            DeleteFileAssetCommand command =
                    mapper.toDeleteFileAssetCommand(fileAssetId, null, "01912345-6789-7abc-def0-123456789001", "01912345-6789-7abc-def0-123456789100");

            // then
            assertThat(command.fileAssetId()).isEqualTo(fileAssetId);
            assertThat(command.reason()).isNull();
        }
    }

    @Nested
    @DisplayName("toDeleteApiResponse 테스트")
    class ToDeleteApiResponseTest {

        @Test
        @DisplayName("삭제 응답을 API 응답으로 변환할 수 있다")
        void toDeleteApiResponse_ShouldConvertCorrectly() {
            // given
            Instant processedAt = Instant.parse("2025-11-27T10:30:00Z");
            DeleteFileAssetResponse response =
                    DeleteFileAssetResponse.of("file-deleted-123", processedAt);

            // when
            DeleteFileAssetApiResponse apiResponse = mapper.toDeleteApiResponse(response);

            // then
            assertThat(apiResponse.id()).isEqualTo("file-deleted-123");
            assertThat(apiResponse.deletedAt()).isEqualTo(processedAt);
        }
    }

    @Nested
    @DisplayName("toGenerateDownloadUrlCommand 테스트")
    class ToGenerateDownloadUrlCommandTest {

        @Test
        @DisplayName("유효 기간을 지정하여 Command를 생성할 수 있다")
        void toGenerateDownloadUrlCommand_WithExpiration_ShouldCreateCommand() {
            // given
            String fileAssetId = "file-asset-123";
            GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest(120);
            String tenantId = "01912345-6789-7abc-def0-123456789001";
            String organizationId = "01912345-6789-7abc-def0-123456789100";

            // when
            GenerateDownloadUrlCommand command =
                    mapper.toGenerateDownloadUrlCommand(
                            fileAssetId, request, tenantId, organizationId);

            // then
            assertThat(command.fileAssetId()).isEqualTo(fileAssetId);
            assertThat(command.tenantId()).isEqualTo(tenantId);
            assertThat(command.organizationId()).isEqualTo(organizationId);
            assertThat(command.expirationMinutes()).isEqualTo(120);
        }

        @Test
        @DisplayName("request가 null인 경우 기본값(60분)으로 Command를 생성한다")
        void toGenerateDownloadUrlCommand_WithNullRequest_ShouldUseDefaultExpiration() {
            // given
            String fileAssetId = "file-asset-456";

            // when
            GenerateDownloadUrlCommand command =
                    mapper.toGenerateDownloadUrlCommand(fileAssetId, null, "01912345-6789-7abc-def0-123456789001", "01912345-6789-7abc-def0-123456789100");

            // then
            assertThat(command.fileAssetId()).isEqualTo(fileAssetId);
            assertThat(command.expirationMinutes()).isEqualTo(60);
        }
    }

    @Nested
    @DisplayName("toBatchGenerateDownloadUrlCommand 테스트")
    class ToBatchGenerateDownloadUrlCommandTest {

        @Test
        @DisplayName("일괄 다운로드 URL 생성 Command를 생성할 수 있다")
        void toBatchGenerateDownloadUrlCommand_ShouldCreateCommand() {
            // given
            List<String> fileAssetIds = List.of("file-1", "file-2", "file-3");
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(fileAssetIds, 180);
            String tenantId = "01912345-6789-7abc-def0-123456789001";
            String organizationId = "01912345-6789-7abc-def0-123456789100";

            // when
            BatchGenerateDownloadUrlCommand command =
                    mapper.toBatchGenerateDownloadUrlCommand(request, tenantId, organizationId);

            // then
            assertThat(command.fileAssetIds()).containsExactly("file-1", "file-2", "file-3");
            assertThat(command.tenantId()).isEqualTo(tenantId);
            assertThat(command.organizationId()).isEqualTo(organizationId);
            assertThat(command.expirationMinutes()).isEqualTo(180);
        }

        @Test
        @DisplayName("유효 기간이 null인 경우 기본값(60분)이 적용된다")
        void toBatchGenerateDownloadUrlCommand_WithNullExpiration_ShouldUseDefault() {
            // given
            List<String> fileAssetIds = List.of("file-1");
            BatchGenerateDownloadUrlApiRequest request =
                    new BatchGenerateDownloadUrlApiRequest(fileAssetIds, null);

            // when
            BatchGenerateDownloadUrlCommand command =
                    mapper.toBatchGenerateDownloadUrlCommand(request, "01912345-6789-7abc-def0-123456789001", "01912345-6789-7abc-def0-123456789100");

            // then
            assertThat(command.expirationMinutes()).isEqualTo(60);
        }
    }

    @Nested
    @DisplayName("toDownloadUrlApiResponse 테스트")
    class ToDownloadUrlApiResponseTest {

        @Test
        @DisplayName("다운로드 URL 응답을 API 응답으로 변환할 수 있다")
        void toDownloadUrlApiResponse_ShouldConvertCorrectly() {
            // given
            Instant expiresAt = Instant.parse("2025-11-27T12:00:00Z");
            DownloadUrlResponse response =
                    DownloadUrlResponse.of(
                            "file-asset-123",
                            "https://s3.amazonaws.com/bucket/key?presigned",
                            "document.pdf",
                            "application/pdf",
                            1024 * 1024L,
                            expiresAt);

            // when
            DownloadUrlApiResponse apiResponse = mapper.toDownloadUrlApiResponse(response);

            // then
            assertThat(apiResponse.fileAssetId()).isEqualTo("file-asset-123");
            assertThat(apiResponse.downloadUrl())
                    .isEqualTo("https://s3.amazonaws.com/bucket/key?presigned");
            assertThat(apiResponse.fileName()).isEqualTo("document.pdf");
            assertThat(apiResponse.contentType()).isEqualTo("application/pdf");
            assertThat(apiResponse.fileSize()).isEqualTo(1024 * 1024L);
            assertThat(apiResponse.expiresAt()).isEqualTo(expiresAt);
        }
    }

    @Nested
    @DisplayName("toBatchDownloadUrlApiResponse 테스트")
    class ToBatchDownloadUrlApiResponseTest {

        @Test
        @DisplayName("일괄 다운로드 URL 응답을 API 응답으로 변환할 수 있다 (모두 성공)")
        void toBatchDownloadUrlApiResponse_AllSuccess_ShouldConvertCorrectly() {
            // given
            Instant expiresAt = Instant.parse("2025-11-27T12:00:00Z");
            List<DownloadUrlResponse> downloadUrls =
                    List.of(
                            DownloadUrlResponse.of(
                                    "file-1",
                                    "https://url1",
                                    "file1.pdf",
                                    "application/pdf",
                                    1024L,
                                    expiresAt),
                            DownloadUrlResponse.of(
                                    "file-2",
                                    "https://url2",
                                    "file2.pdf",
                                    "application/pdf",
                                    2048L,
                                    expiresAt));

            BatchDownloadUrlResponse response = BatchDownloadUrlResponse.ofSuccess(downloadUrls);

            // when
            BatchDownloadUrlApiResponse apiResponse =
                    mapper.toBatchDownloadUrlApiResponse(response);

            // then
            assertThat(apiResponse.downloadUrls()).hasSize(2);
            assertThat(apiResponse.downloadUrls().get(0).fileAssetId()).isEqualTo("file-1");
            assertThat(apiResponse.downloadUrls().get(1).fileAssetId()).isEqualTo("file-2");
            assertThat(apiResponse.failures()).isEmpty();
        }

        @Test
        @DisplayName("일괄 다운로드 URL 응답을 API 응답으로 변환할 수 있다 (일부 실패)")
        void toBatchDownloadUrlApiResponse_PartialFailure_ShouldConvertCorrectly() {
            // given
            Instant expiresAt = Instant.parse("2025-11-27T12:00:00Z");
            List<DownloadUrlResponse> downloadUrls =
                    List.of(
                            DownloadUrlResponse.of(
                                    "file-1",
                                    "https://url1",
                                    "file1.pdf",
                                    "application/pdf",
                                    1024L,
                                    expiresAt));

            List<BatchDownloadUrlResponse.FailedDownloadUrl> failures =
                    List.of(
                            BatchDownloadUrlResponse.FailedDownloadUrl.of(
                                    "file-2", "NOT_FOUND", "파일을 찾을 수 없습니다"),
                            BatchDownloadUrlResponse.FailedDownloadUrl.of(
                                    "file-3", "ACCESS_DENIED", "접근 권한이 없습니다"));

            BatchDownloadUrlResponse response = BatchDownloadUrlResponse.of(downloadUrls, failures);

            // when
            BatchDownloadUrlApiResponse apiResponse =
                    mapper.toBatchDownloadUrlApiResponse(response);

            // then
            assertThat(apiResponse.downloadUrls()).hasSize(1);
            assertThat(apiResponse.failures()).hasSize(2);
            assertThat(apiResponse.failures().get(0).fileAssetId()).isEqualTo("file-2");
            assertThat(apiResponse.failures().get(0).errorCode()).isEqualTo("NOT_FOUND");
            assertThat(apiResponse.failures().get(1).fileAssetId()).isEqualTo("file-3");
            assertThat(apiResponse.failures().get(1).errorCode()).isEqualTo("ACCESS_DENIED");
        }

        @Test
        @DisplayName("빈 응답도 처리할 수 있다")
        void toBatchDownloadUrlApiResponse_EmptyResponse_ShouldConvertCorrectly() {
            // given
            BatchDownloadUrlResponse response = BatchDownloadUrlResponse.ofSuccess(List.of());

            // when
            BatchDownloadUrlApiResponse apiResponse =
                    mapper.toBatchDownloadUrlApiResponse(response);

            // then
            assertThat(apiResponse.downloadUrls()).isEmpty();
            assertThat(apiResponse.failures()).isEmpty();
        }
    }
}

package com.ryuqq.fileflow.application.asset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.asset.dto.command.BatchGenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.dto.response.BatchDownloadUrlResponse.FailedDownloadUrl;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("BatchGenerateDownloadUrlService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class BatchGenerateDownloadUrlServiceTest {

    private static final String FILE_ASSET_ID_1 = "550e8400-e29b-41d4-a716-446655440001";
    private static final String FILE_ASSET_ID_2 = "550e8400-e29b-41d4-a716-446655440002";
    private static final String FILE_ASSET_ID_3 = "550e8400-e29b-41d4-a716-446655440003";
    private static final long ORG_ID = 10L;
    private static final long TENANT_ID = 20L;
    private static final int DEFAULT_EXPIRATION = 60;
    private static final String BUCKET_NAME = "test-bucket";
    private static final String S3_KEY_VALUE = "uploads/document.pdf";
    private static final String FILE_NAME = "document.pdf";
    private static final String CONTENT_TYPE = "application/pdf";
    private static final long FILE_SIZE = 1024 * 1024L;
    private static final String PRESIGNED_URL =
            "https://s3.amazonaws.com/test-bucket/key?signature";

    @Mock private FileAssetQueryPort fileAssetQueryPort;
    @Mock private S3ClientPort s3ClientPort;

    @InjectMocks private BatchGenerateDownloadUrlService batchGenerateDownloadUrlService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("모든 파일에 대해 Presigned URL을 성공적으로 생성한다")
        void execute_AllSuccess_ShouldReturnAllSuccessResponses() {
            // given
            List<String> fileAssetIds = List.of(FILE_ASSET_ID_1, FILE_ASSET_ID_2);
            BatchGenerateDownloadUrlCommand command =
                    BatchGenerateDownloadUrlCommand.of(
                            fileAssetIds, TENANT_ID, ORG_ID, DEFAULT_EXPIRATION);

            FileAsset fileAsset1 = createMockFileAsset(FILE_ASSET_ID_1);
            FileAsset fileAsset2 = createMockFileAsset(FILE_ASSET_ID_2);

            when(fileAssetQueryPort.findById(
                            FileAssetId.of(UUID.fromString(FILE_ASSET_ID_1)), ORG_ID, TENANT_ID))
                    .thenReturn(Optional.of(fileAsset1));
            when(fileAssetQueryPort.findById(
                            FileAssetId.of(UUID.fromString(FILE_ASSET_ID_2)), ORG_ID, TENANT_ID))
                    .thenReturn(Optional.of(fileAsset2));
            when(s3ClientPort.generatePresignedGetUrl(
                            any(S3Bucket.class), any(S3Key.class), any(Duration.class)))
                    .thenReturn(PRESIGNED_URL);

            // when
            BatchDownloadUrlResponse response = batchGenerateDownloadUrlService.execute(command);

            // then
            assertThat(response.successCount()).isEqualTo(2);
            assertThat(response.failureCount()).isZero();
            assertThat(response.downloadUrls()).hasSize(2);
            assertThat(response.failures()).isEmpty();
        }

        @Test
        @DisplayName("일부 파일이 없으면 부분 성공 응답을 반환한다")
        void execute_PartialSuccess_ShouldReturnMixedResponse() {
            // given
            List<String> fileAssetIds = List.of(FILE_ASSET_ID_1, FILE_ASSET_ID_2, FILE_ASSET_ID_3);
            BatchGenerateDownloadUrlCommand command =
                    BatchGenerateDownloadUrlCommand.of(
                            fileAssetIds, TENANT_ID, ORG_ID, DEFAULT_EXPIRATION);

            FileAsset fileAsset1 = createMockFileAsset(FILE_ASSET_ID_1);

            when(fileAssetQueryPort.findById(
                            FileAssetId.of(UUID.fromString(FILE_ASSET_ID_1)), ORG_ID, TENANT_ID))
                    .thenReturn(Optional.of(fileAsset1));
            when(fileAssetQueryPort.findById(
                            FileAssetId.of(UUID.fromString(FILE_ASSET_ID_2)), ORG_ID, TENANT_ID))
                    .thenReturn(Optional.empty());
            when(fileAssetQueryPort.findById(
                            FileAssetId.of(UUID.fromString(FILE_ASSET_ID_3)), ORG_ID, TENANT_ID))
                    .thenReturn(Optional.empty());
            when(s3ClientPort.generatePresignedGetUrl(
                            any(S3Bucket.class), any(S3Key.class), any(Duration.class)))
                    .thenReturn(PRESIGNED_URL);

            // when
            BatchDownloadUrlResponse response = batchGenerateDownloadUrlService.execute(command);

            // then
            assertThat(response.successCount()).isEqualTo(1);
            assertThat(response.failureCount()).isEqualTo(2);
            assertThat(response.downloadUrls()).hasSize(1);
            assertThat(response.failures()).hasSize(2);
            assertThat(response.failures().get(0).errorCode()).isEqualTo("FILE_ASSET_NOT_FOUND");
        }

        @Test
        @DisplayName("모든 파일이 없으면 모두 실패 응답을 반환한다")
        void execute_AllFailure_ShouldReturnAllFailureResponses() {
            // given
            List<String> fileAssetIds = List.of(FILE_ASSET_ID_1, FILE_ASSET_ID_2);
            BatchGenerateDownloadUrlCommand command =
                    BatchGenerateDownloadUrlCommand.of(
                            fileAssetIds, TENANT_ID, ORG_ID, DEFAULT_EXPIRATION);

            when(fileAssetQueryPort.findById(any(FileAssetId.class), eq(ORG_ID), eq(TENANT_ID)))
                    .thenReturn(Optional.empty());

            // when
            BatchDownloadUrlResponse response = batchGenerateDownloadUrlService.execute(command);

            // then
            assertThat(response.successCount()).isZero();
            assertThat(response.failureCount()).isEqualTo(2);
            assertThat(response.downloadUrls()).isEmpty();
            assertThat(response.failures()).hasSize(2);

            verify(s3ClientPort, never()).generatePresignedGetUrl(any(), any(), any());
        }

        @Test
        @DisplayName("배치 사이즈를 초과하면 모두 실패 응답을 반환한다")
        void execute_ExceedBatchSize_ShouldReturnAllFailureResponses() {
            // given
            List<String> fileAssetIds = new ArrayList<>();
            IntStream.range(0, 101).forEach(i -> fileAssetIds.add(UUID.randomUUID().toString()));

            BatchGenerateDownloadUrlCommand command =
                    BatchGenerateDownloadUrlCommand.of(
                            fileAssetIds, TENANT_ID, ORG_ID, DEFAULT_EXPIRATION);

            // when
            BatchDownloadUrlResponse response = batchGenerateDownloadUrlService.execute(command);

            // then
            assertThat(response.successCount()).isZero();
            assertThat(response.failureCount()).isEqualTo(101);
            assertThat(response.downloadUrls()).isEmpty();
            assertThat(response.failures()).hasSize(101);

            FailedDownloadUrl firstFailure = response.failures().get(0);
            assertThat(firstFailure.errorCode()).isEqualTo("BATCH_SIZE_EXCEEDED");
            assertThat(firstFailure.errorMessage()).contains("100");

            verify(fileAssetQueryPort, never()).findById(any(), any(), any());
            verify(s3ClientPort, never()).generatePresignedGetUrl(any(), any(), any());
        }

        @Test
        @DisplayName("빈 목록으로 요청하면 빈 응답을 반환한다")
        void execute_EmptyList_ShouldReturnEmptyResponse() {
            // given
            List<String> fileAssetIds = List.of();
            BatchGenerateDownloadUrlCommand command =
                    BatchGenerateDownloadUrlCommand.of(
                            fileAssetIds, TENANT_ID, ORG_ID, DEFAULT_EXPIRATION);

            // when
            BatchDownloadUrlResponse response = batchGenerateDownloadUrlService.execute(command);

            // then
            assertThat(response.successCount()).isZero();
            assertThat(response.failureCount()).isZero();
            assertThat(response.downloadUrls()).isEmpty();
            assertThat(response.failures()).isEmpty();
        }
    }

    private FileAsset createMockFileAsset(String fileAssetId) {
        FileAsset fileAsset = mock(FileAsset.class);
        S3Bucket bucket = new S3Bucket(BUCKET_NAME);
        S3Key s3Key = new S3Key(S3_KEY_VALUE);

        when(fileAsset.getIdValue()).thenReturn(fileAssetId);
        when(fileAsset.getBucket()).thenReturn(bucket);
        when(fileAsset.getS3Key()).thenReturn(s3Key);
        when(fileAsset.getFileNameValue()).thenReturn(FILE_NAME);
        when(fileAsset.getContentTypeValue()).thenReturn(CONTENT_TYPE);
        when(fileAsset.getFileSizeValue()).thenReturn(FILE_SIZE);

        return fileAsset;
    }
}

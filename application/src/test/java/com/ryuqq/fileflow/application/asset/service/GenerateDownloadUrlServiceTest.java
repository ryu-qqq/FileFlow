package com.ryuqq.fileflow.application.asset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.asset.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.asset.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.asset.port.out.query.FileAssetQueryPort;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GenerateDownloadUrlService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class GenerateDownloadUrlServiceTest {

    private static final String FILE_ASSET_ID = "550e8400-e29b-41d4-a716-446655440000";
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

    @InjectMocks private GenerateDownloadUrlService generateDownloadUrlService;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("Presigned Download URL을 생성하여 반환한다")
        void execute_ShouldGeneratePresignedUrl() {
            // given
            GenerateDownloadUrlCommand command =
                    GenerateDownloadUrlCommand.of(
                            FILE_ASSET_ID, TENANT_ID, ORG_ID, DEFAULT_EXPIRATION);

            FileAssetId fileAssetId = FileAssetId.of(UUID.fromString(FILE_ASSET_ID));
            FileAsset fileAsset = mock(FileAsset.class);
            S3Bucket bucket = new S3Bucket(BUCKET_NAME);
            S3Key s3Key = new S3Key(S3_KEY_VALUE);

            when(fileAssetQueryPort.findById(fileAssetId, ORG_ID, TENANT_ID))
                    .thenReturn(Optional.of(fileAsset));
            when(fileAsset.getIdValue()).thenReturn(FILE_ASSET_ID);
            when(fileAsset.getBucket()).thenReturn(bucket);
            when(fileAsset.getS3Key()).thenReturn(s3Key);
            when(fileAsset.getFileNameValue()).thenReturn(FILE_NAME);
            when(fileAsset.getContentTypeValue()).thenReturn(CONTENT_TYPE);
            when(fileAsset.getFileSizeValue()).thenReturn(FILE_SIZE);
            when(s3ClientPort.generatePresignedGetUrl(eq(bucket), eq(s3Key), any(Duration.class)))
                    .thenReturn(PRESIGNED_URL);

            // when
            DownloadUrlResponse response = generateDownloadUrlService.execute(command);

            // then
            assertThat(response.fileAssetId()).isEqualTo(FILE_ASSET_ID);
            assertThat(response.downloadUrl()).isEqualTo(PRESIGNED_URL);
            assertThat(response.fileName()).isEqualTo(FILE_NAME);
            assertThat(response.contentType()).isEqualTo(CONTENT_TYPE);
            assertThat(response.fileSize()).isEqualTo(FILE_SIZE);
            assertThat(response.expiresAt()).isNotNull();

            verify(fileAssetQueryPort).findById(fileAssetId, ORG_ID, TENANT_ID);
            verify(s3ClientPort)
                    .generatePresignedGetUrl(
                            eq(bucket), eq(s3Key), eq(Duration.ofMinutes(DEFAULT_EXPIRATION)));
        }

        @Test
        @DisplayName("사용자 지정 유효 기간으로 URL을 생성한다")
        void execute_WithCustomExpiration_ShouldGenerateUrlWithCustomDuration() {
            // given
            int customExpiration = 180;
            GenerateDownloadUrlCommand command =
                    GenerateDownloadUrlCommand.of(
                            FILE_ASSET_ID, TENANT_ID, ORG_ID, customExpiration);

            FileAssetId fileAssetId = FileAssetId.of(UUID.fromString(FILE_ASSET_ID));
            FileAsset fileAsset = mock(FileAsset.class);
            S3Bucket bucket = new S3Bucket(BUCKET_NAME);
            S3Key s3Key = new S3Key(S3_KEY_VALUE);

            when(fileAssetQueryPort.findById(fileAssetId, ORG_ID, TENANT_ID))
                    .thenReturn(Optional.of(fileAsset));
            when(fileAsset.getIdValue()).thenReturn(FILE_ASSET_ID);
            when(fileAsset.getBucket()).thenReturn(bucket);
            when(fileAsset.getS3Key()).thenReturn(s3Key);
            when(fileAsset.getFileNameValue()).thenReturn(FILE_NAME);
            when(fileAsset.getContentTypeValue()).thenReturn(CONTENT_TYPE);
            when(fileAsset.getFileSizeValue()).thenReturn(FILE_SIZE);
            when(s3ClientPort.generatePresignedGetUrl(eq(bucket), eq(s3Key), any(Duration.class)))
                    .thenReturn(PRESIGNED_URL);

            // when
            DownloadUrlResponse response = generateDownloadUrlService.execute(command);

            // then
            assertThat(response.fileAssetId()).isEqualTo(FILE_ASSET_ID);
            verify(s3ClientPort)
                    .generatePresignedGetUrl(
                            eq(bucket), eq(s3Key), eq(Duration.ofMinutes(customExpiration)));
        }

        @Test
        @DisplayName("파일 자산이 없으면 FileAssetNotFoundException을 던진다")
        void execute_WhenNotFound_ShouldThrowException() {
            // given
            GenerateDownloadUrlCommand command =
                    GenerateDownloadUrlCommand.of(
                            FILE_ASSET_ID, TENANT_ID, ORG_ID, DEFAULT_EXPIRATION);

            FileAssetId fileAssetId = FileAssetId.of(UUID.fromString(FILE_ASSET_ID));
            when(fileAssetQueryPort.findById(fileAssetId, ORG_ID, TENANT_ID))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> generateDownloadUrlService.execute(command))
                    .isInstanceOf(FileAssetNotFoundException.class)
                    .hasMessageContaining(FILE_ASSET_ID);

            verify(s3ClientPort, never()).generatePresignedGetUrl(any(), any(), any());
        }
    }
}

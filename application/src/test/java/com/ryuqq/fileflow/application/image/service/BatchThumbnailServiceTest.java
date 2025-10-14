package com.ryuqq.fileflow.application.image.service;

import com.ryuqq.fileflow.application.file.port.out.SaveFileRelationshipPort;
import com.ryuqq.fileflow.application.image.dto.BatchThumbnailCommand;
import com.ryuqq.fileflow.application.image.dto.BatchThumbnailResult;
import com.ryuqq.fileflow.application.image.dto.ThumbnailGenerationResult;
import com.ryuqq.fileflow.application.image.port.in.GenerateThumbnailUseCase;
import com.ryuqq.fileflow.application.upload.port.out.SaveFileAssetPort;
import com.ryuqq.fileflow.domain.file.FileRelationship;
import com.ryuqq.fileflow.domain.file.FileRelationshipType;
import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand;
import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand.ThumbnailSize;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import com.ryuqq.fileflow.domain.upload.vo.FileAsset;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * BatchThumbnailService 단위 테스트
 *
 * 테스트 시나리오:
 * 1. 정상적인 배치 썸네일 생성 (SMALL + MEDIUM)
 * 2. 단일 썸네일 생성 (SMALL만)
 * 3. FileAsset 저장 검증
 * 4. FileRelationship 저장 검증
 * 5. 예외 처리 검증
 *
 * @author sangwon-ryu
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BatchThumbnailService 단위 테스트")
class BatchThumbnailServiceTest {

    @Mock
    private GenerateThumbnailUseCase generateThumbnailUseCase;

    @Mock
    private SaveFileAssetPort saveFileAssetPort;

    @Mock
    private SaveFileRelationshipPort saveFileRelationshipPort;

    private BatchThumbnailService batchThumbnailService;

    @BeforeEach
    void setUp() {
        batchThumbnailService = new BatchThumbnailService(
                generateThumbnailUseCase,
                saveFileAssetPort,
                saveFileRelationshipPort
        );
    }

    @Test
    @DisplayName("SMALL과 MEDIUM 썸네일을 일괄 생성하고 메타데이터를 저장한다")
    void shouldGenerateBatchThumbnailsSuccessfully() {
        // Given
        String sourceS3Uri = "s3://bucket/images/test-image.jpg";
        String imageId = FileId.generate().value(); // UUID 형식 사용
        String tenantId = "tenant-001";
        List<ThumbnailSize> sizes = List.of(ThumbnailSize.SMALL, ThumbnailSize.MEDIUM);

        BatchThumbnailCommand command = BatchThumbnailCommand.of(
                sourceS3Uri,
                imageId,
                ImageFormat.JPEG,
                tenantId,
                sizes
        );

        // 썸네일 생성 결과 Mock
        ThumbnailGenerationResult smallResult = ThumbnailGenerationResult.of(
                imageId,
                sourceS3Uri,
                "s3://bucket/thumbnails/small/test-image-123.webp",
                ThumbnailSize.SMALL,
                ImageDimension.of(2000, 1500),
                ImageDimension.of(300, 225),
                1_000_000L,
                50_000L,
                Duration.ofMillis(100)
        );

        ThumbnailGenerationResult mediumResult = ThumbnailGenerationResult.of(
                imageId,
                sourceS3Uri,
                "s3://bucket/thumbnails/medium/test-image-123.webp",
                ThumbnailSize.MEDIUM,
                ImageDimension.of(2000, 1500),
                ImageDimension.of(800, 600),
                1_000_000L,
                150_000L,
                Duration.ofMillis(200)
        );

        List<ThumbnailGenerationResult> thumbnailResults = List.of(smallResult, mediumResult);

        when(generateThumbnailUseCase.generateThumbnails(anyList()))
                .thenReturn(thumbnailResults);

        // FileAsset 저장 Mock
        when(saveFileAssetPort.save(any(FileAsset.class)))
                .thenAnswer(invocation -> {
                    FileAsset asset = invocation.getArgument(0);
                    // ID가 없는 새 FileAsset을 ID가 있는 것처럼 반환
                    return FileAsset.reconstitute(
                            FileId.generate(),
                            asset.getSessionId(),
                            asset.getTenantId(),
                            asset.getS3Location(),
                            asset.getChecksum(),
                            asset.getFileSize(),
                            asset.getContentType(),
                            asset.getCreatedAt()
                    );
                });

        // FileRelationship 저장 Mock
        when(saveFileRelationshipPort.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BatchThumbnailResult result = batchThumbnailService.generateBatchThumbnails(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSourceImageId()).isEqualTo(imageId);
        assertThat(result.getTotalThumbnailCount()).isEqualTo(2);
        assertThat(result.getThumbnails()).hasSize(2);
        assertThat(result.getErrorMessage()).isNull();

        // GenerateThumbnailCommand 생성 검증
        ArgumentCaptor<List<GenerateThumbnailCommand>> commandsCaptor = ArgumentCaptor.forClass(List.class);
        verify(generateThumbnailUseCase).generateThumbnails(commandsCaptor.capture());

        List<GenerateThumbnailCommand> capturedCommands = commandsCaptor.getValue();
        assertThat(capturedCommands).hasSize(2);
        assertThat(capturedCommands.get(0).thumbnailSize()).isEqualTo(ThumbnailSize.SMALL);
        assertThat(capturedCommands.get(1).thumbnailSize()).isEqualTo(ThumbnailSize.MEDIUM);

        // FileAsset 저장 검증 (2개 썸네일)
        verify(saveFileAssetPort, times(2)).save(any(FileAsset.class));

        // FileRelationship 저장 검증
        ArgumentCaptor<List<FileRelationship>> relationshipsCaptor = ArgumentCaptor.forClass(List.class);
        verify(saveFileRelationshipPort).saveAll(relationshipsCaptor.capture());

        List<FileRelationship> capturedRelationships = relationshipsCaptor.getValue();
        assertThat(capturedRelationships).hasSize(2);

        // 첫 번째 관계 검증 (SMALL)
        FileRelationship smallRelationship = capturedRelationships.get(0);
        assertThat(smallRelationship.getSourceFileId().value()).isEqualTo(imageId);
        assertThat(smallRelationship.getRelationshipType()).isEqualTo(FileRelationshipType.THUMBNAIL);
        assertThat(smallRelationship.getMetadataValue("thumbnail_size")).isEqualTo("SMALL");
        assertThat(smallRelationship.getMetadataValue("width")).isEqualTo(300);
        assertThat(smallRelationship.getMetadataValue("height")).isEqualTo(225);

        // 두 번째 관계 검증 (MEDIUM)
        FileRelationship mediumRelationship = capturedRelationships.get(1);
        assertThat(mediumRelationship.getSourceFileId().value()).isEqualTo(imageId);
        assertThat(mediumRelationship.getRelationshipType()).isEqualTo(FileRelationshipType.THUMBNAIL);
        assertThat(mediumRelationship.getMetadataValue("thumbnail_size")).isEqualTo("MEDIUM");
        assertThat(mediumRelationship.getMetadataValue("width")).isEqualTo(800);
        assertThat(mediumRelationship.getMetadataValue("height")).isEqualTo(600);
    }

    @Test
    @DisplayName("단일 썸네일 크기만 생성할 수 있다")
    void shouldGenerateSingleThumbnail() {
        // Given
        String imageId = FileId.generate().value(); // UUID 형식 사용
        BatchThumbnailCommand command = BatchThumbnailCommand.of(
                "s3://bucket/images/test.jpg",
                imageId,
                ImageFormat.JPEG,
                "tenant-001",
                List.of(ThumbnailSize.SMALL)
        );

        ThumbnailGenerationResult result = ThumbnailGenerationResult.of(
                imageId,
                "s3://bucket/images/test.jpg",
                String.format("s3://bucket/thumbnails/small/%s.webp", imageId),
                ThumbnailSize.SMALL,
                ImageDimension.of(1000, 800),
                ImageDimension.of(300, 240),
                500_000L,
                30_000L,
                Duration.ofMillis(50)
        );

        when(generateThumbnailUseCase.generateThumbnails(anyList()))
                .thenReturn(List.of(result));

        when(saveFileAssetPort.save(any(FileAsset.class)))
                .thenAnswer(invocation -> {
                    FileAsset asset = invocation.getArgument(0);
                    return FileAsset.reconstitute(
                            FileId.generate(),
                            asset.getSessionId(),
                            asset.getTenantId(),
                            asset.getS3Location(),
                            asset.getChecksum(),
                            asset.getFileSize(),
                            asset.getContentType(),
                            asset.getCreatedAt()
                    );
                });

        when(saveFileRelationshipPort.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BatchThumbnailResult batchResult = batchThumbnailService.generateBatchThumbnails(command);

        // Then
        assertThat(batchResult.isSuccess()).isTrue();
        assertThat(batchResult.getTotalThumbnailCount()).isEqualTo(1);
        assertThat(batchResult.getThumbnails()).hasSize(1);

        verify(saveFileAssetPort, times(1)).save(any(FileAsset.class));
        verify(saveFileRelationshipPort).saveAll(argThat(list -> list.size() == 1));
    }

    @Test
    @DisplayName("썸네일 생성 실패 시 실패 결과를 반환한다")
    void shouldReturnFailureResultWhenThumbnailGenerationFails() {
        // Given
        String imageId = FileId.generate().value(); // UUID 형식 사용
        BatchThumbnailCommand command = BatchThumbnailCommand.of(
                "s3://bucket/images/test.jpg",
                imageId,
                ImageFormat.JPEG,
                "tenant-001",
                List.of(ThumbnailSize.SMALL)
        );

        when(generateThumbnailUseCase.generateThumbnails(anyList()))
                .thenThrow(new RuntimeException("S3 connection failed"));

        // When
        BatchThumbnailResult result = batchThumbnailService.generateBatchThumbnails(command);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("S3 connection failed");
        assertThat(result.getTotalThumbnailCount()).isEqualTo(0);
        assertThat(result.getThumbnails()).isEmpty();

        // 실패 시에는 저장이 일어나지 않음
        verify(saveFileAssetPort, never()).save(any(FileAsset.class));
        verify(saveFileRelationshipPort, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Command가 null이면 예외가 발생한다")
    void shouldThrowExceptionWhenCommandIsNull() {
        // When & Then
        assertThatThrownBy(() -> batchThumbnailService.generateBatchThumbnails(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("BatchThumbnailCommand must not be null");
    }

    @Test
    @DisplayName("FileAsset 저장 실패 시 실패 결과를 반환한다")
    void shouldReturnFailureResultWhenFileAssetSaveFails() {
        // Given
        String imageId = FileId.generate().value(); // UUID 형식 사용
        BatchThumbnailCommand command = BatchThumbnailCommand.of(
                "s3://bucket/images/test.jpg",
                imageId,
                ImageFormat.JPEG,
                "tenant-001",
                List.of(ThumbnailSize.SMALL)
        );

        ThumbnailGenerationResult result = ThumbnailGenerationResult.of(
                imageId,
                "s3://bucket/images/test.jpg",
                String.format("s3://bucket/thumbnails/small/%s.webp", imageId),
                ThumbnailSize.SMALL,
                ImageDimension.of(1000, 800),
                ImageDimension.of(300, 240),
                500_000L,
                30_000L,
                Duration.ofMillis(50)
        );

        when(generateThumbnailUseCase.generateThumbnails(anyList()))
                .thenReturn(List.of(result));

        when(saveFileAssetPort.save(any(FileAsset.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        BatchThumbnailResult batchResult = batchThumbnailService.generateBatchThumbnails(command);

        // Then
        assertThat(batchResult.isSuccess()).isFalse();
        assertThat(batchResult.getErrorMessage()).contains("Database connection failed");

        // FileRelationship 저장은 시도되지 않음
        verify(saveFileRelationshipPort, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("S3 URI 파싱이 올바르게 동작한다")
    void shouldParseS3UriCorrectly() {
        // Given
        String imageId = FileId.generate().value(); // UUID 형식 사용
        BatchThumbnailCommand command = BatchThumbnailCommand.of(
                "s3://my-bucket/path/to/image.jpg",
                imageId,
                ImageFormat.JPEG,
                "tenant-001",
                List.of(ThumbnailSize.SMALL)
        );

        ThumbnailGenerationResult result = ThumbnailGenerationResult.of(
                imageId,
                "s3://my-bucket/path/to/image.jpg",
                String.format("s3://my-bucket/thumbnails/small/%s.webp", imageId),
                ThumbnailSize.SMALL,
                ImageDimension.of(1000, 800),
                ImageDimension.of(300, 240),
                500_000L,
                30_000L,
                Duration.ofMillis(50)
        );

        when(generateThumbnailUseCase.generateThumbnails(anyList()))
                .thenReturn(List.of(result));

        when(saveFileAssetPort.save(any(FileAsset.class)))
                .thenAnswer(invocation -> {
                    FileAsset asset = invocation.getArgument(0);
                    // S3Location 검증
                    assertThat(asset.getS3Location().bucket()).isEqualTo("my-bucket");
                    assertThat(asset.getS3Location().key()).isEqualTo(String.format("thumbnails/small/%s.webp", imageId));
                    return FileAsset.reconstitute(
                            FileId.generate(),
                            asset.getSessionId(),
                            asset.getTenantId(),
                            asset.getS3Location(),
                            asset.getChecksum(),
                            asset.getFileSize(),
                            asset.getContentType(),
                            asset.getCreatedAt()
                    );
                });

        when(saveFileRelationshipPort.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BatchThumbnailResult batchResult = batchThumbnailService.generateBatchThumbnails(command);

        // Then
        assertThat(batchResult.isSuccess()).isTrue();
        verify(saveFileAssetPort).save(any(FileAsset.class));
    }
}

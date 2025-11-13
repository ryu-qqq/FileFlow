package com.ryuqq.fileflow.application.file.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.application.file.manager.FileQueryManager;
import com.ryuqq.fileflow.application.file.port.out.MetadataPort;
import com.ryuqq.fileflow.application.file.port.out.SaveExtractedDataPort;
import com.ryuqq.fileflow.application.file.port.out.SaveFileVariantPort;
import com.ryuqq.fileflow.application.file.port.out.ThumbnailPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import com.ryuqq.fileflow.domain.file.asset.FileAssetId;
import com.ryuqq.fileflow.domain.file.extraction.ExtractedData;
import com.ryuqq.fileflow.domain.file.extraction.ExtractionMethod;
import com.ryuqq.fileflow.domain.file.extraction.ExtractionType;
import com.ryuqq.fileflow.domain.file.metadata.FileMetadata;
import com.ryuqq.fileflow.domain.file.thumbnail.ThumbnailInfo;
import com.ryuqq.fileflow.domain.file.variant.FileVariant;
import com.ryuqq.fileflow.domain.file.variant.VariantType;
import com.ryuqq.fileflow.domain.pipeline.PipelineResult;
import com.ryuqq.fileflow.domain.pipeline.fixture.PipelineResultFixture;
import com.ryuqq.fileflow.domain.file.asset.fixture.FileAssetFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * PipelineWorker 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>Pipeline 처리 성공 시나리오</li>
 *   <li>FileAsset 미존재 시 실패 처리</li>
 *   <li>썸네일 생성 실패 시 계속 진행</li>
 *   <li>메타데이터 추출 실패 시 계속 진행</li>
 *   <li>Multi-tenant 지원 (tenantId, organizationId 추출)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
class PipelineWorkerTest {

    @InjectMocks
    private PipelineWorker pipelineWorker;

    @Mock
    private FileQueryManager fileQueryManager;

    @Mock
    private ThumbnailPort thumbnailPort;

    @Mock
    private MetadataPort metadataPort;

    @Mock
    private SaveFileVariantPort saveFileVariantPort;

    @Mock
    private SaveExtractedDataPort saveExtractedDataPort;

    @Mock
    private ObjectMapper objectMapper;

    private FileAsset fileAsset;

    @BeforeEach
    void setUp() {
        fileAsset = FileAssetFixture.createProcessing(1L);
    }

    @Test
    @DisplayName("Pipeline 처리가 성공하면 PipelineResult.success()를 반환한다")
    void returnSuccessWhenPipelineProcessingSucceeds() throws Exception {
        // Given
        Long fileAssetId = 1L;
        given(fileQueryManager.findById(fileAssetId))
            .willReturn(Optional.of(fileAsset));

        // 이미지가 아니므로 썸네일 생성 스킵
        given(metadataPort.extractMetadata(fileAsset))
            .willReturn(new FileMetadata(fileAsset.getId(), Map.of("width", "1920", "height", "1080")));

        given(objectMapper.writeValueAsString(any()))
            .willReturn("{\"width\":\"1920\",\"height\":\"1080\"}");

        given(saveExtractedDataPort.save(any(ExtractedData.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // When
        PipelineResult result = pipelineWorker.startPipeline(fileAssetId);

        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(metadataPort).extractMetadata(fileAsset);
        verify(saveExtractedDataPort).save(any(ExtractedData.class));
    }

    @Test
    @DisplayName("FileAsset이 존재하지 않으면 PipelineResult.failure()를 반환한다")
    void returnFailureWhenFileAssetNotFound() {
        // Given
        Long fileAssetId = 999L;
        given(fileQueryManager.findById(fileAssetId))
            .willReturn(Optional.empty());

        // When
        PipelineResult result = pipelineWorker.startPipeline(fileAssetId);

        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.errorMessage()).contains("FileAsset not found");
        verify(thumbnailPort, never()).generateThumbnail(any());
        verify(metadataPort, never()).extractMetadata(any());
    }

    @Test
    @DisplayName("이미지 파일인 경우 썸네일을 생성하고 FileVariant로 저장한다")
    void generateThumbnailForImageFile() throws Exception {
        // Given
        FileAsset imageFile = FileAssetFixture.createProcessing(1L);
        Long fileAssetId = 1L;
        given(fileQueryManager.findById(fileAssetId))
            .willReturn(Optional.of(imageFile));

        ThumbnailInfo thumbnailInfo = new ThumbnailInfo(
            com.ryuqq.fileflow.domain.upload.StorageKey.of("thumbnails/test-thumb.jpg"),
            com.ryuqq.fileflow.domain.file.thumbnail.ImageWidth.of(200),
            com.ryuqq.fileflow.domain.file.thumbnail.ImageHeight.of(200),
            com.ryuqq.fileflow.domain.upload.FileSize.of(1024L),
            com.ryuqq.fileflow.domain.upload.MimeType.of("image/jpeg")
        );

        given(thumbnailPort.generateThumbnail(imageFile))
            .willReturn(thumbnailInfo);

        // 빈 Map 반환 (메타데이터 저장 스킵 - 썸네일만 테스트)
        given(metadataPort.extractMetadata(imageFile))
            .willReturn(new FileMetadata(imageFile.getId(), Map.of()));

        given(saveFileVariantPort.save(any(FileVariant.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // When
        PipelineResult result = pipelineWorker.startPipeline(fileAssetId);

        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(thumbnailPort).generateThumbnail(imageFile);
        verify(saveFileVariantPort).save(any(FileVariant.class));
    }

    @Test
    @DisplayName("썸네일 생성 실패 시에도 Pipeline 처리를 계속한다")
    void continuePipelineWhenThumbnailGenerationFails() throws Exception {
        // Given
        FileAsset imageFile = FileAssetFixture.createProcessing(1L);
        Long fileAssetId = 1L;
        given(fileQueryManager.findById(fileAssetId))
            .willReturn(Optional.of(imageFile));

        given(thumbnailPort.generateThumbnail(imageFile))
            .willThrow(new RuntimeException("Thumbnail generation failed"));

        // 빈 Map이 아닌 실제 메타데이터 반환 (saveExtractedDataPort 호출을 위해)
        given(metadataPort.extractMetadata(imageFile))
            .willReturn(new FileMetadata(imageFile.getId(), Map.of("width", "1920", "height", "1080")));

        given(objectMapper.writeValueAsString(any()))
            .willReturn("{\"width\":\"1920\",\"height\":\"1080\"}");

        given(saveExtractedDataPort.save(any(ExtractedData.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // When
        PipelineResult result = pipelineWorker.startPipeline(fileAssetId);

        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(metadataPort).extractMetadata(imageFile);
        verify(saveExtractedDataPort).save(any(ExtractedData.class));
    }

    @Test
    @DisplayName("메타데이터 추출 실패 시에도 Pipeline 처리를 계속한다")
    void continuePipelineWhenMetadataExtractionFails() throws Exception {
        // Given
        Long fileAssetId = 1L;
        given(fileQueryManager.findById(fileAssetId))
            .willReturn(Optional.of(fileAsset));

        given(metadataPort.extractMetadata(fileAsset))
            .willThrow(new RuntimeException("Metadata extraction failed"));

        // When
        PipelineResult result = pipelineWorker.startPipeline(fileAssetId);

        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(saveExtractedDataPort, never()).save(any());
    }

    @Test
    @DisplayName("ExtractedData 저장 시 FileAsset의 tenantId와 organizationId를 사용한다")
    void useFileAssetTenantIdAndOrganizationIdForExtractedData() throws Exception {
        // Given
        Long fileAssetId = 1L;
        Long tenantId = 100L;
        Long organizationId = 200L;

        // createCustomWithId()를 사용하여 ID를 포함한 FileAsset 생성
        FileAsset customFileAsset = FileAssetFixture.createCustomWithId(
            fileAssetId,
            tenantId,
            organizationId,
            1L,
            "test.jpg",
            1024L,
            "image/jpeg",
            "uploads/test.jpg",
            "checksum"
        );

        given(fileQueryManager.findById(fileAssetId))
            .willReturn(Optional.of(customFileAsset));

        given(metadataPort.extractMetadata(customFileAsset))
            .willReturn(new FileMetadata(customFileAsset.getId(), Map.of("test", "value")));

        given(objectMapper.writeValueAsString(any()))
            .willReturn("{\"test\":\"value\"}");

        given(saveExtractedDataPort.save(any(ExtractedData.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<ExtractedData> extractedDataCaptor = ArgumentCaptor.forClass(ExtractedData.class);

        // When
        PipelineResult result = pipelineWorker.startPipeline(fileAssetId);

        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(saveExtractedDataPort).save(extractedDataCaptor.capture());

        ExtractedData savedData = extractedDataCaptor.getValue();
        assertThat(savedData.getTenantId()).isEqualTo(tenantId);
        assertThat(savedData.getOrganizationId()).isEqualTo(organizationId);
        assertThat(savedData.getExtractionType()).isEqualTo(ExtractionType.METADATA);
        assertThat(savedData.getExtractionMethod()).isEqualTo(ExtractionMethod.TIKA);
    }

    @Test
    @DisplayName("예외 발생 시 PipelineResult.failure()를 반환한다")
    void returnFailureWhenExceptionOccurs() {
        // Given
        Long fileAssetId = 1L;
        given(fileQueryManager.findById(fileAssetId))
            .willThrow(new RuntimeException("Unexpected error"));

        // When
        PipelineResult result = pipelineWorker.startPipeline(fileAssetId);

        // Then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.errorMessage()).isNotNull();
    }
}


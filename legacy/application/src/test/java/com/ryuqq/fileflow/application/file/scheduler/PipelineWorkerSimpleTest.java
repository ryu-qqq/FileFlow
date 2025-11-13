package com.ryuqq.fileflow.application.file.scheduler;

import com.ryuqq.fileflow.application.file.manager.FileQueryManager;
import com.ryuqq.fileflow.application.file.port.out.MetadataPort;
import com.ryuqq.fileflow.application.file.port.out.SaveExtractedDataPort;
import com.ryuqq.fileflow.application.file.port.out.SaveFileVariantPort;
import com.ryuqq.fileflow.application.file.port.out.ThumbnailPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * PipelineWorker 단위 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>FileAsset이 존재하지 않을 때 Pipeline 종료</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PipelineWorkerSimpleTest {

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

    @Test
    @DisplayName("FileAsset이 존재하지 않으면 Pipeline이 종료되어야 한다")
    void stopPipelineWhenFileAssetNotFound() {
        // Given: FileAsset 조회 실패
        given(fileQueryManager.findById(anyLong()))
            .willReturn(Optional.empty());

        // When: Pipeline 실행
        pipelineWorker.startPipeline(999L);

        // Then: 모든 작업이 호출되지 않아야 함
        verify(thumbnailPort, never()).generateThumbnail(any());
        verify(metadataPort, never()).extractMetadata(any());
        verify(saveFileVariantPort, never()).save(any());
        verify(saveExtractedDataPort, never()).save(any());
    }
}

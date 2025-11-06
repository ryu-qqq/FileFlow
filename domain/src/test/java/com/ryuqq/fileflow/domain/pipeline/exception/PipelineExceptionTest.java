package com.ryuqq.fileflow.domain.pipeline.exception;

import com.ryuqq.fileflow.domain.common.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pipeline Exception 테스트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Tag("unit")
@Tag("domain")
class PipelineExceptionTest {

    @Test
    @DisplayName("FileAssetNotFoundException을 생성할 수 있다")
    void createFileAssetNotFoundException() {
        // Given
        Long fileAssetId = 999L;

        // When
        FileAssetNotFoundException exception = new FileAssetNotFoundException(fileAssetId);

        // Then
        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.code()).isEqualTo("PIPELINE-001");
        assertThat(exception.getMessage()).contains("FileAsset not found");
        assertThat(exception.getMessage()).contains(fileAssetId.toString());
    }

    @Test
    @DisplayName("ThumbnailGenerationFailedException을 생성할 수 있다")
    void createThumbnailGenerationFailedException() {
        // Given
        Long fileAssetId = 1L;
        Throwable cause = new RuntimeException("Thumbnail error");

        // When
        ThumbnailGenerationFailedException exception =
            new ThumbnailGenerationFailedException(fileAssetId, cause);

        // Then
        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.code()).isEqualTo("PIPELINE-002");
        assertThat(exception.getMessage()).contains("Thumbnail generation failed");
        assertThat(exception.getMessage()).contains(fileAssetId.toString());
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("MetadataExtractionFailedException을 생성할 수 있다")
    void createMetadataExtractionFailedException() {
        // Given
        Long fileAssetId = 1L;
        Throwable cause = new RuntimeException("Metadata error");

        // When
        MetadataExtractionFailedException exception =
            new MetadataExtractionFailedException(fileAssetId, cause);

        // Then
        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.code()).isEqualTo("PIPELINE-003");
        assertThat(exception.getMessage()).contains("Metadata extraction failed");
        assertThat(exception.getMessage()).contains(fileAssetId.toString());
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("InvalidOutboxStateTransitionException을 생성할 수 있다")
    void createInvalidOutboxStateTransitionException() {
        // Given
        String currentStatus = "PENDING";
        String targetStatus = "COMPLETED";

        // When
        InvalidOutboxStateTransitionException exception =
            new InvalidOutboxStateTransitionException(currentStatus, targetStatus);

        // Then
        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.code()).isEqualTo("PIPELINE-101");
        assertThat(exception.getMessage()).contains(currentStatus);
        assertThat(exception.getMessage()).contains(targetStatus);
    }

    @Test
    @DisplayName("OutboxMaxRetryExceededException을 생성할 수 있다")
    void createOutboxMaxRetryExceededException() {
        // Given
        Long outboxId = 1L;
        int retryCount = 5;
        int maxRetryCount = 3;

        // When
        OutboxMaxRetryExceededException exception =
            new OutboxMaxRetryExceededException(outboxId, retryCount, maxRetryCount);

        // Then
        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.code()).isEqualTo("PIPELINE-102");
        assertThat(exception.getMessage()).contains("Max retry count exceeded");
        assertThat(exception.getMessage()).contains(outboxId.toString());
        assertThat(exception.getMessage()).contains(String.valueOf(retryCount));
        assertThat(exception.getMessage()).contains(String.valueOf(maxRetryCount));
    }

    @Test
    @DisplayName("PipelineErrorCode의 모든 값이 정의되어 있다")
    void allPipelineErrorCodesDefined() {
        // Then
        assertThat(PipelineErrorCode.PIPELINE_FILE_ASSET_NOT_FOUND.getCode())
            .isEqualTo("PIPELINE-001");
        assertThat(PipelineErrorCode.PIPELINE_THUMBNAIL_GENERATION_FAILED.getCode())
            .isEqualTo("PIPELINE-002");
        assertThat(PipelineErrorCode.PIPELINE_METADATA_EXTRACTION_FAILED.getCode())
            .isEqualTo("PIPELINE-003");
        assertThat(PipelineErrorCode.OUTBOX_ALREADY_PROCESSING.getCode())
            .isEqualTo("PIPELINE-100");
        assertThat(PipelineErrorCode.OUTBOX_INVALID_STATE_TRANSITION.getCode())
            .isEqualTo("PIPELINE-101");
        assertThat(PipelineErrorCode.OUTBOX_MAX_RETRY_EXCEEDED.getCode())
            .isEqualTo("PIPELINE-102");
    }
}


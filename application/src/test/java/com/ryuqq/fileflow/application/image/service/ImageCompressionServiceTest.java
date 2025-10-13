package com.ryuqq.fileflow.application.image.service;

import com.ryuqq.fileflow.application.image.ImageConversionException;
import com.ryuqq.fileflow.application.image.dto.CompressImageCommand;
import com.ryuqq.fileflow.application.image.dto.ImageConversionResult;
import com.ryuqq.fileflow.application.image.port.out.ImageConversionPort;
import com.ryuqq.fileflow.domain.image.vo.CompressionQuality;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import com.ryuqq.fileflow.domain.image.vo.ImageOptimizationRequest;
import com.ryuqq.fileflow.domain.image.vo.ImageOptimizationResult;
import com.ryuqq.fileflow.domain.image.vo.OptimizationStrategy;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ImageCompressionService 단위 테스트
 *
 * @author sangwon-ryu
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageCompressionService 단위 테스트")
class ImageCompressionServiceTest {

    @Mock
    private ImageConversionPort imageConversionPort;

    private ImageCompressionService imageCompressionService;

    @BeforeEach
    void setUp() {
        imageCompressionService = new ImageCompressionService(imageConversionPort);
    }

    @Test
    @DisplayName("JPEG 이미지를 품질 90%로 압축한다")
    void compressJpegImage() {
        // Given
        FileId fileId = FileId.generate();
        String sourceS3Uri = "s3://bucket/test.jpg";
        CompressImageCommand command = CompressImageCommand.withDefaults(
                fileId,
                sourceS3Uri,
                ImageFormat.JPEG
        );

        ImageOptimizationResult optimizationResult = ImageOptimizationResult.of(
                "s3://bucket/test-compressed.jpg",
                ImageFormat.JPEG,
                ImageFormat.JPEG,
                OptimizationStrategy.COMPRESS_ONLY,
                1_000_000L, // 1MB 원본
                600_000L,   // 600KB 압축 (40% 감소)
                ImageDimension.of(1920, 1080),
                ImageDimension.of(1920, 1080),
                Duration.ofSeconds(1)
        );

        when(imageConversionPort.supports(ImageFormat.JPEG)).thenReturn(true);
        when(imageConversionPort.compressImage(any(ImageOptimizationRequest.class)))
                .thenReturn(optimizationResult);

        // When
        ImageConversionResult result = imageCompressionService.compressImage(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.fileId()).isEqualTo(fileId);
        assertThat(result.resultS3Uri()).isEqualTo("s3://bucket/test-compressed.jpg");
        assertThat(result.originalFormat()).isEqualTo(ImageFormat.JPEG);
        assertThat(result.resultFormat()).isEqualTo(ImageFormat.JPEG);
        assertThat(result.originalSizeBytes()).isEqualTo(1_000_000L);
        assertThat(result.convertedSizeBytes()).isEqualTo(600_000L);

        // ImageConversionPort에 올바른 요청이 전달되었는지 확인
        ArgumentCaptor<ImageOptimizationRequest> requestCaptor =
                ArgumentCaptor.forClass(ImageOptimizationRequest.class);
        verify(imageConversionPort).compressImage(requestCaptor.capture());

        ImageOptimizationRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getSourceS3Uri()).isEqualTo(sourceS3Uri);
        assertThat(capturedRequest.getSourceFormat()).isEqualTo(ImageFormat.JPEG);
        assertThat(capturedRequest.getStrategy()).isEqualTo(OptimizationStrategy.COMPRESS_ONLY);
        assertThat(capturedRequest.getQuality()).isEqualTo(CompressionQuality.defaultQuality());
    }

    @Test
    @DisplayName("PNG 이미지를 품질 90%로 압축한다")
    void compressPngImage() {
        // Given
        FileId fileId = FileId.generate();
        String sourceS3Uri = "s3://bucket/test.png";
        CompressImageCommand command = CompressImageCommand.withDefaults(
                fileId,
                sourceS3Uri,
                ImageFormat.PNG
        );

        ImageOptimizationResult optimizationResult = ImageOptimizationResult.of(
                "s3://bucket/test-compressed.png",
                ImageFormat.PNG,
                ImageFormat.PNG,
                OptimizationStrategy.COMPRESS_ONLY,
                2_000_000L, // 2MB 원본
                1_000_000L, // 1MB 압축 (50% 감소)
                ImageDimension.of(1920, 1080),
                ImageDimension.of(1920, 1080),
                Duration.ofSeconds(1)
        );

        when(imageConversionPort.supports(ImageFormat.PNG)).thenReturn(true);
        when(imageConversionPort.compressImage(any(ImageOptimizationRequest.class)))
                .thenReturn(optimizationResult);

        // When
        ImageConversionResult result = imageCompressionService.compressImage(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.originalFormat()).isEqualTo(ImageFormat.PNG);
        assertThat(result.resultFormat()).isEqualTo(ImageFormat.PNG);
    }

    @Test
    @DisplayName("압축 효과가 10% 미만이면 예외가 발생한다")
    void throwsExceptionWhenCompressionEffectivenessIsLow() {
        // Given
        FileId fileId = FileId.generate();
        CompressImageCommand command = CompressImageCommand.withDefaults(
                fileId,
                "s3://bucket/test.jpg",
                ImageFormat.JPEG
        );

        // 압축 효과가 5%만 발생 (10% 미만)
        ImageOptimizationResult optimizationResult = ImageOptimizationResult.of(
                "s3://bucket/test-compressed.jpg",
                ImageFormat.JPEG,
                ImageFormat.JPEG,
                OptimizationStrategy.COMPRESS_ONLY,
                1_000_000L, // 1MB 원본
                950_000L,   // 950KB 압축 (5% 감소)
                ImageDimension.of(1920, 1080),
                ImageDimension.of(1920, 1080),
                Duration.ofSeconds(1)
        );

        when(imageConversionPort.supports(ImageFormat.JPEG)).thenReturn(true);
        when(imageConversionPort.compressImage(any(ImageOptimizationRequest.class)))
                .thenReturn(optimizationResult);

        // When & Then
        assertThatThrownBy(() -> imageCompressionService.compressImage(command))
                .isInstanceOf(ImageConversionException.class)
                .hasMessageContaining("Compression effectiveness too low");
    }

    @Test
    @DisplayName("압축된 파일이 원본보다 크면 예외가 발생한다")
    void throwsExceptionWhenCompressedSizeIsLarger() {
        // Given
        FileId fileId = FileId.generate();
        CompressImageCommand command = CompressImageCommand.withDefaults(
                fileId,
                "s3://bucket/test.jpg",
                ImageFormat.JPEG
        );

        // 압축된 파일이 원본보다 큼
        ImageOptimizationResult optimizationResult = ImageOptimizationResult.of(
                "s3://bucket/test-compressed.jpg",
                ImageFormat.JPEG,
                ImageFormat.JPEG,
                OptimizationStrategy.COMPRESS_ONLY,
                1_000_000L, // 1MB 원본
                1_100_000L, // 1.1MB 압축 (오히려 증가)
                ImageDimension.of(1920, 1080),
                ImageDimension.of(1920, 1080),
                Duration.ofSeconds(1)
        );

        when(imageConversionPort.supports(ImageFormat.JPEG)).thenReturn(true);
        when(imageConversionPort.compressImage(any(ImageOptimizationRequest.class)))
                .thenReturn(optimizationResult);

        // When & Then
        assertThatThrownBy(() -> imageCompressionService.compressImage(command))
                .isInstanceOf(ImageConversionException.class)
                .hasMessageContaining("compressed size")
                .hasMessageContaining("is not smaller than original");
    }

    @Test
    @DisplayName("지원하지 않는 포맷이면 예외가 발생한다")
    void throwsExceptionForUnsupportedFormat() {
        // Given
        FileId fileId = FileId.generate();
        CompressImageCommand command = CompressImageCommand.withDefaults(
                fileId,
                "s3://bucket/test.jpg",
                ImageFormat.JPEG
        );

        when(imageConversionPort.supports(ImageFormat.JPEG)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> imageCompressionService.compressImage(command))
                .isInstanceOf(ImageConversionException.class)
                .hasMessageContaining("format");
    }

    @Test
    @DisplayName("Command가 null이면 예외가 발생한다")
    void throwsExceptionWhenCommandIsNull() {
        // When & Then
        assertThatThrownBy(() -> imageCompressionService.compressImage(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("CompressImageCommand must not be null");
    }

    @Test
    @DisplayName("예상치 못한 예외가 발생하면 ImageConversionException으로 래핑한다")
    void throwsImageConversionExceptionWhenUnexpectedErrorOccurs() {
        // Given
        FileId fileId = FileId.generate();
        CompressImageCommand command = CompressImageCommand.withDefaults(
                fileId,
                "s3://bucket/test.jpg",
                ImageFormat.JPEG
        );

        when(imageConversionPort.supports(ImageFormat.JPEG)).thenReturn(true);
        when(imageConversionPort.compressImage(any(ImageOptimizationRequest.class)))
                .thenThrow(new RuntimeException("Unexpected database error"));

        // When & Then
        assertThatThrownBy(() -> imageCompressionService.compressImage(command))
                .isInstanceOf(ImageConversionException.class)
                .hasMessageContaining("Failed to compress image")
                .hasCauseInstanceOf(RuntimeException.class);
    }
}

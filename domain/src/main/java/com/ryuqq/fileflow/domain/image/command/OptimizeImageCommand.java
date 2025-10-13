package com.ryuqq.fileflow.domain.image.command;

import com.ryuqq.fileflow.domain.image.vo.CompressionQuality;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import com.ryuqq.fileflow.domain.image.vo.OptimizationStrategy;

/**
 * 이미지 최적화 명령을 나타내는 Command Object
 *
 * CQRS 패턴:
 * - 이미지 최적화 요청의 의도를 명시적으로 표현
 * - 불변 객체로 안전한 전달 보장
 */
public record OptimizeImageCommand(
        String imageId,
        String sourceS3Uri,
        ImageFormat sourceFormat,
        OptimizationStrategy strategy,
        CompressionQuality quality,
        ImageDimension targetDimension,
        boolean preserveMetadata
) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public OptimizeImageCommand {
        CommandValidators.validateImageId(imageId);
        CommandValidators.validateSourceS3Uri(sourceS3Uri);
        validateSourceFormat(sourceFormat);
        validateStrategy(strategy);
        validateQuality(quality);
        // targetDimension은 optional이므로 null 허용
    }

    /**
     * OptimizeImageCommand를 생성합니다.
     *
     * @param imageId 이미지 ID
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param sourceFormat 소스 이미지 포맷
     * @param strategy 최적화 전략
     * @param quality 압축 품질
     * @param targetDimension 타겟 크기 (optional)
     * @param preserveMetadata 메타데이터 보존 여부
     * @return OptimizeImageCommand 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static OptimizeImageCommand of(
            String imageId,
            String sourceS3Uri,
            ImageFormat sourceFormat,
            OptimizationStrategy strategy,
            CompressionQuality quality,
            ImageDimension targetDimension,
            boolean preserveMetadata
    ) {
        return new OptimizeImageCommand(
                imageId,
                sourceS3Uri,
                sourceFormat,
                strategy,
                quality,
                targetDimension,
                preserveMetadata
        );
    }

    /**
     * 기본 설정으로 OptimizeImageCommand를 생성합니다.
     * - 전략: AUTO
     * - 품질: 90%
     * - 크기 조정: 없음
     * - 메타데이터 보존: false
     *
     * @param imageId 이미지 ID
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param sourceFormat 소스 이미지 포맷
     * @return OptimizeImageCommand 인스턴스
     */
    public static OptimizeImageCommand withDefaults(
            String imageId,
            String sourceS3Uri,
            ImageFormat sourceFormat
    ) {
        return new OptimizeImageCommand(
                imageId,
                sourceS3Uri,
                sourceFormat,
                OptimizationStrategy.AUTO,
                CompressionQuality.defaultQuality(),
                null,
                false
        );
    }

    // ========== Validation Methods ==========

    private static void validateSourceFormat(ImageFormat sourceFormat) {
        if (sourceFormat == null) {
            throw new IllegalArgumentException("Source format cannot be null");
        }
    }

    private static void validateStrategy(OptimizationStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Optimization strategy cannot be null");
        }
    }

    private static void validateQuality(CompressionQuality quality) {
        if (quality == null) {
            throw new IllegalArgumentException("Compression quality cannot be null");
        }
    }
}

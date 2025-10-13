package com.ryuqq.fileflow.domain.image.vo;

import java.util.Objects;

/**
 * 이미지 최적화 요청을 표현하는 Value Object
 *
 * 불변성:
 * - 모든 필드는 final
 * - 생성 후 변경 불가
 *
 * 비즈니스 규칙:
 * - 소스 이미지 포맷 필수
 * - 최적화 전략 선택 가능 (기본값: AUTO)
 * - 압축 품질 지정 가능 (기본값: 90%)
 * - 타겟 크기 지정 가능 (선택)
 */
public final class ImageOptimizationRequest {

    private final String sourceS3Uri;
    private final ImageFormat sourceFormat;
    private final OptimizationStrategy strategy;
    private final CompressionQuality quality;
    private final ImageDimension targetDimension;
    private final boolean preserveMetadata;

    private ImageOptimizationRequest(
            String sourceS3Uri,
            ImageFormat sourceFormat,
            OptimizationStrategy strategy,
            CompressionQuality quality,
            ImageDimension targetDimension,
            boolean preserveMetadata
    ) {
        this.sourceS3Uri = sourceS3Uri;
        this.sourceFormat = sourceFormat;
        this.strategy = strategy;
        this.quality = quality;
        this.targetDimension = targetDimension;
        this.preserveMetadata = preserveMetadata;
    }

    /**
     * ImageOptimizationRequest를 생성합니다.
     *
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param sourceFormat 소스 이미지 포맷
     * @param strategy 최적화 전략
     * @param quality 압축 품질
     * @param targetDimension 타겟 크기 (null 가능)
     * @param preserveMetadata 메타데이터 보존 여부
     * @return ImageOptimizationRequest 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static ImageOptimizationRequest of(
            String sourceS3Uri,
            ImageFormat sourceFormat,
            OptimizationStrategy strategy,
            CompressionQuality quality,
            ImageDimension targetDimension,
            boolean preserveMetadata
    ) {
        validateSourceS3Uri(sourceS3Uri);
        validateSourceFormat(sourceFormat);
        validateStrategy(strategy);
        validateQuality(quality);
        // targetDimension은 optional이므로 null 허용

        return new ImageOptimizationRequest(
                sourceS3Uri,
                sourceFormat,
                strategy,
                quality,
                targetDimension,
                preserveMetadata
        );
    }

    /**
     * 기본 설정으로 ImageOptimizationRequest를 생성합니다.
     * - 전략: AUTO
     * - 품질: 90%
     * - 크기 조정: 없음
     * - 메타데이터 보존: false
     *
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param sourceFormat 소스 이미지 포맷
     * @return ImageOptimizationRequest 인스턴스
     */
    public static ImageOptimizationRequest withDefaults(
            String sourceS3Uri,
            ImageFormat sourceFormat
    ) {
        return of(
                sourceS3Uri,
                sourceFormat,
                OptimizationStrategy.AUTO,
                CompressionQuality.defaultQuality(),
                null,
                false
        );
    }

    /**
     * 자동 전략인지 확인합니다.
     *
     * @return 자동 전략이면 true
     */
    public boolean isAutoStrategy() {
        return strategy.isAuto();
    }

    /**
     * 크기 조정이 필요한지 확인합니다.
     *
     * @return targetDimension이 지정되었으면 true
     */
    public boolean needsResize() {
        return targetDimension != null;
    }

    /**
     * WebP로 변환하는 요청인지 확인합니다.
     *
     * @return WebP 변환 전략이면 true
     */
    public boolean convertsToWebP() {
        return strategy.convertsToWebP();
    }

    /**
     * 고품질 압축을 사용하는지 확인합니다.
     *
     * @return 압축 품질이 90% 이상이면 true
     */
    public boolean isHighQuality() {
        return quality.isHighQuality();
    }

    /**
     * 최적화 전략을 결정합니다.
     * AUTO 전략인 경우 소스 포맷에 따라 최적 전략을 반환합니다.
     *
     * @return 결정된 최적화 전략
     */
    public OptimizationStrategy determineStrategy() {
        if (strategy.isAuto()) {
            return OptimizationStrategy.determineOptimal(sourceFormat);
        }
        return strategy;
    }

    /**
     * 타겟 포맷을 결정합니다.
     *
     * @return 타겟 이미지 포맷
     */
    public ImageFormat determineTargetFormat() {
        OptimizationStrategy actualStrategy = determineStrategy();

        if (actualStrategy.convertsToWebP()) {
            return ImageFormat.WEBP;
        }

        return sourceFormat;
    }

    // ========== Validation Methods ==========

    private static void validateSourceS3Uri(String sourceS3Uri) {
        if (sourceS3Uri == null || sourceS3Uri.trim().isEmpty()) {
            throw new IllegalArgumentException("Source S3 URI cannot be null or empty");
        }
        if (!sourceS3Uri.startsWith("s3://")) {
            throw new IllegalArgumentException("Source S3 URI must start with 's3://'");
        }
    }

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

    // ========== Getters ==========

    public String getSourceS3Uri() {
        return sourceS3Uri;
    }

    public ImageFormat getSourceFormat() {
        return sourceFormat;
    }

    public OptimizationStrategy getStrategy() {
        return strategy;
    }

    public CompressionQuality getQuality() {
        return quality;
    }

    public ImageDimension getTargetDimension() {
        return targetDimension;
    }

    public boolean isPreserveMetadata() {
        return preserveMetadata;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageOptimizationRequest that = (ImageOptimizationRequest) o;
        return preserveMetadata == that.preserveMetadata &&
               Objects.equals(sourceS3Uri, that.sourceS3Uri) &&
               sourceFormat == that.sourceFormat &&
               strategy == that.strategy &&
               Objects.equals(quality, that.quality) &&
               Objects.equals(targetDimension, that.targetDimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceS3Uri, sourceFormat, strategy, quality, targetDimension, preserveMetadata);
    }

    @Override
    public String toString() {
        return "ImageOptimizationRequest{" +
                "sourceS3Uri='" + sourceS3Uri + '\'' +
                ", sourceFormat=" + sourceFormat +
                ", strategy=" + strategy +
                ", quality=" + quality +
                ", targetDimension=" + targetDimension +
                ", preserveMetadata=" + preserveMetadata +
                '}';
    }
}

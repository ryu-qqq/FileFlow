package com.ryuqq.fileflow.domain.image.vo;

import com.ryuqq.fileflow.domain.image.util.FileSizeFormatter;

import java.time.Duration;
import java.util.Objects;

/**
 * 이미지 최적화 결과를 표현하는 Value Object
 *
 * 불변성:
 * - 모든 필드는 final
 * - 생성 후 변경 불가
 *
 * 비즈니스 규칙:
 * - 원본 크기와 최적화 후 크기를 비교하여 감소율 계산
 * - 처리 시간 기록
 * - 적용된 최적화 전략 기록
 */
public final class ImageOptimizationResult {

    private final String resultS3Uri;
    private final ImageFormat originalFormat;
    private final ImageFormat resultFormat;
    private final OptimizationStrategy appliedStrategy;
    private final long originalSizeBytes;
    private final long optimizedSizeBytes;
    private final ImageDimension originalDimension;
    private final ImageDimension resultDimension;
    private final Duration processingTime;

    private ImageOptimizationResult(
            String resultS3Uri,
            ImageFormat originalFormat,
            ImageFormat resultFormat,
            OptimizationStrategy appliedStrategy,
            long originalSizeBytes,
            long optimizedSizeBytes,
            ImageDimension originalDimension,
            ImageDimension resultDimension,
            Duration processingTime
    ) {
        this.resultS3Uri = resultS3Uri;
        this.originalFormat = originalFormat;
        this.resultFormat = resultFormat;
        this.appliedStrategy = appliedStrategy;
        this.originalSizeBytes = originalSizeBytes;
        this.optimizedSizeBytes = optimizedSizeBytes;
        this.originalDimension = originalDimension;
        this.resultDimension = resultDimension;
        this.processingTime = processingTime;
    }

    /**
     * ImageOptimizationResult를 생성합니다.
     *
     * @param resultS3Uri 결과 이미지 S3 URI
     * @param originalFormat 원본 이미지 포맷
     * @param resultFormat 결과 이미지 포맷
     * @param appliedStrategy 적용된 최적화 전략
     * @param originalSizeBytes 원본 파일 크기 (bytes)
     * @param optimizedSizeBytes 최적화 후 파일 크기 (bytes)
     * @param originalDimension 원본 이미지 크기
     * @param resultDimension 결과 이미지 크기
     * @param processingTime 처리 시간
     * @return ImageOptimizationResult 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static ImageOptimizationResult of(
            String resultS3Uri,
            ImageFormat originalFormat,
            ImageFormat resultFormat,
            OptimizationStrategy appliedStrategy,
            long originalSizeBytes,
            long optimizedSizeBytes,
            ImageDimension originalDimension,
            ImageDimension resultDimension,
            Duration processingTime
    ) {
        validateResultS3Uri(resultS3Uri);
        validateImageFormat("Original format", originalFormat);
        validateImageFormat("Result format", resultFormat);
        validateAppliedStrategy(appliedStrategy);
        validateSize("Original size", originalSizeBytes);
        validateSize("Optimized size", optimizedSizeBytes);
        validateDimension("Original dimension", originalDimension);
        validateDimension("Result dimension", resultDimension);
        validateProcessingTime(processingTime);

        return new ImageOptimizationResult(
                resultS3Uri,
                originalFormat,
                resultFormat,
                appliedStrategy,
                originalSizeBytes,
                optimizedSizeBytes,
                originalDimension,
                resultDimension,
                processingTime
        );
    }

    /**
     * 파일 크기 감소율을 계산합니다.
     *
     * @return 감소율 (0.0 ~ 1.0)
     */
    public double getReductionRate() {
        if (originalSizeBytes == 0) {
            return 0.0;
        }
        long reduced = originalSizeBytes - optimizedSizeBytes;
        return (double) reduced / originalSizeBytes;
    }

    /**
     * 파일 크기 감소율을 퍼센트로 반환합니다.
     *
     * @return 감소율 (0 ~ 100)
     */
    public double getReductionPercentage() {
        return getReductionRate() * 100;
    }

    /**
     * 감소된 파일 크기를 반환합니다.
     *
     * @return 감소된 바이트 수
     */
    public long getReducedBytes() {
        return originalSizeBytes - optimizedSizeBytes;
    }

    /**
     * 최적화가 성공적이었는지 확인합니다.
     * 파일 크기가 줄어들었으면 성공으로 간주합니다.
     *
     * @return 파일 크기가 줄어들었으면 true
     */
    public boolean isSuccessful() {
        return optimizedSizeBytes < originalSizeBytes;
    }

    /**
     * 크기가 조정되었는지 확인합니다.
     *
     * @return 원본과 결과 크기가 다르면 true
     */
    public boolean wasResized() {
        return !originalDimension.equals(resultDimension);
    }

    /**
     * 포맷이 변환되었는지 확인합니다.
     *
     * @return 포맷이 변경되었으면 true
     */
    public boolean wasFormatConverted() {
        return !resultFormat.equals(originalFormat);
    }

    /**
     * WebP로 변환되었는지 확인합니다.
     *
     * @return 결과 포맷이 WebP이면 true
     */
    public boolean convertedToWebP() {
        return resultFormat.isWebP();
    }

    /**
     * 사람이 읽기 쉬운 최적화 요약을 반환합니다.
     *
     * @return 최적화 요약 문자열
     */
    public String getSummary() {
        return String.format(
                "Optimization successful: %s → %s (%.1f%% reduction), " +
                "Format: %s, Dimension: %s → %s, Processing time: %dms",
                FileSizeFormatter.format(originalSizeBytes),
                FileSizeFormatter.format(optimizedSizeBytes),
                getReductionPercentage(),
                resultFormat,
                originalDimension,
                resultDimension,
                processingTime.toMillis()
        );
    }

    // ========== Validation Methods ==========

    private static void validateResultS3Uri(String resultS3Uri) {
        if (resultS3Uri == null || resultS3Uri.trim().isEmpty()) {
            throw new IllegalArgumentException("Result S3 URI cannot be null or empty");
        }
        if (!resultS3Uri.startsWith("s3://")) {
            throw new IllegalArgumentException("Result S3 URI must start with 's3://'");
        }
    }

    private static void validateImageFormat(String name, ImageFormat format) {
        if (format == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

    private static void validateAppliedStrategy(OptimizationStrategy appliedStrategy) {
        if (appliedStrategy == null) {
            throw new IllegalArgumentException("Applied strategy cannot be null");
        }
    }

    private static void validateSize(String name, long size) {
        if (size < 0) {
            throw new IllegalArgumentException(name + " cannot be negative, but was: " + size);
        }
    }

    private static void validateDimension(String name, ImageDimension dimension) {
        if (dimension == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

    private static void validateProcessingTime(Duration processingTime) {
        if (processingTime == null) {
            throw new IllegalArgumentException("Processing time cannot be null");
        }
        if (processingTime.isNegative()) {
            throw new IllegalArgumentException("Processing time cannot be negative");
        }
    }

    // ========== Getters ==========

    public String getResultS3Uri() {
        return resultS3Uri;
    }

    public ImageFormat getOriginalFormat() {
        return originalFormat;
    }

    public ImageFormat getResultFormat() {
        return resultFormat;
    }

    public OptimizationStrategy getAppliedStrategy() {
        return appliedStrategy;
    }

    public long getOriginalSizeBytes() {
        return originalSizeBytes;
    }

    public long getOptimizedSizeBytes() {
        return optimizedSizeBytes;
    }

    public ImageDimension getOriginalDimension() {
        return originalDimension;
    }

    public ImageDimension getResultDimension() {
        return resultDimension;
    }

    public Duration getProcessingTime() {
        return processingTime;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageOptimizationResult that = (ImageOptimizationResult) o;
        return originalSizeBytes == that.originalSizeBytes &&
               optimizedSizeBytes == that.optimizedSizeBytes &&
               Objects.equals(resultS3Uri, that.resultS3Uri) &&
               originalFormat == that.originalFormat &&
               resultFormat == that.resultFormat &&
               appliedStrategy == that.appliedStrategy &&
               Objects.equals(originalDimension, that.originalDimension) &&
               Objects.equals(resultDimension, that.resultDimension) &&
               Objects.equals(processingTime, that.processingTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultS3Uri, originalFormat, resultFormat, appliedStrategy, originalSizeBytes,
                optimizedSizeBytes, originalDimension, resultDimension, processingTime);
    }

    @Override
    public String toString() {
        return "ImageOptimizationResult{" +
                "resultS3Uri='" + resultS3Uri + '\'' +
                ", originalFormat=" + originalFormat +
                ", resultFormat=" + resultFormat +
                ", appliedStrategy=" + appliedStrategy +
                ", originalSizeBytes=" + originalSizeBytes +
                ", optimizedSizeBytes=" + optimizedSizeBytes +
                ", originalDimension=" + originalDimension +
                ", resultDimension=" + resultDimension +
                ", processingTime=" + processingTime +
                '}';
    }
}

package com.ryuqq.fileflow.domain.image.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 이미지 최적화 완료 이벤트
 * 이미지 최적화가 성공적으로 완료되었음을 알립니다.
 */
public final class ImageOptimizationCompleted implements DomainEvent {

    private final String imageId;
    private final String sourceS3Uri;
    private final String resultS3Uri;
    private final ImageFormat resultFormat;
    private final long originalSizeBytes;
    private final long optimizedSizeBytes;
    private final LocalDateTime occurredOn;

    private ImageOptimizationCompleted(
            String imageId,
            String sourceS3Uri,
            String resultS3Uri,
            ImageFormat resultFormat,
            long originalSizeBytes,
            long optimizedSizeBytes,
            LocalDateTime occurredOn
    ) {
        this.imageId = imageId;
        this.sourceS3Uri = sourceS3Uri;
        this.resultS3Uri = resultS3Uri;
        this.resultFormat = resultFormat;
        this.originalSizeBytes = originalSizeBytes;
        this.optimizedSizeBytes = optimizedSizeBytes;
        this.occurredOn = occurredOn;
    }

    public static ImageOptimizationCompleted of(
            String imageId,
            String sourceS3Uri,
            String resultS3Uri,
            ImageFormat resultFormat,
            long originalSizeBytes,
            long optimizedSizeBytes
    ) {
        return new ImageOptimizationCompleted(
                imageId,
                sourceS3Uri,
                resultS3Uri,
                resultFormat,
                originalSizeBytes,
                optimizedSizeBytes,
                LocalDateTime.now()
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

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "ImageOptimizationCompleted";
    }

    // ========== Getters ==========

    public String getImageId() {
        return imageId;
    }

    public String getSourceS3Uri() {
        return sourceS3Uri;
    }

    public String getResultS3Uri() {
        return resultS3Uri;
    }

    public ImageFormat getResultFormat() {
        return resultFormat;
    }

    public long getOriginalSizeBytes() {
        return originalSizeBytes;
    }

    public long getOptimizedSizeBytes() {
        return optimizedSizeBytes;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageOptimizationCompleted that = (ImageOptimizationCompleted) o;
        return originalSizeBytes == that.originalSizeBytes &&
               optimizedSizeBytes == that.optimizedSizeBytes &&
               Objects.equals(imageId, that.imageId) &&
               Objects.equals(sourceS3Uri, that.sourceS3Uri) &&
               Objects.equals(resultS3Uri, that.resultS3Uri) &&
               resultFormat == that.resultFormat &&
               Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageId, sourceS3Uri, resultS3Uri, resultFormat,
                originalSizeBytes, optimizedSizeBytes, occurredOn);
    }

    @Override
    public String toString() {
        return "ImageOptimizationCompleted{" +
                "imageId='" + imageId + '\'' +
                ", sourceS3Uri='" + sourceS3Uri + '\'' +
                ", resultS3Uri='" + resultS3Uri + '\'' +
                ", resultFormat=" + resultFormat +
                ", originalSizeBytes=" + originalSizeBytes +
                ", optimizedSizeBytes=" + optimizedSizeBytes +
                ", reductionPercentage=" + String.format("%.2f%%", getReductionPercentage()) +
                ", occurredOn=" + occurredOn +
                '}';
    }
}

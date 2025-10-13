package com.ryuqq.fileflow.domain.image.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import com.ryuqq.fileflow.domain.image.vo.OptimizationStrategy;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 이미지 최적화 요청 이벤트
 * 이미지 최적화가 요청되었음을 알립니다.
 */
public final class ImageOptimizationRequested implements DomainEvent {

    private final String imageId;
    private final String sourceS3Uri;
    private final ImageFormat sourceFormat;
    private final OptimizationStrategy strategy;
    private final LocalDateTime occurredOn;

    private ImageOptimizationRequested(
            String imageId,
            String sourceS3Uri,
            ImageFormat sourceFormat,
            OptimizationStrategy strategy,
            LocalDateTime occurredOn
    ) {
        this.imageId = imageId;
        this.sourceS3Uri = sourceS3Uri;
        this.sourceFormat = sourceFormat;
        this.strategy = strategy;
        this.occurredOn = occurredOn;
    }

    public static ImageOptimizationRequested of(
            String imageId,
            String sourceS3Uri,
            ImageFormat sourceFormat,
            OptimizationStrategy strategy
    ) {
        return new ImageOptimizationRequested(
                imageId,
                sourceS3Uri,
                sourceFormat,
                strategy,
                LocalDateTime.now()
        );
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "ImageOptimizationRequested";
    }

    // ========== Getters ==========

    public String getImageId() {
        return imageId;
    }

    public String getSourceS3Uri() {
        return sourceS3Uri;
    }

    public ImageFormat getSourceFormat() {
        return sourceFormat;
    }

    public OptimizationStrategy getStrategy() {
        return strategy;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageOptimizationRequested that = (ImageOptimizationRequested) o;
        return Objects.equals(imageId, that.imageId) &&
               Objects.equals(sourceS3Uri, that.sourceS3Uri) &&
               sourceFormat == that.sourceFormat &&
               strategy == that.strategy &&
               Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageId, sourceS3Uri, sourceFormat, strategy, occurredOn);
    }

    @Override
    public String toString() {
        return "ImageOptimizationRequested{" +
                "imageId='" + imageId + '\'' +
                ", sourceS3Uri='" + sourceS3Uri + '\'' +
                ", sourceFormat=" + sourceFormat +
                ", strategy=" + strategy +
                ", occurredOn=" + occurredOn +
                '}';
    }
}

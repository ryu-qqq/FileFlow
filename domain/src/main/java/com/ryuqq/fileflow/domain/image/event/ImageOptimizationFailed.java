package com.ryuqq.fileflow.domain.image.event;

import com.ryuqq.fileflow.domain.common.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 이미지 최적화 실패 이벤트
 * 이미지 최적화가 실패했음을 알립니다.
 */
public final class ImageOptimizationFailed implements DomainEvent {

    private final String imageId;
    private final String sourceS3Uri;
    private final String errorMessage;
    private final String errorType;
    private final LocalDateTime occurredOn;

    private ImageOptimizationFailed(
            String imageId,
            String sourceS3Uri,
            String errorMessage,
            String errorType,
            LocalDateTime occurredOn
    ) {
        this.imageId = imageId;
        this.sourceS3Uri = sourceS3Uri;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
        this.occurredOn = occurredOn;
    }

    public static ImageOptimizationFailed of(
            String imageId,
            String sourceS3Uri,
            String errorMessage,
            String errorType
    ) {
        return new ImageOptimizationFailed(
                imageId,
                sourceS3Uri,
                errorMessage,
                errorType,
                LocalDateTime.now()
        );
    }

    /**
     * 간단한 에러 정보로 이벤트를 생성합니다.
     *
     * @param imageId 이미지 ID
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param errorMessage 에러 메시지
     * @return ImageOptimizationFailed 인스턴스
     */
    public static ImageOptimizationFailed of(
            String imageId,
            String sourceS3Uri,
            String errorMessage
    ) {
        return of(imageId, sourceS3Uri, errorMessage, "UNKNOWN");
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "ImageOptimizationFailed";
    }

    // ========== Getters ==========

    public String getImageId() {
        return imageId;
    }

    public String getSourceS3Uri() {
        return sourceS3Uri;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorType() {
        return errorType;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageOptimizationFailed that = (ImageOptimizationFailed) o;
        return Objects.equals(imageId, that.imageId) &&
               Objects.equals(sourceS3Uri, that.sourceS3Uri) &&
               Objects.equals(errorMessage, that.errorMessage) &&
               Objects.equals(errorType, that.errorType) &&
               Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageId, sourceS3Uri, errorMessage, errorType, occurredOn);
    }

    @Override
    public String toString() {
        return "ImageOptimizationFailed{" +
                "imageId='" + imageId + '\'' +
                ", sourceS3Uri='" + sourceS3Uri + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorType='" + errorType + '\'' +
                ", occurredOn=" + occurredOn +
                '}';
    }
}

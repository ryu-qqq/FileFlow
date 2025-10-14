package com.ryuqq.fileflow.application.image.dto;

import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand.ThumbnailSize;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;

import java.time.Duration;

/**
 * 썸네일 생성 결과 DTO
 *
 * 역할:
 * - 썸네일 생성 작업의 결과를 담는 Data Transfer Object
 * - Application Layer에서 외부로 전달되는 응답 데이터
 *
 * 포함 정보:
 * - 생성된 썸네일의 S3 URI
 * - 썸네일 크기 정보
 * - 원본 대비 크기 정보
 * - 처리 시간
 *
 * @author sangwon-ryu
 */
public record ThumbnailGenerationResult(
        String imageId,
        String sourceS3Uri,
        String thumbnailS3Uri,
        ThumbnailSize thumbnailSize,
        ImageDimension originalDimension,
        ImageDimension thumbnailDimension,
        long originalSizeBytes,
        long thumbnailSizeBytes,
        Duration processingTime
) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public ThumbnailGenerationResult {
        if (imageId == null || imageId.isBlank()) {
            throw new IllegalArgumentException("Image ID cannot be null or blank");
        }
        if (sourceS3Uri == null || sourceS3Uri.isBlank()) {
            throw new IllegalArgumentException("Source S3 URI cannot be null or blank");
        }
        if (thumbnailS3Uri == null || thumbnailS3Uri.isBlank()) {
            throw new IllegalArgumentException("Thumbnail S3 URI cannot be null or blank");
        }
        if (thumbnailSize == null) {
            throw new IllegalArgumentException("Thumbnail size cannot be null");
        }
        if (originalDimension == null) {
            throw new IllegalArgumentException("Original dimension cannot be null");
        }
        if (thumbnailDimension == null) {
            throw new IllegalArgumentException("Thumbnail dimension cannot be null");
        }
        if (originalSizeBytes < 0) {
            throw new IllegalArgumentException("Original size cannot be negative");
        }
        if (thumbnailSizeBytes < 0) {
            throw new IllegalArgumentException("Thumbnail size cannot be negative");
        }
        if (processingTime == null || processingTime.isNegative()) {
            throw new IllegalArgumentException("Processing time must be non-negative");
        }
    }

    /**
     * ThumbnailGenerationResult를 생성합니다.
     *
     * @param imageId 이미지 ID
     * @param sourceS3Uri 원본 이미지 S3 URI
     * @param thumbnailS3Uri 썸네일 이미지 S3 URI
     * @param thumbnailSize 썸네일 크기
     * @param originalDimension 원본 이미지 크기
     * @param thumbnailDimension 썸네일 이미지 크기
     * @param originalSizeBytes 원본 파일 크기 (bytes)
     * @param thumbnailSizeBytes 썸네일 파일 크기 (bytes)
     * @param processingTime 처리 시간
     * @return ThumbnailGenerationResult 인스턴스
     */
    public static ThumbnailGenerationResult of(
            String imageId,
            String sourceS3Uri,
            String thumbnailS3Uri,
            ThumbnailSize thumbnailSize,
            ImageDimension originalDimension,
            ImageDimension thumbnailDimension,
            long originalSizeBytes,
            long thumbnailSizeBytes,
            Duration processingTime
    ) {
        return new ThumbnailGenerationResult(
                imageId,
                sourceS3Uri,
                thumbnailS3Uri,
                thumbnailSize,
                originalDimension,
                thumbnailDimension,
                originalSizeBytes,
                thumbnailSizeBytes,
                processingTime
        );
    }

    /**
     * 파일 크기 감소율을 계산합니다.
     *
     * @return 감소율 (0.0 ~ 1.0)
     */
    public double getReductionRatio() {
        if (originalSizeBytes == 0) {
            return 0.0;
        }
        return 1.0 - ((double) thumbnailSizeBytes / originalSizeBytes);
    }

    /**
     * 파일 크기 감소율을 퍼센트로 반환합니다.
     *
     * @return 감소율 퍼센트 (0.0 ~ 100.0)
     */
    public double getReductionPercentage() {
        return getReductionRatio() * 100.0;
    }

    /**
     * 썸네일이 원본보다 작은지 확인합니다.
     *
     * @return 썸네일이 더 작으면 true
     */
    public boolean isSmaller() {
        return thumbnailSizeBytes < originalSizeBytes;
    }

    /**
     * 업스케일링이 발생했는지 확인합니다.
     * (원본보다 썸네일 크기가 더 큰 경우)
     *
     * @return 업스케일링 발생 여부
     */
    public boolean isUpscaled() {
        return thumbnailDimension.getWidth() > originalDimension.getWidth()
                || thumbnailDimension.getHeight() > originalDimension.getHeight();
    }
}

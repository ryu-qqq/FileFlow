package com.ryuqq.fileflow.domain.image.command;

import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;

/**
 * 썸네일 생성 명령을 나타내는 Command Object
 *
 * CQRS 패턴:
 * - 썸네일 생성 요청의 의도를 명시적으로 표현
 * - 불변 객체로 안전한 전달 보장
 *
 * 비즈니스 규칙:
 * - Small: 300x300
 * - Medium: 800x800
 * - 비율 유지하면서 리사이징
 */
public record GenerateThumbnailCommand(
        String imageId,
        String sourceS3Uri,
        ImageFormat sourceFormat,
        ThumbnailSize thumbnailSize,
        boolean maintainAspectRatio
) {

    /**
     * Compact constructor로 검증 로직 수행
     */
    public GenerateThumbnailCommand {
        validateImageId(imageId);
        validateSourceS3Uri(sourceS3Uri);
        validateSourceFormat(sourceFormat);
        validateThumbnailSize(thumbnailSize);
    }

    /**
     * GenerateThumbnailCommand를 생성합니다.
     *
     * @param imageId 이미지 ID
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param sourceFormat 소스 이미지 포맷
     * @param thumbnailSize 썸네일 크기
     * @param maintainAspectRatio 비율 유지 여부
     * @return GenerateThumbnailCommand 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static GenerateThumbnailCommand of(
            String imageId,
            String sourceS3Uri,
            ImageFormat sourceFormat,
            ThumbnailSize thumbnailSize,
            boolean maintainAspectRatio
    ) {
        return new GenerateThumbnailCommand(
                imageId,
                sourceS3Uri,
                sourceFormat,
                thumbnailSize,
                maintainAspectRatio
        );
    }

    /**
     * 비율을 유지하는 썸네일 생성 명령을 생성합니다.
     *
     * @param imageId 이미지 ID
     * @param sourceS3Uri 소스 이미지 S3 URI
     * @param sourceFormat 소스 이미지 포맷
     * @param thumbnailSize 썸네일 크기
     * @return GenerateThumbnailCommand 인스턴스
     */
    public static GenerateThumbnailCommand withAspectRatio(
            String imageId,
            String sourceS3Uri,
            ImageFormat sourceFormat,
            ThumbnailSize thumbnailSize
    ) {
        return new GenerateThumbnailCommand(
                imageId,
                sourceS3Uri,
                sourceFormat,
                thumbnailSize,
                true
        );
    }

    /**
     * 타겟 이미지 크기를 반환합니다.
     *
     * @return 썸네일 이미지 크기
     */
    public ImageDimension getTargetDimension() {
        return thumbnailSize.getDimension();
    }

    // ========== Validation Methods ==========

    private static void validateImageId(String imageId) {
        if (imageId == null || imageId.trim().isEmpty()) {
            throw new IllegalArgumentException("ImageId cannot be null or empty");
        }
    }

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

    private static void validateThumbnailSize(ThumbnailSize thumbnailSize) {
        if (thumbnailSize == null) {
            throw new IllegalArgumentException("Thumbnail size cannot be null");
        }
    }

    /**
     * 썸네일 크기를 정의하는 Enum
     */
    public enum ThumbnailSize {
        SMALL(ImageDimension.square(300), "Small thumbnail (300x300)"),
        MEDIUM(ImageDimension.square(800), "Medium thumbnail (800x800)");

        private final ImageDimension dimension;
        private final String description;

        ThumbnailSize(ImageDimension dimension, String description) {
            this.dimension = dimension;
            this.description = description;
        }

        public ImageDimension getDimension() {
            return dimension;
        }

        public String getDescription() {
            return description;
        }
    }
}

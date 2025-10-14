package com.ryuqq.fileflow.application.image.dto;

import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand.ThumbnailSize;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;

import java.util.List;
import java.util.Objects;

/**
 * Batch Thumbnail 생성 Command
 *
 * 하나의 원본 이미지에 대해 여러 크기의 썸네일을 일괄 생성하기 위한 Command입니다.
 *
 * 비즈니스 규칙:
 * - 원본 이미지는 1회만 로드하여 메모리 효율성 확보
 * - 여러 썸네일 크기를 동시에 생성 (SMALL, MEDIUM)
 * - 각 썸네일은 WebP 포맷으로 변환되어 S3에 업로드
 *
 * @author sangwon-ryu
 * @param sourceS3Uri 원본 이미지 S3 URI
 * @param imageId 원본 이미지 ID (썸네일 저장 경로 및 관계 설정에 사용)
 * @param sourceFormat 원본 이미지 포맷
 * @param tenantId 테넌트 ID
 * @param thumbnailSizes 생성할 썸네일 크기 목록 (SMALL, MEDIUM 등)
 * @param maintainAspectRatio Aspect Ratio 유지 여부
 */
public record BatchThumbnailCommand(
        String sourceS3Uri,
        String imageId,
        ImageFormat sourceFormat,
        String tenantId,
        List<ThumbnailSize> thumbnailSizes,
        boolean maintainAspectRatio
) {

    /**
     * Canonical Constructor with Validation
     */
    public BatchThumbnailCommand {
        Objects.requireNonNull(sourceS3Uri, "Source S3 URI cannot be null");
        Objects.requireNonNull(imageId, "Image ID cannot be null");
        Objects.requireNonNull(sourceFormat, "Source format cannot be null");
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(thumbnailSizes, "Thumbnail sizes cannot be null");

        if (sourceS3Uri.isBlank()) {
            throw new IllegalArgumentException("Source S3 URI cannot be blank");
        }
        if (imageId.isBlank()) {
            throw new IllegalArgumentException("Image ID cannot be blank");
        }
        if (tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be blank");
        }
        if (thumbnailSizes.isEmpty()) {
            throw new IllegalArgumentException("Thumbnail sizes cannot be empty");
        }
    }

    /**
     * 기본 Aspect Ratio 유지 설정으로 Command 생성
     *
     * @param sourceS3Uri 원본 이미지 S3 URI
     * @param imageId 원본 이미지 ID
     * @param sourceFormat 원본 이미지 포맷
     * @param tenantId 테넌트 ID
     * @param thumbnailSizes 생성할 썸네일 크기 목록
     * @return BatchThumbnailCommand 인스턴스
     */
    public static BatchThumbnailCommand of(
            String sourceS3Uri,
            String imageId,
            ImageFormat sourceFormat,
            String tenantId,
            List<ThumbnailSize> thumbnailSizes
    ) {
        return new BatchThumbnailCommand(sourceS3Uri, imageId, sourceFormat, tenantId, thumbnailSizes, true);
    }
}

package com.ryuqq.fileflow.adapter.image.thumbnail;

import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand.ThumbnailSize;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;

import java.awt.image.BufferedImage;

/**
 * 썸네일 생성 전략 인터페이스
 *
 * 역할:
 * - 이미지 리사이징 시 다양한 썸네일 생성 전략을 정의
 * - Strategy 패턴으로 확장 가능한 썸네일 크기 지원
 *
 * 구현체:
 * - SmallThumbnailStrategy: 300x300 썸네일 생성
 * - MediumThumbnailStrategy: 800x800 썸네일 생성
 * - CustomSizeThumbnailStrategy: 사용자 정의 크기 썸네일 생성
 *
 * 비즈니스 규칙:
 * - Aspect Ratio 유지
 * - 업스케일링 방지 (원본보다 크게 만들지 않음)
 * - 고품질 리샘플링 (Lanczos3)
 * - WebP 포맷으로 출력
 *
 * @author sangwon-ryu
 */
public interface ThumbnailGenerationStrategy {

    /**
     * 이미지를 리사이징하여 썸네일을 생성합니다.
     *
     * @param sourceImage 원본 이미지
     * @param maintainAspectRatio 비율 유지 여부
     * @return 리사이징된 썸네일 이미지
     */
    BufferedImage generateThumbnail(BufferedImage sourceImage, boolean maintainAspectRatio);

    /**
     * 이 전략이 지원하는 썸네일 크기를 반환합니다.
     *
     * @return 썸네일 크기
     */
    ThumbnailSize getSupportedSize();

    /**
     * 타겟 이미지 크기를 반환합니다.
     *
     * @return 썸네일 이미지 크기
     */
    ImageDimension getTargetDimension();

    /**
     * 업스케일링 방지 여부를 확인합니다.
     * 원본 이미지가 타겟보다 작을 경우 원본 크기를 유지합니다.
     *
     * @param sourceImage 원본 이미지
     * @return 업스케일링이 필요한지 여부
     */
    default boolean needsUpscaling(BufferedImage sourceImage) {
        ImageDimension target = getTargetDimension();
        return sourceImage.getWidth() < target.getWidth() || sourceImage.getHeight() < target.getHeight();
    }

    /**
     * 썸네일 파일명을 생성합니다.
     * 규칙: {원본명}_{크기}.webp (예: product-123_small.webp)
     *
     * @param originalFileName 원본 파일명
     * @return 썸네일 파일명
     */
    default String generateThumbnailFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Original file name cannot be null or blank");
        }

        // 확장자 제거
        String baseName = originalFileName.contains(".")
                ? originalFileName.substring(0, originalFileName.lastIndexOf('.'))
                : originalFileName;

        // {원본명}_{크기}.webp 형식으로 생성
        return String.format("%s_%s.webp", baseName, getSupportedSize().name().toLowerCase());
    }
}

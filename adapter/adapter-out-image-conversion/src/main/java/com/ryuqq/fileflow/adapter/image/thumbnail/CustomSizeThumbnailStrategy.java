package com.ryuqq.fileflow.adapter.image.thumbnail;

import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand.ThumbnailSize;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 커스텀 크기 썸네일 생성 전략
 *
 * 역할:
 * - 사용자 정의 크기의 썸네일 생성
 * - 고품질 리샘플링 (Lanczos3)
 * - Aspect Ratio 유지 옵션 지원
 *
 * 비즈니스 규칙:
 * - 원본보다 큰 크기는 업스케일링 하지 않음
 * - Aspect Ratio 유지 시 fit 방식 적용
 * - WebP 포맷으로 출력
 *
 * 사용 시나리오:
 * - SMALL, MEDIUM 외 특수한 크기가 필요한 경우
 * - 동적으로 크기를 지정해야 하는 경우
 * - 향후 확장을 위한 유연성 제공
 *
 * 기술:
 * - Thumbnailator 라이브러리 사용
 * - 기본 리샘플링: Lanczos3 (고품질)
 *
 * 주의사항:
 * - Spring Bean으로 등록하지 않음 (동적 생성 목적)
 * - 매번 새로운 인스턴스 생성하여 사용
 *
 * @author sangwon-ryu
 */
public class CustomSizeThumbnailStrategy implements ThumbnailGenerationStrategy {

    private final ImageDimension customDimension;
    private final ThumbnailSize size;

    /**
     * 커스텀 크기 썸네일 전략을 생성합니다.
     *
     * @param customDimension 커스텀 이미지 크기
     * @param size 연관된 ThumbnailSize (null 가능, CUSTOM 용도)
     */
    public CustomSizeThumbnailStrategy(ImageDimension customDimension, ThumbnailSize size) {
        if (customDimension == null) {
            throw new IllegalArgumentException("Custom dimension cannot be null");
        }
        if (customDimension.getWidth() <= 0 || customDimension.getHeight() <= 0) {
            throw new IllegalArgumentException("Custom dimension must have positive width and height");
        }

        this.customDimension = customDimension;
        this.size = size;
    }

    /**
     * 커스텀 크기 썸네일 전략을 생성합니다. (size 없이)
     *
     * @param customDimension 커스텀 이미지 크기
     */
    public CustomSizeThumbnailStrategy(ImageDimension customDimension) {
        this(customDimension, null);
    }

    /**
     * 특정 ThumbnailSize의 크기로 커스텀 전략을 생성합니다.
     *
     * @param size ThumbnailSize
     * @return CustomSizeThumbnailStrategy 인스턴스
     */
    public static CustomSizeThumbnailStrategy of(ThumbnailSize size) {
        if (size == null) {
            throw new IllegalArgumentException("ThumbnailSize cannot be null");
        }
        return new CustomSizeThumbnailStrategy(size.getDimension(), size);
    }

    /**
     * 특정 너비와 높이로 커스텀 전략을 생성합니다.
     *
     * @param width 너비
     * @param height 높이
     * @return CustomSizeThumbnailStrategy 인스턴스
     */
    public static CustomSizeThumbnailStrategy of(int width, int height) {
        return new CustomSizeThumbnailStrategy(ImageDimension.of(width, height));
    }

    @Override
    public BufferedImage generateThumbnail(BufferedImage sourceImage, boolean maintainAspectRatio) {
        if (sourceImage == null) {
            throw new IllegalArgumentException("Source image cannot be null");
        }

        // 업스케일링 방지: 원본이 타겟보다 작으면 원본 반환
        if (needsUpscaling(sourceImage)) {
            return sourceImage;
        }

        try {
            if (maintainAspectRatio) {
                // Aspect Ratio 유지하면서 커스텀 크기 이내로 축소 (fit)
                return Thumbnails.of(sourceImage)
                        .size(customDimension.getWidth(), customDimension.getHeight())
                        .asBufferedImage();
            } else {
                // Aspect Ratio 무시하고 정확히 커스텀 크기로 크롭/리사이즈
                return Thumbnails.of(sourceImage)
                        .forceSize(customDimension.getWidth(), customDimension.getHeight())
                        .asBufferedImage();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate custom size thumbnail", e);
        }
    }

    @Override
    public ThumbnailSize getSupportedSize() {
        // null일 수 있음 (순수 커스텀 크기의 경우)
        return size;
    }

    @Override
    public ImageDimension getTargetDimension() {
        return customDimension;
    }

    /**
     * 커스텀 파일명을 생성합니다.
     * size가 null인 경우 크기 정보를 파일명에 포함합니다.
     *
     * @param originalFileName 원본 파일명
     * @return 썸네일 파일명
     */
    @Override
    public String generateThumbnailFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Original file name cannot be null or blank");
        }

        // 확장자 제거
        String baseName = originalFileName.contains(".")
                ? originalFileName.substring(0, originalFileName.lastIndexOf('.'))
                : originalFileName;

        // size가 있으면 기본 규칙, 없으면 커스텀 규칙
        if (size != null) {
            return String.format("%s_%s.webp", baseName, size.name().toLowerCase());
        } else {
            return String.format("%s_%dx%d.webp", baseName, customDimension.getWidth(), customDimension.getHeight());
        }
    }
}

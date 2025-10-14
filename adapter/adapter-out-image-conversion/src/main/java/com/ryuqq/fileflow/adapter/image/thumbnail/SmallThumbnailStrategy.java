package com.ryuqq.fileflow.adapter.image.thumbnail;

import com.ryuqq.fileflow.adapter.image.ImageResizer;
import com.ryuqq.fileflow.adapter.image.ResamplingAlgorithm;
import com.ryuqq.fileflow.adapter.image.config.ImageProcessingConfig;
import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand.ThumbnailSize;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

/**
 * Small 썸네일 생성 전략 (300x300)
 *
 * 역할:
 * - 300x300 크기의 썸네일 생성
 * - 고품질 리샘플링 (Lanczos3)
 * - Progressive downsampling 적용
 * - Unsharp Mask 샤프닝
 * - Aspect Ratio 유지 옵션 지원
 *
 * 비즈니스 규칙:
 * - 원본이 300x300보다 작으면 업스케일링 하지 않음
 * - Aspect Ratio 유지 시 fit 방식 적용
 * - WebP 포맷으로 출력 (파일명 규칙: {원본명}_small.webp)
 *
 * 기술:
 * - ImageResizer 유틸리티 사용
 * - 설정 기반 리샘플링 알고리즘
 * - Progressive downsampling으로 품질 향상
 *
 * @author sangwon-ryu
 */
@Component
public class SmallThumbnailStrategy implements ThumbnailGenerationStrategy {

    private static final ThumbnailSize SIZE = ThumbnailSize.SMALL;
    private static final ImageDimension TARGET_DIMENSION = SIZE.getDimension();

    private final ImageProcessingConfig imageProcessingConfig;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param imageProcessingConfig 이미지 처리 설정
     */
    public SmallThumbnailStrategy(ImageProcessingConfig imageProcessingConfig) {
        this.imageProcessingConfig = imageProcessingConfig;
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

        // ImageResizer를 사용한 고품질 리사이징
        ResamplingAlgorithm algorithm = imageProcessingConfig.getResamplingAlgorithm();
        boolean applySharpen = imageProcessingConfig.isSharpenEnabled();

        return ImageResizer.resize(
                sourceImage,
                TARGET_DIMENSION.getWidth(),
                TARGET_DIMENSION.getHeight(),
                algorithm,
                maintainAspectRatio,
                applySharpen
        );
    }

    @Override
    public ThumbnailSize getSupportedSize() {
        return SIZE;
    }

    @Override
    public ImageDimension getTargetDimension() {
        return TARGET_DIMENSION;
    }
}

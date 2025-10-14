package com.ryuqq.fileflow.adapter.image.thumbnail;

import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand.ThumbnailSize;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Small 썸네일 생성 전략 (300x300)
 *
 * 역할:
 * - 300x300 크기의 썸네일 생성
 * - 고품질 리샘플링 (Lanczos3)
 * - Aspect Ratio 유지 옵션 지원
 *
 * 비즈니스 규칙:
 * - 원본이 300x300보다 작으면 업스케일링 하지 않음
 * - Aspect Ratio 유지 시 fit 방식 적용
 * - WebP 포맷으로 출력 (파일명 규칙: {원본명}_small.webp)
 *
 * 기술:
 * - Thumbnailator 라이브러리 사용
 * - 기본 리샘플링: Lanczos3 (고품질)
 *
 * @author sangwon-ryu
 */
@Component
public class SmallThumbnailStrategy implements ThumbnailGenerationStrategy {

    private static final ThumbnailSize SIZE = ThumbnailSize.SMALL;
    private static final ImageDimension TARGET_DIMENSION = SIZE.getDimension();

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
                // Aspect Ratio 유지하면서 300x300 이내로 축소 (fit)
                return Thumbnails.of(sourceImage)
                        .size(TARGET_DIMENSION.getWidth(), TARGET_DIMENSION.getHeight())
                        .asBufferedImage();
            } else {
                // Aspect Ratio 무시하고 정확히 300x300으로 크롭/리사이즈
                return Thumbnails.of(sourceImage)
                        .forceSize(TARGET_DIMENSION.getWidth(), TARGET_DIMENSION.getHeight())
                        .asBufferedImage();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate small thumbnail", e);
        }
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

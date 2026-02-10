package com.ryuqq.fileflow.domain.transform.vo;

/**
 * 이미지 변환 파라미터.
 *
 * <p>TransformType에 따라 사용되는 필드가 다릅니다:
 *
 * <ul>
 *   <li>RESIZE: width, height, maintainAspectRatio
 *   <li>CONVERT: targetFormat
 *   <li>COMPRESS: quality
 *   <li>THUMBNAIL: width, height (고정 작은 사이즈)
 * </ul>
 *
 * @param width 대상 너비 (px, nullable)
 * @param height 대상 높이 (px, nullable)
 * @param maintainAspectRatio 비율 유지 여부 (RESIZE 시)
 * @param targetFormat 대상 포맷 (CONVERT 시, 예: "webp", "png", "jpeg")
 * @param quality 압축 품질 (COMPRESS 시, 1-100)
 */
public record TransformParams(
        Integer width,
        Integer height,
        boolean maintainAspectRatio,
        String targetFormat,
        Integer quality) {

    public TransformParams {
        if (width != null && width <= 0) {
            throw new IllegalArgumentException("width must be positive, got: " + width);
        }
        if (height != null && height <= 0) {
            throw new IllegalArgumentException("height must be positive, got: " + height);
        }
        if (quality != null && (quality < 1 || quality > 100)) {
            throw new IllegalArgumentException("quality must be 1-100, got: " + quality);
        }
    }

    public static TransformParams forResize(int width, int height, boolean maintainAspectRatio) {
        return new TransformParams(width, height, maintainAspectRatio, null, null);
    }

    public static TransformParams forConvert(String targetFormat) {
        if (targetFormat == null || targetFormat.isBlank()) {
            throw new IllegalArgumentException("targetFormat must not be blank");
        }
        return new TransformParams(null, null, false, targetFormat.toLowerCase(), null);
    }

    public static TransformParams forCompress(int quality) {
        return new TransformParams(null, null, false, null, quality);
    }

    public static TransformParams forThumbnail(int width, int height) {
        return new TransformParams(width, height, true, null, null);
    }
}

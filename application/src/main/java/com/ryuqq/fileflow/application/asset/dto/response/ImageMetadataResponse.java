package com.ryuqq.fileflow.application.asset.dto.response;

/**
 * 이미지 메타데이터 응답.
 *
 * <p>이미지에서 추출한 메타 정보를 담는다.
 *
 * @param width 이미지 너비 (픽셀)
 * @param height 이미지 높이 (픽셀)
 * @param format 이미지 포맷 (예: "jpeg", "png", "webp")
 * @param colorSpace 색상 공간 (예: "RGB", "CMYK")
 */
public record ImageMetadataResponse(int width, int height, String format, String colorSpace) {

    /**
     * 정적 팩토리 메서드.
     *
     * @param width 이미지 너비
     * @param height 이미지 높이
     * @param format 이미지 포맷
     * @param colorSpace 색상 공간
     * @return ImageMetadataResponse
     */
    public static ImageMetadataResponse of(
            int width, int height, String format, String colorSpace) {
        return new ImageMetadataResponse(width, height, format, colorSpace);
    }

    /**
     * 가로 세로 비율을 반환한다.
     *
     * @return 가로/세로 비율
     */
    public double aspectRatio() {
        if (height == 0) {
            return 0;
        }
        return (double) width / height;
    }

    /**
     * 가로 방향 이미지인지 확인한다.
     *
     * @return 가로가 세로보다 크면 true
     */
    public boolean isLandscape() {
        return width > height;
    }

    /**
     * 세로 방향 이미지인지 확인한다.
     *
     * @return 세로가 가로보다 크면 true
     */
    public boolean isPortrait() {
        return height > width;
    }
}

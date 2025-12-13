package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 이미지 크기(너비, 높이) Value Object.
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>너비와 높이는 0보다 커야 한다.
 *   <li>너비와 높이는 항상 함께 사용된다 (단독 사용 불가).
 *   <li>최대 크기 제한: 65535 x 65535 (대부분의 이미지 포맷 제한)
 * </ul>
 *
 * <p><strong>DDD 원칙</strong>:
 *
 * <ul>
 *   <li>Immutable Value Object
 *   <li>Self-validating (검증 로직 내장)
 *   <li>Always used together (width/height 단독 존재 불가)
 * </ul>
 *
 * @param width 이미지 너비 (픽셀)
 * @param height 이미지 높이 (픽셀)
 */
public record ImageDimension(int width, int height) {

    private static final int MIN_DIMENSION = 1;
    private static final int MAX_DIMENSION = 65535;

    /** Compact Constructor (검증 로직). */
    public ImageDimension {
        if (width < MIN_DIMENSION || width > MAX_DIMENSION) {
            throw new IllegalArgumentException(
                    String.format(
                            "이미지 너비는 %d ~ %d 사이여야 합니다: %d", MIN_DIMENSION, MAX_DIMENSION, width));
        }
        if (height < MIN_DIMENSION || height > MAX_DIMENSION) {
            throw new IllegalArgumentException(
                    String.format(
                            "이미지 높이는 %d ~ %d 사이여야 합니다: %d", MIN_DIMENSION, MAX_DIMENSION, height));
        }
    }

    /**
     * 정적 팩토리 메서드.
     *
     * @param width 이미지 너비 (픽셀)
     * @param height 이미지 높이 (픽셀)
     * @return ImageDimension
     * @throws IllegalArgumentException 검증 실패 시
     */
    public static ImageDimension of(int width, int height) {
        return new ImageDimension(width, height);
    }

    /**
     * 가로/세로 비율을 반환합니다.
     *
     * @return 가로/세로 비율 (width / height)
     */
    public double aspectRatio() {
        return (double) width / height;
    }

    /**
     * 가로 이미지인지 확인합니다.
     *
     * @return 너비가 높이보다 크면 true
     */
    public boolean isLandscape() {
        return width > height;
    }

    /**
     * 세로 이미지인지 확인합니다.
     *
     * @return 높이가 너비보다 크면 true
     */
    public boolean isPortrait() {
        return height > width;
    }

    /**
     * 정사각형 이미지인지 확인합니다.
     *
     * @return 너비와 높이가 같으면 true
     */
    public boolean isSquare() {
        return width == height;
    }

    /**
     * 총 픽셀 수를 반환합니다.
     *
     * @return 총 픽셀 수 (width * height)
     */
    public long totalPixels() {
        return (long) width * height;
    }

    /**
     * 메가픽셀 단위로 변환합니다.
     *
     * @return 메가픽셀 (예: 12.0 MP)
     */
    public double toMegaPixels() {
        return totalPixels() / 1_000_000.0;
    }

    /**
     * 비율을 유지하면서 최대 크기에 맞게 축소합니다.
     *
     * @param maxWidth 최대 너비
     * @param maxHeight 최대 높이
     * @return 축소된 ImageDimension (원본이 작으면 원본 반환)
     */
    public ImageDimension scaleToFit(int maxWidth, int maxHeight) {
        if (width <= maxWidth && height <= maxHeight) {
            return this;
        }

        double widthRatio = (double) maxWidth / width;
        double heightRatio = (double) maxHeight / height;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = Math.max(1, (int) Math.round(width * ratio));
        int newHeight = Math.max(1, (int) Math.round(height * ratio));

        return new ImageDimension(newWidth, newHeight);
    }

    /**
     * 사람이 읽기 쉬운 형식으로 변환합니다.
     *
     * <p>예: "1920x1080", "800x600"
     *
     * @return "너비x높이" 형식의 문자열
     */
    public String toDisplayString() {
        return width + "x" + height;
    }
}

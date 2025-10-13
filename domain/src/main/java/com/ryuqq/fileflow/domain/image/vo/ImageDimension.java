package com.ryuqq.fileflow.domain.image.vo;

import java.util.Objects;

/**
 * 이미지 크기(너비, 높이)를 표현하는 Value Object
 *
 * 불변성:
 * - 모든 필드는 final
 * - 생성 후 변경 불가
 *
 * 비즈니스 규칙:
 * - 너비와 높이는 양수여야 함
 * - 최대 크기 제한 (10000 x 10000)
 */
public final class ImageDimension {

    private static final int MAX_DIMENSION = 10000;
    private static final int MIN_DIMENSION = 1;

    private final int width;
    private final int height;

    private ImageDimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * ImageDimension을 생성합니다.
     *
     * @param width 너비
     * @param height 높이
     * @return ImageDimension 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static ImageDimension of(int width, int height) {
        validateDimension("Width", width);
        validateDimension("Height", height);

        return new ImageDimension(width, height);
    }

    /**
     * 정사각형 이미지 크기를 생성합니다.
     *
     * @param size 너비와 높이
     * @return ImageDimension 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static ImageDimension square(int size) {
        return of(size, size);
    }

    /**
     * 주어진 크기에 맞게 비율을 유지하면서 리사이징합니다.
     *
     * @param maxWidth 최대 너비
     * @param maxHeight 최대 높이
     * @return 리사이징된 ImageDimension
     */
    public ImageDimension resize(int maxWidth, int maxHeight) {
        if (maxWidth <= 0 || maxHeight <= 0) {
            throw new IllegalArgumentException("Max dimensions must be positive");
        }

        // 이미 작으면 그대로 반환
        if (width <= maxWidth && height <= maxHeight) {
            return this;
        }

        double widthRatio = (double) maxWidth / width;
        double heightRatio = (double) maxHeight / height;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        return ImageDimension.of(newWidth, newHeight);
    }

    /**
     * 가로 세로 비율을 계산합니다.
     *
     * @return 가로/세로 비율
     */
    public double getAspectRatio() {
        return (double) width / height;
    }

    /**
     * 전체 픽셀 수를 계산합니다.
     *
     * @return 너비 * 높이
     */
    public long getTotalPixels() {
        return (long) width * height;
    }

    /**
     * 가로 이미지인지 확인합니다.
     *
     * @return 너비 > 높이이면 true
     */
    public boolean isLandscape() {
        return width > height;
    }

    /**
     * 세로 이미지인지 확인합니다.
     *
     * @return 높이 > 너비이면 true
     */
    public boolean isPortrait() {
        return height > width;
    }

    /**
     * 정사각형 이미지인지 확인합니다.
     *
     * @return 너비 == 높이이면 true
     */
    public boolean isSquare() {
        return width == height;
    }

    /**
     * 주어진 크기보다 큰지 확인합니다.
     *
     * @param other 비교할 크기
     * @return 더 크면 true
     */
    public boolean isLargerThan(ImageDimension other) {
        if (other == null) {
            throw new IllegalArgumentException("Other dimension cannot be null");
        }
        return getTotalPixels() > other.getTotalPixels();
    }

    /**
     * 썸네일 크기인지 확인합니다.
     *
     * @return 너비와 높이가 모두 1000 이하이면 true
     */
    public boolean isThumbnailSize() {
        return width <= 1000 && height <= 1000;
    }

    private static void validateDimension(String name, int value) {
        if (value < MIN_DIMENSION) {
            throw new IllegalArgumentException(
                    name + " must be at least " + MIN_DIMENSION + ", but was: " + value
            );
        }
        if (value > MAX_DIMENSION) {
            throw new IllegalArgumentException(
                    name + " cannot exceed " + MAX_DIMENSION + ", but was: " + value
            );
        }
    }

    // ========== Getters ==========

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageDimension that = (ImageDimension) o;
        return width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}

package com.ryuqq.fileflow.domain.policy.vo;

/**
 * Dimension Value Object
 * 이미지의 너비와 높이를 픽셀 단위로 나타내는 불변 객체
 *
 * <p>불변성 규칙:</p>
 * <ul>
 *   <li>모든 필드는 private final</li>
 *   <li>생성자는 private - static factory method 사용</li>
 *   <li>setter 메서드 없음</li>
 * </ul>
 *
 * @since 1.0.0
 */
public final class Dimension {
    private static final int MIN_DIMENSION = 1;
    private static final int MAX_DIMENSION = 50000;

    private final int width;
    private final int height;

    /**
     * Private 생성자 - static factory method를 통해서만 생성 가능
     */
    private Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Dimension을 생성하는 static factory method
     *
     * @param width  너비 (픽셀, 1 이상 50000 이하)
     * @param height 높이 (픽셀, 1 이상 50000 이하)
     * @return 생성된 Dimension 인스턴스
     * @throws IllegalArgumentException width 또는 height가 유효 범위를 벗어난 경우
     */
    public static Dimension of(int width, int height) {
        validateDimension("width", width);
        validateDimension("height", height);
        return new Dimension(width, height);
    }

    /**
     * 차원 값의 유효성을 검증합니다.
     */
    private static void validateDimension(String fieldName, int value) {
        if (value < MIN_DIMENSION) {
            throw new IllegalArgumentException(
                String.format("%s must be at least %d, but was: %d",
                    fieldName, MIN_DIMENSION, value)
            );
        }
        if (value > MAX_DIMENSION) {
            throw new IllegalArgumentException(
                String.format("%s must not exceed %d, but was: %d",
                    fieldName, MAX_DIMENSION, value)
            );
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * 이 차원이 주어진 최대 차원 내에 있는지 검증합니다.
     *
     * @param maxDimension 최대 허용 차원
     * @return 최대 차원 내에 있으면 true, 초과하면 false
     */
    public boolean isWithin(Dimension maxDimension) {
        if (maxDimension == null) {
            throw new IllegalArgumentException("maxDimension cannot be null");
        }
        return this.width <= maxDimension.width && this.height <= maxDimension.height;
    }

    /**
     * 레거시 호환성을 위한 메서드
     * @deprecated Use {@link #isWithin(Dimension)} instead
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public boolean exceedsLimit(Dimension maxDimension) {
        return !isWithin(maxDimension);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dimension dimension = (Dimension) o;
        return width == dimension.width && height == dimension.height;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }

    @Override
    public String toString() {
        return "Dimension{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}

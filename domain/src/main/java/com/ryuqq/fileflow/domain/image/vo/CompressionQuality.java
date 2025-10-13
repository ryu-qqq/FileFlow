package com.ryuqq.fileflow.domain.image.vo;

import java.util.Objects;

/**
 * 이미지 압축 품질을 표현하는 Value Object
 *
 * 불변성:
 * - value는 final
 * - 생성 후 변경 불가
 *
 * 비즈니스 규칙:
 * - 압축 품질은 1~100 범위
 * - 기본 품질은 90% (고품질)
 * - 90% 이상은 고품질로 간주
 */
public final class CompressionQuality {

    private static final int MIN_QUALITY = 1;
    private static final int MAX_QUALITY = 100;
    private static final int DEFAULT_QUALITY = 90;
    private static final int HIGH_QUALITY_THRESHOLD = 90;

    private final int value;

    private CompressionQuality(int value) {
        this.value = value;
    }

    /**
     * CompressionQuality를 생성합니다.
     *
     * @param value 압축 품질 (1-100)
     * @return CompressionQuality 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static CompressionQuality of(int value) {
        validateQuality(value);
        return new CompressionQuality(value);
    }

    /**
     * 기본 압축 품질(90%)을 반환합니다.
     *
     * @return CompressionQuality 인스턴스
     */
    public static CompressionQuality defaultQuality() {
        return new CompressionQuality(DEFAULT_QUALITY);
    }

    /**
     * 최대 압축 품질(100%)을 반환합니다.
     *
     * @return CompressionQuality 인스턴스
     */
    public static CompressionQuality maxQuality() {
        return new CompressionQuality(MAX_QUALITY);
    }

    /**
     * 최소 압축 품질(1%)을 반환합니다.
     *
     * @return CompressionQuality 인스턴스
     */
    public static CompressionQuality minQuality() {
        return new CompressionQuality(MIN_QUALITY);
    }

    /**
     * 고품질 압축인지 확인합니다.
     *
     * @return 품질이 90% 이상이면 true
     */
    public boolean isHighQuality() {
        return value >= HIGH_QUALITY_THRESHOLD;
    }

    /**
     * 중간 품질 압축인지 확인합니다.
     *
     * @return 품질이 70~89% 범위이면 true
     */
    public boolean isMediumQuality() {
        return value >= 70 && value < HIGH_QUALITY_THRESHOLD;
    }

    /**
     * 낮은 품질 압축인지 확인합니다.
     *
     * @return 품질이 70% 미만이면 true
     */
    public boolean isLowQuality() {
        return value < 70;
    }

    /**
     * 다른 품질보다 높은지 확인합니다.
     *
     * @param other 비교할 품질
     * @return 더 높으면 true
     */
    public boolean isHigherThan(CompressionQuality other) {
        if (other == null) {
            throw new IllegalArgumentException("Other quality cannot be null");
        }
        return value > other.value;
    }

    /**
     * 다른 품질보다 낮은지 확인합니다.
     *
     * @param other 비교할 품질
     * @return 더 낮으면 true
     */
    public boolean isLowerThan(CompressionQuality other) {
        if (other == null) {
            throw new IllegalArgumentException("Other quality cannot be null");
        }
        return value < other.value;
    }

    /**
     * 압축 품질을 백분율로 반환합니다.
     *
     * @return 품질 (1-100)
     */
    public int getValue() {
        return value;
    }

    /**
     * 압축 품질을 0.0~1.0 범위의 float로 반환합니다.
     *
     * @return 품질 (0.0-1.0)
     */
    public float asFloat() {
        return value / 100.0f;
    }

    /**
     * 압축 품질 레벨을 문자열로 반환합니다.
     *
     * @return "HIGH", "MEDIUM", "LOW"
     */
    public String getQualityLevel() {
        if (isHighQuality()) {
            return "HIGH";
        } else if (isMediumQuality()) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private static void validateQuality(int value) {
        if (value < MIN_QUALITY || value > MAX_QUALITY) {
            throw new IllegalArgumentException(
                    "Compression quality must be between " + MIN_QUALITY + " and " +
                    MAX_QUALITY + ", but was: " + value
            );
        }
    }

    // ========== Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompressionQuality that = (CompressionQuality) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value + "%";
    }
}

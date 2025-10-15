package com.ryuqq.fileflow.domain.file;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 썸네일 관계의 메타데이터를 표현하는 Value Object
 *
 * 불변성:
 * - 모든 필드는 final로 선언
 * - 생성 후 변경 불가
 *
 * 비즈니스 규칙:
 * - 썸네일 크기 정보 (width, height) 필수
 * - 리샘플링 알고리즘 정보 저장
 * - 생성 시간 추적
 *
 * @author sangwon-ryu
 */
public final class ThumbnailMetadata {

    private static final String WIDTH_KEY = "width";
    private static final String HEIGHT_KEY = "height";
    private static final String ALGORITHM_KEY = "algorithm";
    private static final String CREATED_AT_KEY = "createdAt";

    private final int width;
    private final int height;
    private final String algorithm;
    private final LocalDateTime createdAt;

    private ThumbnailMetadata(
            int width,
            int height,
            String algorithm,
            LocalDateTime createdAt
    ) {
        this.width = width;
        this.height = height;
        this.algorithm = algorithm;
        this.createdAt = createdAt;
    }

    /**
     * 썸네일 메타데이터를 생성합니다.
     *
     * @param width 썸네일 너비
     * @param height 썸네일 높이
     * @param algorithm 리샘플링 알고리즘
     * @param createdAt 생성 시간
     * @return ThumbnailMetadata 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 입력 시
     */
    public static ThumbnailMetadata of(
            int width,
            int height,
            String algorithm,
            LocalDateTime createdAt
    ) {
        validateWidth(width);
        validateHeight(height);
        validateAlgorithm(algorithm);
        validateCreatedAt(createdAt);

        return new ThumbnailMetadata(width, height, algorithm, createdAt);
    }

    /**
     * Map으로부터 썸네일 메타데이터를 재구성합니다.
     *
     * @param metadata 메타데이터 맵
     * @return ThumbnailMetadata 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 메타데이터 시
     */
    public static ThumbnailMetadata fromMap(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Metadata cannot be null or empty");
        }

        try {
            int width = (Integer) metadata.get(WIDTH_KEY);
            int height = (Integer) metadata.get(HEIGHT_KEY);
            String algorithm = (String) metadata.get(ALGORITHM_KEY);
            LocalDateTime createdAt = (LocalDateTime) metadata.get(CREATED_AT_KEY);

            return of(width, height, algorithm, createdAt);
        } catch (ClassCastException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid thumbnail metadata format", e);
        }
    }

    /**
     * Map 형식으로 변환합니다 (FileRelationship 저장용).
     *
     * @return 메타데이터 맵
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(WIDTH_KEY, width);
        map.put(HEIGHT_KEY, height);
        map.put(ALGORITHM_KEY, algorithm);
        map.put(CREATED_AT_KEY, createdAt);
        return map;
    }

    // ========== Business Logic Methods ==========

    /**
     * SMALL 썸네일 크기(300x300)인지 확인합니다.
     *
     * @return SMALL 크기이면 true
     */
    public boolean isSmallSize() {
        return width == 300 && height == 300;
    }

    /**
     * MEDIUM 썸네일 크기(800x800)인지 확인합니다.
     *
     * @return MEDIUM 크기이면 true
     */
    public boolean isMediumSize() {
        return width == 800 && height == 800;
    }

    /**
     * 종횡비를 반환합니다.
     *
     * @return 종횡비 (width / height)
     */
    public double getAspectRatio() {
        return (double) width / height;
    }

    /**
     * 정사각형 썸네일인지 확인합니다.
     *
     * @return 정사각형이면 true
     */
    public boolean isSquare() {
        return width == height;
    }

    // ========== Validation Methods ==========

    private static void validateWidth(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive: " + width);
        }
    }

    private static void validateHeight(int height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive: " + height);
        }
    }

    private static void validateAlgorithm(String algorithm) {
        if (algorithm == null || algorithm.trim().isEmpty()) {
            throw new IllegalArgumentException("Algorithm cannot be null or empty");
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
        if (createdAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("CreatedAt cannot be in the future");
        }
    }

    // ========== Getters ==========

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ========== Override Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThumbnailMetadata that = (ThumbnailMetadata) o;
        return width == that.width
                && height == that.height
                && Objects.equals(algorithm, that.algorithm)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, algorithm, createdAt);
    }

    @Override
    public String toString() {
        return "ThumbnailMetadata{" +
                "width=" + width +
                ", height=" + height +
                ", algorithm='" + algorithm + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

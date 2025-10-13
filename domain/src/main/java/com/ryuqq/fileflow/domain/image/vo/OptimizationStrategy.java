package com.ryuqq.fileflow.domain.image.vo;

/**
 * 이미지 최적화 전략을 정의하는 Enum
 *
 * 비즈니스 규칙:
 * - COMPRESS_ONLY: 기존 포맷 유지하면서 압축만 수행
 * - CONVERT_TO_WEBP: WebP로 변환 (손실 압축)
 * - CONVERT_TO_WEBP_LOSSLESS: WebP로 변환 (무손실)
 * - AUTO: 자동으로 최적 전략 선택
 */
public enum OptimizationStrategy {

    /**
     * 기존 포맷을 유지하면서 압축만 수행
     * - 포맷 변환 없음
     * - 품질 기반 압축 적용
     */
    COMPRESS_ONLY("Compress only without format conversion"),

    /**
     * WebP 포맷으로 변환 (손실 압축)
     * - 파일 크기를 최대한 줄임
     * - 일반적으로 25-35% 파일 크기 감소
     */
    CONVERT_TO_WEBP("Convert to WebP with lossy compression"),

    /**
     * WebP 포맷으로 변환 (무손실)
     * - 원본 품질 유지
     * - 투명도가 있는 PNG에 적합
     */
    CONVERT_TO_WEBP_LOSSLESS("Convert to WebP with lossless compression"),

    /**
     * 자동으로 최적 전략 선택
     * - 이미지 특성에 따라 최적 전략 결정
     * - 투명도 있으면 무손실, 없으면 손실 압축
     */
    AUTO("Automatically select optimal strategy");

    private final String description;

    OptimizationStrategy(String description) {
        this.description = description;
    }

    /**
     * 주어진 이미지 포맷에 대해 최적 전략을 결정합니다.
     *
     * @param format 이미지 포맷
     * @return 최적화 전략
     */
    public static OptimizationStrategy determineOptimal(ImageFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("ImageFormat cannot be null");
        }

        // 이미 WebP면 압축만 수행
        if (format.isWebP()) {
            return COMPRESS_ONLY;
        }

        // 투명도를 지원하는 포맷은 무손실 WebP로 변환
        if (format.supportsTransparency()) {
            return CONVERT_TO_WEBP_LOSSLESS;
        }

        // 그 외는 손실 압축 WebP로 변환
        return CONVERT_TO_WEBP;
    }

    /**
     * WebP로 변환하는 전략인지 확인합니다.
     *
     * @return WebP 변환 여부
     */
    public boolean convertsToWebP() {
        return this == CONVERT_TO_WEBP || this == CONVERT_TO_WEBP_LOSSLESS;
    }

    /**
     * 무손실 압축을 사용하는 전략인지 확인합니다.
     *
     * @return 무손실 압축 여부
     */
    public boolean isLossless() {
        return this == CONVERT_TO_WEBP_LOSSLESS;
    }

    /**
     * 자동 전략인지 확인합니다.
     *
     * @return 자동 전략 여부
     */
    public boolean isAuto() {
        return this == AUTO;
    }

    /**
     * 전략 설명을 반환합니다.
     *
     * @return 전략 설명
     */
    public String getDescription() {
        return description;
    }
}

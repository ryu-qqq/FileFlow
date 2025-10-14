package com.ryuqq.fileflow.adapter.image;

import java.awt.RenderingHints;

/**
 * 이미지 리샘플링 알고리즘 열거형
 *
 * 역할:
 * - 이미지 리사이징 시 사용할 리샘플링 알고리즘 정의
 * - 각 알고리즘의 RenderingHints 매핑
 * - 성능 vs 품질 트레이드오프 제공
 *
 * 알고리즘 특성:
 * - NEAREST_NEIGHBOR: 가장 빠름, 최저 품질 (계단 현상)
 * - BILINEAR: 빠름, 중간 품질 (선형 보간)
 * - BICUBIC: 느림, 높은 품질 (3차 보간)
 * - LANCZOS3: 가장 느림, 최고 품질 (sinc 기반, 에지 선명도 유지)
 *
 * 권장 사용:
 * - 썸네일 생성: LANCZOS3 (고품질 요구)
 * - 실시간 프리뷰: BILINEAR (속도 우선)
 * - 대량 배치 처리: BICUBIC (품질/성능 균형)
 *
 * @author sangwon-ryu
 */
public enum ResamplingAlgorithm {

    /**
     * Nearest Neighbor (최근접 이웃)
     *
     * 특성:
     * - 가장 빠른 알고리즘
     * - 픽셀을 단순 복제하여 리샘플링
     * - 계단 현상(Aliasing) 발생
     * - 썸네일 생성에 부적합
     *
     * 사용 사례: 픽셀 아트, 속도가 최우선인 경우
     */
    NEAREST_NEIGHBOR(
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
            "Nearest Neighbor",
            "가장 빠름",
            "최저 품질",
            1.0
    ),

    /**
     * Bilinear (선형 보간)
     *
     * 특성:
     * - 빠른 알고리즘
     * - 2x2 픽셀 영역 선형 보간
     * - 부드러운 이미지 생성
     * - 일반적인 용도로 충분
     *
     * 사용 사례: 실시간 프리뷰, 빠른 리사이징
     */
    BILINEAR(
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,
            "Bilinear",
            "빠름",
            "중간 품질",
            2.0
    ),

    /**
     * Bicubic (3차 보간)
     *
     * 특성:
     * - 느린 알고리즘
     * - 4x4 픽셀 영역 3차 보간
     * - 높은 품질의 자연스러운 이미지
     * - 에지 보존 양호
     *
     * 사용 사례: 고품질 썸네일, 사진 리사이징
     */
    BICUBIC(
            RenderingHints.VALUE_INTERPOLATION_BICUBIC,
            "Bicubic",
            "느림",
            "높은 품질",
            3.0
    ),

    /**
     * Lanczos3 (Lanczos 필터)
     *
     * 특성:
     * - 가장 느린 알고리즘
     * - Sinc 함수 기반 리샘플링
     * - 최고 품질의 이미지 생성
     * - 에지 선명도 최대 유지
     * - 링잉 아티팩트(Ringing Artifact) 발생 가능
     *
     * 사용 사례: 프로페셔널 썸네일 생성, 고품질 이미지 다운스케일링
     *
     * 주의: Java AWT에서 직접 지원하지 않으므로 커스텀 구현 필요
     */
    LANCZOS3(
            null, // 커스텀 구현 필요
            "Lanczos3",
            "가장 느림",
            "최고 품질",
            4.0
    );

    private final Object renderingHintValue;
    private final String displayName;
    private final String performanceCharacteristic;
    private final String qualityCharacteristic;
    private final double qualityScore; // 1.0 (최저) ~ 4.0 (최고)

    /**
     * ResamplingAlgorithm 생성자
     *
     * @param renderingHintValue RenderingHints 값 (LANCZOS3는 null)
     * @param displayName 표시 이름
     * @param performanceCharacteristic 성능 특성
     * @param qualityCharacteristic 품질 특성
     * @param qualityScore 품질 점수 (1.0 ~ 4.0)
     */
    ResamplingAlgorithm(
            Object renderingHintValue,
            String displayName,
            String performanceCharacteristic,
            String qualityCharacteristic,
            double qualityScore
    ) {
        this.renderingHintValue = renderingHintValue;
        this.displayName = displayName;
        this.performanceCharacteristic = performanceCharacteristic;
        this.qualityCharacteristic = qualityCharacteristic;
        this.qualityScore = qualityScore;
    }

    /**
     * Java AWT RenderingHints 값을 반환합니다.
     * LANCZOS3는 커스텀 구현이 필요하므로 null을 반환합니다.
     *
     * @return RenderingHints 값 (LANCZOS3는 null)
     */
    public Object getRenderingHintValue() {
        return renderingHintValue;
    }

    /**
     * 표시 이름을 반환합니다.
     *
     * @return 표시 이름
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 성능 특성을 반환합니다.
     *
     * @return 성능 특성 문자열
     */
    public String getPerformanceCharacteristic() {
        return performanceCharacteristic;
    }

    /**
     * 품질 특성을 반환합니다.
     *
     * @return 품질 특성 문자열
     */
    public String getQualityCharacteristic() {
        return qualityCharacteristic;
    }

    /**
     * 품질 점수를 반환합니다.
     * 1.0 (최저 품질) ~ 4.0 (최고 품질)
     *
     * @return 품질 점수
     */
    public double getQualityScore() {
        return qualityScore;
    }

    /**
     * 커스텀 구현이 필요한 알고리즘인지 확인합니다.
     * LANCZOS3는 Java AWT에서 직접 지원하지 않으므로 true를 반환합니다.
     *
     * @return 커스텀 구현 필요 여부
     */
    public boolean needsCustomImplementation() {
        return renderingHintValue == null;
    }

    /**
     * 알고리즘 정보를 문자열로 반환합니다.
     *
     * @return 알고리즘 정보 문자열
     */
    @Override
    public String toString() {
        return String.format("%s (성능: %s, 품질: %s, 점수: %.1f)",
                displayName, performanceCharacteristic, qualityCharacteristic, qualityScore);
    }
}

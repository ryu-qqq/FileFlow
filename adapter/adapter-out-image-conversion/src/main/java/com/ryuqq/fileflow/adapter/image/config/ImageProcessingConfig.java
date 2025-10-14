package com.ryuqq.fileflow.adapter.image.config;

import com.ryuqq.fileflow.adapter.image.ResamplingAlgorithm;
import org.springframework.stereotype.Component;

/**
 * 이미지 처리 설정 클래스
 *
 * 역할:
 * - 이미지 리샘플링 알고리즘 설정
 * - Progressive downsampling 설정
 * - Unsharp Mask 샤프닝 설정
 * - 환경별 최적화 설정 관리
 *
 * 설정 파일 (application.yml):
 * <pre>
 * image:
 *   processing:
 *     resampling-algorithm: LANCZOS3
 *     progressive-downsampling-enabled: true
 *     progressive-threshold: 2.0
 *     sharpen-enabled: true
 *     sharpen-amount: 0.5
 * </pre>
 *
 * 환경별 설정 예시:
 * - local: BILINEAR (빠른 개발)
 * - dev: BICUBIC (품질/성능 균형)
 * - prod: LANCZOS3 (최고 품질)
 *
 * @author sangwon-ryu
 */
@Component
public class ImageProcessingConfig {

    /**
     * 리샘플링 알고리즘
     * 기본값: LANCZOS3 (최고 품질)
     */
    private ResamplingAlgorithm resamplingAlgorithm = ResamplingAlgorithm.LANCZOS3;

    /**
     * Progressive downsampling 활성화 여부
     * 기본값: true (품질 향상)
     */
    private boolean progressiveDownsamplingEnabled = true;

    /**
     * Progressive downsampling 임계값
     * 원본이 타겟의 N배 이상일 때 단계적 축소 적용
     * 기본값: 2.0 (2배 이상)
     */
    private double progressiveThreshold = 2.0;

    /**
     * Unsharp Mask 샤프닝 활성화 여부
     * 기본값: true (선명도 보정)
     */
    private boolean sharpenEnabled = true;

    /**
     * 샤프닝 강도
     * 범위: 0.0 (샤프닝 없음) ~ 1.0 (최대 샤프닝)
     * 기본값: 0.5 (중간 강도)
     */
    private float sharpenAmount = 0.5f;

    // Getters and Setters (NO Lombok)

    public ResamplingAlgorithm getResamplingAlgorithm() {
        return resamplingAlgorithm;
    }

    public void setResamplingAlgorithm(ResamplingAlgorithm resamplingAlgorithm) {
        this.resamplingAlgorithm = resamplingAlgorithm;
    }

    public boolean isProgressiveDownsamplingEnabled() {
        return progressiveDownsamplingEnabled;
    }

    public void setProgressiveDownsamplingEnabled(boolean progressiveDownsamplingEnabled) {
        this.progressiveDownsamplingEnabled = progressiveDownsamplingEnabled;
    }

    public double getProgressiveThreshold() {
        return progressiveThreshold;
    }

    public void setProgressiveThreshold(double progressiveThreshold) {
        if (progressiveThreshold < 1.0) {
            throw new IllegalArgumentException("Progressive threshold must be at least 1.0");
        }
        this.progressiveThreshold = progressiveThreshold;
    }

    public boolean isSharpenEnabled() {
        return sharpenEnabled;
    }

    public void setSharpenEnabled(boolean sharpenEnabled) {
        this.sharpenEnabled = sharpenEnabled;
    }

    public float getSharpenAmount() {
        return sharpenAmount;
    }

    public void setSharpenAmount(float sharpenAmount) {
        if (sharpenAmount < 0.0f || sharpenAmount > 1.0f) {
            throw new IllegalArgumentException("Sharpen amount must be between 0.0 and 1.0");
        }
        this.sharpenAmount = sharpenAmount;
    }

    @Override
    public String toString() {
        return String.format(
                "ImageProcessingConfig{algorithm=%s, progressive=%s (threshold=%.1f), sharpen=%s (amount=%.2f)}",
                resamplingAlgorithm.getDisplayName(),
                progressiveDownsamplingEnabled,
                progressiveThreshold,
                sharpenEnabled,
                sharpenAmount
        );
    }
}

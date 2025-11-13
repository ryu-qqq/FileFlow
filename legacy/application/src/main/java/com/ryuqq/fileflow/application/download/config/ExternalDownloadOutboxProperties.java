package com.ryuqq.fileflow.application.download.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * External Download Outbox Configuration Properties
 * Outbox 스케줄러 설정을 외부화
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "fileflow.download.outbox")
public class ExternalDownloadOutboxProperties {

    /**
     * 한 번에 처리할 메시지 배치 크기
     */
    private int batchSize = 10;

    /**
     * PROCESSING 상태로 오래 머문 메시지를 재처리하는 임계값 (분)
     */
    private int staleMinutes = 5;

    /**
     * 최대 재시도 횟수
     */
    private int maxRetryCount = 3;

    /**
     * 스케줄러 실행 지연 시간 (밀리초)
     */
    private long fixedDelay = 30_000;

    /**
     * 애플리케이션 시작 후 첫 실행까지 대기 시간 (밀리초)
     */
    private long initialDelay = 10_000;

    /**
     * 재시도 기본 지연 시간 (초)
     * 지수 백오프의 기본값
     */
    private int retryBaseDelaySeconds = 60;

    /**
     * 재시도 최대 지연 시간 (초)
     * 지수 백오프의 최대값
     */
    private int retryMaxDelaySeconds = 3600;

    /**
     * 재시도 지연 시간 증가 배수
     * 예: 2.0이면 60초 → 120초 → 240초
     */
    private double retryMultiplier = 2.0;

    // Getters and Setters
    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getStaleMinutes() {
        return staleMinutes;
    }

    public void setStaleMinutes(int staleMinutes) {
        this.staleMinutes = staleMinutes;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public long getFixedDelay() {
        return fixedDelay;
    }

    public void setFixedDelay(long fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public int getRetryBaseDelaySeconds() {
        return retryBaseDelaySeconds;
    }

    public void setRetryBaseDelaySeconds(int retryBaseDelaySeconds) {
        this.retryBaseDelaySeconds = retryBaseDelaySeconds;
    }

    public int getRetryMaxDelaySeconds() {
        return retryMaxDelaySeconds;
    }

    public void setRetryMaxDelaySeconds(int retryMaxDelaySeconds) {
        this.retryMaxDelaySeconds = retryMaxDelaySeconds;
    }

    public double getRetryMultiplier() {
        return retryMultiplier;
    }

    public void setRetryMultiplier(double retryMultiplier) {
        this.retryMultiplier = retryMultiplier;
    }
}
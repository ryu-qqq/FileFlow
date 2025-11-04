package com.ryuqq.fileflow.application.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Pipeline Outbox Configuration Properties
 *
 * <p>PipelineOutbox Scheduler 설정을 외부화합니다.</p>
 *
 * <p><strong>설정 위치:</strong></p>
 * <ul>
 *   <li>application.yml: fileflow.pipeline.outbox.*</li>
 * </ul>
 *
 * <p><strong>설정 항목:</strong></p>
 * <ul>
 *   <li>batchSize: 한 번에 처리할 메시지 배치 크기</li>
 *   <li>staleMinutes: PROCESSING 상태 임계값 (분)</li>
 *   <li>maxRetryCount: 최대 재시도 횟수</li>
 *   <li>fixedDelay: 스케줄러 실행 지연 시간 (밀리초)</li>
 *   <li>initialDelay: 첫 실행까지 대기 시간 (밀리초)</li>
 *   <li>retryBaseDelaySeconds: 재시도 기본 지연 시간 (초)</li>
 *   <li>retryMaxDelaySeconds: 재시도 최대 지연 시간 (초)</li>
 *   <li>retryMultiplier: 재시도 지연 시간 증가 배수</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "fileflow.pipeline.outbox")
public class PipelineOutboxProperties {

    /**
     * 한 번에 처리할 메시지 배치 크기
     *
     * <p>기본값: 10</p>
     */
    private int batchSize = 10;

    /**
     * PROCESSING 상태로 오래 머문 메시지를 재처리하는 임계값 (분)
     *
     * <p>기본값: 5분</p>
     * <p>5분 이상 PROCESSING 상태인 메시지는 Worker 크래시로 간주하여 재처리</p>
     */
    private int staleMinutes = 5;

    /**
     * 최대 재시도 횟수
     *
     * <p>기본값: 3회</p>
     * <p>3회 실패 시 영구 실패로 표시</p>
     */
    private int maxRetryCount = 3;

    /**
     * 스케줄러 실행 지연 시간 (밀리초)
     *
     * <p>기본값: 30초</p>
     * <p>이전 실행 완료 후 30초 대기</p>
     */
    private long fixedDelay = 30_000;

    /**
     * 애플리케이션 시작 후 첫 실행까지 대기 시간 (밀리초)
     *
     * <p>기본값: 10초</p>
     * <p>애플리케이션 초기화 시간 확보</p>
     */
    private long initialDelay = 10_000;

    /**
     * 재시도 기본 지연 시간 (초)
     *
     * <p>기본값: 60초</p>
     * <p>첫 번째 재시도 시 60초 대기</p>
     */
    private int retryBaseDelaySeconds = 60;

    /**
     * 재시도 최대 지연 시간 (초)
     *
     * <p>기본값: 3600초 (1시간)</p>
     * <p>지수 백오프 최대값</p>
     */
    private int retryMaxDelaySeconds = 3600;

    /**
     * 재시도 지연 시간 증가 배수
     *
     * <p>기본값: 2.0</p>
     * <p>예: 60초 → 120초 → 240초 → ...</p>
     */
    private double retryMultiplier = 2.0;

    // ===== Getter/Setter =====

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

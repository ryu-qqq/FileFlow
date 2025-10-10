package com.ryuqq.fileflow.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 재시도 및 Circuit Breaker 공통 설정 프로퍼티
 *
 * AWS S3 및 SQS 어댑터에서 공통으로 사용하는 재시도 정책과
 * Circuit Breaker 설정을 중앙화합니다.
 *
 * @author sangwon-ryu
 */
@ConfigurationProperties(prefix = "fileflow.resilience")
public class RetryProperties {

    private final Retry retry = new Retry();
    private final CircuitBreaker circuitBreaker = new CircuitBreaker();

    public Retry getRetry() {
        return retry;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    /**
     * 재시도 정책 설정
     */
    public static class Retry {
        /**
         * 최대 재시도 횟수
         */
        private int maxAttempts = 3;

        /**
         * 초기 대기 시간 (밀리초)
         */
        private long initialIntervalMillis = 1000L;

        /**
         * 최대 대기 시간 (밀리초)
         */
        private long maxIntervalMillis = 10000L;

        /**
         * 백오프 배수 (각 재시도마다 대기 시간에 곱해지는 값)
         */
        private double multiplier = 2.0;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialIntervalMillis() {
            return initialIntervalMillis;
        }

        public void setInitialIntervalMillis(long initialIntervalMillis) {
            this.initialIntervalMillis = initialIntervalMillis;
        }

        public long getMaxIntervalMillis() {
            return maxIntervalMillis;
        }

        public void setMaxIntervalMillis(long maxIntervalMillis) {
            this.maxIntervalMillis = maxIntervalMillis;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }

    /**
     * Circuit Breaker 정책 설정
     */
    public static class CircuitBreaker {
        /**
         * 실패율 임계값 (퍼센트)
         */
        private float failureRateThreshold = 50.0f;

        /**
         * 느린 호출 비율 임계값 (퍼센트)
         */
        private int slowCallRateThreshold = 100;

        /**
         * 느린 호출 판단 기준 시간 (초)
         */
        private long slowCallDurationThresholdSeconds = 10L;

        /**
         * OPEN 상태 유지 시간 (초)
         */
        private long waitDurationInOpenStateSeconds = 30L;

        /**
         * 슬라이딩 윈도우 크기
         */
        private int slidingWindowSize = 10;

        /**
         * Circuit Breaker 활성화를 위한 최소 호출 수
         */
        private int minimumNumberOfCalls = 5;

        public float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(float failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public int getSlowCallRateThreshold() {
            return slowCallRateThreshold;
        }

        public void setSlowCallRateThreshold(int slowCallRateThreshold) {
            this.slowCallRateThreshold = slowCallRateThreshold;
        }

        public long getSlowCallDurationThresholdSeconds() {
            return slowCallDurationThresholdSeconds;
        }

        public void setSlowCallDurationThresholdSeconds(long slowCallDurationThresholdSeconds) {
            this.slowCallDurationThresholdSeconds = slowCallDurationThresholdSeconds;
        }

        public long getWaitDurationInOpenStateSeconds() {
            return waitDurationInOpenStateSeconds;
        }

        public void setWaitDurationInOpenStateSeconds(long waitDurationInOpenStateSeconds) {
            this.waitDurationInOpenStateSeconds = waitDurationInOpenStateSeconds;
        }

        public int getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public void setSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }

        public int getMinimumNumberOfCalls() {
            return minimumNumberOfCalls;
        }

        public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
            this.minimumNumberOfCalls = minimumNumberOfCalls;
        }
    }
}

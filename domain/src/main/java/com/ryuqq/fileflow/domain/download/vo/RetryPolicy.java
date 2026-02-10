package com.ryuqq.fileflow.domain.download.vo;

public record RetryPolicy(int retryCount, int maxRetries) {

    public RetryPolicy {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be >= 0");
        }
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount must be >= 0");
        }
    }

    public static RetryPolicy ofDefault(int maxRetries) {
        return new RetryPolicy(0, maxRetries);
    }

    public static RetryPolicy of(int retryCount, int maxRetries) {
        return new RetryPolicy(retryCount, maxRetries);
    }

    public boolean canRetry() {
        return retryCount < maxRetries;
    }

    public boolean isExhausted() {
        return retryCount >= maxRetries;
    }

    public RetryPolicy increment() {
        return new RetryPolicy(retryCount + 1, maxRetries);
    }
}

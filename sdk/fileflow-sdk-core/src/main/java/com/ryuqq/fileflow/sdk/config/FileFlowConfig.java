package com.ryuqq.fileflow.sdk.config;

import java.time.Duration;
import java.util.Objects;

public record FileFlowConfig(
        String baseUrl,
        String serviceName,
        String serviceToken,
        Duration connectTimeout,
        Duration readTimeout) {

    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    public FileFlowConfig {
        Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        if (baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        Objects.requireNonNull(serviceName, "serviceName must not be null");
        if (serviceName.isBlank()) {
            throw new IllegalArgumentException("serviceName must not be blank");
        }
        Objects.requireNonNull(serviceToken, "serviceToken must not be null");
        if (serviceToken.isBlank()) {
            throw new IllegalArgumentException("serviceToken must not be blank");
        }
        if (connectTimeout == null) {
            connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        }
        if (readTimeout == null) {
            readTimeout = DEFAULT_READ_TIMEOUT;
        }
    }

    public static FileFlowConfig of(String baseUrl, String serviceName, String serviceToken) {
        return new FileFlowConfig(
                baseUrl, serviceName, serviceToken, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }
}

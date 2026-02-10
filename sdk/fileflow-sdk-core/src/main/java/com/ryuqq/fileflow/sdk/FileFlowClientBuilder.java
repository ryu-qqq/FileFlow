package com.ryuqq.fileflow.sdk;

import com.ryuqq.fileflow.sdk.client.internal.DefaultFileFlowClient;
import com.ryuqq.fileflow.sdk.config.FileFlowConfig;
import java.time.Duration;
import java.util.Objects;

public final class FileFlowClientBuilder {

    private String baseUrl;
    private String serviceName;
    private String serviceToken;
    private Duration connectTimeout = FileFlowConfig.DEFAULT_CONNECT_TIMEOUT;
    private Duration readTimeout = FileFlowConfig.DEFAULT_READ_TIMEOUT;

    FileFlowClientBuilder() {}

    public FileFlowClientBuilder baseUrl(String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        return this;
    }

    public FileFlowClientBuilder serviceName(String serviceName) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName must not be null");
        return this;
    }

    public FileFlowClientBuilder serviceToken(String serviceToken) {
        this.serviceToken = Objects.requireNonNull(serviceToken, "serviceToken must not be null");
        return this;
    }

    public FileFlowClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout =
                Objects.requireNonNull(connectTimeout, "connectTimeout must not be null");
        return this;
    }

    public FileFlowClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = Objects.requireNonNull(readTimeout, "readTimeout must not be null");
        return this;
    }

    public FileFlowClient build() {
        validate();
        FileFlowConfig config =
                new FileFlowConfig(baseUrl, serviceName, serviceToken, connectTimeout, readTimeout);
        return new DefaultFileFlowClient(config);
    }

    private void validate() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("baseUrl must be set");
        }
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalStateException("serviceName must be set");
        }
        if (serviceToken == null || serviceToken.isBlank()) {
            throw new IllegalStateException("serviceToken must be set");
        }
    }
}

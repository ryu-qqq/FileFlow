package com.ryuqq.fileflow.sdk.autoconfigure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for FileFlow SDK.
 *
 * <p>Example configuration in application.yml:
 *
 * <pre>
 * fileflow:
 *   base-url: https://fileflow.example.com
 *   service-token: your-service-token
 *   service-name: my-service
 *   connect-timeout: 5s
 *   read-timeout: 30s
 *   write-timeout: 30s
 *   log-requests: false
 *   async: false
 * </pre>
 */
@ConfigurationProperties(prefix = "fileflow")
public class FileFlowProperties {

    /** Base URL of the FileFlow API. */
    private String baseUrl;

    /**
     * Service token for authentication. Used as a fallback when no ThreadLocal token is available.
     */
    private String serviceToken;

    /** Service name for X-Service-Name header. Used to identify the calling service. */
    private String serviceName;

    /** Connection timeout. */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** Read timeout. */
    private Duration readTimeout = Duration.ofSeconds(30);

    /** Write timeout. */
    private Duration writeTimeout = Duration.ofSeconds(30);

    /** Whether to log HTTP requests. */
    private boolean logRequests = false;

    /**
     * Whether to create an async client (WebClient-based) instead of sync client
     * (RestClient-based).
     */
    private boolean async = false;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public boolean isLogRequests() {
        return logRequests;
    }

    public void setLogRequests(boolean logRequests) {
        this.logRequests = logRequests;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
}

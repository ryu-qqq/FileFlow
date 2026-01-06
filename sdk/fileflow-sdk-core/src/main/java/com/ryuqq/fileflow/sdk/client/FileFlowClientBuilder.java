package com.ryuqq.fileflow.sdk.client;

import com.ryuqq.fileflow.sdk.auth.ChainTokenResolver;
import com.ryuqq.fileflow.sdk.auth.TokenResolver;
import com.ryuqq.fileflow.sdk.config.FileFlowClientConfig;
import java.time.Duration;
import java.util.Objects;

/**
 * Builder for creating {@link FileFlowClient} instances.
 *
 * <p><strong>Basic usage:</strong>
 *
 * <pre>{@code
 * FileFlowClient client = FileFlowClient.builder()
 *     .baseUrl("https://fileflow.example.com")
 *     .serviceToken("your-service-token")
 *     .build();
 * }</pre>
 *
 * <p><strong>With custom token resolver:</strong>
 *
 * <pre>{@code
 * FileFlowClient client = FileFlowClient.builder()
 *     .baseUrl("https://fileflow.example.com")
 *     .tokenResolver(ChainTokenResolver.withFallback("service-token"))
 *     .connectTimeout(Duration.ofSeconds(10))
 *     .readTimeout(Duration.ofSeconds(60))
 *     .build();
 * }</pre>
 *
 * <p><strong>Async client:</strong>
 *
 * <pre>{@code
 * FileFlowAsyncClient asyncClient = FileFlowClient.builder()
 *     .baseUrl("https://fileflow.example.com")
 *     .serviceToken("your-service-token")
 *     .buildAsync();
 * }</pre>
 */
public final class FileFlowClientBuilder {

    private String baseUrl;
    private TokenResolver tokenResolver;
    private String serviceToken;
    private String serviceName;
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(30);
    private Duration writeTimeout = Duration.ofSeconds(30);
    private boolean logRequests = false;

    FileFlowClientBuilder() {
        // Package-private constructor
    }

    /**
     * Sets the base URL of the FileFlow API.
     *
     * @param baseUrl the base URL (e.g., "https://fileflow.example.com")
     * @return this builder
     */
    public FileFlowClientBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Sets a static service token for authentication.
     *
     * <p>This creates a {@link ChainTokenResolver} that tries ThreadLocal first, then falls back to
     * the service token. The token will be sent via X-Service-Token header.
     *
     * @param serviceToken the service token
     * @return this builder
     */
    public FileFlowClientBuilder serviceToken(String serviceToken) {
        this.serviceToken = serviceToken;
        return this;
    }

    /**
     * Sets the service name for X-Service-Name header.
     *
     * <p>This is used to identify the calling service in server logs and for access control.
     *
     * @param serviceName the service name (e.g., "product-service", "order-service")
     * @return this builder
     */
    public FileFlowClientBuilder serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Sets a custom token resolver for authentication.
     *
     * <p>If both {@code tokenResolver} and {@code serviceToken} are set, the custom resolver takes
     * precedence.
     *
     * @param tokenResolver the token resolver
     * @return this builder
     */
    public FileFlowClientBuilder tokenResolver(TokenResolver tokenResolver) {
        this.tokenResolver = tokenResolver;
        return this;
    }

    /**
     * Sets the connection timeout.
     *
     * @param connectTimeout the connection timeout (default: 5 seconds)
     * @return this builder
     */
    public FileFlowClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Sets the read timeout.
     *
     * @param readTimeout the read timeout (default: 30 seconds)
     * @return this builder
     */
    public FileFlowClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Sets the write timeout.
     *
     * @param writeTimeout the write timeout (default: 30 seconds)
     * @return this builder
     */
    public FileFlowClientBuilder writeTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * Enables or disables request logging.
     *
     * @param logRequests true to enable request logging
     * @return this builder
     */
    public FileFlowClientBuilder logRequests(boolean logRequests) {
        this.logRequests = logRequests;
        return this;
    }

    /**
     * Builds a synchronous FileFlowClient using RestClient.
     *
     * @return the configured FileFlowClient
     * @throws NullPointerException if baseUrl is not set
     * @throws IllegalArgumentException if neither tokenResolver nor serviceToken is set
     */
    public FileFlowClient build() {
        return new FileFlowSyncClient(buildConfig());
    }

    /**
     * Builds an asynchronous FileFlowAsyncClient using WebClient.
     *
     * @return the configured FileFlowAsyncClient
     * @throws NullPointerException if baseUrl is not set
     * @throws IllegalArgumentException if neither tokenResolver nor serviceToken is set
     */
    public FileFlowAsyncClient buildAsync() {
        return new FileFlowAsyncClientImpl(buildConfig());
    }

    private FileFlowClientConfig buildConfig() {
        Objects.requireNonNull(baseUrl, "baseUrl must be set");

        TokenResolver resolvedTokenResolver = resolveTokenResolver();

        return FileFlowClientConfig.builder()
                .baseUrl(normalizeBaseUrl(baseUrl))
                .tokenResolver(resolvedTokenResolver)
                .serviceName(serviceName)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .writeTimeout(writeTimeout)
                .logRequests(logRequests)
                .build();
    }

    private TokenResolver resolveTokenResolver() {
        if (tokenResolver != null) {
            return tokenResolver;
        }
        if (serviceToken != null) {
            return ChainTokenResolver.withFallback(serviceToken);
        }
        throw new IllegalArgumentException("Either tokenResolver or serviceToken must be set");
    }

    private String normalizeBaseUrl(String url) {
        // Remove trailing slash
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}

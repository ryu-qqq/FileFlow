package com.ryuqq.fileflow.sdk.config;

import com.ryuqq.fileflow.sdk.auth.TokenResolver;
import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for FileFlow SDK client.
 *
 * <p>Use {@link Builder} to create instances.
 */
public final class FileFlowClientConfig {

    private final String baseUrl;
    private final TokenResolver tokenResolver;
    private final String serviceName;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final boolean logRequests;

    private FileFlowClientConfig(Builder builder) {
        this.baseUrl = Objects.requireNonNull(builder.baseUrl, "baseUrl must not be null");
        this.tokenResolver =
                Objects.requireNonNull(builder.tokenResolver, "tokenResolver must not be null");
        this.serviceName = builder.serviceName;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.logRequests = builder.logRequests;
    }

    /**
     * Returns the base URL of the FileFlow API.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the token resolver for authentication.
     *
     * @return the token resolver
     */
    public TokenResolver getTokenResolver() {
        return tokenResolver;
    }

    /**
     * Returns the service name for X-Service-Name header.
     *
     * @return the service name, or null if not set
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Returns the connection timeout.
     *
     * @return the connection timeout
     */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Returns the read timeout.
     *
     * @return the read timeout
     */
    public Duration getReadTimeout() {
        return readTimeout;
    }

    /**
     * Returns the write timeout.
     *
     * @return the write timeout
     */
    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * Returns whether request logging is enabled.
     *
     * @return true if request logging is enabled
     */
    public boolean isLogRequests() {
        return logRequests;
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link FileFlowClientConfig}. */
    public static final class Builder {

        private String baseUrl;
        private TokenResolver tokenResolver;
        private String serviceName;
        private Duration connectTimeout = Duration.ofSeconds(5);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration writeTimeout = Duration.ofSeconds(30);
        private boolean logRequests = false;

        private Builder() {}

        /**
         * Sets the base URL of the FileFlow API.
         *
         * @param baseUrl the base URL (e.g., "https://fileflow.example.com")
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets the token resolver for authentication.
         *
         * @param tokenResolver the token resolver
         * @return this builder
         */
        public Builder tokenResolver(TokenResolver tokenResolver) {
            this.tokenResolver = tokenResolver;
            return this;
        }

        /**
         * Sets the service name for X-Service-Name header.
         *
         * @param serviceName the service name
         * @return this builder
         */
        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        /**
         * Sets the connection timeout.
         *
         * @param connectTimeout the connection timeout
         * @return this builder
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Sets the read timeout.
         *
         * @param readTimeout the read timeout
         * @return this builder
         */
        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * Sets the write timeout.
         *
         * @param writeTimeout the write timeout
         * @return this builder
         */
        public Builder writeTimeout(Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        /**
         * Enables or disables request logging.
         *
         * @param logRequests true to enable request logging
         * @return this builder
         */
        public Builder logRequests(boolean logRequests) {
            this.logRequests = logRequests;
            return this;
        }

        /**
         * Builds the configuration.
         *
         * @return the configuration
         * @throws NullPointerException if required fields are not set
         */
        public FileFlowClientConfig build() {
            return new FileFlowClientConfig(this);
        }
    }
}

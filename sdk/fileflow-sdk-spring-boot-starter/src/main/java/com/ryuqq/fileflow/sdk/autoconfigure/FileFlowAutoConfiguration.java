package com.ryuqq.fileflow.sdk.autoconfigure;

import com.ryuqq.fileflow.sdk.client.FileFlowAsyncClient;
import com.ryuqq.fileflow.sdk.client.FileFlowClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for FileFlow SDK.
 *
 * <p>Automatically configures either a synchronous or asynchronous FileFlow client based on the
 * application properties.
 *
 * <p>Required properties:
 *
 * <ul>
 *   <li>{@code fileflow.base-url} - The base URL of the FileFlow API
 *   <li>{@code fileflow.service-token} - The service token for authentication
 * </ul>
 *
 * <p>Optional properties:
 *
 * <ul>
 *   <li>{@code fileflow.async} - Set to {@code true} to create an async client (default: false)
 *   <li>{@code fileflow.connect-timeout} - Connection timeout (default: 5s)
 *   <li>{@code fileflow.read-timeout} - Read timeout (default: 30s)
 *   <li>{@code fileflow.write-timeout} - Write timeout (default: 30s)
 *   <li>{@code fileflow.log-requests} - Enable request logging (default: false)
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(FileFlowClient.class)
@ConditionalOnProperty(prefix = "fileflow", name = "base-url")
@EnableConfigurationProperties(FileFlowProperties.class)
public class FileFlowAutoConfiguration {

    /**
     * Creates a synchronous FileFlowClient.
     *
     * <p>This bean is created when {@code fileflow.async} is not set or is {@code false}.
     *
     * @param properties the FileFlow properties
     * @return the configured FileFlowClient
     */
    @Bean
    @ConditionalOnMissingBean(FileFlowClient.class)
    @ConditionalOnProperty(
            prefix = "fileflow",
            name = "async",
            havingValue = "false",
            matchIfMissing = true)
    public FileFlowClient fileFlowClient(FileFlowProperties properties) {
        return FileFlowClient.builder()
                .baseUrl(properties.getBaseUrl())
                .serviceToken(properties.getServiceToken())
                .serviceName(properties.getServiceName())
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getReadTimeout())
                .writeTimeout(properties.getWriteTimeout())
                .logRequests(properties.isLogRequests())
                .build();
    }

    /**
     * Creates an asynchronous FileFlowAsyncClient.
     *
     * <p>This bean is created when {@code fileflow.async} is {@code true}.
     *
     * @param properties the FileFlow properties
     * @return the configured FileFlowAsyncClient
     */
    @Bean
    @ConditionalOnMissingBean(FileFlowAsyncClient.class)
    @ConditionalOnProperty(prefix = "fileflow", name = "async", havingValue = "true")
    public FileFlowAsyncClient fileFlowAsyncClient(FileFlowProperties properties) {
        return FileFlowClient.builder()
                .baseUrl(properties.getBaseUrl())
                .serviceToken(properties.getServiceToken())
                .serviceName(properties.getServiceName())
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getReadTimeout())
                .writeTimeout(properties.getWriteTimeout())
                .logRequests(properties.isLogRequests())
                .buildAsync();
    }
}

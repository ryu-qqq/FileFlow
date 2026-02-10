package com.ryuqq.fileflow.sdk.autoconfigure;

import com.ryuqq.fileflow.sdk.FileFlowClient;
import com.ryuqq.fileflow.sdk.api.AssetApi;
import com.ryuqq.fileflow.sdk.api.DownloadTaskApi;
import com.ryuqq.fileflow.sdk.api.MultipartUploadSessionApi;
import com.ryuqq.fileflow.sdk.api.SingleUploadSessionApi;
import com.ryuqq.fileflow.sdk.api.TransformRequestApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(FileFlowClient.class)
@ConditionalOnProperty(prefix = "fileflow", name = "base-url")
@EnableConfigurationProperties(FileFlowProperties.class)
public class FileFlowAutoConfiguration {

    private final FileFlowProperties properties;

    public FileFlowAutoConfiguration(FileFlowProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public FileFlowClient fileFlowClient() {
        return FileFlowClient.builder()
                .baseUrl(properties.getBaseUrl())
                .serviceName(properties.getServiceName())
                .serviceToken(properties.getServiceToken())
                .connectTimeout(properties.getTimeout().getConnect())
                .readTimeout(properties.getTimeout().getRead())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SingleUploadSessionApi singleUploadSessionApi(FileFlowClient client) {
        return client.singleUploadSession();
    }

    @Bean
    @ConditionalOnMissingBean
    public MultipartUploadSessionApi multipartUploadSessionApi(FileFlowClient client) {
        return client.multipartUploadSession();
    }

    @Bean
    @ConditionalOnMissingBean
    public AssetApi assetApi(FileFlowClient client) {
        return client.asset();
    }

    @Bean
    @ConditionalOnMissingBean
    public DownloadTaskApi downloadTaskApi(FileFlowClient client) {
        return client.downloadTask();
    }

    @Bean
    @ConditionalOnMissingBean
    public TransformRequestApi transformRequestApi(FileFlowClient client) {
        return client.transformRequest();
    }
}

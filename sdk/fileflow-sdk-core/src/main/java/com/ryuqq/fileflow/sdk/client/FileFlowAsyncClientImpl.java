package com.ryuqq.fileflow.sdk.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryuqq.fileflow.sdk.api.ExternalDownloadAsyncApi;
import com.ryuqq.fileflow.sdk.api.FileAssetAsyncApi;
import com.ryuqq.fileflow.sdk.api.UploadSessionAsyncApi;
import com.ryuqq.fileflow.sdk.client.internal.ExternalDownloadAsyncApiImpl;
import com.ryuqq.fileflow.sdk.client.internal.FileAssetAsyncApiImpl;
import com.ryuqq.fileflow.sdk.client.internal.HttpClientAsyncSupport;
import com.ryuqq.fileflow.sdk.client.internal.UploadSessionAsyncApiImpl;
import com.ryuqq.fileflow.sdk.config.FileFlowClientConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/** Async implementation of FileFlowClient using WebClient. */
final class FileFlowAsyncClientImpl implements FileFlowAsyncClient {

    private final FileAssetAsyncApi fileAssetApi;
    private final UploadSessionAsyncApi uploadSessionApi;
    private final ExternalDownloadAsyncApi externalDownloadApi;

    FileFlowAsyncClientImpl(FileFlowClientConfig config) {
        WebClient webClient = createWebClient(config);
        ObjectMapper objectMapper = createObjectMapper();
        HttpClientAsyncSupport httpSupport =
                new HttpClientAsyncSupport(webClient, objectMapper, config);

        this.fileAssetApi = new FileAssetAsyncApiImpl(httpSupport);
        this.uploadSessionApi = new UploadSessionAsyncApiImpl(httpSupport);
        this.externalDownloadApi = new ExternalDownloadAsyncApiImpl(httpSupport);
    }

    @Override
    public FileAssetAsyncApi fileAssets() {
        return fileAssetApi;
    }

    @Override
    public UploadSessionAsyncApi uploadSessions() {
        return uploadSessionApi;
    }

    @Override
    public ExternalDownloadAsyncApi externalDownloads() {
        return externalDownloadApi;
    }

    private WebClient createWebClient(FileFlowClientConfig config) {
        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}

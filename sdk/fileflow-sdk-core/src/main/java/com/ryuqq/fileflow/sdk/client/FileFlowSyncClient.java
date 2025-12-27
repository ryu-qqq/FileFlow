package com.ryuqq.fileflow.sdk.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ryuqq.fileflow.sdk.api.ExternalDownloadApi;
import com.ryuqq.fileflow.sdk.api.FileAssetApi;
import com.ryuqq.fileflow.sdk.api.UploadSessionApi;
import com.ryuqq.fileflow.sdk.client.internal.ExternalDownloadApiImpl;
import com.ryuqq.fileflow.sdk.client.internal.FileAssetApiImpl;
import com.ryuqq.fileflow.sdk.client.internal.HttpClientSupport;
import com.ryuqq.fileflow.sdk.client.internal.UploadSessionApiImpl;
import com.ryuqq.fileflow.sdk.config.FileFlowClientConfig;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Synchronous implementation of FileFlowClient using RestClient. */
final class FileFlowSyncClient implements FileFlowClient {

    private final FileAssetApi fileAssetApi;
    private final UploadSessionApi uploadSessionApi;
    private final ExternalDownloadApi externalDownloadApi;

    FileFlowSyncClient(FileFlowClientConfig config) {
        RestClient restClient = createRestClient(config);
        ObjectMapper objectMapper = createObjectMapper();
        HttpClientSupport httpSupport = new HttpClientSupport(restClient, objectMapper, config);

        this.fileAssetApi = new FileAssetApiImpl(httpSupport);
        this.uploadSessionApi = new UploadSessionApiImpl(httpSupport);
        this.externalDownloadApi = new ExternalDownloadApiImpl(httpSupport);
    }

    @Override
    public FileAssetApi fileAssets() {
        return fileAssetApi;
    }

    @Override
    public UploadSessionApi uploadSessions() {
        return uploadSessionApi;
    }

    @Override
    public ExternalDownloadApi externalDownloads() {
        return externalDownloadApi;
    }

    private RestClient createRestClient(FileFlowClientConfig config) {
        RequestConfig requestConfig =
                RequestConfig.custom()
                        .setConnectionRequestTimeout(
                                Timeout.of(
                                        config.getConnectTimeout().toMillis(),
                                        TimeUnit.MILLISECONDS))
                        .setResponseTimeout(
                                Timeout.of(
                                        config.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS))
                        .build();

        CloseableHttpClient httpClient =
                HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .baseUrl(config.getBaseUrl())
                .requestFactory(factory)
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

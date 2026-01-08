package com.ryuqq.fileflow.sdk.client.internal;

import com.ryuqq.fileflow.sdk.api.FileAssetAsyncApi;
import com.ryuqq.fileflow.sdk.model.asset.DownloadUrlRequest;
import com.ryuqq.fileflow.sdk.model.asset.DownloadUrlResponse;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetResponse;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetStatisticsResponse;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Async implementation of FileAssetApi using WebClient. */
public final class FileAssetAsyncApiImpl implements FileAssetAsyncApi {

    private static final String BASE_PATH = "/api/v1/file/file-assets";

    private final HttpClientAsyncSupport httpClient;

    /**
     * Creates a new FileAssetAsyncApiImpl.
     *
     * @param httpClient the async HTTP client support
     */
    public FileAssetAsyncApiImpl(HttpClientAsyncSupport httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Mono<DownloadUrlResponse> generateDownloadUrl(String fileAssetId) {
        return httpClient.post(
                BASE_PATH + "/" + fileAssetId + "/download-url",
                DownloadUrlRequest.withDefaults(),
                new ParameterizedTypeReference<ApiResponse<DownloadUrlResponse>>() {});
    }

    @Override
    public Mono<DownloadUrlResponse> generateDownloadUrl(String fileAssetId, Duration expiresIn) {
        return httpClient.post(
                BASE_PATH + "/" + fileAssetId + "/download-url",
                DownloadUrlRequest.expiresIn(expiresIn),
                new ParameterizedTypeReference<ApiResponse<DownloadUrlResponse>>() {});
    }

    @Override
    public Flux<DownloadUrlResponse> batchGenerateDownloadUrl(List<String> fileAssetIds) {
        Map<String, Object> request = Map.of("fileAssetIds", fileAssetIds);

        return httpClient
                .<List<DownloadUrlResponse>>post(
                        BASE_PATH + "/batch-download-url",
                        request,
                        new ParameterizedTypeReference<ApiResponse<List<DownloadUrlResponse>>>() {})
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Void> delete(String fileAssetId) {
        return httpClient
                .<Void>patch(
                        BASE_PATH + "/" + fileAssetId + "/delete",
                        null,
                        new ParameterizedTypeReference<ApiResponse<Void>>() {})
                .then();
    }

    @Override
    public Mono<FileAssetResponse> get(String fileAssetId) {
        return httpClient.get(
                BASE_PATH + "/" + fileAssetId,
                new ParameterizedTypeReference<ApiResponse<FileAssetResponse>>() {});
    }

    @Override
    public Mono<PageResponse<FileAssetResponse>> list(int page, int size) {
        return httpClient.get(
                BASE_PATH,
                Map.of("page", page, "size", size),
                new ParameterizedTypeReference<ApiResponse<PageResponse<FileAssetResponse>>>() {});
    }

    @Override
    public Mono<FileAssetStatisticsResponse> getStatistics() {
        return httpClient.get(
                BASE_PATH + "/statistics",
                new ParameterizedTypeReference<ApiResponse<FileAssetStatisticsResponse>>() {});
    }
}

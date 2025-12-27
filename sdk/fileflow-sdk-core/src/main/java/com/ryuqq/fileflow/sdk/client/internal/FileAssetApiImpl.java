package com.ryuqq.fileflow.sdk.client.internal;

import com.ryuqq.fileflow.sdk.api.FileAssetApi;
import com.ryuqq.fileflow.sdk.model.asset.DownloadUrlRequest;
import com.ryuqq.fileflow.sdk.model.asset.DownloadUrlResponse;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetResponse;
import com.ryuqq.fileflow.sdk.model.common.ApiResponse;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;

/** Implementation of FileAssetApi. */
public final class FileAssetApiImpl implements FileAssetApi {

    private static final String BASE_PATH = "/api/v1/file/file-assets";

    private final HttpClientSupport httpClient;

    /**
     * Creates a new FileAssetApiImpl.
     *
     * @param httpClient the HTTP client support
     */
    public FileAssetApiImpl(HttpClientSupport httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public DownloadUrlResponse generateDownloadUrl(String fileAssetId) {
        return httpClient.post(
                BASE_PATH + "/" + fileAssetId + "/download-url",
                DownloadUrlRequest.withDefaults(),
                new ParameterizedTypeReference<ApiResponse<DownloadUrlResponse>>() {});
    }

    @Override
    public DownloadUrlResponse generateDownloadUrl(String fileAssetId, Duration expiresIn) {
        return httpClient.post(
                BASE_PATH + "/" + fileAssetId + "/download-url",
                DownloadUrlRequest.expiresIn(expiresIn),
                new ParameterizedTypeReference<ApiResponse<DownloadUrlResponse>>() {});
    }

    @Override
    public List<DownloadUrlResponse> batchGenerateDownloadUrl(List<String> fileAssetIds) {
        return batchGenerateDownloadUrl(fileAssetIds, null);
    }

    @Override
    public List<DownloadUrlResponse> batchGenerateDownloadUrl(
            List<String> fileAssetIds, Duration expiresIn) {
        Map<String, Object> request = new HashMap<>();
        request.put("fileAssetIds", fileAssetIds);
        if (expiresIn != null) {
            request.put("expiresInSeconds", expiresIn.toSeconds());
        }

        return httpClient.post(
                BASE_PATH + "/batch-download-url",
                request,
                new ParameterizedTypeReference<ApiResponse<List<DownloadUrlResponse>>>() {});
    }

    @Override
    public void delete(String fileAssetId) {
        httpClient.patch(
                BASE_PATH + "/" + fileAssetId + "/delete",
                null,
                new ParameterizedTypeReference<ApiResponse<Void>>() {});
    }

    @Override
    public void batchDelete(List<String> fileAssetIds) {
        Map<String, Object> request = Map.of("fileAssetIds", fileAssetIds);

        httpClient.post(
                BASE_PATH + "/batch-delete",
                request,
                new ParameterizedTypeReference<ApiResponse<Void>>() {});
    }

    @Override
    public FileAssetResponse get(String fileAssetId) {
        return httpClient.get(
                BASE_PATH + "/" + fileAssetId,
                new ParameterizedTypeReference<ApiResponse<FileAssetResponse>>() {});
    }

    @Override
    public PageResponse<FileAssetResponse> list(int page, int size) {
        return httpClient.get(
                BASE_PATH,
                Map.of("page", page, "size", size),
                new ParameterizedTypeReference<ApiResponse<PageResponse<FileAssetResponse>>>() {});
    }

    @Override
    public void retry(String fileAssetId) {
        httpClient.post(
                BASE_PATH + "/" + fileAssetId + "/retry",
                new ParameterizedTypeReference<ApiResponse<Void>>() {});
    }
}

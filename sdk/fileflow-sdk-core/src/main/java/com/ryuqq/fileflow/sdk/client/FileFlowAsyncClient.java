package com.ryuqq.fileflow.sdk.client;

import com.ryuqq.fileflow.sdk.api.ExternalDownloadAsyncApi;
import com.ryuqq.fileflow.sdk.api.FileAssetAsyncApi;
import com.ryuqq.fileflow.sdk.api.UploadSessionAsyncApi;

/**
 * Async version of FileFlowClient using WebClient and reactive types.
 *
 * <p><strong>Usage example:</strong>
 *
 * <pre>{@code
 * FileFlowAsyncClient client = FileFlowClient.builder()
 *     .baseUrl("https://fileflow.example.com")
 *     .serviceToken("your-service-token")
 *     .buildAsync();
 *
 * // Generate download URL reactively
 * Mono<DownloadUrlResponse> response = client.fileAssets()
 *     .generateDownloadUrl(fileAssetId);
 *
 * // Subscribe to the result
 * response.subscribe(r -> System.out.println(r.getDownloadUrl()));
 * }</pre>
 *
 * @see FileFlowClient
 * @see FileAssetAsyncApi
 */
public interface FileFlowAsyncClient {

    /**
     * Returns the async FileAsset API.
     *
     * @return the async FileAsset API
     */
    FileAssetAsyncApi fileAssets();

    /**
     * Returns the async UploadSession API.
     *
     * @return the async UploadSession API
     */
    UploadSessionAsyncApi uploadSessions();

    /**
     * Returns the async ExternalDownload API.
     *
     * @return the async ExternalDownload API
     */
    ExternalDownloadAsyncApi externalDownloads();
}

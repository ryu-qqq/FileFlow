package com.ryuqq.fileflow.sdk.client;

import com.ryuqq.fileflow.sdk.api.ExternalDownloadApi;
import com.ryuqq.fileflow.sdk.api.FileAssetApi;
import com.ryuqq.fileflow.sdk.api.UploadSessionApi;

/**
 * Main entry point for the FileFlow SDK.
 *
 * <p>Provides access to all FileFlow API operations through domain-specific API interfaces.
 *
 * <p><strong>Usage example:</strong>
 *
 * <pre>{@code
 * FileFlowClient client = FileFlowClient.builder()
 *     .baseUrl("https://fileflow.example.com")
 *     .serviceToken("your-service-token")
 *     .build();
 *
 * // Generate download URL
 * DownloadUrlResponse response = client.fileAssets()
 *     .generateDownloadUrl(fileAssetId);
 *
 * // Initialize upload session
 * InitSingleUploadResponse session = client.uploadSessions()
 *     .initSingle(InitSingleUploadRequest.builder()
 *         .filename("document.pdf")
 *         .contentType("application/pdf")
 *         .fileSize(1024L)
 *         .build());
 * }</pre>
 *
 * @see FileFlowClientBuilder
 * @see FileAssetApi
 * @see UploadSessionApi
 * @see ExternalDownloadApi
 */
public interface FileFlowClient {

    /**
     * Returns the FileAsset API for file asset operations.
     *
     * <p>Provides operations for:
     *
     * <ul>
     *   <li>Generating presigned download URLs
     *   <li>Batch download URL generation
     *   <li>File deletion (single and batch)
     *   <li>File asset queries
     * </ul>
     *
     * @return the FileAsset API
     */
    FileAssetApi fileAssets();

    /**
     * Returns the UploadSession API for upload operations.
     *
     * <p>Provides operations for:
     *
     * <ul>
     *   <li>Single file upload (presigned PUT URL)
     *   <li>Multipart upload initialization and completion
     *   <li>Upload session management
     * </ul>
     *
     * @return the UploadSession API
     */
    UploadSessionApi uploadSessions();

    /**
     * Returns the ExternalDownload API for downloading from external URLs.
     *
     * <p>Provides operations for:
     *
     * <ul>
     *   <li>Requesting async downloads from external URLs
     *   <li>Checking download status
     * </ul>
     *
     * @return the ExternalDownload API
     */
    ExternalDownloadApi externalDownloads();

    /**
     * Creates a new builder for configuring a FileFlowClient.
     *
     * @return a new builder instance
     */
    static FileFlowClientBuilder builder() {
        return new FileFlowClientBuilder();
    }
}

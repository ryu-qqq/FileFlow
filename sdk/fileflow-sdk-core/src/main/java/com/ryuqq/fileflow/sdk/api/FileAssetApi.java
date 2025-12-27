package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.asset.DownloadUrlResponse;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetResponse;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import java.time.Duration;
import java.util.List;

/**
 * API for file asset operations.
 *
 * <p>Provides operations for managing file assets including:
 *
 * <ul>
 *   <li>Generating presigned download URLs
 *   <li>Batch download URL generation
 *   <li>File deletion
 *   <li>File asset queries
 * </ul>
 */
public interface FileAssetApi {

    /**
     * Generates a presigned download URL with default expiration.
     *
     * @param fileAssetId the file asset ID
     * @return the download URL response
     */
    DownloadUrlResponse generateDownloadUrl(String fileAssetId);

    /**
     * Generates a presigned download URL with custom expiration.
     *
     * @param fileAssetId the file asset ID
     * @param expiresIn how long the URL should be valid
     * @return the download URL response
     */
    DownloadUrlResponse generateDownloadUrl(String fileAssetId, Duration expiresIn);

    /**
     * Generates presigned download URLs for multiple files.
     *
     * @param fileAssetIds the file asset IDs (max 100)
     * @return list of download URL responses
     */
    List<DownloadUrlResponse> batchGenerateDownloadUrl(List<String> fileAssetIds);

    /**
     * Generates presigned download URLs for multiple files with custom expiration.
     *
     * @param fileAssetIds the file asset IDs (max 100)
     * @param expiresIn how long the URLs should be valid
     * @return list of download URL responses
     */
    List<DownloadUrlResponse> batchGenerateDownloadUrl(
            List<String> fileAssetIds, Duration expiresIn);

    /**
     * Deletes a file asset (soft delete).
     *
     * @param fileAssetId the file asset ID to delete
     */
    void delete(String fileAssetId);

    /**
     * Deletes multiple file assets (soft delete).
     *
     * @param fileAssetIds the file asset IDs to delete (max 100)
     */
    void batchDelete(List<String> fileAssetIds);

    /**
     * Gets a file asset by ID.
     *
     * @param fileAssetId the file asset ID
     * @return the file asset
     */
    FileAssetResponse get(String fileAssetId);

    /**
     * Lists file assets with pagination.
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated file assets
     */
    PageResponse<FileAssetResponse> list(int page, int size);

    /**
     * Retries processing a failed file asset.
     *
     * @param fileAssetId the file asset ID
     */
    void retry(String fileAssetId);
}

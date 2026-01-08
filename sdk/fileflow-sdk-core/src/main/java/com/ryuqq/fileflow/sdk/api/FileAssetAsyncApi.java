package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.asset.DownloadUrlResponse;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetResponse;
import com.ryuqq.fileflow.sdk.model.asset.FileAssetStatisticsResponse;
import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import java.time.Duration;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Async API for file asset operations using reactive types. */
public interface FileAssetAsyncApi {

    /**
     * Generates a presigned download URL with default expiration.
     *
     * @param fileAssetId the file asset ID
     * @return Mono emitting the download URL response
     */
    Mono<DownloadUrlResponse> generateDownloadUrl(String fileAssetId);

    /**
     * Generates a presigned download URL with custom expiration.
     *
     * @param fileAssetId the file asset ID
     * @param expiresIn how long the URL should be valid
     * @return Mono emitting the download URL response
     */
    Mono<DownloadUrlResponse> generateDownloadUrl(String fileAssetId, Duration expiresIn);

    /**
     * Generates presigned download URLs for multiple files.
     *
     * @param fileAssetIds the file asset IDs (max 100)
     * @return Flux emitting download URL responses
     */
    Flux<DownloadUrlResponse> batchGenerateDownloadUrl(List<String> fileAssetIds);

    /**
     * Deletes a file asset (soft delete).
     *
     * @param fileAssetId the file asset ID to delete
     * @return Mono completing when done
     */
    Mono<Void> delete(String fileAssetId);

    /**
     * Gets a file asset by ID.
     *
     * @param fileAssetId the file asset ID
     * @return Mono emitting the file asset
     */
    Mono<FileAssetResponse> get(String fileAssetId);

    /**
     * Lists file assets with pagination.
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @return Mono emitting paginated file assets
     */
    Mono<PageResponse<FileAssetResponse>> list(int page, int size);

    /**
     * Gets file asset statistics.
     *
     * <p>Returns aggregated counts by status (PENDING, PROCESSING, COMPLETED, FAILED) and by
     * category (IMAGE, VIDEO, DOCUMENT, etc.).
     *
     * @return Mono emitting the statistics response
     */
    Mono<FileAssetStatisticsResponse> getStatistics();
}

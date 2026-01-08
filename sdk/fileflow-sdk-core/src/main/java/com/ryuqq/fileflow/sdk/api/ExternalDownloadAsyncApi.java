package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadSearchRequest;
import reactor.core.publisher.Mono;

/**
 * Async API for external download operations using reactive types.
 *
 * <p>Provides non-blocking operations for downloading files from external URLs including:
 *
 * <ul>
 *   <li>Requesting downloads from external URLs
 *   <li>Retrieving download details
 *   <li>Listing downloads with filtering
 * </ul>
 */
public interface ExternalDownloadAsyncApi {

    /**
     * Requests an async download from an external URL.
     *
     * @param idempotencyKey the idempotency key (UUID format) to ensure retry safety
     * @param sourceUrl the external URL to download from
     * @param webhookUrl optional webhook URL for completion notification
     * @return Mono emitting the external download ID
     */
    Mono<String> request(String idempotencyKey, String sourceUrl, String webhookUrl);

    /**
     * Requests an async download from an external URL without webhook.
     *
     * @param idempotencyKey the idempotency key (UUID format) to ensure retry safety
     * @param sourceUrl the external URL to download from
     * @return Mono emitting the external download ID
     */
    default Mono<String> request(String idempotencyKey, String sourceUrl) {
        return request(idempotencyKey, sourceUrl, null);
    }

    /**
     * Retrieves an external download by ID.
     *
     * @param id the external download ID
     * @return Mono emitting the download details
     */
    Mono<ExternalDownloadDetailResponse> get(String id);

    /**
     * Lists external downloads with optional filtering.
     *
     * @param request the search criteria
     * @return Mono emitting paginated list of downloads
     */
    Mono<PageResponse<ExternalDownloadResponse>> list(ExternalDownloadSearchRequest request);
}

package com.ryuqq.fileflow.sdk.api;

import com.ryuqq.fileflow.sdk.model.common.PageResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadDetailResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadResponse;
import com.ryuqq.fileflow.sdk.model.download.ExternalDownloadSearchRequest;

/**
 * API for external download operations.
 *
 * <p>Provides operations for downloading files from external URLs including:
 *
 * <ul>
 *   <li>Requesting downloads from external URLs
 *   <li>Retrieving download details
 *   <li>Listing downloads with filtering
 * </ul>
 */
public interface ExternalDownloadApi {

    /**
     * Requests an async download from an external URL.
     *
     * @param idempotencyKey the idempotency key (UUID format) to ensure retry safety
     * @param sourceUrl the external URL to download from
     * @param webhookUrl optional webhook URL for completion notification
     * @return the external download ID
     */
    String request(String idempotencyKey, String sourceUrl, String webhookUrl);

    /**
     * Requests an async download from an external URL without webhook.
     *
     * @param idempotencyKey the idempotency key (UUID format) to ensure retry safety
     * @param sourceUrl the external URL to download from
     * @return the external download ID
     */
    default String request(String idempotencyKey, String sourceUrl) {
        return request(idempotencyKey, sourceUrl, null);
    }

    /**
     * Retrieves an external download by ID.
     *
     * @param id the external download ID
     * @return the download details
     */
    ExternalDownloadDetailResponse get(String id);

    /**
     * Lists external downloads with optional filtering.
     *
     * @param request the search criteria
     * @return paginated list of downloads
     */
    PageResponse<ExternalDownloadResponse> list(ExternalDownloadSearchRequest request);
}

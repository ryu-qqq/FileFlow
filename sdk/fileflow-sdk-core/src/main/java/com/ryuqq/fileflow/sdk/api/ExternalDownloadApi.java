package com.ryuqq.fileflow.sdk.api;

/**
 * API for external download operations.
 *
 * <p>Provides operations for downloading files from external URLs.
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
}

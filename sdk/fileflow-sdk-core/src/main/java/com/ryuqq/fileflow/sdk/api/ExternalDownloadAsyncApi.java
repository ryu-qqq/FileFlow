package com.ryuqq.fileflow.sdk.api;

import reactor.core.publisher.Mono;

/**
 * Async API for external download operations using reactive types.
 *
 * <p>Provides operations for downloading files from external URLs.
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
}

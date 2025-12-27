package com.ryuqq.fileflow.sdk.model.asset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;

/** Request for generating a presigned download URL. */
public final class DownloadUrlRequest {

    private final Long expiresInSeconds;

    private DownloadUrlRequest(Long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    /**
     * Creates a request with default expiration.
     *
     * @return a new request
     */
    public static DownloadUrlRequest withDefaults() {
        return new DownloadUrlRequest(null);
    }

    /**
     * Creates a request with the specified expiration.
     *
     * @param expiresIn the expiration duration
     * @return a new request
     */
    public static DownloadUrlRequest expiresIn(Duration expiresIn) {
        return new DownloadUrlRequest(expiresIn.toSeconds());
    }

    /**
     * Returns the expiration in seconds.
     *
     * @return the expiration in seconds, or null for default
     */
    @JsonProperty("expiresInSeconds")
    public Long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}

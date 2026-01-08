package com.ryuqq.fileflow.sdk.model.download;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Response for external download operations.
 *
 * <p>Contains basic information about an external download request.
 */
public final class ExternalDownloadResponse {

    private final String id;
    private final String status;
    private final Instant createdAt;

    /**
     * Creates an ExternalDownloadResponse.
     *
     * @param id the external download ID
     * @param status the current status (PENDING, PROCESSING, COMPLETED, FAILED)
     * @param createdAt the creation timestamp
     */
    @JsonCreator
    public ExternalDownloadResponse(
            @JsonProperty("id") String id,
            @JsonProperty("status") String status,
            @JsonProperty("createdAt") Instant createdAt) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

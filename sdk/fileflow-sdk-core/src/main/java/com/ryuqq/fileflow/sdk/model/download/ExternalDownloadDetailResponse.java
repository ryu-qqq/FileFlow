package com.ryuqq.fileflow.sdk.model.download;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Detailed response for external download operations.
 *
 * <p>Contains full information about an external download request including source URL, error
 * details, and associated file asset.
 */
public final class ExternalDownloadDetailResponse {

    private final String id;
    private final String sourceUrl;
    private final String status;
    private final String fileAssetId;
    private final String errorMessage;
    private final int retryCount;
    private final String webhookUrl;
    private final Instant createdAt;
    private final Instant updatedAt;

    /**
     * Creates an ExternalDownloadDetailResponse.
     *
     * @param id the external download ID
     * @param sourceUrl the source URL being downloaded
     * @param status the current status (PENDING, PROCESSING, COMPLETED, FAILED)
     * @param fileAssetId the created file asset ID (null if not completed)
     * @param errorMessage the error message (null if no error)
     * @param retryCount the number of retry attempts
     * @param webhookUrl the callback webhook URL (null if not set)
     * @param createdAt the creation timestamp
     * @param updatedAt the last update timestamp
     */
    @JsonCreator
    public ExternalDownloadDetailResponse(
            @JsonProperty("id") String id,
            @JsonProperty("sourceUrl") String sourceUrl,
            @JsonProperty("status") String status,
            @JsonProperty("fileAssetId") String fileAssetId,
            @JsonProperty("errorMessage") String errorMessage,
            @JsonProperty("retryCount") int retryCount,
            @JsonProperty("webhookUrl") String webhookUrl,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("updatedAt") Instant updatedAt) {
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.status = status;
        this.fileAssetId = fileAssetId;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.webhookUrl = webhookUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getFileAssetId() {
        return fileAssetId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

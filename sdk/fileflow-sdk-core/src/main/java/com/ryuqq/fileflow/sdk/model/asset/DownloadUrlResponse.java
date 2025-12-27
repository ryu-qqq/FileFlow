package com.ryuqq.fileflow.sdk.model.asset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/** Response containing a presigned download URL. */
public final class DownloadUrlResponse {

    private final String fileAssetId;
    private final String downloadUrl;
    private final LocalDateTime expiresAt;

    /**
     * Creates a new DownloadUrlResponse.
     *
     * @param fileAssetId the file asset ID
     * @param downloadUrl the presigned download URL
     * @param expiresAt when the URL expires
     */
    @JsonCreator
    public DownloadUrlResponse(
            @JsonProperty("fileAssetId") String fileAssetId,
            @JsonProperty("downloadUrl") String downloadUrl,
            @JsonProperty("expiresAt") LocalDateTime expiresAt) {
        this.fileAssetId = fileAssetId;
        this.downloadUrl = downloadUrl;
        this.expiresAt = expiresAt;
    }

    /**
     * Returns the file asset ID.
     *
     * @return the file asset ID
     */
    public String getFileAssetId() {
        return fileAssetId;
    }

    /**
     * Returns the presigned download URL.
     *
     * @return the download URL
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Returns when the URL expires.
     *
     * @return the expiration time
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}

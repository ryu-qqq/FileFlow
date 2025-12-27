package com.ryuqq.fileflow.sdk.model.asset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/** Response containing file asset details. */
public final class FileAssetResponse {

    private final String id;
    private final String filename;
    private final String contentType;
    private final Long fileSize;
    private final String status;
    private final String category;
    private final String s3Key;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Creates a new FileAssetResponse.
     *
     * @param id the file asset ID
     * @param filename the original filename
     * @param contentType the MIME content type
     * @param fileSize the file size in bytes
     * @param status the processing status
     * @param category the file category
     * @param s3Key the S3 object key
     * @param createdAt when the asset was created
     * @param updatedAt when the asset was last updated
     */
    @JsonCreator
    public FileAssetResponse(
            @JsonProperty("id") String id,
            @JsonProperty("filename") String filename,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("fileSize") Long fileSize,
            @JsonProperty("status") String status,
            @JsonProperty("category") String category,
            @JsonProperty("s3Key") String s3Key,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("updatedAt") LocalDateTime updatedAt) {
        this.id = id;
        this.filename = filename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.status = status;
        this.category = category;
        this.s3Key = s3Key;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getStatus() {
        return status;
    }

    public String getCategory() {
        return category;
    }

    public String getS3Key() {
        return s3Key;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

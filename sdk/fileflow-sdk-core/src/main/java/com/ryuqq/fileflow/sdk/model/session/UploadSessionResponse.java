package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Response containing basic upload session information.
 *
 * <p>Used for session list queries.
 */
public final class UploadSessionResponse {

    private final String sessionId;
    private final String fileName;
    private final long fileSize;
    private final String contentType;
    private final String uploadType;
    private final String status;
    private final String bucket;
    private final String key;
    private final Instant createdAt;
    private final Instant expiresAt;

    /**
     * Creates an UploadSessionResponse.
     *
     * @param sessionId the session ID
     * @param fileName the file name
     * @param fileSize the file size in bytes
     * @param contentType the content type
     * @param uploadType the upload type (SINGLE/MULTIPART)
     * @param status the session status
     * @param bucket the S3 bucket name
     * @param key the S3 object key
     * @param createdAt the creation timestamp
     * @param expiresAt the expiration timestamp
     */
    @JsonCreator
    public UploadSessionResponse(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("fileName") String fileName,
            @JsonProperty("fileSize") long fileSize,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("uploadType") String uploadType,
            @JsonProperty("status") String status,
            @JsonProperty("bucket") String bucket,
            @JsonProperty("key") String key,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("expiresAt") Instant expiresAt) {
        this.sessionId = sessionId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.uploadType = uploadType;
        this.status = status;
        this.bucket = bucket;
        this.key = key;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public String getUploadType() {
        return uploadType;
    }

    public String getStatus() {
        return status;
    }

    public String getBucket() {
        return bucket;
    }

    public String getKey() {
        return key;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}

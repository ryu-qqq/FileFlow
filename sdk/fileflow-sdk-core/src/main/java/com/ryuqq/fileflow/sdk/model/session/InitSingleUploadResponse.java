package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/** Response for initializing a single file upload session. */
public final class InitSingleUploadResponse {

    private final String sessionId;
    private final String presignedUrl;
    private final LocalDateTime expiresAt;
    private final String s3Key;

    /**
     * Creates a new InitSingleUploadResponse.
     *
     * @param sessionId the upload session ID
     * @param presignedUrl the presigned PUT URL for uploading
     * @param expiresAt when the presigned URL expires
     * @param s3Key the S3 object key
     */
    @JsonCreator
    public InitSingleUploadResponse(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("presignedUrl") String presignedUrl,
            @JsonProperty("expiresAt") LocalDateTime expiresAt,
            @JsonProperty("s3Key") String s3Key) {
        this.sessionId = sessionId;
        this.presignedUrl = presignedUrl;
        this.expiresAt = expiresAt;
        this.s3Key = s3Key;
    }

    /**
     * Returns the upload session ID.
     *
     * @return the session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the presigned PUT URL for uploading the file to S3.
     *
     * @return the presigned URL
     */
    public String getPresignedUrl() {
        return presignedUrl;
    }

    /**
     * Returns when the presigned URL expires.
     *
     * @return the expiration time
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Returns the S3 object key.
     *
     * @return the S3 key
     */
    public String getS3Key() {
        return s3Key;
    }
}

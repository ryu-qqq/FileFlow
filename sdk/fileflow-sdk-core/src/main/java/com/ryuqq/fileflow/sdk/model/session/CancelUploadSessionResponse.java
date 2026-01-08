package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response for cancelling an upload session.
 *
 * <p>Contains the cancelled session information.
 */
public final class CancelUploadSessionResponse {

    private final String sessionId;
    private final String status;
    private final String bucket;
    private final String key;

    /**
     * Creates a CancelUploadSessionResponse.
     *
     * @param sessionId the session ID
     * @param status the session status (FAILED)
     * @param bucket the S3 bucket name
     * @param key the S3 object key
     */
    @JsonCreator
    public CancelUploadSessionResponse(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("status") String status,
            @JsonProperty("bucket") String bucket,
            @JsonProperty("key") String key) {
        this.sessionId = sessionId;
        this.status = status;
        this.bucket = bucket;
        this.key = key;
    }

    public String getSessionId() {
        return sessionId;
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
}

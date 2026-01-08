package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Response for marking a part as uploaded.
 *
 * <p>Confirms that the part upload has been recorded in the session.
 */
public final class MarkPartUploadedResponse {

    private final String sessionId;
    private final int partNumber;
    private final String etag;
    private final Instant uploadedAt;

    /**
     * Creates a MarkPartUploadedResponse.
     *
     * @param sessionId the session ID
     * @param partNumber the part number (1-based)
     * @param etag the part ETag
     * @param uploadedAt the upload timestamp
     */
    @JsonCreator
    public MarkPartUploadedResponse(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("partNumber") int partNumber,
            @JsonProperty("etag") String etag,
            @JsonProperty("uploadedAt") Instant uploadedAt) {
        this.sessionId = sessionId;
        this.partNumber = partNumber;
        this.etag = etag;
        this.uploadedAt = uploadedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public String getEtag() {
        return etag;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}

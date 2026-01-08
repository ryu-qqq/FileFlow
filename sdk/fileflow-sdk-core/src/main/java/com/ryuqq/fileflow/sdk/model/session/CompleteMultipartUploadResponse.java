package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Response for completing a multipart upload session.
 *
 * <p>Contains the completed session information and merged part list.
 */
public final class CompleteMultipartUploadResponse {

    private final String sessionId;
    private final String status;
    private final String bucket;
    private final String key;
    private final String uploadId;
    private final int totalParts;
    private final List<CompletedPartInfo> completedParts;
    private final Instant completedAt;

    /**
     * Creates a CompleteMultipartUploadResponse.
     *
     * @param sessionId the session ID
     * @param status the session status (COMPLETED)
     * @param bucket the S3 bucket name
     * @param key the S3 object key
     * @param uploadId the S3 multipart upload ID
     * @param totalParts the total number of parts
     * @param completedParts the list of completed parts
     * @param completedAt the completion timestamp
     */
    @JsonCreator
    public CompleteMultipartUploadResponse(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("status") String status,
            @JsonProperty("bucket") String bucket,
            @JsonProperty("key") String key,
            @JsonProperty("uploadId") String uploadId,
            @JsonProperty("totalParts") int totalParts,
            @JsonProperty("completedParts") List<CompletedPartInfo> completedParts,
            @JsonProperty("completedAt") Instant completedAt) {
        this.sessionId = sessionId;
        this.status = status;
        this.bucket = bucket;
        this.key = key;
        this.uploadId = uploadId;
        this.totalParts = totalParts;
        this.completedParts = completedParts;
        this.completedAt = completedAt;
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

    public String getUploadId() {
        return uploadId;
    }

    public int getTotalParts() {
        return totalParts;
    }

    public List<CompletedPartInfo> getCompletedParts() {
        return completedParts;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    /**
     * Information about a completed part.
     */
    public static final class CompletedPartInfo {

        private final int partNumber;
        private final String etag;
        private final long size;
        private final Instant uploadedAt;

        /**
         * Creates a CompletedPartInfo.
         *
         * @param partNumber the part number (1-based)
         * @param etag the part ETag
         * @param size the part size in bytes
         * @param uploadedAt the upload timestamp
         */
        @JsonCreator
        public CompletedPartInfo(
                @JsonProperty("partNumber") int partNumber,
                @JsonProperty("etag") String etag,
                @JsonProperty("size") long size,
                @JsonProperty("uploadedAt") Instant uploadedAt) {
            this.partNumber = partNumber;
            this.etag = etag;
            this.size = size;
            this.uploadedAt = uploadedAt;
        }

        public int getPartNumber() {
            return partNumber;
        }

        public String getEtag() {
            return etag;
        }

        public long getSize() {
            return size;
        }

        public Instant getUploadedAt() {
            return uploadedAt;
        }
    }
}

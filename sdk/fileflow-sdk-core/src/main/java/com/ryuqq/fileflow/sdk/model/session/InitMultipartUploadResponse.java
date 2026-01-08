package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Response for multipart upload session initialization.
 *
 * <p>Contains the session information and presigned URLs for each part.
 */
public final class InitMultipartUploadResponse {

    private final String sessionId;
    private final String uploadId;
    private final int totalParts;
    private final long partSize;
    private final Instant expiresAt;
    private final String bucket;
    private final String key;
    private final List<PartInfo> parts;

    /**
     * Creates an InitMultipartUploadResponse.
     *
     * @param sessionId the session ID
     * @param uploadId the S3 multipart upload ID
     * @param totalParts the total number of parts
     * @param partSize the size of each part in bytes
     * @param expiresAt the session expiration time
     * @param bucket the S3 bucket name
     * @param key the S3 object key
     * @param parts the list of part information
     */
    @JsonCreator
    public InitMultipartUploadResponse(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("uploadId") String uploadId,
            @JsonProperty("totalParts") int totalParts,
            @JsonProperty("partSize") long partSize,
            @JsonProperty("expiresAt") Instant expiresAt,
            @JsonProperty("bucket") String bucket,
            @JsonProperty("key") String key,
            @JsonProperty("parts") List<PartInfo> parts) {
        this.sessionId = sessionId;
        this.uploadId = uploadId;
        this.totalParts = totalParts;
        this.partSize = partSize;
        this.expiresAt = expiresAt;
        this.bucket = bucket;
        this.key = key;
        this.parts = parts != null ? new ArrayList<>(parts) : new ArrayList<>();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUploadId() {
        return uploadId;
    }

    public int getTotalParts() {
        return totalParts;
    }

    public long getPartSize() {
        return partSize;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getBucket() {
        return bucket;
    }

    public String getKey() {
        return key;
    }

    public List<PartInfo> getParts() {
        return new ArrayList<>(parts);
    }

    /** Part information with presigned URL. */
    public static final class PartInfo {

        private final int partNumber;
        private final String presignedUrl;

        /**
         * Creates a PartInfo.
         *
         * @param partNumber the part number (1-based)
         * @param presignedUrl the presigned URL for uploading this part
         */
        @JsonCreator
        public PartInfo(
                @JsonProperty("partNumber") int partNumber,
                @JsonProperty("presignedUrl") String presignedUrl) {
            this.partNumber = partNumber;
            this.presignedUrl = presignedUrl;
        }

        public int getPartNumber() {
            return partNumber;
        }

        public String getPresignedUrl() {
            return presignedUrl;
        }
    }
}

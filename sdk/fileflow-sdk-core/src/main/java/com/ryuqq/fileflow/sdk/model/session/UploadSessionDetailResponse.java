package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Response containing detailed upload session information.
 *
 * <p>For multipart sessions, includes part information.
 */
public final class UploadSessionDetailResponse {

    private final String sessionId;
    private final String fileName;
    private final long fileSize;
    private final String contentType;
    private final String uploadType;
    private final String status;
    private final String bucket;
    private final String key;
    private final String uploadId;
    private final Integer totalParts;
    private final Integer uploadedParts;
    private final List<PartDetail> parts;
    private final String etag;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final Instant completedAt;

    /**
     * Creates an UploadSessionDetailResponse.
     *
     * @param sessionId the session ID
     * @param fileName the file name
     * @param fileSize the file size in bytes
     * @param contentType the content type
     * @param uploadType the upload type (SINGLE/MULTIPART)
     * @param status the session status
     * @param bucket the S3 bucket name
     * @param key the S3 object key
     * @param uploadId the S3 multipart upload ID (null for SINGLE)
     * @param totalParts the total number of parts (null for SINGLE)
     * @param uploadedParts the number of uploaded parts (null for SINGLE)
     * @param parts the part details (null for SINGLE)
     * @param etag the ETag (when completed)
     * @param createdAt the creation timestamp
     * @param expiresAt the expiration timestamp
     * @param completedAt the completion timestamp (null if not completed)
     */
    @JsonCreator
    public UploadSessionDetailResponse(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("fileName") String fileName,
            @JsonProperty("fileSize") long fileSize,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("uploadType") String uploadType,
            @JsonProperty("status") String status,
            @JsonProperty("bucket") String bucket,
            @JsonProperty("key") String key,
            @JsonProperty("uploadId") String uploadId,
            @JsonProperty("totalParts") Integer totalParts,
            @JsonProperty("uploadedParts") Integer uploadedParts,
            @JsonProperty("parts") List<PartDetail> parts,
            @JsonProperty("etag") String etag,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("expiresAt") Instant expiresAt,
            @JsonProperty("completedAt") Instant completedAt) {
        this.sessionId = sessionId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.uploadType = uploadType;
        this.status = status;
        this.bucket = bucket;
        this.key = key;
        this.uploadId = uploadId;
        this.totalParts = totalParts;
        this.uploadedParts = uploadedParts;
        this.parts = parts != null ? new ArrayList<>(parts) : new ArrayList<>();
        this.etag = etag;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.completedAt = completedAt;
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

    public String getUploadId() {
        return uploadId;
    }

    public Integer getTotalParts() {
        return totalParts;
    }

    public Integer getUploadedParts() {
        return uploadedParts;
    }

    public List<PartDetail> getParts() {
        return new ArrayList<>(parts);
    }

    public String getEtag() {
        return etag;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    /**
     * Returns whether this is a multipart upload session.
     *
     * @return true if multipart, false if single
     */
    public boolean isMultipart() {
        return "MULTIPART".equals(uploadType);
    }

    /** Part detail information. */
    public static final class PartDetail {

        private final int partNumber;
        private final String etag;
        private final long size;
        private final Instant uploadedAt;

        /**
         * Creates a PartDetail.
         *
         * @param partNumber the part number (1-based)
         * @param etag the part ETag
         * @param size the part size in bytes
         * @param uploadedAt the upload timestamp
         */
        @JsonCreator
        public PartDetail(
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

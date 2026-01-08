package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for initializing a multipart file upload session.
 *
 * <p>Used for uploading large files in parts. Each part can be uploaded separately using the
 * presigned URLs returned in the response.
 */
public final class InitMultipartUploadRequest {

    private final String fileName;
    private final long fileSize;
    private final String contentType;
    private final long partSize;
    private final String uploadCategory;
    private final String customPath;

    private InitMultipartUploadRequest(Builder builder) {
        this.fileName = builder.fileName;
        this.fileSize = builder.fileSize;
        this.contentType = builder.contentType;
        this.partSize = builder.partSize;
        this.uploadCategory = builder.uploadCategory;
        this.customPath = builder.customPath;
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @JsonProperty("fileName")
    public String getFileName() {
        return fileName;
    }

    @JsonProperty("fileSize")
    public long getFileSize() {
        return fileSize;
    }

    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("partSize")
    public long getPartSize() {
        return partSize;
    }

    @JsonProperty("uploadCategory")
    public String getUploadCategory() {
        return uploadCategory;
    }

    @JsonProperty("customPath")
    public String getCustomPath() {
        return customPath;
    }

    /** Builder for {@link InitMultipartUploadRequest}. */
    public static final class Builder {

        private String fileName;
        private long fileSize;
        private String contentType;
        private long partSize = 5 * 1024 * 1024; // Default 5MB
        private String uploadCategory;
        private String customPath;

        private Builder() {}

        /**
         * Sets the file name.
         *
         * @param fileName the file name with extension
         * @return this builder
         */
        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        /**
         * Sets the file size in bytes.
         *
         * @param fileSize the file size
         * @return this builder
         */
        public Builder fileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        /**
         * Sets the content type (MIME type).
         *
         * @param contentType the content type
         * @return this builder
         */
        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Sets the part size in bytes. Default is 5MB.
         *
         * @param partSize the part size
         * @return this builder
         */
        public Builder partSize(long partSize) {
            this.partSize = partSize;
            return this;
        }

        /**
         * Sets the upload category.
         *
         * @param uploadCategory the upload category (required for Admin/Seller)
         * @return this builder
         */
        public Builder uploadCategory(String uploadCategory) {
            this.uploadCategory = uploadCategory;
            return this;
        }

        /**
         * Sets the custom S3 path (SYSTEM only).
         *
         * @param customPath the custom path
         * @return this builder
         */
        public Builder customPath(String customPath) {
            this.customPath = customPath;
            return this;
        }

        /**
         * Builds the request.
         *
         * @return the built request
         */
        public InitMultipartUploadRequest build() {
            return new InitMultipartUploadRequest(this);
        }
    }
}

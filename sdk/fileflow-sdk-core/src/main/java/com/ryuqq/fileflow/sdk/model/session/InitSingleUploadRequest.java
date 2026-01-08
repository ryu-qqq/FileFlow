package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Request for initializing a single file upload session. */
public final class InitSingleUploadRequest {

    private final String idempotencyKey;
    private final String fileName;
    private final String contentType;
    private final Long fileSize;
    private final String uploadCategory;
    private final String customPath;

    private InitSingleUploadRequest(Builder builder) {
        this.idempotencyKey = builder.idempotencyKey;
        this.fileName = builder.fileName;
        this.contentType = builder.contentType;
        this.fileSize = builder.fileSize;
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

    @JsonProperty("idempotencyKey")
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    @JsonProperty("fileName")
    public String getFileName() {
        return fileName;
    }

    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("fileSize")
    public Long getFileSize() {
        return fileSize;
    }

    @JsonProperty("uploadCategory")
    public String getUploadCategory() {
        return uploadCategory;
    }

    @JsonProperty("customPath")
    public String getCustomPath() {
        return customPath;
    }

    /** Builder for {@link InitSingleUploadRequest}. */
    public static final class Builder {

        private String idempotencyKey;
        private String fileName;
        private String contentType;
        private Long fileSize;
        private String uploadCategory;
        private String customPath;

        private Builder() {}

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder fileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder uploadCategory(String uploadCategory) {
            this.uploadCategory = uploadCategory;
            return this;
        }

        public Builder customPath(String customPath) {
            this.customPath = customPath;
            return this;
        }

        public InitSingleUploadRequest build() {
            return new InitSingleUploadRequest(this);
        }
    }
}

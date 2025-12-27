package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Request for initializing a single file upload session. */
public final class InitSingleUploadRequest {

    private final String filename;
    private final String contentType;
    private final Long fileSize;
    private final String category;

    private InitSingleUploadRequest(Builder builder) {
        this.filename = builder.filename;
        this.contentType = builder.contentType;
        this.fileSize = builder.fileSize;
        this.category = builder.category;
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("fileSize")
    public Long getFileSize() {
        return fileSize;
    }

    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    /** Builder for {@link InitSingleUploadRequest}. */
    public static final class Builder {

        private String filename;
        private String contentType;
        private Long fileSize;
        private String category;

        private Builder() {}

        public Builder filename(String filename) {
            this.filename = filename;
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

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public InitSingleUploadRequest build() {
            return new InitSingleUploadRequest(this);
        }
    }
}

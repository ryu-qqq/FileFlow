package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for searching upload sessions.
 *
 * <p>Supports filtering by status and upload type with pagination.
 */
public final class UploadSessionSearchRequest {

    private final SessionStatus status;
    private final UploadType uploadType;
    private final int page;
    private final int size;

    private UploadSessionSearchRequest(Builder builder) {
        this.status = builder.status;
        this.uploadType = builder.uploadType;
        this.page = builder.page;
        this.size = builder.size;
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @JsonProperty("status")
    public SessionStatus getStatus() {
        return status;
    }

    @JsonProperty("uploadType")
    public UploadType getUploadType() {
        return uploadType;
    }

    @JsonProperty("page")
    public int getPage() {
        return page;
    }

    @JsonProperty("size")
    public int getSize() {
        return size;
    }

    /** Session status filter. */
    public enum SessionStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        EXPIRED,
        CANCELLED
    }

    /** Upload type filter. */
    public enum UploadType {
        SINGLE,
        MULTIPART
    }

    /** Builder for {@link UploadSessionSearchRequest}. */
    public static final class Builder {

        private SessionStatus status;
        private UploadType uploadType;
        private int page = 0;
        private int size = 20;

        private Builder() {}

        /**
         * Sets the status filter.
         *
         * @param status the session status to filter by
         * @return this builder
         */
        public Builder status(SessionStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the upload type filter.
         *
         * @param uploadType the upload type to filter by
         * @return this builder
         */
        public Builder uploadType(UploadType uploadType) {
            this.uploadType = uploadType;
            return this;
        }

        /**
         * Sets the page number (0-based).
         *
         * @param page the page number
         * @return this builder
         */
        public Builder page(int page) {
            this.page = page;
            return this;
        }

        /**
         * Sets the page size.
         *
         * @param size the page size (max 100)
         * @return this builder
         */
        public Builder size(int size) {
            this.size = Math.min(size, 100);
            return this;
        }

        /**
         * Builds the request.
         *
         * @return the built request
         */
        public UploadSessionSearchRequest build() {
            return new UploadSessionSearchRequest(this);
        }
    }
}

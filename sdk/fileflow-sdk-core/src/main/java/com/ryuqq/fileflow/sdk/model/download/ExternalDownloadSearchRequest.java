package com.ryuqq.fileflow.sdk.model.download;

/**
 * Request for searching external downloads.
 *
 * <p>Supports filtering by status and pagination.
 */
public final class ExternalDownloadSearchRequest {

    private final String status;
    private final int page;
    private final int size;

    private ExternalDownloadSearchRequest(Builder builder) {
        this.status = builder.status;
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

    public String getStatus() {
        return status;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    /** Builder for ExternalDownloadSearchRequest. */
    public static final class Builder {

        private String status;
        private int page = 0;
        private int size = 20;

        private Builder() {}

        /**
         * Sets the status filter.
         *
         * @param status the status to filter by (PENDING, PROCESSING, COMPLETED, FAILED)
         * @return this builder
         */
        public Builder status(String status) {
            this.status = status;
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
         * @param size the page size (1-100)
         * @return this builder
         */
        public Builder size(int size) {
            this.size = size;
            return this;
        }

        /**
         * Builds the request.
         *
         * @return the built request
         */
        public ExternalDownloadSearchRequest build() {
            return new ExternalDownloadSearchRequest(this);
        }
    }
}

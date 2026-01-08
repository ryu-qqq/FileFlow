package com.ryuqq.fileflow.sdk.model.session;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for marking a part as uploaded in a multipart upload session.
 *
 * <p>Used to record that a specific part has been successfully uploaded to S3.
 */
public final class MarkPartUploadedRequest {

    private final int partNumber;
    private final String etag;
    private final long size;

    private MarkPartUploadedRequest(Builder builder) {
        this.partNumber = builder.partNumber;
        this.etag = builder.etag;
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

    @JsonProperty("partNumber")
    public int getPartNumber() {
        return partNumber;
    }

    @JsonProperty("etag")
    public String getEtag() {
        return etag;
    }

    @JsonProperty("size")
    public long getSize() {
        return size;
    }

    /** Builder for {@link MarkPartUploadedRequest}. */
    public static final class Builder {

        private int partNumber;
        private String etag;
        private long size;

        private Builder() {}

        /**
         * Sets the part number (1-based).
         *
         * @param partNumber the part number
         * @return this builder
         */
        public Builder partNumber(int partNumber) {
            this.partNumber = partNumber;
            return this;
        }

        /**
         * Sets the ETag returned by S3 for this part.
         *
         * @param etag the S3 ETag
         * @return this builder
         */
        public Builder etag(String etag) {
            this.etag = etag;
            return this;
        }

        /**
         * Sets the part size in bytes.
         *
         * @param size the part size
         * @return this builder
         */
        public Builder size(long size) {
            this.size = size;
            return this;
        }

        /**
         * Builds the request.
         *
         * @return the built request
         */
        public MarkPartUploadedRequest build() {
            return new MarkPartUploadedRequest(this);
        }
    }
}

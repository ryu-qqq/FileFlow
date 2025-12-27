package com.ryuqq.fileflow.sdk.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Standard API response wrapper for FileFlow API.
 *
 * @param <T> the type of the response data
 */
public final class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final LocalDateTime timestamp;
    private final String requestId;

    /**
     * Creates a new ApiResponse.
     *
     * @param success whether the request was successful
     * @param data the response data
     * @param timestamp the response timestamp
     * @param requestId the request ID for tracing
     */
    @JsonCreator
    public ApiResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("data") T data,
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("requestId") String requestId) {
        this.success = success;
        this.data = data;
        this.timestamp = timestamp;
        this.requestId = requestId;
    }

    /**
     * Returns whether the request was successful.
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the response data.
     *
     * @return the data
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the response timestamp.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the request ID.
     *
     * @return the request ID
     */
    public String getRequestId() {
        return requestId;
    }
}

package com.ryuqq.fileflow.sdk.exception;

/** Exception thrown when the server encounters an error (HTTP 5xx). */
public class FileFlowServerException extends FileFlowException {

    /**
     * Constructs a new FileFlowServerException.
     *
     * @param statusCode the HTTP status code (5xx)
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     */
    public FileFlowServerException(int statusCode, String errorCode, String errorMessage) {
        super(statusCode, errorCode, errorMessage);
    }

    /**
     * Constructs a new FileFlowServerException with a cause.
     *
     * @param statusCode the HTTP status code (5xx)
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     * @param cause the underlying cause
     */
    public FileFlowServerException(
            int statusCode, String errorCode, String errorMessage, Throwable cause) {
        super(statusCode, errorCode, errorMessage, cause);
    }
}

package com.ryuqq.fileflow.sdk.exception;

/** Exception thrown when a requested resource is not found (HTTP 404). */
public class FileFlowNotFoundException extends FileFlowException {

    private static final int STATUS_CODE = 404;

    /**
     * Constructs a new FileFlowNotFoundException.
     *
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     */
    public FileFlowNotFoundException(String errorCode, String errorMessage) {
        super(STATUS_CODE, errorCode, errorMessage);
    }

    /**
     * Constructs a new FileFlowNotFoundException with a cause.
     *
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     * @param cause the underlying cause
     */
    public FileFlowNotFoundException(String errorCode, String errorMessage, Throwable cause) {
        super(STATUS_CODE, errorCode, errorMessage, cause);
    }
}

package com.ryuqq.fileflow.sdk.exception;

/** Exception thrown when the request is invalid (HTTP 400). */
public class FileFlowBadRequestException extends FileFlowException {

    private static final int STATUS_CODE = 400;

    /**
     * Constructs a new FileFlowBadRequestException.
     *
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     */
    public FileFlowBadRequestException(String errorCode, String errorMessage) {
        super(STATUS_CODE, errorCode, errorMessage);
    }

    /**
     * Constructs a new FileFlowBadRequestException with a cause.
     *
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     * @param cause the underlying cause
     */
    public FileFlowBadRequestException(String errorCode, String errorMessage, Throwable cause) {
        super(STATUS_CODE, errorCode, errorMessage, cause);
    }
}

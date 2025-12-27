package com.ryuqq.fileflow.sdk.exception;

/** Exception thrown when authentication fails (HTTP 401). */
public class FileFlowUnauthorizedException extends FileFlowException {

    private static final int STATUS_CODE = 401;

    /**
     * Constructs a new FileFlowUnauthorizedException.
     *
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     */
    public FileFlowUnauthorizedException(String errorCode, String errorMessage) {
        super(STATUS_CODE, errorCode, errorMessage);
    }

    /**
     * Constructs a new FileFlowUnauthorizedException with a cause.
     *
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     * @param cause the underlying cause
     */
    public FileFlowUnauthorizedException(String errorCode, String errorMessage, Throwable cause) {
        super(STATUS_CODE, errorCode, errorMessage, cause);
    }
}

package com.ryuqq.fileflow.sdk.exception;

/** Exception thrown when access is forbidden (HTTP 403). */
public class FileFlowForbiddenException extends FileFlowException {

    private static final int STATUS_CODE = 403;

    /**
     * Constructs a new FileFlowForbiddenException.
     *
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     */
    public FileFlowForbiddenException(String errorCode, String errorMessage) {
        super(STATUS_CODE, errorCode, errorMessage);
    }

    /**
     * Constructs a new FileFlowForbiddenException with a cause.
     *
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     * @param cause the underlying cause
     */
    public FileFlowForbiddenException(String errorCode, String errorMessage, Throwable cause) {
        super(STATUS_CODE, errorCode, errorMessage, cause);
    }
}

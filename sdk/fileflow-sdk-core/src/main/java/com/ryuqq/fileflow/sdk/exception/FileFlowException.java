package com.ryuqq.fileflow.sdk.exception;

/**
 * Base exception for all FileFlow SDK errors.
 *
 * <p>This is an unchecked exception to simplify error handling in client code. All specific
 * FileFlow exceptions extend this class.
 */
public class FileFlowException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;
    private final String errorMessage;

    /**
     * Constructs a new FileFlowException.
     *
     * @param statusCode HTTP status code from the API response
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     */
    public FileFlowException(int statusCode, String errorCode, String errorMessage) {
        super(formatMessage(statusCode, errorCode, errorMessage));
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Constructs a new FileFlowException with a cause.
     *
     * @param statusCode HTTP status code from the API response
     * @param errorCode application-specific error code
     * @param errorMessage human-readable error message
     * @param cause the underlying cause
     */
    public FileFlowException(
            int statusCode, String errorCode, String errorMessage, Throwable cause) {
        super(formatMessage(statusCode, errorCode, errorMessage), cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Constructs a new FileFlowException with only a message.
     *
     * @param message the error message
     */
    public FileFlowException(String message) {
        super(message);
        this.statusCode = 0;
        this.errorCode = "UNKNOWN";
        this.errorMessage = message;
    }

    /**
     * Constructs a new FileFlowException with a message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public FileFlowException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.errorCode = "UNKNOWN";
        this.errorMessage = message;
    }

    /**
     * Returns the HTTP status code from the API response.
     *
     * @return the HTTP status code, or 0 if not available
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the application-specific error code.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the human-readable error message from the API.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    private static String formatMessage(int statusCode, String errorCode, String errorMessage) {
        return String.format("[%d] %s: %s", statusCode, errorCode, errorMessage);
    }
}

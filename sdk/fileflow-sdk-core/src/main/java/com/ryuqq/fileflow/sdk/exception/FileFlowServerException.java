package com.ryuqq.fileflow.sdk.exception;

public class FileFlowServerException extends FileFlowException {

    public FileFlowServerException(int statusCode, String errorCode, String errorMessage) {
        super(statusCode, errorCode, errorMessage);
    }

    public FileFlowServerException(
            int statusCode, String errorCode, String errorMessage, Throwable cause) {
        super(statusCode, errorCode, errorMessage, cause);
    }
}

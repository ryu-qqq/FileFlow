package com.ryuqq.fileflow.sdk.exception;

public class FileFlowBadRequestException extends FileFlowException {

    public FileFlowBadRequestException(String errorCode, String errorMessage) {
        super(400, errorCode, errorMessage);
    }

    public FileFlowBadRequestException(String errorCode, String errorMessage, Throwable cause) {
        super(400, errorCode, errorMessage, cause);
    }
}

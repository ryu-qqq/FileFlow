package com.ryuqq.fileflow.sdk.exception;

public class FileFlowForbiddenException extends FileFlowException {

    public FileFlowForbiddenException(String errorCode, String errorMessage) {
        super(403, errorCode, errorMessage);
    }
}

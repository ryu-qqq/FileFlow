package com.ryuqq.fileflow.sdk.exception;

public class FileFlowNotFoundException extends FileFlowException {

    public FileFlowNotFoundException(String errorCode, String errorMessage) {
        super(404, errorCode, errorMessage);
    }
}

package com.ryuqq.fileflow.sdk.exception;

public class FileFlowConflictException extends FileFlowException {

    public FileFlowConflictException(String errorCode, String errorMessage) {
        super(409, errorCode, errorMessage);
    }
}

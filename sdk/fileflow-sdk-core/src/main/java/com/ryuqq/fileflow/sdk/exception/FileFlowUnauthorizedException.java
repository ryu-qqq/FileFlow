package com.ryuqq.fileflow.sdk.exception;

public class FileFlowUnauthorizedException extends FileFlowException {

    public FileFlowUnauthorizedException(String errorCode, String errorMessage) {
        super(401, errorCode, errorMessage);
    }
}

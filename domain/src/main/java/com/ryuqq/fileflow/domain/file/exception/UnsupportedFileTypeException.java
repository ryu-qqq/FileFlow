package com.ryuqq.fileflow.domain.file.exception;

/**
 * 허용되지 않은 MIME 타입 예외.
 */
public class UnsupportedFileTypeException extends RuntimeException {

    public UnsupportedFileTypeException(String mimeType) {
        super("Unsupported mime type: " + mimeType);
    }
}


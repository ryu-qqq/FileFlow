package com.ryuqq.fileflow.domain.upload.exception;

/**
 * 업로드 세션을 찾을 수 없을 때 발생하는 예외
 *
 * @author sangwon-ryu
 */
public class UploadSessionNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Upload session not found";

    public UploadSessionNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public UploadSessionNotFoundException(String sessionId) {
        super(String.format("Upload session not found: %s", sessionId));
    }

    public UploadSessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

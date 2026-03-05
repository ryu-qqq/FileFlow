package com.ryuqq.fileflow.application.download.exception;

/**
 * 재시도가 불가능한 영구적 다운로드 실패.
 *
 * <p>HTTP 4xx, 유효하지 않은 URL 등 재시도해도 결과가 동일한 실패에 사용합니다.
 */
public class PermanentDownloadFailureException extends RuntimeException {

    public PermanentDownloadFailureException(String message) {
        super(message);
    }

    public PermanentDownloadFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}

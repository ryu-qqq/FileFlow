package com.ryuqq.fileflow.application.download.dto.response;

public record FileDownloadResult(
        boolean success,
        String fileName,
        String contentType,
        long fileSize,
        String etag,
        String errorMessage,
        boolean retryable) {

    public static FileDownloadResult success(
            String fileName, String contentType, long fileSize, String etag) {
        return new FileDownloadResult(true, fileName, contentType, fileSize, etag, null, true);
    }

    public static FileDownloadResult failure(String errorMessage) {
        return new FileDownloadResult(false, null, null, 0, null, errorMessage, true);
    }

    public static FileDownloadResult permanentFailure(String errorMessage) {
        return new FileDownloadResult(false, null, null, 0, null, errorMessage, false);
    }
}

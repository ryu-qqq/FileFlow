package com.ryuqq.fileflow.application.download.dto.response;

public record FileDownloadResult(
        boolean success,
        String fileName,
        String contentType,
        long fileSize,
        String etag,
        String errorMessage) {

    public static FileDownloadResult success(
            String fileName, String contentType, long fileSize, String etag) {
        return new FileDownloadResult(true, fileName, contentType, fileSize, etag, null);
    }

    public static FileDownloadResult failure(String errorMessage) {
        return new FileDownloadResult(false, null, null, 0, null, errorMessage);
    }
}

package com.ryuqq.fileflow.domain.download.vo;

import com.ryuqq.fileflow.domain.download.exception.DownloadErrorCode;
import com.ryuqq.fileflow.domain.download.exception.DownloadException;
import java.time.Instant;
import java.util.Objects;

public record DownloadedFileInfo(
        String fileName, String contentType, long fileSize, String etag, Instant completedAt) {

    public DownloadedFileInfo {
        Objects.requireNonNull(fileName, "fileName must not be null");
        if (fileName.isBlank()) {
            throw new DownloadException(
                    DownloadErrorCode.INVALID_DOWNLOADED_FILE, "fileName must not be blank");
        }
        Objects.requireNonNull(contentType, "contentType must not be null");
        if (fileSize <= 0) {
            throw new DownloadException(
                    DownloadErrorCode.INVALID_DOWNLOADED_FILE,
                    "fileSize must be positive: " + fileSize);
        }
        Objects.requireNonNull(etag, "etag must not be null");
        Objects.requireNonNull(completedAt, "completedAt must not be null");
    }

    public static DownloadedFileInfo of(
            String fileName, String contentType, long fileSize, String etag, Instant completedAt) {
        return new DownloadedFileInfo(fileName, contentType, fileSize, etag, completedAt);
    }
}

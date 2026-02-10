package com.ryuqq.fileflow.application.download.dto.response;

import java.util.Objects;

public record RawDownloadedFile(String fileName, String contentType, byte[] data) {

    public RawDownloadedFile {
        Objects.requireNonNull(fileName, "fileName must not be null");
        Objects.requireNonNull(contentType, "contentType must not be null");
        Objects.requireNonNull(data, "data must not be null");
        if (data.length == 0) {
            throw new IllegalArgumentException("data must not be empty");
        }
    }

    public long fileSize() {
        return data.length;
    }

    public static RawDownloadedFile of(String fileName, String contentType, byte[] data) {
        return new RawDownloadedFile(fileName, contentType, data);
    }
}

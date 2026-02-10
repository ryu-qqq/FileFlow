package com.ryuqq.fileflow.application.transform.dto.result;

import java.util.Objects;

public record ImageProcessingResult(
        byte[] data, int width, int height, String contentType, String extension) {

    public ImageProcessingResult {
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(contentType, "contentType must not be null");
        Objects.requireNonNull(extension, "extension must not be null");
        if (data.length == 0) {
            throw new IllegalArgumentException("data must not be empty");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive");
        }
    }

    public long fileSize() {
        return data.length;
    }
}

package com.ryuqq.fileflow.application.asset.dto.result;

public record ImageMetadataResult(int width, int height) {

    public ImageMetadataResult {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive");
        }
    }
}

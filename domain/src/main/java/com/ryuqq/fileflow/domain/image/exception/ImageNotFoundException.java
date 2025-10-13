package com.ryuqq.fileflow.domain.image.exception;

/**
 * 이미지를 찾을 수 없을 때 발생하는 예외
 */
public class ImageNotFoundException extends RuntimeException {

    private final String imageId;

    public ImageNotFoundException(String imageId) {
        super(String.format("Image not found: %s", imageId));
        this.imageId = imageId;
    }

    public ImageNotFoundException(String imageId, Throwable cause) {
        super(String.format("Image not found: %s", imageId), cause);
        this.imageId = imageId;
    }

    public String getImageId() {
        return imageId;
    }
}

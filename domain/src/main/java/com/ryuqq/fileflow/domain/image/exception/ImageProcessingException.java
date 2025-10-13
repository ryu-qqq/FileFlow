package com.ryuqq.fileflow.domain.image.exception;

/**
 * 이미지 처리 중 발생하는 예외
 * 이미지 최적화, 리사이징, OCR 등의 처리 중 에러가 발생할 때 사용합니다.
 */
public class ImageProcessingException extends RuntimeException {

    private final String imageId;
    private final String operation;

    public ImageProcessingException(String imageId, String operation, String message) {
        super(String.format(
                "Image processing failed for image '%s' during operation '%s': %s",
                imageId,
                operation,
                message
        ));
        this.imageId = imageId;
        this.operation = operation;
    }

    public ImageProcessingException(String imageId, String operation, String message, Throwable cause) {
        super(String.format(
                "Image processing failed for image '%s' during operation '%s': %s",
                imageId,
                operation,
                message
        ), cause);
        this.imageId = imageId;
        this.operation = operation;
    }

    public ImageProcessingException(String message) {
        super(message);
        this.imageId = null;
        this.operation = null;
    }

    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.imageId = null;
        this.operation = null;
    }

    public String getImageId() {
        return imageId;
    }

    public String getOperation() {
        return operation;
    }
}

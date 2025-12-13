package com.ryuqq.fileflow.adapter.out.image;

/**
 * 이미지 처리 중 발생하는 예외.
 *
 * <p>Scrimage 라이브러리 사용 시 발생하는 IOException을 래핑하여 명확한 예외 의미를 전달한다.
 */
public class ImageProcessingException extends RuntimeException {

    public ImageProcessingException(String message) {
        super(message);
    }

    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

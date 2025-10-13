package com.ryuqq.fileflow.application.image;

/**
 * 이미지 변환 중 발생하는 예외
 *
 * 비즈니스 규칙:
 * - 지원하지 않는 포맷
 * - 변환 실패
 * - S3 연동 실패
 * - 이미지 처리 오류
 *
 * @author sangwon-ryu
 */
public class ImageConversionException extends RuntimeException {

    public ImageConversionException(String message) {
        super(message);
    }

    public ImageConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 지원하지 않는 포맷으로 인한 예외
     *
     * @param format 지원하지 않는 포맷
     * @return ImageConversionException 인스턴스
     */
    public static ImageConversionException unsupportedFormat(String format) {
        return new ImageConversionException("Unsupported image format: " + format);
    }

    /**
     * 변환 실패로 인한 예외
     *
     * @param message 실패 메시지
     * @param cause 원인
     * @return ImageConversionException 인스턴스
     */
    public static ImageConversionException conversionFailed(String message, Throwable cause) {
        return new ImageConversionException("Image conversion failed: " + message, cause);
    }

    /**
     * S3 연동 실패로 인한 예외
     *
     * @param operation 실패한 작업 (download/upload)
     * @param s3Uri S3 URI
     * @param cause 원인
     * @return ImageConversionException 인스턴스
     */
    public static ImageConversionException s3OperationFailed(String operation, String s3Uri, Throwable cause) {
        return new ImageConversionException(
                String.format("S3 %s operation failed for URI: %s", operation, s3Uri),
                cause
        );
    }
}

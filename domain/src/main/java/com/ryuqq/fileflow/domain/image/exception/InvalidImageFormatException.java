package com.ryuqq.fileflow.domain.image.exception;

/**
 * 지원하지 않는 이미지 포맷 예외
 * 이미지 포맷이 지원되지 않을 때 발생합니다.
 */
public class InvalidImageFormatException extends RuntimeException {

    private final String providedFormat;
    private final String supportedFormats;

    public InvalidImageFormatException(String providedFormat, String supportedFormats) {
        super(String.format(
                "Invalid image format: '%s'. Supported formats: %s",
                providedFormat,
                supportedFormats
        ));
        this.providedFormat = providedFormat;
        this.supportedFormats = supportedFormats;
    }

    public InvalidImageFormatException(String providedFormat, String supportedFormats, Throwable cause) {
        super(String.format(
                "Invalid image format: '%s'. Supported formats: %s",
                providedFormat,
                supportedFormats
        ), cause);
        this.providedFormat = providedFormat;
        this.supportedFormats = supportedFormats;
    }

    public String getProvidedFormat() {
        return providedFormat;
    }

    public String getSupportedFormats() {
        return supportedFormats;
    }
}

package com.ryuqq.fileflow.application.file;

/**
 * 메타데이터 추출 중 발생하는 예외
 *
 * @author sangwon-ryu
 */
public class MetadataExtractionException extends RuntimeException {

    public MetadataExtractionException(String message) {
        super(message);
    }

    public MetadataExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}

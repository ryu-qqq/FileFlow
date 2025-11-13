package com.ryuqq.fileflow.application.upload.dto.command;

/**
 * Multipart Upload 완료 결과
 *
 * <p>S3 Multipart Upload 완료 후 반환되는 결과입니다.</p>
 *
 * @param etag ETag
 * @param location S3 Object Location URL
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record CompleteMultipartUploadResult(
    String etag,
    String location
) {
    /**
     * Static Factory Method
     *
     * @param etag ETag
     * @param location S3 Object Location URL
     * @return CompleteMultipartUploadResult
     */
    public static CompleteMultipartUploadResult of(String etag, String location) {
        return new CompleteMultipartUploadResult(etag, location);
    }
}

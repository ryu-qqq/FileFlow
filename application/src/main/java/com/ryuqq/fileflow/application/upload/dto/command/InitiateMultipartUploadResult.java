package com.ryuqq.fileflow.application.upload.dto.command;

/**
 * Multipart Upload 초기화 결과
 *
 * <p>S3 Multipart Upload 초기화 후 반환되는 결과입니다.</p>
 *
 * @param uploadId Upload ID
 * @param bucket S3 Bucket
 * @param key S3 Object Key
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record InitiateMultipartUploadResult(
    String uploadId,
    String bucket,
    String key
) {
    /**
     * Static Factory Method
     *
     * @param uploadId Upload ID
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @return InitiateMultipartUploadResult
     */
    public static InitiateMultipartUploadResult of(String uploadId, String bucket, String key) {
        return new InitiateMultipartUploadResult(uploadId, bucket, key);
    }
}

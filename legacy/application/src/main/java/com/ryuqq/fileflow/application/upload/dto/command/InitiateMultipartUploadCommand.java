package com.ryuqq.fileflow.application.upload.dto.command;

/**
 * Multipart Upload 초기화 Command
 *
 * <p>S3 Multipart Upload를 초기화하기 위한 Command입니다.</p>
 *
 * @param bucket S3 Bucket
 * @param key S3 Object Key
 * @param contentType Content Type
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record InitiateMultipartUploadCommand(
    String bucket,
    String key,
    String contentType
) {
    /**
     * Static Factory Method
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @param contentType Content Type
     * @return InitiateMultipartUploadCommand
     */
    public static InitiateMultipartUploadCommand of(String bucket, String key, String contentType) {
        return new InitiateMultipartUploadCommand(bucket, key, contentType);
    }
}

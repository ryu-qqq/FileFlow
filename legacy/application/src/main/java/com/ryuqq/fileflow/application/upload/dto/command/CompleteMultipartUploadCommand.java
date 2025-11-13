package com.ryuqq.fileflow.application.upload.dto.command;

import java.util.List;

/**
 * Multipart Upload 완료 Command
 *
 * <p>S3 Multipart Upload를 완료하기 위한 Command입니다.</p>
 *
 * @param bucket S3 Bucket
 * @param key S3 Object Key
 * @param uploadId Upload ID
 * @param parts 완료된 파트 목록
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record CompleteMultipartUploadCommand(
    String bucket,
    String key,
    String uploadId,
    List<CompletedPartCommand> parts
) {
    /**
     * Static Factory Method
     *
     * @param bucket S3 Bucket
     * @param key S3 Object Key
     * @param uploadId Upload ID
     * @param parts 완료된 파트 목록
     * @return CompleteMultipartUploadCommand
     */
    public static CompleteMultipartUploadCommand of(
        String bucket,
        String key,
        String uploadId,
        List<CompletedPartCommand> parts
    ) {
        return new CompleteMultipartUploadCommand(bucket, key, uploadId, parts);
    }
}

package com.ryuqq.fileflow.application.upload.dto.response;

/**
 * S3 초기화 결과
 *
 * @param uploadId S3 Upload ID
 * @param storageKey Storage Key
 * @param bucket S3 Bucket 이름
 * @param partCount 파트 개수
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record S3InitResultResponse(
    String uploadId,
    String storageKey,
    String bucket,
    int partCount
) {
}

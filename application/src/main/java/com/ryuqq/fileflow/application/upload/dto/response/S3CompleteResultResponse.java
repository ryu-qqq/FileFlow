package com.ryuqq.fileflow.application.upload.dto.response;

/**
 * S3 완료 결과
 *
 * @param etag ETag
 * @param location S3 Location
 * @param fileSize 파일 크기
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record S3CompleteResultResponse(
    String etag,
    String location,
    Long fileSize
) {
}

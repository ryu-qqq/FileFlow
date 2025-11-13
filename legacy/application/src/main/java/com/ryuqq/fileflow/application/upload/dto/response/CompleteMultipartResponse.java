package com.ryuqq.fileflow.application.upload.dto.response;

/**
 * Multipart 업로드 완료 Response
 *
 * @param fileId 파일 ID
 * @param etag ETag
 * @param location S3 Location
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record CompleteMultipartResponse(
    Long fileId,
    String etag,
    String location
) {
    /**
     * Static Factory Method
     *
     * @param fileId 파일 ID
     * @param etag ETag
     * @param location S3 Location
     * @return CompleteMultipartResponse 인스턴스
     */
    public static CompleteMultipartResponse of(Long fileId, String etag, String location) {
        return new CompleteMultipartResponse(fileId, etag, location);
    }
}

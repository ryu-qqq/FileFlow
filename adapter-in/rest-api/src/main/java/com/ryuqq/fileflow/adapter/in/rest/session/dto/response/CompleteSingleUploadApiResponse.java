package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import java.time.LocalDateTime;

/**
 * 단일 파일 업로드 완료 API Response.
 *
 * <p>완료된 세션 정보를 반환합니다.
 *
 * @param sessionId 세션 ID
 * @param status 세션 상태 (COMPLETED)
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @param etag S3 ETag
 * @param completedAt 완료 시각 (UTC)
 * @author development-team
 * @since 1.0.0
 */
public record CompleteSingleUploadApiResponse(
        String sessionId,
        String status,
        String bucket,
        String key,
        String etag,
        LocalDateTime completedAt) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param status 세션 상태
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @param etag S3 ETag
     * @param completedAt 완료 시각
     * @return CompleteSingleUploadApiResponse
     */
    public static CompleteSingleUploadApiResponse of(
            String sessionId,
            String status,
            String bucket,
            String key,
            String etag,
            LocalDateTime completedAt) {
        return new CompleteSingleUploadApiResponse(
                sessionId, status, bucket, key, etag, completedAt);
    }
}

package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

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
@Schema(description = "단일 업로드 완료 응답")
public record CompleteSingleUploadApiResponse(
        @Schema(description = "세션 ID", example = "session-123") String sessionId,
        @Schema(description = "세션 상태", example = "COMPLETED") String status,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") String bucket,
        @Schema(description = "S3 객체 키", example = "uploads/file.jpg") String key,
        @Schema(description = "S3 ETag", example = "\"d41d8cd98f00b204e9800998ecf8427e\"")
                String etag,
        @Schema(description = "완료 시각") Instant completedAt) {

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
            Instant completedAt) {
        return new CompleteSingleUploadApiResponse(
                sessionId, status, bucket, key, etag, completedAt);
    }
}

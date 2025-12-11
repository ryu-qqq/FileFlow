package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * 단일 파일 업로드 세션 초기화 API Response.
 *
 * <p>생성된 세션 정보와 Presigned URL을 반환합니다.
 *
 * @param sessionId 세션 ID
 * @param presignedUrl Presigned URL (15분 유효)
 * @param expiresAt 세션 만료 시각 (UTC)
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "단일 업로드 세션 초기화 응답")
public record InitSingleUploadApiResponse(
        @Schema(description = "세션 ID", example = "session-123") String sessionId,
        @Schema(description = "Presigned URL (15분 유효)", example = "https://s3.amazonaws.com/...") String presignedUrl,
        @Schema(description = "세션 만료 시각") Instant expiresAt,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") String bucket,
        @Schema(description = "S3 객체 키", example = "uploads/file.jpg") String key) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param presignedUrl Presigned URL
     * @param expiresAt 만료 시각
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @return InitSingleUploadApiResponse
     */
    public static InitSingleUploadApiResponse of(
            String sessionId, String presignedUrl, Instant expiresAt, String bucket, String key) {
        return new InitSingleUploadApiResponse(sessionId, presignedUrl, expiresAt, bucket, key);
    }
}

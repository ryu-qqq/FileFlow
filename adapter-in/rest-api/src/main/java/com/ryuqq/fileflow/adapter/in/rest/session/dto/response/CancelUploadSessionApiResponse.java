package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 업로드 세션 취소 API Response.
 *
 * <p>취소된 세션 정보를 반환합니다.
 *
 * @param sessionId 세션 ID
 * @param status 세션 상태 (FAILED)
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "업로드 세션 취소 응답")
public record CancelUploadSessionApiResponse(
        @Schema(description = "세션 ID", example = "session-123") String sessionId,
        @Schema(description = "세션 상태", example = "FAILED") String status,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") String bucket,
        @Schema(description = "S3 객체 키", example = "uploads/file.jpg") String key) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param status 세션 상태
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @return CancelUploadSessionApiResponse
     */
    public static CancelUploadSessionApiResponse of(
            String sessionId, String status, String bucket, String key) {
        return new CancelUploadSessionApiResponse(sessionId, status, bucket, key);
    }
}

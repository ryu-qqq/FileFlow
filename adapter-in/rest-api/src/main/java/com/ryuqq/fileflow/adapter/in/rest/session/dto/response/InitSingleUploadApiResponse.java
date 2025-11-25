package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import java.time.LocalDateTime;

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
public record InitSingleUploadApiResponse(
        String sessionId, String presignedUrl, LocalDateTime expiresAt, String bucket, String key) {

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
            String sessionId,
            String presignedUrl,
            LocalDateTime expiresAt,
            String bucket,
            String key) {
        return new InitSingleUploadApiResponse(sessionId, presignedUrl, expiresAt, bucket, key);
    }
}

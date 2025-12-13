package com.ryuqq.fileflow.application.session.dto.response;

import java.time.Instant;

/**
 * 단일 파일 업로드 세션 초기화 Response.
 *
 * <p>생성된 세션 정보와 Presigned URL을 반환합니다.
 *
 * @param sessionId 세션 ID
 * @param presignedUrl Presigned URL (15분 유효)
 * @param expiresAt 세션 만료 시각 (UTC)
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 */
public record InitSingleUploadResponse(
        String sessionId, String presignedUrl, Instant expiresAt, String bucket, String key) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param presignedUrl Presigned URL
     * @param expiresAt 만료 시각
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @return InitSingleUploadResponse
     */
    public static InitSingleUploadResponse of(
            String sessionId, String presignedUrl, Instant expiresAt, String bucket, String key) {
        return new InitSingleUploadResponse(sessionId, presignedUrl, expiresAt, bucket, key);
    }
}

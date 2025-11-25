package com.ryuqq.fileflow.application.session.dto.response;

import java.time.LocalDateTime;

/**
 * 업로드 세션 만료 처리 Response.
 *
 * <p>만료 처리된 세션 정보를 반환합니다.
 *
 * @param sessionId 세션 ID
 * @param status 세션 상태 (EXPIRED)
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @param expiresAt 만료 시각 (UTC)
 */
public record ExpireUploadSessionResponse(
        String sessionId, String status, String bucket, String key, LocalDateTime expiresAt) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param status 세션 상태
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @param expiresAt 만료 시각
     * @return ExpireUploadSessionResponse
     */
    public static ExpireUploadSessionResponse of(
            String sessionId, String status, String bucket, String key, LocalDateTime expiresAt) {
        return new ExpireUploadSessionResponse(sessionId, status, bucket, key, expiresAt);
    }
}

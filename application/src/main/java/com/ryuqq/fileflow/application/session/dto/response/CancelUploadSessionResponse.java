package com.ryuqq.fileflow.application.session.dto.response;

/**
 * 업로드 세션 취소 Response.
 *
 * <p>취소된 세션 정보를 반환합니다.
 *
 * @param sessionId 세션 ID
 * @param status 세션 상태 (FAILED)
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 */
public record CancelUploadSessionResponse(
        String sessionId, String status, String bucket, String key) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param status 세션 상태
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @return CancelUploadSessionResponse
     */
    public static CancelUploadSessionResponse of(
            String sessionId, String status, String bucket, String key) {
        return new CancelUploadSessionResponse(sessionId, status, bucket, key);
    }
}

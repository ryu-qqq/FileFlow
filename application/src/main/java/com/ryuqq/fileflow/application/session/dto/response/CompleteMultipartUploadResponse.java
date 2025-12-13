package com.ryuqq.fileflow.application.session.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Multipart 파일 업로드 완료 Response.
 *
 * <p>완료된 세션 정보와 병합된 Part 목록을 반환합니다.
 *
 * @param sessionId 세션 ID
 * @param status 세션 상태 (COMPLETED)
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @param uploadId S3 Multipart Upload ID
 * @param totalParts 전체 Part 개수
 * @param completedParts 완료된 Part 목록
 * @param completedAt 완료 시각 (UTC)
 */
public record CompleteMultipartUploadResponse(
        String sessionId,
        String status,
        String bucket,
        String key,
        String uploadId,
        int totalParts,
        List<CompletedPartInfo> completedParts,
        Instant completedAt) {

    /**
     * 완료된 Part 정보.
     *
     * @param partNumber Part 번호 (1-based)
     * @param etag Part ETag
     * @param size Part 크기 (바이트)
     * @param uploadedAt 업로드 시각 (UTC)
     */
    public record CompletedPartInfo(int partNumber, String etag, long size, Instant uploadedAt) {

        /**
         * 값 기반 생성.
         *
         * @param partNumber Part 번호
         * @param etag Part ETag
         * @param size Part 크기
         * @param uploadedAt 업로드 시각
         * @return CompletedPartInfo
         */
        public static CompletedPartInfo of(
                int partNumber, String etag, long size, Instant uploadedAt) {
            return new CompletedPartInfo(partNumber, etag, size, uploadedAt);
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param status 세션 상태
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @param uploadId S3 Upload ID
     * @param totalParts 전체 Part 개수
     * @param completedParts 완료된 Part 목록
     * @param completedAt 완료 시각
     * @return CompleteMultipartUploadResponse
     */
    public static CompleteMultipartUploadResponse of(
            String sessionId,
            String status,
            String bucket,
            String key,
            String uploadId,
            int totalParts,
            List<CompletedPartInfo> completedParts,
            Instant completedAt) {
        return new CompleteMultipartUploadResponse(
                sessionId, status, bucket, key, uploadId, totalParts, completedParts, completedAt);
    }
}

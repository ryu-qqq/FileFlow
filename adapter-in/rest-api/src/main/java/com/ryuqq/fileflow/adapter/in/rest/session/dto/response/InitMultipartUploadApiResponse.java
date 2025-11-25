package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Multipart 파일 업로드 세션 초기화 API Response.
 *
 * <p>생성된 세션 정보와 각 Part 정보 목록을 반환합니다.
 *
 * @param sessionId 세션 ID
 * @param uploadId S3 Multipart Upload ID
 * @param totalParts 전체 Part 개수
 * @param partSize 각 Part 크기 (바이트)
 * @param expiresAt 세션 만료 시각 (UTC, 24시간)
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @param parts Part 정보 목록
 * @author development-team
 * @since 1.0.0
 */
public record InitMultipartUploadApiResponse(
        String sessionId,
        String uploadId,
        int totalParts,
        long partSize,
        LocalDateTime expiresAt,
        String bucket,
        String key,
        List<PartInfoApiResponse> parts) {

    /**
     * Part 정보 API Response.
     *
     * @param partNumber Part 번호 (1-based)
     * @param presignedUrl Presigned URL
     */
    public record PartInfoApiResponse(int partNumber, String presignedUrl) {

        /**
         * 값 기반 생성.
         *
         * @param partNumber Part 번호
         * @param presignedUrl Presigned URL
         * @return PartInfoApiResponse
         */
        public static PartInfoApiResponse of(int partNumber, String presignedUrl) {
            return new PartInfoApiResponse(partNumber, presignedUrl);
        }
    }

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param uploadId S3 Upload ID
     * @param totalParts 전체 Part 개수
     * @param partSize Part 크기
     * @param expiresAt 만료 시각
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @param parts Part 정보 목록
     * @return InitMultipartUploadApiResponse
     */
    public static InitMultipartUploadApiResponse of(
            String sessionId,
            String uploadId,
            int totalParts,
            long partSize,
            LocalDateTime expiresAt,
            String bucket,
            String key,
            List<PartInfoApiResponse> parts) {
        return new InitMultipartUploadApiResponse(
                sessionId, uploadId, totalParts, partSize, expiresAt, bucket, key, parts);
    }
}

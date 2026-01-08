package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.ArrayList;
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
@Schema(description = "Multipart 업로드 세션 초기화 응답")
public record InitMultipartUploadApiResponse(
        @Schema(description = "세션 ID", example = "session-123") String sessionId,
        @Schema(description = "S3 Multipart Upload ID", example = "upload-456") String uploadId,
        @Schema(description = "전체 Part 개수", example = "5") int totalParts,
        @Schema(description = "각 Part 크기 (bytes)", example = "5242880") long partSize,
        @Schema(description = "세션 만료 시각") Instant expiresAt,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") String bucket,
        @Schema(description = "S3 객체 키", example = "uploads/file.jpg") String key,
        @Schema(description = "Part 정보 목록") List<PartInfoApiResponse> parts) {
    /** 생성자에서 방어적 복사 수행. */
    public InitMultipartUploadApiResponse {
        if (parts != null) {
            parts = new ArrayList<>(parts);
        }
    }

    /**
     * Part 정보 API Response.
     *
     * @param partNumber Part 번호 (1-based)
     * @param presignedUrl Presigned URL
     */
    @Schema(description = "Part 정보")
    public record PartInfoApiResponse(
            @Schema(description = "Part 번호 (1-based)", example = "1") int partNumber,
            @Schema(description = "Presigned URL", example = "https://s3.amazonaws.com/...")
                    String presignedUrl) {

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
            Instant expiresAt,
            String bucket,
            String key,
            List<PartInfoApiResponse> parts) {
        return new InitMultipartUploadApiResponse(
                sessionId, uploadId, totalParts, partSize, expiresAt, bucket, key, parts);
    }
}

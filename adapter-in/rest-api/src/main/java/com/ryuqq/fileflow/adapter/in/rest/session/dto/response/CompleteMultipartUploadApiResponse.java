package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Multipart 파일 업로드 완료 API Response.
 *
 * <p>
 * 완료된 세션 정보와 병합된 Part 목록을 반환합니다.
 *
 * @param sessionId 세션 ID
 * @param status 세션 상태 (COMPLETED)
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @param uploadId S3 Multipart Upload ID
 * @param totalParts 전체 Part 개수
 * @param completedParts 완료된 Part 목록
 * @param completedAt 완료 시각 (UTC)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "Multipart 업로드 완료 응답")
public record CompleteMultipartUploadApiResponse(
        @Schema(description = "세션 ID", example = "session-123") String sessionId,
        @Schema(description = "세션 상태", example = "COMPLETED") String status,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") String bucket,
        @Schema(description = "S3 객체 키", example = "uploads/file.jpg") String key,
        @Schema(description = "S3 Multipart Upload ID", example = "upload-456") String uploadId,
        @Schema(description = "전체 Part 개수", example = "5") int totalParts,
        @Schema(description = "완료된 Part 목록") List<CompletedPartInfoApiResponse> completedParts,
        @Schema(description = "완료 시각") Instant completedAt) {
    /**
     * 생성자에서 방어적 복사 수행.
     */
    public CompleteMultipartUploadApiResponse {
        if (completedParts != null) {
            completedParts = new ArrayList<>(completedParts);
        }
    }

    /**
     * 완료된 Part 정보 API Response.
     *
     * @param partNumber Part 번호 (1-based)
     * @param etag Part ETag
     * @param size Part 크기 (바이트)
     * @param uploadedAt 업로드 시각 (UTC)
     */
    @Schema(description = "완료된 Part 정보")
    public record CompletedPartInfoApiResponse(
            @Schema(description = "Part 번호 (1-based)", example = "1") int partNumber,
            @Schema(description = "Part ETag",
                    example = "\"d41d8cd98f00b204e9800998ecf8427e\"") String etag,
            @Schema(description = "Part 크기 (bytes)", example = "5242880") long size,
            @Schema(description = "업로드 시각") Instant uploadedAt) {

        /**
         * 값 기반 생성.
         *
         * @param partNumber Part 번호
         * @param etag Part ETag
         * @param size Part 크기
         * @param uploadedAt 업로드 시각
         * @return CompletedPartInfoApiResponse
         */
        public static CompletedPartInfoApiResponse of(int partNumber, String etag, long size,
                Instant uploadedAt) {
            return new CompletedPartInfoApiResponse(partNumber, etag, size, uploadedAt);
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
     * @return CompleteMultipartUploadApiResponse
     */
    public static CompleteMultipartUploadApiResponse of(String sessionId, String status,
            String bucket, String key, String uploadId, int totalParts,
            List<CompletedPartInfoApiResponse> completedParts, Instant completedAt) {
        return new CompleteMultipartUploadApiResponse(sessionId, status, bucket, key, uploadId,
                totalParts, completedParts, completedAt);
    }
}

package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/**
 * 업로드 세션 상세 API Response.
 *
 * <p>업로드 세션의 상세 정보를 반환합니다. Multipart 세션의 경우 Part 정보를 포함합니다.
 *
 * @param sessionId 세션 ID
 * @param fileName 파일명
 * @param fileSize 파일 크기 (바이트)
 * @param contentType Content-Type
 * @param uploadType 업로드 타입 (SINGLE/MULTIPART)
 * @param status 세션 상태
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @param uploadId S3 Multipart Upload ID (Multipart 전용)
 * @param totalParts 전체 Part 개수 (Multipart 전용)
 * @param uploadedParts 업로드 완료된 Part 개수 (Multipart 전용)
 * @param parts Part 정보 목록 (Multipart 전용)
 * @param etag ETag (완료 시)
 * @param createdAt 생성 시각
 * @param expiresAt 만료 시각
 * @param completedAt 완료 시각
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "업로드 세션 상세 응답")
public record UploadSessionDetailApiResponse(
        @Schema(description = "세션 ID", example = "session-123") String sessionId,
        @Schema(description = "파일명", example = "image.jpg") String fileName,
        @Schema(description = "파일 크기 (bytes)", example = "1024000") long fileSize,
        @Schema(description = "Content-Type", example = "image/jpeg") String contentType,
        @Schema(description = "업로드 타입", example = "MULTIPART") String uploadType,
        @Schema(description = "세션 상태", example = "PENDING") String status,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") String bucket,
        @Schema(description = "S3 객체 키", example = "uploads/file.jpg") String key,
        @Schema(description = "S3 Multipart Upload ID (Multipart 전용)", nullable = true)
                String uploadId,
        @Schema(description = "전체 Part 개수 (Multipart 전용)", nullable = true) Integer totalParts,
        @Schema(description = "업로드 완료된 Part 개수 (Multipart 전용)", nullable = true)
                Integer uploadedParts,
        @Schema(description = "Part 정보 목록 (Multipart 전용)", nullable = true)
                List<PartDetailApiResponse> parts,
        @Schema(description = "ETag (완료 시)", nullable = true) String etag,
        @Schema(description = "생성 시각") Instant createdAt,
        @Schema(description = "만료 시각") Instant expiresAt,
        @Schema(description = "완료 시각", nullable = true) Instant completedAt) {

    /**
     * Part 상세 정보.
     *
     * @param partNumber Part 번호 (1-based)
     * @param etag ETag
     * @param size Part 크기 (바이트)
     * @param uploadedAt 업로드 완료 시각
     */
    @Schema(description = "Part 상세 정보")
    public record PartDetailApiResponse(
            @Schema(description = "Part 번호 (1-based)", example = "1") int partNumber,
            @Schema(description = "ETag", example = "\"d41d8cd98f00b204e9800998ecf8427e\"")
                    String etag,
            @Schema(description = "Part 크기 (bytes)", example = "5242880") long size,
            @Schema(description = "업로드 완료 시각") Instant uploadedAt) {

        /**
         * 값 기반 생성.
         *
         * @param partNumber Part 번호
         * @param etag ETag
         * @param size Part 크기
         * @param uploadedAt 업로드 완료 시각
         * @return PartDetailApiResponse
         */
        public static PartDetailApiResponse of(
                int partNumber, String etag, long size, Instant uploadedAt) {
            return new PartDetailApiResponse(partNumber, etag, size, uploadedAt);
        }
    }

    /**
     * 단일 업로드 세션용 생성.
     *
     * @param sessionId 세션 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content-Type
     * @param status 세션 상태
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @param etag ETag
     * @param createdAt 생성 시각
     * @param expiresAt 만료 시각
     * @param completedAt 완료 시각
     * @return UploadSessionDetailApiResponse
     */
    public static UploadSessionDetailApiResponse ofSingle(
            String sessionId,
            String fileName,
            long fileSize,
            String contentType,
            String status,
            String bucket,
            String key,
            String etag,
            Instant createdAt,
            Instant expiresAt,
            Instant completedAt) {
        return new UploadSessionDetailApiResponse(
                sessionId,
                fileName,
                fileSize,
                contentType,
                "SINGLE",
                status,
                bucket,
                key,
                null,
                null,
                null,
                null,
                etag,
                createdAt,
                expiresAt,
                completedAt);
    }

    /**
     * Multipart 업로드 세션용 생성.
     *
     * @param sessionId 세션 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content-Type
     * @param status 세션 상태
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @param uploadId S3 Upload ID
     * @param totalParts 전체 Part 개수
     * @param uploadedParts 업로드 완료된 Part 개수
     * @param parts Part 정보 목록
     * @param etag ETag
     * @param createdAt 생성 시각
     * @param expiresAt 만료 시각
     * @param completedAt 완료 시각
     * @return UploadSessionDetailApiResponse
     */
    public static UploadSessionDetailApiResponse ofMultipart(
            String sessionId,
            String fileName,
            long fileSize,
            String contentType,
            String status,
            String bucket,
            String key,
            String uploadId,
            int totalParts,
            int uploadedParts,
            List<PartDetailApiResponse> parts,
            String etag,
            Instant createdAt,
            Instant expiresAt,
            Instant completedAt) {
        return new UploadSessionDetailApiResponse(
                sessionId,
                fileName,
                fileSize,
                contentType,
                "MULTIPART",
                status,
                bucket,
                key,
                uploadId,
                totalParts,
                uploadedParts,
                parts,
                etag,
                createdAt,
                expiresAt,
                completedAt);
    }
}

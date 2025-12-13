package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * 업로드 세션 API Response.
 *
 * <p>업로드 세션의 기본 정보를 반환합니다.
 *
 * @param sessionId 세션 ID
 * @param fileName 파일명
 * @param fileSize 파일 크기 (바이트)
 * @param contentType Content-Type
 * @param uploadType 업로드 타입 (SINGLE/MULTIPART)
 * @param status 세션 상태
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @param createdAt 생성 시각
 * @param expiresAt 만료 시각
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "업로드 세션 응답")
public record UploadSessionApiResponse(
        @Schema(description = "세션 ID", example = "session-123") String sessionId,
        @Schema(description = "파일명", example = "image.jpg") String fileName,
        @Schema(description = "파일 크기 (bytes)", example = "1024000") long fileSize,
        @Schema(description = "Content-Type", example = "image/jpeg") String contentType,
        @Schema(description = "업로드 타입", example = "SINGLE") String uploadType,
        @Schema(description = "세션 상태", example = "PENDING") String status,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") String bucket,
        @Schema(description = "S3 객체 키", example = "uploads/file.jpg") String key,
        @Schema(description = "생성 시각") Instant createdAt,
        @Schema(description = "만료 시각") Instant expiresAt) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content-Type
     * @param uploadType 업로드 타입
     * @param status 세션 상태
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @param createdAt 생성 시각
     * @param expiresAt 만료 시각
     * @return UploadSessionApiResponse
     */
    public static UploadSessionApiResponse of(
            String sessionId,
            String fileName,
            long fileSize,
            String contentType,
            String uploadType,
            String status,
            String bucket,
            String key,
            Instant createdAt,
            Instant expiresAt) {
        return new UploadSessionApiResponse(
                sessionId,
                fileName,
                fileSize,
                contentType,
                uploadType,
                status,
                bucket,
                key,
                createdAt,
                expiresAt);
    }
}

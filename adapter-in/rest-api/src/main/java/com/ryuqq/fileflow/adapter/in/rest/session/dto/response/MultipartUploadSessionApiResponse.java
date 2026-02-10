package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * MultipartUploadSessionApiResponse - 멀티파트 업로드 세션 API 응답.
 *
 * <p>API-DTO-001: Record 타입 필수.
 *
 * <p>API-DTO-005: 날짜 String 변환 필수 (Instant 타입 사용 금지).
 */
@Schema(description = "멀티파트 업로드 세션 응답")
public record MultipartUploadSessionApiResponse(
        @Schema(description = "세션 ID", example = "sess_abc123") String sessionId,
        @Schema(description = "S3 멀티파트 업로드 ID") String uploadId,
        @Schema(description = "S3 객체 키", example = "public/2026/01/video.mp4") String s3Key,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") String bucket,
        @Schema(description = "접근 유형", example = "PUBLIC") String accessType,
        @Schema(description = "원본 파일명", example = "large-video.mp4") String fileName,
        @Schema(description = "MIME 타입", example = "video/mp4") String contentType,
        @Schema(description = "파트 크기 (bytes)", example = "5242880") long partSize,
        @Schema(description = "세션 상태", example = "INITIATED") String status,
        @Schema(description = "완료된 파트 수", example = "3") int completedPartCount,
        @Schema(description = "완료된 파트 목록") List<CompletedPartApiResponse> completedParts,
        @Schema(description = "만료 시각 (ISO 8601)", example = "2026-01-23T10:30:00+09:00")
                String expiresAt,
        @Schema(description = "생성 시각 (ISO 8601)", example = "2026-01-23T09:30:00+09:00")
                String createdAt) {

    @Schema(description = "완료된 파트 정보")
    public record CompletedPartApiResponse(
            @Schema(description = "파트 번호", example = "1") int partNumber,
            @Schema(description = "파트 ETag", example = "\"d41d8cd98f00b204e9800998ecf8427e\"")
                    String etag,
            @Schema(description = "파트 크기 (bytes)", example = "5242880") long size) {}
}

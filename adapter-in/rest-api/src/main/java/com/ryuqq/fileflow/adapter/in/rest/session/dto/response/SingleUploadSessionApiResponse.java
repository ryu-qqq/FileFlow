package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * SingleUploadSessionApiResponse - 단건 업로드 세션 API 응답.
 *
 * <p>API-DTO-001: Record 타입 필수.
 *
 * <p>API-DTO-005: 날짜 String 변환 필수 (Instant 타입 사용 금지).
 */
@Schema(description = "단건 업로드 세션 응답")
public record SingleUploadSessionApiResponse(
        @Schema(description = "세션 ID", example = "sess_abc123") String sessionId,
        @Schema(description = "Presigned Upload URL") String presignedUrl,
        @Schema(description = "S3 객체 키", example = "public/2026/01/file.jpg") String s3Key,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") String bucket,
        @Schema(description = "접근 유형", example = "PUBLIC") String accessType,
        @Schema(description = "원본 파일명", example = "product-image.jpg") String fileName,
        @Schema(description = "MIME 타입", example = "image/jpeg") String contentType,
        @Schema(description = "세션 상태", example = "INITIATED") String status,
        @Schema(description = "만료 시각 (ISO 8601)", example = "2026-01-23T10:30:00+09:00")
                String expiresAt,
        @Schema(description = "생성 시각 (ISO 8601)", example = "2026-01-23T09:30:00+09:00")
                String createdAt) {}

package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AssetApiResponse - Asset API 응답.
 *
 * <p>API-DTO-001: Record 타입 필수.
 *
 * <p>API-DTO-005: 날짜 String 변환 필수 (Instant 타입 사용 금지).
 */
@Schema(description = "Asset 응답")
public record AssetApiResponse(
        @Schema(description = "Asset ID", example = "asset_abc123") String assetId,
        @Schema(description = "S3 객체 키", example = "public/2026/01/image.jpg") String s3Key,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") String bucket,
        @Schema(description = "접근 유형", example = "PUBLIC") String accessType,
        @Schema(description = "파일명", example = "product-image.jpg") String fileName,
        @Schema(description = "파일 크기 (bytes)", example = "1048576") long fileSize,
        @Schema(description = "MIME 타입", example = "image/jpeg") String contentType,
        @Schema(description = "ETag", example = "\"d41d8cd98f00b204e9800998ecf8427e\"") String etag,
        @Schema(description = "파일 확장자", example = "jpg") String extension,
        @Schema(description = "생성 경로", example = "SINGLE_UPLOAD") String origin,
        @Schema(description = "생성 경로 원본 ID", example = "sess_abc123") String originId,
        @Schema(description = "파일 용도", example = "PRODUCT_IMAGE") String purpose,
        @Schema(description = "요청 서비스명", example = "commerce-api") String source,
        @Schema(description = "생성 시각 (ISO 8601)", example = "2026-01-23T09:30:00+09:00")
                String createdAt) {}

package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AssetMetadataApiResponse - Asset 메타데이터 API 응답.
 *
 * <p>API-DTO-001: Record 타입 필수.
 *
 * <p>API-DTO-005: 날짜 String 변환 필수 (Instant 타입 사용 금지).
 */
@Schema(description = "Asset 메타데이터 응답")
public record AssetMetadataApiResponse(
        @Schema(description = "메타데이터 ID", example = "meta_abc123") String metadataId,
        @Schema(description = "Asset ID", example = "asset_abc123") String assetId,
        @Schema(description = "이미지 너비 (px)", example = "1920") int width,
        @Schema(description = "이미지 높이 (px)", example = "1080") int height,
        @Schema(description = "변환 유형", example = "ORIGINAL") String transformType,
        @Schema(description = "생성 시각 (ISO 8601)", example = "2026-01-23T09:30:00+09:00")
                String createdAt) {}

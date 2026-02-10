package com.ryuqq.fileflow.adapter.in.rest.transform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * TransformRequestApiResponse - 이미지 변환 요청 API 응답.
 *
 * <p>API-DTO-001: Record 타입 필수.
 *
 * <p>API-DTO-005: 날짜 String 변환 필수 (Instant 타입 사용 금지).
 */
@Schema(description = "이미지 변환 요청 응답")
public record TransformRequestApiResponse(
        @Schema(description = "변환 요청 ID", example = "tr_abc123") String transformRequestId,
        @Schema(description = "원본 Asset ID", example = "asset_abc123") String sourceAssetId,
        @Schema(description = "원본 Content-Type", example = "image/jpeg") String sourceContentType,
        @Schema(description = "변환 유형", example = "RESIZE") String transformType,
        @Schema(description = "목표 너비 (px)", example = "800", nullable = true) Integer width,
        @Schema(description = "목표 높이 (px)", example = "600", nullable = true) Integer height,
        @Schema(description = "품질 (1-100)", example = "85", nullable = true) Integer quality,
        @Schema(description = "변환 대상 포맷", example = "webp", nullable = true) String targetFormat,
        @Schema(description = "작업 상태", example = "PENDING") String status,
        @Schema(description = "결과 Asset ID", nullable = true) String resultAssetId,
        @Schema(description = "마지막 에러 메시지", nullable = true) String lastError,
        @Schema(description = "생성 시각 (ISO 8601)", example = "2026-01-23T09:30:00+09:00")
                String createdAt,
        @Schema(description = "완료 시각 (ISO 8601)", nullable = true) String completedAt) {}

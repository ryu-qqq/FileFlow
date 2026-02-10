package com.ryuqq.fileflow.adapter.in.rest.transform.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이미지 변환 요청 생성")
public record CreateTransformRequestApiRequest(
        @Schema(description = "원본 Asset ID", example = "asset_abc123") @NotBlank
                String sourceAssetId,
        @Schema(description = "변환 유형", example = "RESIZE") @NotBlank String transformType,
        @Schema(description = "목표 너비 (px)", example = "800", nullable = true) Integer width,
        @Schema(description = "목표 높이 (px)", example = "600", nullable = true) Integer height,
        @Schema(description = "품질 (1-100)", example = "85", nullable = true) Integer quality,
        @Schema(description = "변환 대상 포맷", example = "webp", nullable = true) String targetFormat) {}

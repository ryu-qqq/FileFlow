package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * PresignedPartUrlApiResponse - 멀티파트 파트별 Presigned URL API 응답.
 *
 * <p>API-DTO-001: Record 타입 필수.
 */
@Schema(description = "멀티파트 파트별 Presigned URL 응답")
public record PresignedPartUrlApiResponse(
        @Schema(description = "파트 업로드용 Presigned URL") String presignedUrl,
        @Schema(description = "파트 번호", example = "1") int partNumber,
        @Schema(description = "Presigned URL 만료까지 남은 시간 (초)", example = "3600")
                long expiresInSeconds) {}

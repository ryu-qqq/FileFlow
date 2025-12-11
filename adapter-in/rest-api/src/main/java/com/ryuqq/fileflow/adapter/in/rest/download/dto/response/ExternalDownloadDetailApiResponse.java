package com.ryuqq.fileflow.adapter.in.rest.download.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * 외부 다운로드 상세 API Response.
 *
 * @param id ExternalDownload ID
 * @param sourceUrl 외부 이미지 URL
 * @param status 현재 상태
 * @param fileAssetId 생성된 FileAsset ID (UUID String, nullable)
 * @param errorMessage 에러 메시지 (nullable)
 * @param retryCount 재시도 횟수
 * @param webhookUrl 콜백 URL (nullable)
 * @param createdAt 생성 시간
 * @param updatedAt 수정 시간
 */
@Schema(description = "외부 다운로드 상세 응답")
public record ExternalDownloadDetailApiResponse(
        @Schema(description = "외부 다운로드 ID", example = "download-123") String id,
        @Schema(description = "외부 이미지 URL", example = "https://example.com/image.jpg") String sourceUrl,
        @Schema(description = "현재 상태", example = "COMPLETED") String status,
        @Schema(description = "생성된 FileAsset ID", example = "asset-456", nullable = true) String fileAssetId,
        @Schema(description = "에러 메시지", nullable = true) String errorMessage,
        @Schema(description = "재시도 횟수", example = "0") int retryCount,
        @Schema(description = "콜백 URL", nullable = true) String webhookUrl,
        @Schema(description = "생성 시간") Instant createdAt,
        @Schema(description = "수정 시간") Instant updatedAt) {}

package com.ryuqq.fileflow.adapter.in.rest.download.dto.response;

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
public record ExternalDownloadDetailApiResponse(
        String id,
        String sourceUrl,
        String status,
        String fileAssetId,
        String errorMessage,
        int retryCount,
        String webhookUrl,
        Instant createdAt,
        Instant updatedAt) {}

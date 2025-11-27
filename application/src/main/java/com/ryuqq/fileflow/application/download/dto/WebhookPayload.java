package com.ryuqq.fileflow.application.download.dto;

import java.time.Instant;

/**
 * Webhook 페이로드 DTO.
 *
 * @param externalDownloadId ExternalDownload ID
 * @param status 처리 상태 (COMPLETED/FAILED)
 * @param fileAssetId 생성된 FileAsset ID (nullable)
 * @param errorMessage 에러 메시지 (nullable)
 * @param completedAt 완료 시간
 */
public record WebhookPayload(
        Long externalDownloadId,
        String status,
        Long fileAssetId,
        String errorMessage,
        Instant completedAt) {}

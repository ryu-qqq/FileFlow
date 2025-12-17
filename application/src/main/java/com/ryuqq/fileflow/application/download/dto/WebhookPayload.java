package com.ryuqq.fileflow.application.download.dto;

import java.time.Instant;

/**
 * Webhook 페이로드 DTO.
 *
 * <p>ExternalDownload 처리 완료 후 클라이언트 Webhook으로 전송되는 데이터입니다.
 *
 * <p><strong>JSON 예시 (성공)</strong>:
 *
 * <pre>{@code
 * {
 *   "externalDownloadId": "01939c2a-1234-7000-8000-abcdef123456",
 *   "status": "COMPLETED",
 *   "fileAssetId": "01939c2b-5678-7000-8000-abcdef654321",
 *   "fileUrl": "https://cdn.set-of.com/uploads/2025/12/17/abc123.jpg",
 *   "errorMessage": null,
 *   "completedAt": "2025-12-17T10:30:00Z"
 * }
 * }</pre>
 *
 * <p><strong>JSON 예시 (실패)</strong>:
 *
 * <pre>{@code
 * {
 *   "externalDownloadId": "01939c2a-1234-7000-8000-abcdef123456",
 *   "status": "FAILED",
 *   "fileAssetId": null,
 *   "fileUrl": null,
 *   "errorMessage": "Connection timeout",
 *   "completedAt": "2025-12-17T10:30:00Z"
 * }
 * }</pre>
 *
 * @param externalDownloadId ExternalDownload ID (UUID 문자열)
 * @param status 처리 상태 (COMPLETED/FAILED)
 * @param fileAssetId 생성된 FileAsset ID (UUID 문자열, nullable)
 * @param fileUrl CDN 파일 URL (성공 시, nullable)
 * @param errorMessage 에러 메시지 (nullable)
 * @param completedAt 완료 시간
 */
public record WebhookPayload(
        String externalDownloadId,
        String status,
        String fileAssetId,
        String fileUrl,
        String errorMessage,
        Instant completedAt) {}

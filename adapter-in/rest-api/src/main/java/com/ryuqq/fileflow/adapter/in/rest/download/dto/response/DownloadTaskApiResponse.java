package com.ryuqq.fileflow.adapter.in.rest.download.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DownloadTaskApiResponse - 다운로드 작업 API 응답.
 *
 * <p>API-DTO-001: Record 타입 필수.
 *
 * <p>API-DTO-005: 날짜 String 변환 필수 (Instant 타입 사용 금지).
 */
@Schema(description = "다운로드 작업 응답")
public record DownloadTaskApiResponse(
        @Schema(description = "다운로드 작업 ID", example = "dt_abc123") String downloadTaskId,
        @Schema(description = "다운로드 소스 URL", example = "https://example.com/files/image.jpg")
                String sourceUrl,
        @Schema(description = "S3 객체 키", example = "downloads/2026/02/image.jpg") String s3Key,
        @Schema(description = "S3 버킷명", example = "fileflow-bucket") String bucket,
        @Schema(description = "접근 유형", example = "PUBLIC") String accessType,
        @Schema(description = "파일 용도", example = "PRODUCT_IMAGE") String purpose,
        @Schema(description = "요청 서비스명", example = "commerce-api") String source,
        @Schema(description = "작업 상태", example = "PENDING") String status,
        @Schema(description = "재시도 횟수", example = "0") int retryCount,
        @Schema(description = "최대 재시도 횟수", example = "3") int maxRetries,
        @Schema(
                        description = "완료 콜백 URL",
                        example = "https://commerce-api.internal/callbacks/download")
                String callbackUrl,
        @Schema(description = "마지막 에러 메시지", nullable = true) String lastError,
        @Schema(description = "생성 시각 (ISO 8601)", example = "2026-01-23T09:30:00+09:00")
                String createdAt,
        @Schema(description = "시작 시각 (ISO 8601)", nullable = true) String startedAt,
        @Schema(description = "완료 시각 (ISO 8601)", nullable = true) String completedAt) {}

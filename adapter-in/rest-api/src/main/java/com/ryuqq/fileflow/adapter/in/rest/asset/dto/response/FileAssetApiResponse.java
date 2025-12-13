package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * FileAsset API Response DTO.
 *
 * @param id 파일 자산 ID
 * @param sessionId 업로드 세션 ID
 * @param fileName 파일명
 * @param fileSize 파일 크기
 * @param contentType 컨텐츠 타입
 * @param category 파일 카테고리
 * @param bucket S3 버킷
 * @param s3Key S3 키
 * @param etag ETag
 * @param status 상태
 * @param createdAt 생성 시각
 * @param processedAt 처리 완료 시각
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "파일 자산 응답")
public record FileAssetApiResponse(
        @Schema(description = "파일 자산 ID", example = "asset-123") String id,
        @Schema(description = "업로드 세션 ID", example = "session-456") String sessionId,
        @Schema(description = "파일명", example = "image.jpg") String fileName,
        @Schema(description = "파일 크기 (bytes)", example = "1024000") long fileSize,
        @Schema(description = "컨텐츠 타입", example = "image/jpeg") String contentType,
        @Schema(description = "파일 카테고리", example = "IMAGE") String category,
        @Schema(description = "S3 버킷", example = "fileflow-bucket") String bucket,
        @Schema(description = "S3 키", example = "uploads/image.jpg") String s3Key,
        @Schema(description = "ETag", example = "abc123") String etag,
        @Schema(description = "상태", example = "COMPLETED") String status,
        @Schema(description = "생성 시각") Instant createdAt,
        @Schema(description = "처리 완료 시각") Instant processedAt) {}

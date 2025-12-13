package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Presigned Download URL 생성 응답 DTO.
 *
 * <p>생성된 S3 Presigned URL 정보를 반환합니다.
 *
 * @param fileAssetId 파일 자산 ID
 * @param downloadUrl Presigned Download URL
 * @param fileName 파일명
 * @param contentType 컨텐츠 타입
 * @param fileSize 파일 크기 (bytes)
 * @param expiresAt URL 만료 시각
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "다운로드 URL 응답")
public record DownloadUrlApiResponse(
        @Schema(description = "파일 자산 ID", example = "asset-123") String fileAssetId,
        @Schema(description = "다운로드 URL", example = "https://s3.amazonaws.com/...")
                String downloadUrl,
        @Schema(description = "파일명", example = "image.jpg") String fileName,
        @Schema(description = "컨텐츠 타입", example = "image/jpeg") String contentType,
        @Schema(description = "파일 크기 (bytes)", example = "1024000") long fileSize,
        @Schema(description = "URL 만료 시각") Instant expiresAt) {

    /**
     * 값 기반 생성.
     *
     * @param fileAssetId 파일 자산 ID
     * @param downloadUrl Presigned Download URL
     * @param fileName 파일명
     * @param contentType 컨텐츠 타입
     * @param fileSize 파일 크기
     * @param expiresAt URL 만료 시각
     * @return DownloadUrlApiResponse
     */
    public static DownloadUrlApiResponse of(
            String fileAssetId,
            String downloadUrl,
            String fileName,
            String contentType,
            long fileSize,
            Instant expiresAt) {
        return new DownloadUrlApiResponse(
                fileAssetId, downloadUrl, fileName, contentType, fileSize, expiresAt);
    }
}

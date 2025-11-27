package com.ryuqq.fileflow.application.asset.dto.response;

import java.time.LocalDateTime;

/**
 * Presigned Download URL 생성 Response.
 *
 * <p>생성된 S3 Presigned URL 정보를 반환합니다.
 *
 * @param fileAssetId 파일 자산 ID
 * @param downloadUrl Presigned Download URL
 * @param fileName 파일명
 * @param contentType 컨텐츠 타입
 * @param fileSize 파일 크기 (bytes)
 * @param expiresAt URL 만료 시각
 */
public record DownloadUrlResponse(
        String fileAssetId,
        String downloadUrl,
        String fileName,
        String contentType,
        long fileSize,
        LocalDateTime expiresAt) {

    /**
     * 값 기반 생성.
     *
     * @param fileAssetId 파일 자산 ID
     * @param downloadUrl Presigned Download URL
     * @param fileName 파일명
     * @param contentType 컨텐츠 타입
     * @param fileSize 파일 크기
     * @param expiresAt URL 만료 시각
     * @return DownloadUrlResponse
     */
    public static DownloadUrlResponse of(
            String fileAssetId,
            String downloadUrl,
            String fileName,
            String contentType,
            long fileSize,
            LocalDateTime expiresAt) {
        return new DownloadUrlResponse(
                fileAssetId, downloadUrl, fileName, contentType, fileSize, expiresAt);
    }
}

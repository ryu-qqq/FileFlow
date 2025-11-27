package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import java.time.LocalDateTime;

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
public record DownloadUrlApiResponse(
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
     * @return DownloadUrlApiResponse
     */
    public static DownloadUrlApiResponse of(
            String fileAssetId,
            String downloadUrl,
            String fileName,
            String contentType,
            long fileSize,
            LocalDateTime expiresAt) {
        return new DownloadUrlApiResponse(
                fileAssetId, downloadUrl, fileName, contentType, fileSize, expiresAt);
    }
}

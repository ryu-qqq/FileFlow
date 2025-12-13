package com.ryuqq.fileflow.application.asset.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * N8N 워크플로우용 FileAsset 응답 DTO.
 *
 * <p>N8N 워크플로우에서 처리할 FileAsset 정보를 담습니다.
 *
 * @param fileAssetId FileAsset ID
 * @param fileName 파일명
 * @param fileSize 파일 크기 (bytes)
 * @param contentType 컨텐츠 타입 (MIME)
 * @param category 파일 카테고리 (IMAGE, HTML, DOCUMENT 등)
 * @param bucket S3 버킷명
 * @param s3Key S3 키
 * @param status 현재 상태
 * @param processedFiles 처리된 파일 목록 (리사이징된 이미지 등)
 * @param createdAt 생성 시각
 */
public record FileAssetForN8nResponse(
        String fileAssetId,
        String fileName,
        long fileSize,
        String contentType,
        String category,
        String bucket,
        String s3Key,
        String status,
        List<ProcessedFileInfoResponse> processedFiles,
        Instant createdAt) {
    /** Compact Constructor: 불변 리스트 변환. */
    public FileAssetForN8nResponse {
        processedFiles = (processedFiles != null) ? List.copyOf(processedFiles) : List.of();
    }
}

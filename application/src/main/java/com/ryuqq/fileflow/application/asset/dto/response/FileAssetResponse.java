package com.ryuqq.fileflow.application.asset.dto.response;

import java.time.Instant;

/**
 * FileAsset 응답 DTO.
 *
 * <p>REST API Layer와의 결합도를 낮추기 위해 String 기반으로 설계. Domain Enum → String 변환은 Application Layer 내부에서
 * 처리.
 *
 * @param id 파일 자산 ID
 * @param sessionId 업로드 세션 ID
 * @param fileName 파일명
 * @param fileSize 파일 크기
 * @param contentType 컨텐츠 타입
 * @param category 파일 카테고리 (enum name as String)
 * @param bucket S3 버킷
 * @param s3Key S3 키
 * @param etag ETag
 * @param status 상태 (enum name as String)
 * @param createdAt 생성 시각
 * @param processedAt 처리 완료 시각
 * @param lastErrorMessage 마지막 에러 메시지 (실패 시)
 */
public record FileAssetResponse(
        String id,
        String sessionId,
        String fileName,
        long fileSize,
        String contentType,
        String category,
        String bucket,
        String s3Key,
        String etag,
        String status,
        Instant createdAt,
        Instant processedAt,
        String lastErrorMessage) {

    public static FileAssetResponse of(
            String id,
            String sessionId,
            String fileName,
            long fileSize,
            String contentType,
            String category,
            String bucket,
            String s3Key,
            String etag,
            String status,
            Instant createdAt,
            Instant processedAt,
            String lastErrorMessage) {
        return new FileAssetResponse(
                id,
                sessionId,
                fileName,
                fileSize,
                contentType,
                category,
                bucket,
                s3Key,
                etag,
                status,
                createdAt,
                processedAt,
                lastErrorMessage);
    }
}

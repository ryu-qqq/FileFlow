package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import java.time.LocalDateTime;

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
public record FileAssetApiResponse(
        String id,
        String sessionId,
        String fileName,
        long fileSize,
        String contentType,
        FileCategory category,
        String bucket,
        String s3Key,
        String etag,
        FileAssetStatus status,
        LocalDateTime createdAt,
        LocalDateTime processedAt) {}

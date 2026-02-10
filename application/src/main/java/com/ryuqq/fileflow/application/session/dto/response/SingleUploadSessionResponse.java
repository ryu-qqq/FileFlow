package com.ryuqq.fileflow.application.session.dto.response;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;

/**
 * 단건 업로드 세션 응답
 *
 * @param sessionId 세션 ID
 * @param presignedUrl Presigned Upload URL
 * @param s3Key S3 객체 키
 * @param bucket S3 버킷명
 * @param accessType 접근 유형
 * @param fileName 원본 파일명
 * @param contentType MIME 타입
 * @param status 세션 상태
 * @param expiresAt 만료 시각
 * @param createdAt 생성 시각
 */
public record SingleUploadSessionResponse(
        String sessionId,
        String presignedUrl,
        String s3Key,
        String bucket,
        AccessType accessType,
        String fileName,
        String contentType,
        String status,
        Instant expiresAt,
        Instant createdAt) {}

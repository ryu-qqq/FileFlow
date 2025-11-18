package com.ryuqq.fileflow.application.session.dto.response;

/**
 * Presigned URL 발급 Response
 * <p>
 * S3 직접 업로드를 위한 Presigned URL 정보를 반환합니다.
 * </p>
 *
 * @param sessionId 세션 ID (멱등키)
 * @param fileId 파일 ID
 * @param presignedUrl S3 Presigned PUT URL
 * @param expiresIn 만료 시간 (초) - 300초 (5분)
 * @param uploadType 업로드 타입 (SINGLE | MULTIPART)
 */
public record PresignedUrlResponse(
    String sessionId,
    String fileId,
    String presignedUrl,
    int expiresIn,
    String uploadType
) {}

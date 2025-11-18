package com.ryuqq.fileflow.application.session.dto.response;

import java.time.LocalDateTime;

/**
 * 파일 Response
 * <p>
 * 업로드 완료된 파일 정보를 반환합니다.
 * </p>
 *
 * @param sessionId 세션 ID
 * @param fileId 파일 ID
 * @param fileName 파일명
 * @param fileSize 파일 크기 (bytes)
 * @param mimeType MIME 타입
 * @param status 파일 상태 (COMPLETED | PROCESSING | FAILED)
 * @param s3Key S3 Object Key
 * @param s3Bucket S3 버킷명
 * @param createdAt 생성 시각
 */
public record FileResponse(
    String sessionId,
    String fileId,
    String fileName,
    Long fileSize,
    String mimeType,
    String status,
    String s3Key,
    String s3Bucket,
    LocalDateTime createdAt
) {}

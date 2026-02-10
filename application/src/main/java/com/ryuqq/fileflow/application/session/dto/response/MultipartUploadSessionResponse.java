package com.ryuqq.fileflow.application.session.dto.response;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;
import java.util.List;

/**
 * 멀티파트 업로드 세션 응답
 *
 * @param sessionId 세션 ID
 * @param uploadId S3 멀티파트 업로드 ID
 * @param s3Key S3 객체 키
 * @param bucket S3 버킷명
 * @param accessType 접근 유형
 * @param fileName 원본 파일명
 * @param contentType MIME 타입
 * @param partSize 파트 크기 (bytes)
 * @param status 세션 상태
 * @param completedPartCount 완료된 파트 수
 * @param completedParts 완료된 파트 목록
 * @param expiresAt 만료 시각
 * @param createdAt 생성 시각
 */
public record MultipartUploadSessionResponse(
        String sessionId,
        String uploadId,
        String s3Key,
        String bucket,
        AccessType accessType,
        String fileName,
        String contentType,
        long partSize,
        String status,
        int completedPartCount,
        List<CompletedPartResponse> completedParts,
        Instant expiresAt,
        Instant createdAt) {

    public record CompletedPartResponse(int partNumber, String etag, long size) {}
}

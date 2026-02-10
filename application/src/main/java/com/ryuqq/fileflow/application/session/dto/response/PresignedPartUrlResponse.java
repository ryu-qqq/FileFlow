package com.ryuqq.fileflow.application.session.dto.response;

/**
 * 멀티파트 파트별 Presigned URL 응답
 *
 * @param presignedUrl 파트 업로드용 Presigned URL
 * @param partNumber 파트 번호
 * @param expiresInSeconds Presigned URL 만료까지 남은 시간 (초)
 */
public record PresignedPartUrlResponse(
        String presignedUrl, int partNumber, long expiresInSeconds) {}

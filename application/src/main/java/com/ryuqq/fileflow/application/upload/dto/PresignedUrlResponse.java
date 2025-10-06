package com.ryuqq.fileflow.application.upload.dto;

import com.ryuqq.fileflow.domain.upload.model.PresignedUrlInfo;

import java.time.LocalDateTime;

/**
 * Presigned URL 응답 DTO
 *
 * Presigned URL 정보를 클라이언트에 전달하기 위한 응답 객체입니다.
 *
 * @param presignedUrl Presigned URL
 * @param uploadPath 업로드 경로
 * @param expiresAt 만료 시간
 * @author sangwon-ryu
 */
public record PresignedUrlResponse(
        String presignedUrl,
        String uploadPath,
        LocalDateTime expiresAt
) {
    /**
     * PresignedUrlInfo 도메인 객체로부터 Response DTO를 생성합니다.
     *
     * @param urlInfo Presigned URL 정보
     * @return PresignedUrlResponse
     */
    public static PresignedUrlResponse from(PresignedUrlInfo urlInfo) {
        return new PresignedUrlResponse(
                urlInfo.presignedUrl(),
                urlInfo.uploadPath(),
                urlInfo.expiresAt()
        );
    }
}

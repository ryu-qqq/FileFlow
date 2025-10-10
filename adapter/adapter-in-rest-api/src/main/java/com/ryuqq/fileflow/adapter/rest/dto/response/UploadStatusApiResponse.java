package com.ryuqq.fileflow.adapter.rest.dto.response;

import com.ryuqq.fileflow.application.upload.dto.UploadStatusResponse;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;

import java.time.LocalDateTime;

/**
 * Upload Status API Response DTO
 *
 * 업로드 진행률 조회 결과를 전달하는 응답 DTO
 *
 * @param sessionId 세션 ID
 * @param status 세션 상태
 * @param progress 진행률 (0-100%)
 * @param totalBytes 전체 파일 크기 (bytes)
 * @param createdAt 생성 시간
 * @param expiresAt 만료 시간
 * @param isExpired 만료 여부
 * @author sangwon-ryu
 */
public record UploadStatusApiResponse(
        String sessionId,
        UploadStatus status,
        int progress,
        long totalBytes,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        boolean isExpired
) {
    /**
     * Application Response를 REST Response로 변환합니다.
     *
     * @param uploadStatusResponse Application 계층의 응답
     * @return UploadStatusApiResponse
     */
    public static UploadStatusApiResponse from(UploadStatusResponse uploadStatusResponse) {
        return new UploadStatusApiResponse(
                uploadStatusResponse.sessionId(),
                uploadStatusResponse.status(),
                uploadStatusResponse.progress(),
                uploadStatusResponse.totalBytes(),
                uploadStatusResponse.createdAt(),
                uploadStatusResponse.expiresAt(),
                uploadStatusResponse.isExpired()
        );
    }
}

package com.ryuqq.fileflow.application.upload.dto;

import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;

import java.time.LocalDateTime;

/**
 * 업로드 진행률 조회 응답 DTO
 *
 * 업로드 세션의 진행 상태를 클라이언트에 전달하기 위한 응답 객체입니다.
 * 실시간 업로드 바이트 추적이 불가능한 Presigned URL 직접 업로드 방식에서
 * 상태 기반의 진행률 정보를 제공합니다.
 *
 * @param sessionId 세션 ID
 * @param status 세션 상태 (PENDING, UPLOADING, COMPLETED, FAILED, CANCELLED)
 * @param progress 진행률 (0-100%)
 * @param totalBytes 전체 파일 크기 (bytes)
 * @param createdAt 생성 시간
 * @param expiresAt 만료 시간
 * @param isExpired 만료 여부
 * @author sangwon-ryu
 */
public record UploadStatusResponse(
        String sessionId,
        UploadStatus status,
        int progress,
        long totalBytes,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        boolean isExpired
) {
    /**
     * UploadSession 도메인 객체로부터 Response DTO를 생성합니다.
     *
     * 진행률은 상태 기반으로 계산됩니다:
     * - PENDING: 0%
     * - UPLOADING: 50%
     * - COMPLETED: 100%
     * - FAILED/CANCELLED: 0%
     *
     * @param session 업로드 세션 도메인 객체
     * @return UploadStatusResponse
     */
    public static UploadStatusResponse from(UploadSession session) {
        int calculatedProgress = calculateProgress(session.getStatus());

        return new UploadStatusResponse(
                session.getSessionId(),
                session.getStatus(),
                calculatedProgress,
                session.getUploadRequest().fileSizeBytes(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.isExpired()
        );
    }

    /**
     * 업로드 상태 기반으로 진행률을 계산합니다.
     *
     * S3 Presigned URL 직접 업로드 방식에서는 실시간 업로드 바이트 추적이 불가능하므로,
     * 상태 기반의 간단한 진행률 계산을 제공합니다.
     *
     * @param status 업로드 상태
     * @return 진행률 (0-100)
     */
    private static int calculateProgress(UploadStatus status) {
        return switch (status) {
            case PENDING -> 0;
            case UPLOADING -> 50;
            case COMPLETED -> 100;
            case FAILED, CANCELLED -> 0;
        };
    }
}

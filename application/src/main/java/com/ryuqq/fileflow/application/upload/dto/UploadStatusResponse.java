package com.ryuqq.fileflow.application.upload.dto;

import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;

import java.time.LocalDateTime;

/**
 * 업로드 진행률 조회 응답 DTO
 *
 * 업로드 세션의 진행 상태를 클라이언트에 전달하기 위한 응답 객체입니다.
 *
 * 진행률 계산 방식:
 * - 멀티파트 업로드: Redis 기반 실시간 진행률 (완료된 파트 수 / 전체 파트 수)
 * - 단일 파일 업로드: 상태 기반 진행률 (PENDING=0%, UPLOADING=50%, COMPLETED=100%)
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
     * @deprecated 하드코딩된 상태 기반 진행률을 사용합니다. fromWithProgress()를 사용하세요.
     * @param session 업로드 세션 도메인 객체
     * @return UploadStatusResponse
     */
    @Deprecated
    public static UploadStatusResponse from(UploadSession session) {
        return new UploadStatusResponse(
                session.getSessionId(),
                session.getStatus(),
                session.getStatus().getProgress(),
                session.getUploadRequest().fileSizeBytes(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.isExpired()
        );
    }

    /**
     * UploadSession 도메인 객체와 실제 진행률로부터 Response DTO를 생성합니다.
     *
     * 진행률은 Service Layer에서 계산된 실제 진행률을 사용합니다:
     * - 멀티파트 업로드: Redis에서 조회한 실제 완료된 파트 수 기반 진행률
     * - 단일 파일 업로드: 상태 기반 진행률 (PENDING=0%, UPLOADING=50%, COMPLETED=100%)
     *
     * @param session 업로드 세션 도메인 객체
     * @param actualProgress 실제 진행률 (0-100)
     * @return UploadStatusResponse
     */
    public static UploadStatusResponse fromWithProgress(UploadSession session, int actualProgress) {
        return new UploadStatusResponse(
                session.getSessionId(),
                session.getStatus(),
                actualProgress,
                session.getUploadRequest().fileSizeBytes(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.isExpired()
        );
    }
}

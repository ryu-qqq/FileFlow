package com.ryuqq.fileflow.application.upload.dto;

import com.ryuqq.fileflow.domain.upload.model.UploadSession;
import com.ryuqq.fileflow.domain.upload.model.UploadStatus;

import java.time.LocalDateTime;

/**
 * 업로드 세션 조회 응답 DTO
 *
 * 업로드 세션 정보를 클라이언트에 전달하기 위한 응답 객체입니다.
 *
 * @param sessionId 세션 ID
 * @param policyKeyValue 정책 키 값
 * @param fileName 파일명
 * @param fileSize 파일 크기
 * @param contentType Content-Type
 * @param uploaderId 업로더 ID
 * @param status 세션 상태
 * @param createdAt 생성 시간
 * @param expiresAt 만료 시간
 * @param isExpired 만료 여부
 * @param isActive 활성 여부
 * @author sangwon-ryu
 */
public record UploadSessionResponse(
        String sessionId,
        String policyKeyValue,
        String fileName,
        long fileSize,
        String contentType,
        String uploaderId,
        UploadStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        boolean isExpired,
        boolean isActive
) {
    /**
     * UploadSession 도메인 객체로부터 Response DTO를 생성합니다.
     *
     * @param session 업로드 세션 도메인 객체
     * @return UploadSessionResponse
     */
    public static UploadSessionResponse from(UploadSession session) {
        return new UploadSessionResponse(
                session.getSessionId(),
                session.getPolicyKey().getValue(),
                session.getUploadRequest().fileName(),
                session.getUploadRequest().fileSizeBytes(),
                session.getUploadRequest().contentType(),
                session.getUploaderId(),
                session.getStatus(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.isExpired(),
                session.isActive()
        );
    }
}

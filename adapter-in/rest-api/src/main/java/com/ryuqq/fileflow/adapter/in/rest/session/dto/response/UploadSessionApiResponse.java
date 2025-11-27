package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.time.LocalDateTime;

/**
 * 업로드 세션 API Response.
 *
 * <p>업로드 세션의 기본 정보를 반환합니다.
 *
 * @param sessionId 세션 ID
 * @param fileName 파일명
 * @param fileSize 파일 크기 (바이트)
 * @param contentType Content-Type
 * @param uploadType 업로드 타입 (SINGLE/MULTIPART)
 * @param status 세션 상태
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @param createdAt 생성 시각
 * @param expiresAt 만료 시각
 * @author development-team
 * @since 1.0.0
 */
public record UploadSessionApiResponse(
        String sessionId,
        String fileName,
        long fileSize,
        String contentType,
        String uploadType,
        SessionStatus status,
        String bucket,
        String key,
        LocalDateTime createdAt,
        LocalDateTime expiresAt) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content-Type
     * @param uploadType 업로드 타입
     * @param status 세션 상태
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @param createdAt 생성 시각
     * @param expiresAt 만료 시각
     * @return UploadSessionApiResponse
     */
    public static UploadSessionApiResponse of(
            String sessionId,
            String fileName,
            long fileSize,
            String contentType,
            String uploadType,
            SessionStatus status,
            String bucket,
            String key,
            LocalDateTime createdAt,
            LocalDateTime expiresAt) {
        return new UploadSessionApiResponse(
                sessionId,
                fileName,
                fileSize,
                contentType,
                uploadType,
                status,
                bucket,
                key,
                createdAt,
                expiresAt);
    }
}

package com.ryuqq.fileflow.application.session.dto.response;

import java.time.Instant;

/**
 * UploadSession 응답 DTO.
 *
 * <p>업로드 세션의 기본 정보를 반환합니다.
 * REST API Layer와의 결합도를 낮추기 위해 status를 String으로 설계.
 *
 * @param sessionId 세션 ID
 * @param fileName 파일명
 * @param fileSize 파일 크기 (바이트)
 * @param contentType Content-Type
 * @param uploadType 업로드 타입 (SINGLE/MULTIPART)
 * @param status 세션 상태 (enum name as String)
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @param createdAt 생성 시각
 * @param expiresAt 만료 시각
 */
public record UploadSessionResponse(
        String sessionId,
        String fileName,
        long fileSize,
        String contentType,
        String uploadType,
        String status,
        String bucket,
        String key,
        Instant createdAt,
        Instant expiresAt) {

    public static UploadSessionResponse of(
            String sessionId,
            String fileName,
            long fileSize,
            String contentType,
            String uploadType,
            String status,
            String bucket,
            String key,
            Instant createdAt,
            Instant expiresAt) {
        return new UploadSessionResponse(
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

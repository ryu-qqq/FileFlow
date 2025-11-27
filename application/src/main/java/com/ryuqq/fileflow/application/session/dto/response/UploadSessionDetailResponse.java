package com.ryuqq.fileflow.application.session.dto.response;

import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * UploadSession 상세 응답 DTO.
 *
 * <p>업로드 세션의 상세 정보를 반환합니다. Multipart 세션의 경우 Part 정보를 포함합니다.
 *
 * @param sessionId 세션 ID
 * @param fileName 파일명
 * @param fileSize 파일 크기 (바이트)
 * @param contentType Content-Type
 * @param uploadType 업로드 타입 (SINGLE/MULTIPART)
 * @param status 세션 상태
 * @param bucket S3 버킷명
 * @param key S3 객체 키
 * @param uploadId S3 Multipart Upload ID (Multipart 전용)
 * @param totalParts 전체 Part 개수 (Multipart 전용)
 * @param uploadedParts 업로드 완료된 Part 개수 (Multipart 전용)
 * @param parts Part 정보 목록 (Multipart 전용)
 * @param etag ETag (완료 시)
 * @param createdAt 생성 시각
 * @param expiresAt 만료 시각
 * @param completedAt 완료 시각
 */
public record UploadSessionDetailResponse(
        String sessionId,
        String fileName,
        long fileSize,
        String contentType,
        String uploadType,
        SessionStatus status,
        String bucket,
        String key,
        String uploadId,
        Integer totalParts,
        Integer uploadedParts,
        List<PartDetailResponse> parts,
        String etag,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime completedAt) {

    /**
     * Part 상세 정보.
     *
     * @param partNumber Part 번호 (1-based)
     * @param etag ETag
     * @param size Part 크기 (바이트)
     * @param uploadedAt 업로드 완료 시각
     */
    public record PartDetailResponse(
            int partNumber, String etag, long size, LocalDateTime uploadedAt) {

        public static PartDetailResponse of(
                int partNumber, String etag, long size, LocalDateTime uploadedAt) {
            return new PartDetailResponse(partNumber, etag, size, uploadedAt);
        }
    }

    /** 단일 업로드 세션용 생성. */
    public static UploadSessionDetailResponse ofSingle(
            String sessionId,
            String fileName,
            long fileSize,
            String contentType,
            SessionStatus status,
            String bucket,
            String key,
            String etag,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            LocalDateTime completedAt) {
        return new UploadSessionDetailResponse(
                sessionId,
                fileName,
                fileSize,
                contentType,
                "SINGLE",
                status,
                bucket,
                key,
                null,
                null,
                null,
                null,
                etag,
                createdAt,
                expiresAt,
                completedAt);
    }

    /** Multipart 업로드 세션용 생성. */
    public static UploadSessionDetailResponse ofMultipart(
            String sessionId,
            String fileName,
            long fileSize,
            String contentType,
            SessionStatus status,
            String bucket,
            String key,
            String uploadId,
            int totalParts,
            int uploadedParts,
            List<PartDetailResponse> parts,
            String etag,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            LocalDateTime completedAt) {
        return new UploadSessionDetailResponse(
                sessionId,
                fileName,
                fileSize,
                contentType,
                "MULTIPART",
                status,
                bucket,
                key,
                uploadId,
                totalParts,
                uploadedParts,
                parts,
                etag,
                createdAt,
                expiresAt,
                completedAt);
    }
}

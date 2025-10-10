package com.ryuqq.fileflow.adapter.rest.dto.response;

import com.ryuqq.fileflow.application.upload.dto.PresignedUrlResponse;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;
import com.ryuqq.fileflow.application.upload.port.in.CreateUploadSessionUseCase;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;

import java.time.LocalDateTime;

/**
 * Upload Session 생성 API Response DTO
 *
 * 업로드 세션 생성 결과와 Presigned URL을 전달하는 응답 DTO입니다.
 *
 * @param session 생성된 업로드 세션 정보
 * @param presignedUrl Presigned URL 정보
 * @author sangwon-ryu
 */
public record CreateUploadSessionApiResponse(
        SessionInfo session,
        PresignedUrlInfo presignedUrl
) {
    /**
     * Application Response를 REST Response로 변환합니다.
     *
     * @param response Application 계층의 응답
     * @return CreateUploadSessionApiResponse
     */
    public static CreateUploadSessionApiResponse from(
            CreateUploadSessionUseCase.UploadSessionWithUrlResponse response
    ) {
        return new CreateUploadSessionApiResponse(
                SessionInfo.from(response.session()),
                PresignedUrlInfo.from(response.presignedUrl())
        );
    }

    /**
     * 업로드 세션 정보
     *
     * @param sessionId 세션 ID
     * @param status 세션 상태
     * @param fileName 파일명
     * @param fileSize 파일 크기 (bytes)
     * @param contentType Content-Type
     * @param createdAt 생성 시간
     * @param expiresAt 만료 시간
     */
    public record SessionInfo(
            String sessionId,
            UploadStatus status,
            String fileName,
            long fileSize,
            String contentType,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        public static SessionInfo from(UploadSessionResponse response) {
            return new SessionInfo(
                    response.sessionId(),
                    response.status(),
                    response.fileName(),
                    response.fileSize(),
                    response.contentType(),
                    response.createdAt(),
                    response.expiresAt()
            );
        }
    }

    /**
     * Presigned URL 정보
     *
     * @param url Presigned URL
     * @param uploadPath 업로드 경로
     * @param expiresAt 만료 시간
     */
    public record PresignedUrlInfo(
            String url,
            String uploadPath,
            LocalDateTime expiresAt
    ) {
        public static PresignedUrlInfo from(PresignedUrlResponse response) {
            return new PresignedUrlInfo(
                    response.presignedUrl(),
                    response.uploadPath(),
                    response.expiresAt()
            );
        }
    }
}

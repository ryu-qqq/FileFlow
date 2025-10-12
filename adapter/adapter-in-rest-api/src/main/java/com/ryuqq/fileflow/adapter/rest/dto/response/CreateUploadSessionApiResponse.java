package com.ryuqq.fileflow.adapter.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ryuqq.fileflow.application.upload.dto.MultipartUploadResponse;
import com.ryuqq.fileflow.application.upload.dto.PresignedUrlResponse;
import com.ryuqq.fileflow.application.upload.dto.UploadSessionResponse;
import com.ryuqq.fileflow.application.upload.port.in.CreateUploadSessionUseCase;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Upload Session 생성 API Response DTO
 *
 * 업로드 세션 생성 결과와 업로드 URL 정보를 전달하는 응답 DTO입니다.
 * 파일 크기에 따라 presignedUrl(단일 파일) 또는 multipartUpload(멀티파트) 정보가 제공됩니다.
 *
 * @param session 생성된 업로드 세션 정보
 * @param presignedUrl Presigned URL 정보 (단일 파일 업로드 시, 100MB 미만)
 * @param multipartUpload 멀티파트 업로드 정보 (100MB 이상 파일)
 * @author sangwon-ryu
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateUploadSessionApiResponse(
        SessionInfo session,
        PresignedUrlInfo presignedUrl,
        MultipartUploadInfo multipartUpload
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
        PresignedUrlInfo presignedUrlInfo = response.presignedUrl() != null
                ? PresignedUrlInfo.from(response.presignedUrl())
                : null;

        MultipartUploadInfo multipartUploadInfo = response.multipartUpload() != null
                ? MultipartUploadInfo.from(response.multipartUpload())
                : null;

        return new CreateUploadSessionApiResponse(
                SessionInfo.from(response.session()),
                presignedUrlInfo,
                multipartUploadInfo
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

    /**
     * 멀티파트 업로드 정보
     *
     * 100MB 이상 대용량 파일 업로드 시 제공되는 멀티파트 업로드 정보입니다.
     *
     * @param uploadId AWS S3 멀티파트 업로드 ID
     * @param uploadPath S3 업로드 경로
     * @param parts 파트별 업로드 정보 리스트
     */
    public record MultipartUploadInfo(
            String uploadId,
            String uploadPath,
            List<PartInfo> parts
    ) {
        public static MultipartUploadInfo from(MultipartUploadResponse response) {
            List<PartInfo> partInfos = response.parts().stream()
                    .map(PartInfo::from)
                    .toList();

            return new MultipartUploadInfo(
                    response.uploadId(),
                    response.uploadPath(),
                    partInfos
            );
        }

        /**
         * 개별 파트 업로드 정보
         *
         * @param partNumber 파트 번호 (1-based)
         * @param url 파트 업로드용 Presigned URL
         * @param startByte 파트 시작 바이트 위치
         * @param endByte 파트 종료 바이트 위치
         * @param expiresAt Presigned URL 만료 시간
         */
        public record PartInfo(
                int partNumber,
                String url,
                long startByte,
                long endByte,
                LocalDateTime expiresAt
        ) {
            public static PartInfo from(MultipartUploadResponse.PartUploadResponse response) {
                return new PartInfo(
                        response.partNumber(),
                        response.presignedUrl(),
                        response.startByte(),
                        response.endByte(),
                        response.expiresAt()
                );
            }
        }
    }
}

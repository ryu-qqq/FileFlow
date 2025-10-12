package com.ryuqq.fileflow.application.upload.dto;

import com.ryuqq.fileflow.domain.upload.vo.MultipartUploadInfo;
import com.ryuqq.fileflow.domain.upload.vo.PartUploadInfo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 멀티파트 업로드 응답 DTO
 *
 * 멀티파트 업로드 정보를 클라이언트에 전달하기 위한 응답 객체입니다.
 * 100MB 이상 대용량 파일 업로드 시 사용됩니다.
 *
 * @param uploadId AWS S3 멀티파트 업로드 ID
 * @param uploadPath S3 업로드 경로
 * @param parts 파트별 업로드 정보 리스트
 * @author sangwon-ryu
 */
public record MultipartUploadResponse(
        String uploadId,
        String uploadPath,
        List<PartUploadResponse> parts
) {
    /**
     * MultipartUploadInfo 도메인 객체로부터 Response DTO를 생성합니다.
     *
     * @param uploadInfo 멀티파트 업로드 정보
     * @return MultipartUploadResponse
     */
    public static MultipartUploadResponse from(MultipartUploadInfo uploadInfo) {
        List<PartUploadResponse> partResponses = uploadInfo.parts().stream()
                .map(PartUploadResponse::from)
                .toList();

        return new MultipartUploadResponse(
                uploadInfo.uploadId(),
                uploadInfo.uploadPath(),
                partResponses
        );
    }

    /**
     * 개별 파트 업로드 정보 DTO
     *
     * @param partNumber 파트 번호 (1-based)
     * @param presignedUrl 파트 업로드용 Presigned URL
     * @param startByte 파트 시작 바이트 위치
     * @param endByte 파트 종료 바이트 위치
     * @param expiresAt Presigned URL 만료 시간
     */
    public record PartUploadResponse(
            int partNumber,
            String presignedUrl,
            long startByte,
            long endByte,
            LocalDateTime expiresAt
    ) {
        /**
         * PartUploadInfo 도메인 객체로부터 Response DTO를 생성합니다.
         *
         * @param partInfo 파트 업로드 정보
         * @return PartUploadResponse
         */
        public static PartUploadResponse from(PartUploadInfo partInfo) {
            return new PartUploadResponse(
                    partInfo.partNumber(),
                    partInfo.presignedUrl(),
                    partInfo.startByte(),
                    partInfo.endByte(),
                    partInfo.expiresAt()
            );
        }
    }
}

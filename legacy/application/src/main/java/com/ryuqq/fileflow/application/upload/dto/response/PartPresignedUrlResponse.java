package com.ryuqq.fileflow.application.upload.dto.response;

import java.time.Duration;

/**
 * 파트 Presigned URL Response
 *
 * @param partNumber 파트 번호
 * @param presignedUrl Presigned URL
 * @param expiresIn 만료 시간
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record PartPresignedUrlResponse(
    Integer partNumber,
    String presignedUrl,
    Duration expiresIn
) {
    /**
     * Static Factory Method
     *
     * @param partNumber 파트 번호
     * @param presignedUrl Presigned URL
     * @param expiresIn 만료 시간
     * @return PartPresignedUrlResponse 인스턴스
     */
    public static PartPresignedUrlResponse of(
        Integer partNumber,
        String presignedUrl,
        Duration expiresIn
    ) {
        return new PartPresignedUrlResponse(partNumber, presignedUrl, expiresIn);
    }
}

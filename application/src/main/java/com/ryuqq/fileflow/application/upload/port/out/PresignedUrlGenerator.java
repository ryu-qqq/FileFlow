package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.domain.upload.model.PresignedUrlInfo;

/**
 * Presigned URL 생성 Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * S3 등의 스토리지에 대한 Presigned URL 생성을 위한 인터페이스입니다.
 *
 * @author sangwon-ryu
 */
public interface PresignedUrlGenerator {

    /**
     * Presigned URL을 생성합니다.
     *
     * @param fileName 파일명
     * @param contentType Content-Type
     * @param expirationMinutes 만료 시간 (분)
     * @return Presigned URL 정보
     */
    PresignedUrlInfo generatePresignedUrl(
            String fileName,
            String contentType,
            int expirationMinutes
    );
}

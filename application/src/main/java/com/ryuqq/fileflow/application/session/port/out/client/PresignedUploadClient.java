package com.ryuqq.fileflow.application.session.port.out.client;

import java.time.Duration;

/**
 * Presigned URL 생성 클라이언트
 *
 * <p>단건 업로드용 Presigned PUT URL을 생성하고, 저장소 버킷 정보를 제공합니다.
 */
public interface PresignedUploadClient {

    /**
     * 저장소 버킷명 조회
     *
     * @return 버킷명
     */
    String getBucket();

    /**
     * Presigned Upload URL 생성
     *
     * @param s3Key 객체 키
     * @param contentType MIME 타입
     * @param ttl URL 유효 기간
     * @return Presigned URL 문자열
     */
    String generatePresignedUploadUrl(String s3Key, String contentType, Duration ttl);
}

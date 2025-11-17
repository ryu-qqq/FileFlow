package com.ryuqq.fileflow.application.port.out.external;

import java.time.Duration;

/**
 * S3 Client Port (Outbound Port - External API)
 * <p>
 * 외부 API Port 규칙:
 * - 인터페이스명: *ClientPort
 * - 패키지: ..application..port.out.external..
 * - 메서드: 외부 API 호출 메서드
 * - Timeout, Retry 정책 명시 필수
 * </p>
 * <p>
 * Application Layer에서 S3 외부 서비스로의 파일 처리 요청을 위한 Port입니다.
 * </p>
 */
public interface S3ClientPort {

    /**
     * Presigned URL 생성
     * <p>
     * Timeout: 30초
     * Retry: 3회 (Exponential Backoff: 100ms, 200ms, 400ms)
     * </p>
     *
     * @param key S3 객체 키
     * @param expiration URL 만료 시간
     * @return Presigned URL (클라이언트가 직접 업로드/다운로드에 사용)
     */
    String generatePresignedUrl(String key, Duration expiration);

    /**
     * Multipart Upload 시작
     * <p>
     * Timeout: 30초
     * Retry: 3회 (Exponential Backoff: 100ms, 200ms, 400ms)
     * </p>
     *
     * @param key S3 객체 키
     * @param contentType 파일 Content-Type
     * @return Upload ID (Multipart Upload 식별자)
     */
    String initiateMultipartUpload(String key, String contentType);

    /**
     * 객체 메타데이터 조회
     * <p>
     * Timeout: 10초
     * Retry: 3회 (Exponential Backoff: 100ms, 200ms, 400ms)
     * </p>
     *
     * @param key S3 객체 키
     * @return HeadObjectResponse (객체 메타데이터: ContentLength, ContentType, LastModified 등)
     */
    HeadObjectResponse headObject(String key);

    /**
     * URL에서 S3로 파일 업로드
     * <p>
     * Timeout: 5분
     * Retry: 3회 (Exponential Backoff: 1초, 2초, 4초)
     * </p>
     *
     * @param sourceUrl 소스 파일 URL
     * @param targetKey S3 대상 객체 키
     */
    void uploadFromUrl(String sourceUrl, String targetKey);

    /**
     * HeadObject API 응답 DTO
     * <p>
     * S3 객체 메타데이터를 담는 DTO입니다.
     * </p>
     */
    record HeadObjectResponse(
            Long contentLength,
            String contentType,
            String lastModified,
            String etag
    ) {
        public static HeadObjectResponse of(Long contentLength, String contentType, String lastModified, String etag) {
            return new HeadObjectResponse(contentLength, contentType, lastModified, etag);
        }
    }
}

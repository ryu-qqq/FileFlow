package com.ryuqq.fileflow.adapter.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * S3 설정 프로퍼티
 * application.yml의 aws.s3 설정을 바인딩합니다.
 *
 * NO Lombok:
 * - 명시적인 생성자와 getter 사용
 * - 불변성 보장
 */
@Component
public class S3Properties {

    private final String bucketName;
    private final String region;
    private final long presignedUrlExpirationMinutes;
    private final String pathPrefix;

    /**
     * S3 프로퍼티를 생성합니다.
     *
     * @param bucketName S3 버킷 이름
     * @param region AWS 리전
     * @param presignedUrlExpirationMinutes Presigned URL 만료 시간(분)
     * @param pathPrefix S3 경로 접두사
     * @throws IllegalArgumentException 유효하지 않은 설정값인 경우
     */
    public S3Properties(
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.s3.region}") String region,
            @Value("${aws.s3.presigned-url-expiration-minutes:15}") long presignedUrlExpirationMinutes,
            @Value("${aws.s3.path-prefix:}") String pathPrefix
    ) {
        validateBucketName(bucketName);
        validateRegion(region);
        validateExpirationMinutes(presignedUrlExpirationMinutes);

        this.bucketName = bucketName;
        this.region = region;
        this.presignedUrlExpirationMinutes = presignedUrlExpirationMinutes;
        this.pathPrefix = (pathPrefix != null && !pathPrefix.trim().isEmpty())
            ? pathPrefix.trim()
            : "";
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getRegion() {
        return region;
    }

    public long getPresignedUrlExpirationMinutes() {
        return presignedUrlExpirationMinutes;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    // ========== Validation Methods ==========

    private static void validateBucketName(String bucketName) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 bucket name cannot be null or empty");
        }
    }

    private static void validateRegion(String region) {
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("AWS region cannot be null or empty");
        }
    }

    private static void validateExpirationMinutes(long expirationMinutes) {
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("Presigned URL expiration minutes must be positive");
        }
        if (expirationMinutes > 60) {
            throw new IllegalArgumentException("Presigned URL expiration minutes cannot exceed 60 minutes");
        }
    }
}

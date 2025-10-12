package com.ryuqq.fileflow.adapter.s3.adapter;

import com.ryuqq.fileflow.application.upload.port.out.VerifyS3ObjectPort;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * AWS S3 객체 검증 Adapter
 *
 * Hexagonal Architecture의 Outbound Adapter로서,
 * VerifyS3ObjectPort를 구현하여 S3 객체의 존재 여부와 메타데이터를 검증합니다.
 *
 * 구현 Port:
 * - VerifyS3ObjectPort: S3 객체 존재 확인, ETag 조회, 메타데이터 조회
 *
 * AWS SDK 활용:
 * - HeadObject API를 사용하여 객체 메타데이터 조회
 * - 객체 본문을 다운로드하지 않고 헤더 정보만 조회하여 효율적
 *
 * 보안:
 * - AWS 자격 증명은 S3Config에서 관리
 * - IAM 권한: s3:GetObject 또는 s3:HeadObject 필요
 *
 * @author sangwon-ryu
 */
@Component
public class S3ObjectVerificationAdapter implements VerifyS3ObjectPort {

    private final S3Client s3Client;

    /**
     * S3ObjectVerificationAdapter 생성자
     *
     * @param s3Client AWS S3 클라이언트
     * @throws IllegalArgumentException s3Client가 null인 경우
     */
    public S3ObjectVerificationAdapter(S3Client s3Client) {
        if (s3Client == null) {
            throw new IllegalArgumentException("S3Client cannot be null");
        }
        this.s3Client = s3Client;
    }

    // ========== VerifyS3ObjectPort Implementation ==========

    /**
     * S3에 객체가 존재하는지 확인합니다.
     *
     * HeadObject API를 사용하여 객체 존재 여부를 확인합니다.
     * NoSuchKeyException 발생 시 false를 반환합니다.
     *
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @return 존재 여부
     * @throws IllegalArgumentException bucket 또는 key가 null이거나 비어있는 경우
     * @throws RuntimeException HeadObject API 호출 실패 시 (NoSuchKeyException 제외)
     */
    @Override
    public boolean doesObjectExist(String bucket, String key) {
        validateBucket(bucket);
        validateKey(key);

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.headObject(request);
            return true;

        } catch (NoSuchKeyException e) {
            // 객체가 존재하지 않음
            return false;

        } catch (S3Exception e) {
            throw new RuntimeException(
                    String.format("Failed to verify S3 object existence: bucket=%s, key=%s", bucket, key),
                    e
            );
        }
    }

    /**
     * S3 객체의 ETag를 조회합니다.
     *
     * ETag는 S3에서 생성하는 객체의 MD5 해시값으로,
     * 파일 무결성 검증에 사용됩니다.
     *
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @return ETag 값 (따옴표 포함, 예: "d41d8cd98f00b204e9800998ecf8427e")
     * @throws IllegalArgumentException bucket 또는 key가 null이거나 비어있는 경우
     * @throws RuntimeException HeadObject API 호출 실패 시
     */
    @Override
    public String getObjectETag(String bucket, String key) {
        validateBucket(bucket);
        validateKey(key);

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(request);
            return response.eTag();

        } catch (S3Exception e) {
            throw new RuntimeException(
                    String.format("Failed to get S3 object ETag: bucket=%s, key=%s", bucket, key),
                    e
            );
        }
    }

    /**
     * S3 객체의 메타데이터를 조회합니다.
     *
     * @param bucket S3 버킷명
     * @param key S3 객체 키
     * @return S3 객체 메타데이터 (ETag, 파일 크기, Content-Type, 마지막 수정 시간)
     * @throws IllegalArgumentException bucket 또는 key가 null이거나 비어있는 경우
     * @throws RuntimeException HeadObject API 호출 실패 시
     */
    @Override
    public S3ObjectMetadata getObjectMetadata(String bucket, String key) {
        validateBucket(bucket);
        validateKey(key);

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(request);

            return new S3ObjectMetadata(
                    response.eTag(),
                    response.contentLength(),
                    response.contentType(),
                    response.lastModified().toString()
            );

        } catch (S3Exception e) {
            throw new RuntimeException(
                    String.format("Failed to get S3 object metadata: bucket=%s, key=%s", bucket, key),
                    e
            );
        }
    }

    // ========== Validation Methods ==========

    private static void validateBucket(String bucket) {
        if (bucket == null || bucket.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket cannot be null or empty");
        }
    }

    private static void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
    }
}

package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.S3Bucket;

/**
 * S3Bucket Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class S3BucketFixture {

    private S3BucketFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 S3Bucket Fixture */
    public static S3Bucket defaultS3Bucket() {
        return S3Bucket.of("fileflow-test-bucket");
    }

    /** Admin용 S3Bucket Fixture */
    public static S3Bucket adminS3Bucket() {
        return S3Bucket.of("fileflow-admin-bucket");
    }

    /** Seller용 S3Bucket Fixture */
    public static S3Bucket sellerS3Bucket() {
        return S3Bucket.of("fileflow-seller-bucket");
    }

    /** Customer용 S3Bucket Fixture */
    public static S3Bucket customerS3Bucket() {
        return S3Bucket.of("fileflow-customer-bucket");
    }

    /** Custom S3Bucket Fixture */
    public static S3Bucket customS3Bucket(String bucketName) {
        return S3Bucket.of(bucketName);
    }
}

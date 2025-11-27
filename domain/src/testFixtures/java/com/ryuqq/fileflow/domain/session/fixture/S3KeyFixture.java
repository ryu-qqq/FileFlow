package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.S3Key;

/**
 * S3Key Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class S3KeyFixture {

    private S3KeyFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 S3Key Fixture */
    public static S3Key defaultS3Key() {
        return S3Key.of("test/2025/01/test-file.jpg");
    }

    /** Admin용 S3Key Fixture */
    public static S3Key adminS3Key() {
        return S3Key.of("admin/product/2025/01/product-image.jpg");
    }

    /** Seller용 S3Key Fixture */
    public static S3Key sellerS3Key() {
        return S3Key.of("seller-1001/product/2025/01/product-image.jpg");
    }

    /** Customer용 S3Key Fixture */
    public static S3Key customerS3Key() {
        return S3Key.of("customer/2025/01/profile-image.jpg");
    }

    /** Custom S3Key Fixture */
    public static S3Key customS3Key(String key) {
        return S3Key.of(key);
    }
}

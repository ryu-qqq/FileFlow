package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.S3UploadId;

/**
 * S3UploadId Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class S3UploadIdFixture {

    private S3UploadIdFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 S3UploadId Fixture */
    public static S3UploadId defaultS3UploadId() {
        return S3UploadId.of("test-upload-id-12345");
    }

    /** Custom S3UploadId Fixture */
    public static S3UploadId customS3UploadId(String value) {
        return S3UploadId.of(value);
    }
}

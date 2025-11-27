package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.session.vo.PresignedUrl;

/**
 * PresignedUrl Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class PresignedUrlFixture {

    private PresignedUrlFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 PresignedUrl Fixture */
    public static PresignedUrl defaultPresignedUrl() {
        return PresignedUrl.of("https://s3.amazonaws.com/bucket/key?X-Amz-Signature=abc123");
    }

    /** PUT용 PresignedUrl Fixture */
    public static PresignedUrl putPresignedUrl() {
        return PresignedUrl.of(
                "https://s3.amazonaws.com/bucket/key?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=def456");
    }

    /** Custom PresignedUrl Fixture */
    public static PresignedUrl customPresignedUrl(String url) {
        return PresignedUrl.of(url);
    }
}

package com.ryuqq.fileflow.domain.session.vo;

import java.time.Duration;
import java.time.Instant;

public class PartPresignedUrlSpecFixture {

    private static final String DEFAULT_S3_KEY = "public/2026/01/session-001.jpg";
    private static final String DEFAULT_UPLOAD_ID = "upload-id-001";
    private static final int DEFAULT_PART_NUMBER = 1;
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);
    private static final Instant DEFAULT_CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    public static PartPresignedUrlSpec aPartPresignedUrlSpec() {
        return new PartPresignedUrlSpec(
                DEFAULT_S3_KEY,
                DEFAULT_UPLOAD_ID,
                DEFAULT_PART_NUMBER,
                DEFAULT_TTL,
                DEFAULT_CREATED_AT);
    }

    public static PartPresignedUrlSpec aPartPresignedUrlSpec(int partNumber) {
        return new PartPresignedUrlSpec(
                DEFAULT_S3_KEY, DEFAULT_UPLOAD_ID, partNumber, DEFAULT_TTL, DEFAULT_CREATED_AT);
    }

    public static PartPresignedUrlSpec aPartPresignedUrlSpec(
            String s3Key, String uploadId, int partNumber) {
        return new PartPresignedUrlSpec(
                s3Key, uploadId, partNumber, DEFAULT_TTL, DEFAULT_CREATED_AT);
    }
}

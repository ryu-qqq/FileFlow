package com.ryuqq.fileflow.domain.session.vo;

import java.time.Instant;

public class CompletedPartFixture {

    private static final Instant DEFAULT_CREATED_AT = Instant.parse("2026-01-01T00:00:10Z");

    public static CompletedPart aCompletedPart() {
        return CompletedPart.of(1, "etag-part-1", 5_242_880L, DEFAULT_CREATED_AT);
    }

    public static CompletedPart aCompletedPart(int partNumber) {
        return CompletedPart.of(
                partNumber, "etag-part-" + partNumber, 5_242_880L, DEFAULT_CREATED_AT);
    }

    public static CompletedPart aCompletedPart(
            int partNumber, String etag, long size, Instant createdAt) {
        return CompletedPart.of(partNumber, etag, size, createdAt);
    }
}

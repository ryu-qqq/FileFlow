package com.ryuqq.fileflow.adapter.out.persistence.session;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import java.time.Instant;

public class CompletedPartJpaEntityFixture {

    private static final Instant DEFAULT_CREATED_AT = Instant.parse("2026-01-01T00:00:10Z");

    public static CompletedPartJpaEntity aCompletedPartEntity() {
        return CompletedPartJpaEntity.create(
                "multipart-session-001", 1, "etag-part-1", 5_242_880L, DEFAULT_CREATED_AT);
    }

    public static CompletedPartJpaEntity aCompletedPartEntity(String sessionId, int partNumber) {
        return CompletedPartJpaEntity.create(
                sessionId, partNumber, "etag-part-" + partNumber, 5_242_880L, DEFAULT_CREATED_AT);
    }
}

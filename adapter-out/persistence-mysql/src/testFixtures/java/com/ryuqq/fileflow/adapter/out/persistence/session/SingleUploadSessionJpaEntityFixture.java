package com.ryuqq.fileflow.adapter.out.persistence.session;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.vo.SingleSessionStatus;
import java.time.Duration;
import java.time.Instant;

public class SingleUploadSessionJpaEntityFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant DEFAULT_EXPIRES_AT = DEFAULT_NOW.plus(Duration.ofHours(1));

    public static SingleUploadSessionJpaEntity aCreatedEntity() {
        return SingleUploadSessionJpaEntity.create(
                "single-session-001",
                "public/2026/01/file-001.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "https://s3.presigned-url.com/test",
                "product-image",
                "commerce-service",
                SingleSessionStatus.CREATED,
                DEFAULT_EXPIRES_AT,
                DEFAULT_NOW,
                DEFAULT_NOW);
    }

    public static SingleUploadSessionJpaEntity aCompletedEntity() {
        return SingleUploadSessionJpaEntity.create(
                "single-session-002",
                "public/2026/01/file-002.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "https://s3.presigned-url.com/test-completed",
                "product-image",
                "commerce-service",
                SingleSessionStatus.COMPLETED,
                DEFAULT_EXPIRES_AT,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(30));
    }

    public static SingleUploadSessionJpaEntity anExpiredEntity() {
        return SingleUploadSessionJpaEntity.create(
                "single-session-003",
                "public/2026/01/file-003.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "https://s3.presigned-url.com/test-expired",
                "product-image",
                "commerce-service",
                SingleSessionStatus.EXPIRED,
                DEFAULT_EXPIRES_AT,
                DEFAULT_NOW,
                DEFAULT_EXPIRES_AT.plusSeconds(1));
    }

    public static SingleUploadSessionJpaEntity anEntityWithId(String id) {
        return SingleUploadSessionJpaEntity.create(
                id,
                "public/2026/01/file-custom.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "https://s3.presigned-url.com/test-custom",
                "product-image",
                "commerce-service",
                SingleSessionStatus.CREATED,
                DEFAULT_EXPIRES_AT,
                DEFAULT_NOW,
                DEFAULT_NOW);
    }

    public static Instant defaultNow() {
        return DEFAULT_NOW;
    }

    public static Instant defaultExpiresAt() {
        return DEFAULT_EXPIRES_AT;
    }
}

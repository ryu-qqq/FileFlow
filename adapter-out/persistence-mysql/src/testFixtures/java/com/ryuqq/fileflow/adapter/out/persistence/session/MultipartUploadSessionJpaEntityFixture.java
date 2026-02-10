package com.ryuqq.fileflow.adapter.out.persistence.session;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.vo.MultipartSessionStatus;
import java.time.Duration;
import java.time.Instant;

public class MultipartUploadSessionJpaEntityFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant DEFAULT_EXPIRES_AT = DEFAULT_NOW.plus(Duration.ofHours(1));

    public static MultipartUploadSessionJpaEntity anInitiatedEntity() {
        return MultipartUploadSessionJpaEntity.create(
                "multipart-session-001",
                "public/2026/01/file-001.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "upload-id-001",
                5_242_880L,
                "product-image",
                "commerce-service",
                MultipartSessionStatus.INITIATED,
                DEFAULT_EXPIRES_AT,
                DEFAULT_NOW,
                DEFAULT_NOW);
    }

    public static MultipartUploadSessionJpaEntity anUploadingEntity() {
        return MultipartUploadSessionJpaEntity.create(
                "multipart-session-uploading",
                "public/2026/01/file-uploading.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "large-video.mp4",
                "video/mp4",
                "upload-id-uploading",
                5_242_880L,
                "video-upload",
                "commerce-service",
                MultipartSessionStatus.UPLOADING,
                DEFAULT_EXPIRES_AT,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(30));
    }

    public static MultipartUploadSessionJpaEntity aCompletedEntity() {
        return MultipartUploadSessionJpaEntity.create(
                "multipart-session-002",
                "public/2026/01/file-002.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "upload-id-002",
                5_242_880L,
                "product-image",
                "commerce-service",
                MultipartSessionStatus.COMPLETED,
                DEFAULT_EXPIRES_AT,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(60));
    }

    public static MultipartUploadSessionJpaEntity anAbortedEntity() {
        return MultipartUploadSessionJpaEntity.create(
                "multipart-session-aborted",
                "public/2026/01/file-aborted.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "upload-id-aborted",
                5_242_880L,
                "product-image",
                "commerce-service",
                MultipartSessionStatus.ABORTED,
                DEFAULT_EXPIRES_AT,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(45));
    }

    public static MultipartUploadSessionJpaEntity anExpiredEntity() {
        Instant pastExpiry = DEFAULT_NOW.minus(Duration.ofHours(1));
        return MultipartUploadSessionJpaEntity.create(
                "multipart-session-expired",
                "public/2026/01/file-expired.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "upload-id-expired",
                5_242_880L,
                "product-image",
                "commerce-service",
                MultipartSessionStatus.EXPIRED,
                pastExpiry,
                DEFAULT_NOW.minus(Duration.ofHours(2)),
                DEFAULT_NOW.minus(Duration.ofHours(2)));
    }

    public static MultipartUploadSessionJpaEntity anEntityWithId(String id) {
        return MultipartUploadSessionJpaEntity.create(
                id,
                "public/2026/01/file-custom.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "upload-id-custom",
                5_242_880L,
                "product-image",
                "commerce-service",
                MultipartSessionStatus.INITIATED,
                DEFAULT_EXPIRES_AT,
                DEFAULT_NOW,
                DEFAULT_NOW);
    }

    public static MultipartUploadSessionJpaEntity anEntityWithStatus(
            String id, MultipartSessionStatus status) {
        return MultipartUploadSessionJpaEntity.create(
                id,
                "public/2026/01/file-" + id + ".jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                "upload-id-" + id,
                5_242_880L,
                "product-image",
                "commerce-service",
                status,
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

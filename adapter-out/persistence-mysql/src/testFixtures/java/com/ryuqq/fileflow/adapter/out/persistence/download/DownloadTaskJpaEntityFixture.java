package com.ryuqq.fileflow.adapter.out.persistence.download;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;

public class DownloadTaskJpaEntityFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static DownloadTaskJpaEntity aQueuedEntity() {
        return DownloadTaskJpaEntity.create(
                "download-001",
                "https://example.com/image.jpg",
                "test-bucket",
                "public/2026/02/download-001.jpg",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service",
                DownloadTaskStatus.QUEUED,
                0,
                3,
                "https://callback.example.com/done",
                null,
                DEFAULT_NOW,
                DEFAULT_NOW,
                null,
                null);
    }

    public static DownloadTaskJpaEntity aDownloadingEntity() {
        return DownloadTaskJpaEntity.create(
                "download-002",
                "https://example.com/image.jpg",
                "test-bucket",
                "public/2026/02/download-002.jpg",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service",
                DownloadTaskStatus.DOWNLOADING,
                0,
                3,
                "https://callback.example.com/done",
                null,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(10),
                DEFAULT_NOW.plusSeconds(10),
                null);
    }

    public static DownloadTaskJpaEntity aCompletedEntity() {
        return DownloadTaskJpaEntity.create(
                "download-003",
                "https://example.com/image.jpg",
                "test-bucket",
                "public/2026/02/download-003.jpg",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service",
                DownloadTaskStatus.COMPLETED,
                0,
                3,
                "https://callback.example.com/done",
                null,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(30),
                DEFAULT_NOW.plusSeconds(10),
                DEFAULT_NOW.plusSeconds(30));
    }

    public static DownloadTaskJpaEntity anEntityWithId(String id) {
        return DownloadTaskJpaEntity.create(
                id,
                "https://example.com/image.jpg",
                "test-bucket",
                "public/2026/02/" + id + ".jpg",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service",
                DownloadTaskStatus.QUEUED,
                0,
                3,
                "https://callback.example.com/done",
                null,
                DEFAULT_NOW,
                DEFAULT_NOW,
                null,
                null);
    }

    public static DownloadTaskJpaEntity anEntityWithStatus(DownloadTaskStatus status) {
        return DownloadTaskJpaEntity.create(
                "download-status-" + status.name().toLowerCase(),
                "https://example.com/image.jpg",
                "test-bucket",
                "public/2026/02/download-status.jpg",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service",
                status,
                0,
                3,
                null,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW,
                status == DownloadTaskStatus.DOWNLOADING ? DEFAULT_NOW : null,
                status == DownloadTaskStatus.COMPLETED ? DEFAULT_NOW.plusSeconds(30) : null);
    }

    public static DownloadTaskJpaEntity aFailedEntity(String lastError) {
        return DownloadTaskJpaEntity.create(
                "download-failed",
                "https://example.com/image.jpg",
                "test-bucket",
                "public/2026/02/download-failed.jpg",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service",
                DownloadTaskStatus.FAILED,
                3,
                3,
                null,
                lastError,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(60),
                DEFAULT_NOW.plusSeconds(10),
                null);
    }

    public static DownloadTaskJpaEntity aQueuedEntityWithoutCallback() {
        return DownloadTaskJpaEntity.create(
                "download-no-callback",
                "https://example.com/image.jpg",
                "test-bucket",
                "public/2026/02/download-no-callback.jpg",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service",
                DownloadTaskStatus.QUEUED,
                0,
                3,
                null,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW,
                null,
                null);
    }

    public static Instant defaultNow() {
        return DEFAULT_NOW;
    }
}

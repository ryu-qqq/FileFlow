package com.ryuqq.fileflow.adapter.out.persistence.download;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadQueueOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import java.time.Instant;

public class DownloadQueueOutboxJpaEntityFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static DownloadQueueOutboxJpaEntity aPendingEntity() {
        return DownloadQueueOutboxJpaEntity.create(
                "outbox-001", "download-001", OutboxStatus.PENDING, 0, null, DEFAULT_NOW, null);
    }

    public static DownloadQueueOutboxJpaEntity aSentEntity() {
        return DownloadQueueOutboxJpaEntity.create(
                "outbox-002",
                "download-002",
                OutboxStatus.SENT,
                0,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(5));
    }

    public static DownloadQueueOutboxJpaEntity aFailedEntity(String lastError) {
        return DownloadQueueOutboxJpaEntity.create(
                "outbox-003",
                "download-003",
                OutboxStatus.FAILED,
                1,
                lastError,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(10));
    }

    public static Instant defaultNow() {
        return DEFAULT_NOW;
    }
}

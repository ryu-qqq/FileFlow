package com.ryuqq.fileflow.adapter.out.persistence.transform;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformQueueOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import java.time.Instant;

public class TransformQueueOutboxJpaEntityFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static TransformQueueOutboxJpaEntity aPendingEntity() {
        return TransformQueueOutboxJpaEntity.create(
                "outbox-001", "transform-001", OutboxStatus.PENDING, 0, null, DEFAULT_NOW, null);
    }

    public static TransformQueueOutboxJpaEntity aSentEntity() {
        return TransformQueueOutboxJpaEntity.create(
                "outbox-002",
                "transform-002",
                OutboxStatus.SENT,
                0,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(5));
    }

    public static TransformQueueOutboxJpaEntity aFailedEntity(String lastError) {
        return TransformQueueOutboxJpaEntity.create(
                "outbox-003",
                "transform-003",
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

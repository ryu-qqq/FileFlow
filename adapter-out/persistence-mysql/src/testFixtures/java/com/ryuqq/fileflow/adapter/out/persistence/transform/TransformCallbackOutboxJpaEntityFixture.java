package com.ryuqq.fileflow.adapter.out.persistence.transform;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformCallbackOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import java.time.Instant;

public class TransformCallbackOutboxJpaEntityFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static TransformCallbackOutboxJpaEntity aPendingEntity() {
        return TransformCallbackOutboxJpaEntity.create(
                "outbox-001",
                "transform-001",
                "https://callback.example.com/transform-done",
                "COMPLETED",
                OutboxStatus.PENDING,
                0,
                5,
                null,
                DEFAULT_NOW,
                null);
    }

    public static TransformCallbackOutboxJpaEntity aSentEntity() {
        return TransformCallbackOutboxJpaEntity.create(
                "outbox-002",
                "transform-002",
                "https://callback.example.com/transform-done",
                "COMPLETED",
                OutboxStatus.SENT,
                0,
                5,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(5));
    }

    public static Instant defaultNow() {
        return DEFAULT_NOW;
    }
}

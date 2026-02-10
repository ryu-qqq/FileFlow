package com.ryuqq.fileflow.adapter.out.persistence.download;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.CallbackOutboxJpaEntity;
import com.ryuqq.fileflow.domain.download.vo.OutboxStatus;
import java.time.Instant;

public class CallbackOutboxJpaEntityFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static CallbackOutboxJpaEntity aPendingEntity() {
        return CallbackOutboxJpaEntity.create(
                "outbox-001",
                "download-001",
                "https://callback.example.com/done",
                "COMPLETED",
                OutboxStatus.PENDING,
                0,
                null,
                DEFAULT_NOW,
                null);
    }

    public static CallbackOutboxJpaEntity aSentEntity() {
        return CallbackOutboxJpaEntity.create(
                "outbox-002",
                "download-002",
                "https://callback.example.com/done",
                "COMPLETED",
                OutboxStatus.SENT,
                0,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(5));
    }

    public static Instant defaultNow() {
        return DEFAULT_NOW;
    }
}

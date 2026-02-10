package com.ryuqq.fileflow.adapter.out.persistence.transform;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformRequestJpaEntity;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;
import java.time.Instant;

public class TransformRequestJpaEntityFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static TransformRequestJpaEntity aQueuedResizeEntity() {
        return TransformRequestJpaEntity.create(
                "transform-001",
                "asset-001",
                "image/jpeg",
                TransformType.RESIZE,
                TransformStatus.QUEUED,
                null,
                null,
                800,
                600,
                true,
                null,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW,
                null);
    }

    public static TransformRequestJpaEntity aCompletedEntity() {
        return TransformRequestJpaEntity.create(
                "transform-002",
                "asset-001",
                "image/jpeg",
                TransformType.RESIZE,
                TransformStatus.COMPLETED,
                "result-001",
                null,
                800,
                600,
                true,
                null,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(30),
                DEFAULT_NOW.plusSeconds(30));
    }

    public static TransformRequestJpaEntity aConvertEntity() {
        return TransformRequestJpaEntity.create(
                "transform-003",
                "asset-001",
                "image/jpeg",
                TransformType.CONVERT,
                TransformStatus.QUEUED,
                null,
                null,
                null,
                null,
                false,
                "webp",
                null,
                DEFAULT_NOW,
                DEFAULT_NOW,
                null);
    }

    public static TransformRequestJpaEntity anEntityWithStatus(TransformStatus status) {
        return TransformRequestJpaEntity.create(
                "transform-status-" + status.name().toLowerCase(),
                "asset-001",
                "image/jpeg",
                TransformType.RESIZE,
                status,
                status == TransformStatus.COMPLETED ? "result-001" : null,
                status == TransformStatus.FAILED ? "Processing error" : null,
                800,
                600,
                true,
                null,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW,
                status == TransformStatus.COMPLETED ? DEFAULT_NOW.plusSeconds(30) : null);
    }

    public static TransformRequestJpaEntity anEntityWithId(String id) {
        return TransformRequestJpaEntity.create(
                id,
                "asset-001",
                "image/jpeg",
                TransformType.RESIZE,
                TransformStatus.QUEUED,
                null,
                null,
                800,
                600,
                true,
                null,
                null,
                DEFAULT_NOW,
                DEFAULT_NOW,
                null);
    }

    public static Instant defaultNow() {
        return DEFAULT_NOW;
    }
}

package com.ryuqq.fileflow.domain.transform.event;

import java.time.Instant;

public class TransformCompletedEventFixture {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static TransformCompletedEvent aTransformCompletedEvent() {
        return TransformCompletedEvent.of(
                "transform-001", "asset-001", "result-001", "RESIZE", 800, 600, NOW);
    }
}

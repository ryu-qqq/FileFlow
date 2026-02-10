package com.ryuqq.fileflow.domain.download.event;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;

public class DownloadCompletedEventFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static DownloadCompletedEvent aDownloadCompletedEvent() {
        return DownloadCompletedEvent.of(
                "download-001",
                "public/2026/02/download-001.jpg",
                "test-bucket",
                AccessType.PUBLIC,
                "image.jpg",
                "image/jpeg",
                1024L,
                "etag-123",
                "product-image",
                "commerce-service",
                DEFAULT_NOW);
    }
}

package com.ryuqq.fileflow.domain.session.event;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;

public class UploadCompletedEventFixture {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static UploadCompletedEvent aSingleUploadCompletedEvent() {
        return UploadCompletedEvent.of(
                "single-session-001",
                "SINGLE",
                "public/2026/01/file-001.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                1024L,
                "etag-123",
                "product-image",
                "commerce-service",
                NOW);
    }

    public static UploadCompletedEvent aMultipartUploadCompletedEvent() {
        return UploadCompletedEvent.of(
                "multipart-session-001",
                "MULTIPART",
                "public/2026/01/file-001.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg",
                10_485_760L,
                "etag-final",
                "product-image",
                "commerce-service",
                NOW);
    }
}

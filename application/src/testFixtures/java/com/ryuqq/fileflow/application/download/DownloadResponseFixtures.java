package com.ryuqq.fileflow.application.download;

import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.application.download.dto.response.FileDownloadResult;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import java.time.Instant;

/**
 * Download Application Response 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class DownloadResponseFixtures {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private DownloadResponseFixtures() {}

    // ===== DownloadTaskResponse Fixtures =====

    public static DownloadTaskResponse queuedResponse() {
        return queuedResponse("download-001");
    }

    public static DownloadTaskResponse queuedResponse(String downloadTaskId) {
        return new DownloadTaskResponse(
                downloadTaskId,
                "https://example.com/image.jpg",
                "public/2026/02/" + downloadTaskId + ".jpg",
                "test-bucket",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service",
                "QUEUED",
                0,
                3,
                "https://callback.example.com/done",
                null,
                NOW,
                null,
                null);
    }

    public static DownloadTaskResponse completedResponse(String downloadTaskId) {
        return new DownloadTaskResponse(
                downloadTaskId,
                "https://example.com/image.jpg",
                "public/2026/02/" + downloadTaskId + ".jpg",
                "test-bucket",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service",
                "COMPLETED",
                0,
                3,
                "https://callback.example.com/done",
                null,
                NOW,
                NOW.plusSeconds(10),
                NOW.plusSeconds(30));
    }

    // ===== FileDownloadResult Fixtures =====

    public static FileDownloadResult successResult() {
        return FileDownloadResult.success("image.jpg", "image/jpeg", 1024L, "etag-123");
    }

    public static FileDownloadResult failureResult() {
        return FileDownloadResult.failure("Connection timeout");
    }

    public static FileDownloadResult failureResult(String errorMessage) {
        return FileDownloadResult.failure(errorMessage);
    }
}

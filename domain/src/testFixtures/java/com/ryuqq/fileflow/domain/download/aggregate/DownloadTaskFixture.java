package com.ryuqq.fileflow.domain.download.aggregate;

import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.common.vo.StorageInfo;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.CallbackInfo;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import com.ryuqq.fileflow.domain.download.vo.DownloadedFileInfo;
import com.ryuqq.fileflow.domain.download.vo.RetryPolicy;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import java.time.Instant;

public class DownloadTaskFixture {

    private static final Instant DEFAULT_NOW = Instant.parse("2026-01-01T00:00:00Z");

    public static DownloadTask aQueuedTask() {
        return DownloadTask.forNew(
                DownloadTaskId.of("download-001"),
                SourceUrl.of("https://example.com/image.jpg"),
                StorageInfo.of("test-bucket", "public/2026/02/download-001.jpg", AccessType.PUBLIC),
                "product-image",
                "commerce-service",
                CallbackInfo.of("https://callback.example.com/done"),
                DEFAULT_NOW);
    }

    public static DownloadTask aQueuedTaskWithId(String taskId) {
        return DownloadTask.forNew(
                DownloadTaskId.of(taskId),
                SourceUrl.of("https://example.com/image.jpg"),
                StorageInfo.of(
                        "test-bucket", "public/2026/02/" + taskId + ".jpg", AccessType.PUBLIC),
                "product-image",
                "commerce-service",
                CallbackInfo.of("https://callback.example.com/done"),
                DEFAULT_NOW);
    }

    public static DownloadTask aDownloadingTask() {
        DownloadTask task = aQueuedTask();
        task.start(DEFAULT_NOW.plusSeconds(10));
        return task;
    }

    public static DownloadTask aCompletedTask() {
        DownloadTask task = aDownloadingTask();
        task.complete(
                DownloadedFileInfo.of(
                        "image.jpg", "image/jpeg", 1024L, "etag-123", DEFAULT_NOW.plusSeconds(30)));
        task.pollEvents();
        return task;
    }

    public static DownloadTask aFailedTask() {
        DownloadTask task = aDownloadingTask();
        task.fail("timeout", DEFAULT_NOW.plusSeconds(30));
        task.start(DEFAULT_NOW.plusSeconds(40));
        task.fail("timeout", DEFAULT_NOW.plusSeconds(50));
        task.start(DEFAULT_NOW.plusSeconds(60));
        task.fail("timeout", DEFAULT_NOW.plusSeconds(70));
        return task;
    }

    public static DownloadTask aTaskWithoutCallback() {
        return DownloadTask.forNew(
                DownloadTaskId.of("download-002"),
                SourceUrl.of("https://example.com/image.jpg"),
                StorageInfo.of("test-bucket", "public/2026/02/download-002.jpg", AccessType.PUBLIC),
                "product-image",
                "commerce-service",
                CallbackInfo.empty(),
                DEFAULT_NOW);
    }

    public static DownloadTask aReconstitutedTask() {
        return DownloadTask.reconstitute(
                DownloadTaskId.of("download-recon-001"),
                SourceUrl.of("https://example.com/image.jpg"),
                StorageInfo.of(
                        "test-bucket", "public/2026/02/download-recon-001.jpg", AccessType.PUBLIC),
                "product-image",
                "commerce-service",
                DownloadTaskStatus.DOWNLOADING,
                RetryPolicy.of(1, 3),
                CallbackInfo.of("https://callback.example.com/done"),
                "previous error",
                DEFAULT_NOW,
                DEFAULT_NOW.plusSeconds(10),
                DEFAULT_NOW.plusSeconds(10),
                null);
    }
}

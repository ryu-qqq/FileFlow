package com.ryuqq.fileflow.application.download;

import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.dto.command.RecoverZombieDownloadTaskCommand;
import com.ryuqq.fileflow.domain.common.vo.AccessType;

/**
 * Download Application Command 테스트 Fixtures.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class DownloadCommandFixtures {

    private DownloadCommandFixtures() {}

    // ===== CreateDownloadTaskCommand Fixtures =====

    public static CreateDownloadTaskCommand createCommand() {
        return new CreateDownloadTaskCommand(
                "https://example.com/image.jpg",
                "public/2026/02/download-001.jpg",
                "test-bucket",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service",
                "https://callback.example.com/done");
    }

    public static CreateDownloadTaskCommand createCommandWithoutCallback() {
        return new CreateDownloadTaskCommand(
                "https://example.com/image.jpg",
                "public/2026/02/download-002.jpg",
                "test-bucket",
                AccessType.PUBLIC,
                "product-image",
                "commerce-service",
                null);
    }

    public static CreateDownloadTaskCommand createCommand(
            String sourceUrl, String s3Key, AccessType accessType) {
        return new CreateDownloadTaskCommand(
                sourceUrl,
                s3Key,
                "test-bucket",
                accessType,
                "product-image",
                "commerce-service",
                "https://callback.example.com/done");
    }

    // ===== RecoverZombieDownloadTaskCommand Fixtures =====

    public static RecoverZombieDownloadTaskCommand recoverZombieCommand() {
        return RecoverZombieDownloadTaskCommand.of(100, 300);
    }

    public static RecoverZombieDownloadTaskCommand recoverZombieCommand(
            int batchSize, long timeoutSeconds) {
        return RecoverZombieDownloadTaskCommand.of(batchSize, timeoutSeconds);
    }
}

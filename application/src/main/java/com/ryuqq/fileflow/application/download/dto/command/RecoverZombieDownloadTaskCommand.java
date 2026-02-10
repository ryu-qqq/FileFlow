package com.ryuqq.fileflow.application.download.dto.command;

import java.time.Instant;

public record RecoverZombieDownloadTaskCommand(int batchSize, long timeoutSeconds) {

    public static RecoverZombieDownloadTaskCommand of(int batchSize, long timeoutSeconds) {
        return new RecoverZombieDownloadTaskCommand(batchSize, timeoutSeconds);
    }

    public Instant timeoutThreshold() {
        return Instant.now().minusSeconds(timeoutSeconds);
    }
}

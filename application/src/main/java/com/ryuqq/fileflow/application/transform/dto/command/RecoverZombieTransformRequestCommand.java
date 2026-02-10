package com.ryuqq.fileflow.application.transform.dto.command;

import java.time.Instant;

public record RecoverZombieTransformRequestCommand(int batchSize, long timeoutSeconds) {

    public static RecoverZombieTransformRequestCommand of(int batchSize, long timeoutSeconds) {
        return new RecoverZombieTransformRequestCommand(batchSize, timeoutSeconds);
    }

    public Instant timeoutThreshold() {
        return Instant.now().minusSeconds(timeoutSeconds);
    }
}

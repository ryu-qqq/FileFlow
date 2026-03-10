package com.ryuqq.fileflow.application.transform.port.in.command;

public interface RecoverStuckTransformOutboxUseCase {

    int execute(int stuckMinutes);
}

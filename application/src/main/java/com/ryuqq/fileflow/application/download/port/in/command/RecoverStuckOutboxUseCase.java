package com.ryuqq.fileflow.application.download.port.in.command;

public interface RecoverStuckOutboxUseCase {

    int execute(int stuckMinutes);
}

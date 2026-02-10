package com.ryuqq.fileflow.application.download.port.in.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.dto.command.RecoverZombieDownloadTaskCommand;

public interface RecoverZombieDownloadTaskUseCase {

    SchedulerBatchProcessingResult execute(RecoverZombieDownloadTaskCommand command);
}

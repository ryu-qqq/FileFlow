package com.ryuqq.fileflow.application.transform.port.in.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.transform.dto.command.RecoverZombieTransformRequestCommand;

public interface RecoverZombieTransformRequestUseCase {

    SchedulerBatchProcessingResult execute(RecoverZombieTransformRequestCommand command);
}

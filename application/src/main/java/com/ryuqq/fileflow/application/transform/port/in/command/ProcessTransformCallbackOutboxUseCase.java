package com.ryuqq.fileflow.application.transform.port.in.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;

public interface ProcessTransformCallbackOutboxUseCase {

    SchedulerBatchProcessingResult execute(int batchSize);
}

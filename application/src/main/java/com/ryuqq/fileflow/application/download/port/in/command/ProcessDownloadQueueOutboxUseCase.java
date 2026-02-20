package com.ryuqq.fileflow.application.download.port.in.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;

public interface ProcessDownloadQueueOutboxUseCase {

    SchedulerBatchProcessingResult execute(int batchSize);
}

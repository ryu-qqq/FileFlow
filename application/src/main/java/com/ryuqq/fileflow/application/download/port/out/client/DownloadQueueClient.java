package com.ryuqq.fileflow.application.download.port.out.client;

import com.ryuqq.fileflow.application.common.dto.result.OutboxBatchSendResult;
import java.util.List;

public interface DownloadQueueClient {

    void enqueue(String downloadTaskId);

    OutboxBatchSendResult enqueueBatch(List<String> downloadTaskIds);
}

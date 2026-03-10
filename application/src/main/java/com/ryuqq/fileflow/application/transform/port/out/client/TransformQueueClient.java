package com.ryuqq.fileflow.application.transform.port.out.client;

import com.ryuqq.fileflow.application.common.dto.result.OutboxBatchSendResult;
import java.util.List;

public interface TransformQueueClient {

    void enqueue(String transformRequestId);

    OutboxBatchSendResult enqueueBatch(List<String> transformRequestIds);
}

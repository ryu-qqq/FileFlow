package com.ryuqq.fileflow.application.transform.manager.client;

import com.ryuqq.fileflow.application.common.dto.result.OutboxBatchSendResult;
import com.ryuqq.fileflow.application.transform.port.out.client.TransformQueueClient;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TransformQueueManager {

    private final TransformQueueClient transformQueueClient;

    public TransformQueueManager(TransformQueueClient transformQueueClient) {
        this.transformQueueClient = transformQueueClient;
    }

    public void enqueue(String transformRequestId) {
        transformQueueClient.enqueue(transformRequestId);
    }

    public OutboxBatchSendResult enqueueBatch(List<String> transformRequestIds) {
        return transformQueueClient.enqueueBatch(transformRequestIds);
    }
}

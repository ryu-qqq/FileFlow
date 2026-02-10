package com.ryuqq.fileflow.application.transform.manager.client;

import com.ryuqq.fileflow.application.transform.port.out.client.TransformQueueClient;
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
}

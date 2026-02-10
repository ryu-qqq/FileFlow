package com.ryuqq.fileflow.application.transform.port.out.client;

public interface TransformQueueClient {

    void enqueue(String transformRequestId);
}

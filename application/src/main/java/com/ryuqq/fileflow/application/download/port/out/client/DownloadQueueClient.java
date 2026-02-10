package com.ryuqq.fileflow.application.download.port.out.client;

public interface DownloadQueueClient {

    void enqueue(String downloadTaskId);
}

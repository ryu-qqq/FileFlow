package com.ryuqq.fileflow.application.download.manager.client;

import com.ryuqq.fileflow.application.download.port.out.client.DownloadQueueClient;
import org.springframework.stereotype.Component;

@Component
public class DownloadQueueManager {

    private final DownloadQueueClient downloadQueueClient;

    public DownloadQueueManager(DownloadQueueClient downloadQueueClient) {
        this.downloadQueueClient = downloadQueueClient;
    }

    public void enqueue(String downloadTaskId) {
        downloadQueueClient.enqueue(downloadTaskId);
    }
}

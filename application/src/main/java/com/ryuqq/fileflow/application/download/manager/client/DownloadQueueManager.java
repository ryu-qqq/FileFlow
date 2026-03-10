package com.ryuqq.fileflow.application.download.manager.client;

import com.ryuqq.fileflow.application.common.dto.result.OutboxBatchSendResult;
import com.ryuqq.fileflow.application.download.port.out.client.DownloadQueueClient;
import java.util.List;
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

    public OutboxBatchSendResult enqueueBatch(List<String> downloadTaskIds) {
        return downloadQueueClient.enqueueBatch(downloadTaskIds);
    }
}

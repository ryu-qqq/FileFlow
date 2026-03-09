package com.ryuqq.fileflow.application.download.manager.command;

import com.ryuqq.fileflow.application.download.port.out.command.DownloadTaskPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DownloadCommandManager {

    private final DownloadTaskPersistencePort downloadTaskPersistencePort;

    public DownloadCommandManager(DownloadTaskPersistencePort downloadTaskPersistencePort) {
        this.downloadTaskPersistencePort = downloadTaskPersistencePort;
    }

    @Transactional
    public void persist(DownloadTask downloadTask) {
        downloadTaskPersistencePort.persist(downloadTask);
    }

    @Transactional
    public void markFailedById(String downloadTaskId, String errorMessage, Instant failedAt) {
        downloadTaskPersistencePort.markFailedById(downloadTaskId, errorMessage, failedAt);
    }
}

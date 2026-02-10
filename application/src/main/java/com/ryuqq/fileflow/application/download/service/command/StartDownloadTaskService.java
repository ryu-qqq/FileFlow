package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.download.internal.DownloadExecutionCoordinator;
import com.ryuqq.fileflow.application.download.port.in.command.StartDownloadTaskUseCase;
import com.ryuqq.fileflow.application.download.validator.DownloadTaskValidator;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import org.springframework.stereotype.Service;

@Service
public class StartDownloadTaskService implements StartDownloadTaskUseCase {

    private final DownloadTaskValidator downloadTaskValidator;
    private final DownloadExecutionCoordinator downloadExecutionCoordinator;

    public StartDownloadTaskService(
            DownloadTaskValidator downloadTaskValidator,
            DownloadExecutionCoordinator downloadExecutionCoordinator) {
        this.downloadTaskValidator = downloadTaskValidator;
        this.downloadExecutionCoordinator = downloadExecutionCoordinator;
    }

    @Override
    public void execute(String downloadTaskId) {
        DownloadTask downloadTask = downloadTaskValidator.getExistingTask(downloadTaskId);
        downloadExecutionCoordinator.execute(downloadTask);
    }
}

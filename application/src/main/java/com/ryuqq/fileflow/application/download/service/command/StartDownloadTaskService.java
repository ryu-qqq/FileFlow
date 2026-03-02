package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.download.internal.DownloadExecutionCoordinator;
import com.ryuqq.fileflow.application.download.port.in.command.StartDownloadTaskUseCase;
import com.ryuqq.fileflow.application.download.validator.DownloadTaskValidator;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StartDownloadTaskService implements StartDownloadTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(StartDownloadTaskService.class);

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
        DownloadTaskStatus currentStatus = downloadTask.status();

        if (currentStatus != DownloadTaskStatus.QUEUED) {
            log.warn(
                    "QUEUED가 아닌 상태의 태스크, 처리 건너뜀: taskId={}, status={}",
                    downloadTaskId,
                    currentStatus);
            return;
        }

        downloadExecutionCoordinator.execute(downloadTask);
    }
}

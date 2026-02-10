package com.ryuqq.fileflow.application.download.internal;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadCompletionBundle;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadFailureBundle;
import com.ryuqq.fileflow.application.download.dto.response.FileDownloadResult;
import com.ryuqq.fileflow.application.download.factory.command.DownloadCommandFactory;
import com.ryuqq.fileflow.application.download.manager.client.DownloadQueueManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadCommandManager;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DownloadExecutionCoordinator {

    private static final Logger log = LoggerFactory.getLogger(DownloadExecutionCoordinator.class);

    private final DownloadCommandFactory downloadCommandFactory;
    private final FileTransferFacade fileTransferFacade;
    private final DownloadCommandManager downloadCommandManager;
    private final DownloadCompletionFacade downloadCompletionFacade;
    private final DownloadQueueManager downloadQueueManager;

    public DownloadExecutionCoordinator(
            DownloadCommandFactory downloadCommandFactory,
            FileTransferFacade fileTransferFacade,
            DownloadCommandManager downloadCommandManager,
            DownloadCompletionFacade downloadCompletionFacade,
            DownloadQueueManager downloadQueueManager) {
        this.downloadCommandFactory = downloadCommandFactory;
        this.fileTransferFacade = fileTransferFacade;
        this.downloadCommandManager = downloadCommandManager;
        this.downloadCompletionFacade = downloadCompletionFacade;
        this.downloadQueueManager = downloadQueueManager;
    }

    public void execute(DownloadTask downloadTask) {
        StatusChangeContext<String> context =
                downloadCommandFactory.createStartContext(downloadTask.idValue());
        downloadTask.start(context.changedAt());
        downloadCommandManager.persist(downloadTask);

        FileDownloadResult result = fileTransferFacade.transfer(downloadTask);

        if (result.success()) {
            DownloadCompletionBundle bundle =
                    downloadCommandFactory.createCompletionBundle(downloadTask, result);
            downloadCompletionFacade.completeDownload(bundle);
            log.info("다운로드 완료: taskId={}", downloadTask.idValue());
        } else {
            DownloadFailureBundle failureBundle =
                    downloadCommandFactory.createFailureBundle(downloadTask, result.errorMessage());
            downloadCompletionFacade.failDownload(failureBundle);

            if (failureBundle.canRetry()) {
                downloadQueueManager.enqueue(downloadTask.idValue());
            }
            log.error(
                    "다운로드 실패 처리: taskId={}, error={}",
                    downloadTask.idValue(),
                    result.errorMessage());
        }
    }
}

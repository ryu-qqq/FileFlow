package com.ryuqq.fileflow.application.download.internal;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadCompletionBundle;
import com.ryuqq.fileflow.application.download.dto.bundle.DownloadFailureBundle;
import com.ryuqq.fileflow.application.download.dto.response.FileDownloadResult;
import com.ryuqq.fileflow.application.download.factory.command.DownloadCommandFactory;
import com.ryuqq.fileflow.application.download.manager.client.DownloadQueueManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadCommandManager;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
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
        if (downloadTask.status() == DownloadTaskStatus.QUEUED) {
            StatusChangeContext<String> context =
                    downloadCommandFactory.createStartContext(downloadTask.idValue());
            downloadTask.start(context.changedAt());
            downloadCommandManager.persist(downloadTask);

            log.info(
                    "다운로드 시작 persist 완료: taskId={}, version={}",
                    downloadTask.idValue(),
                    downloadTask.version());
        } else if (downloadTask.status() == DownloadTaskStatus.DOWNLOADING) {
            log.warn(
                    "DOWNLOADING 상태 태스크 복구 시도: taskId={}, version={}",
                    downloadTask.idValue(),
                    downloadTask.version());
        } else {
            log.warn(
                    "처리 불필요한 상태: taskId={}, status={}",
                    downloadTask.idValue(),
                    downloadTask.status());
            return;
        }

        try {
            log.info("파일 전송 시작: taskId={}", downloadTask.idValue());
            FileDownloadResult result = fileTransferFacade.transfer(downloadTask);
            log.info(
                    "파일 전송 결과: taskId={}, success={}, error={}",
                    downloadTask.idValue(),
                    result.success(),
                    result.errorMessage());

            if (result.success()) {
                DownloadCompletionBundle bundle =
                        downloadCommandFactory.createCompletionBundle(downloadTask, result);
                downloadCompletionFacade.completeDownload(bundle);
                log.info("다운로드 완료: taskId={}", downloadTask.idValue());
            } else {
                failDownload(downloadTask, result.errorMessage());
            }
        } catch (Exception e) {
            log.error(
                    "다운로드 중 예외 발생: taskId={}", downloadTask.idValue(), e);
            safeFailDownload(downloadTask, e.getMessage());
        }
    }

    private void failDownload(DownloadTask downloadTask, String errorMessage) {
        DownloadFailureBundle failureBundle =
                downloadCommandFactory.createFailureBundle(downloadTask, errorMessage);

        log.info(
                "실패 persist 시작: taskId={}, version={}, status={}",
                downloadTask.idValue(),
                downloadTask.version(),
                downloadTask.status());

        downloadCompletionFacade.failDownload(failureBundle);

        log.info(
                "실패 persist 완료: taskId={}, version={}",
                downloadTask.idValue(),
                downloadTask.version());

        if (failureBundle.canRetry()) {
            downloadQueueManager.enqueue(downloadTask.idValue());
        }
        log.error(
                "다운로드 실패 처리: taskId={}, error={}",
                downloadTask.idValue(),
                errorMessage);
    }

    private void safeFailDownload(DownloadTask downloadTask, String errorMessage) {
        try {
            failDownload(downloadTask, errorMessage);
        } catch (Exception failEx) {
            log.error(
                    "failDownload 자체도 실패, 직접 persist 시도: taskId={}",
                    downloadTask.idValue(),
                    failEx);
            try {
                if (downloadTask.status() != DownloadTaskStatus.FAILED
                        && downloadTask.status() != DownloadTaskStatus.QUEUED) {
                    downloadTask.fail(errorMessage, Instant.now());
                }
                downloadCommandManager.persist(downloadTask);
                log.info(
                        "직접 persist 성공: taskId={}, version={}",
                        downloadTask.idValue(),
                        downloadTask.version());
            } catch (Exception lastResort) {
                log.error(
                        "최종 persist도 실패, 태스크 DOWNLOADING 상태로 stuck 예상: taskId={}",
                        downloadTask.idValue(),
                        lastResort);
            }
        }
    }
}

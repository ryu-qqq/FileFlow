package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.dto.command.RecoverZombieDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.manager.client.DownloadQueueManager;
import com.ryuqq.fileflow.application.download.manager.query.DownloadReadManager;
import com.ryuqq.fileflow.application.download.port.in.command.RecoverZombieDownloadTaskUseCase;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RecoverZombieDownloadTaskService implements RecoverZombieDownloadTaskUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RecoverZombieDownloadTaskService.class);

    private final DownloadReadManager downloadReadManager;
    private final DownloadQueueManager downloadQueueManager;

    public RecoverZombieDownloadTaskService(
            DownloadReadManager downloadReadManager, DownloadQueueManager downloadQueueManager) {
        this.downloadReadManager = downloadReadManager;
        this.downloadQueueManager = downloadQueueManager;
    }

    @Override
    public SchedulerBatchProcessingResult execute(RecoverZombieDownloadTaskCommand command) {
        List<DownloadTask> staleTasks =
                downloadReadManager.getStaleQueuedTasks(
                        command.timeoutThreshold(), command.batchSize());

        int total = staleTasks.size();
        int successCount = 0;
        int failedCount = 0;

        for (DownloadTask task : staleTasks) {
            try {
                downloadQueueManager.enqueue(task.idValue());
                successCount++;
            } catch (Exception e) {
                log.error("좀비 태스크 재큐잉 실패: taskId={}, error={}", task.idValue(), e.getMessage(), e);
                failedCount++;
            }
        }

        return SchedulerBatchProcessingResult.of(total, successCount, failedCount);
    }
}

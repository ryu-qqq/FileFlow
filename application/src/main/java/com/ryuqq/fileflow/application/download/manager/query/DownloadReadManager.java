package com.ryuqq.fileflow.application.download.manager.query;

import com.ryuqq.fileflow.application.download.port.out.query.DownloadTaskQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.exception.DownloadTaskNotFoundException;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DownloadReadManager {

    private final DownloadTaskQueryPort downloadTaskQueryPort;

    public DownloadReadManager(DownloadTaskQueryPort downloadTaskQueryPort) {
        this.downloadTaskQueryPort = downloadTaskQueryPort;
    }

    @Transactional(readOnly = true)
    public DownloadTask getDownloadTask(String downloadTaskId) {
        return downloadTaskQueryPort
                .findById(DownloadTaskId.of(downloadTaskId))
                .orElseThrow(() -> new DownloadTaskNotFoundException(downloadTaskId));
    }

    @Transactional(readOnly = true)
    public List<DownloadTask> getStaleQueuedTasks(Instant createdBefore, int limit) {
        return downloadTaskQueryPort.findByStatusAndCreatedBefore(
                DownloadTaskStatus.QUEUED, createdBefore, limit);
    }
}

package com.ryuqq.fileflow.application.download.port.out.query;

import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DownloadTaskQueryPort {

    Optional<DownloadTask> findById(DownloadTaskId id);

    List<DownloadTask> findByStatusAndCreatedBefore(
            DownloadTaskStatus status, Instant createdBefore, int limit);
}

package com.ryuqq.fileflow.application.download.port.out.command;

import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import java.time.Instant;
import java.util.List;

public interface DownloadQueueOutboxPersistencePort {

    void persist(DownloadQueueOutbox outbox);

    void bulkMarkSent(List<String> ids, Instant now);

    void bulkMarkFailed(List<String> ids, Instant now);
}

package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.DownloadTaskJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadTaskJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.command.DownloadTaskPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class DownloadTaskCommandAdapter implements DownloadTaskPersistencePort {

    private final DownloadTaskJpaRepository jpaRepository;
    private final DownloadTaskJpaMapper mapper;

    public DownloadTaskCommandAdapter(
            DownloadTaskJpaRepository jpaRepository, DownloadTaskJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public long persist(DownloadTask downloadTask) {
        DownloadTaskJpaEntity entity = mapper.toEntity(downloadTask);
        DownloadTaskJpaEntity saved = jpaRepository.save(entity);
        return saved.getVersion();
    }

    private static final Set<DownloadTaskStatus> TERMINAL_STATUSES =
            Set.of(DownloadTaskStatus.COMPLETED, DownloadTaskStatus.FAILED);

    @Override
    public void markFailedById(String downloadTaskId, String errorMessage, Instant failedAt) {
        jpaRepository.updateStatusAndError(
                downloadTaskId,
                DownloadTaskStatus.FAILED,
                errorMessage,
                failedAt,
                TERMINAL_STATUSES);
    }
}

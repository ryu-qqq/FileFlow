package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.DownloadTaskJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadTaskJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.command.DownloadTaskPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
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
    public void persist(DownloadTask downloadTask) {
        DownloadTaskJpaEntity entity = mapper.toEntity(downloadTask);
        jpaRepository.save(entity);
    }
}

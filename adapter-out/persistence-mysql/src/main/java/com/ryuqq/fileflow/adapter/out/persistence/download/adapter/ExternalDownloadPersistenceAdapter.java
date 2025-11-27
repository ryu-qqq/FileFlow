package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.ExternalDownloadJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.ExternalDownloadJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.command.ExternalDownloadPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import org.springframework.stereotype.Repository;

/**
 * ExternalDownload Persistence Adapter.
 *
 * <p>ExternalDownload의 저장/수정을 담당합니다.
 */
@Repository
public class ExternalDownloadPersistenceAdapter implements ExternalDownloadPersistencePort {

    private final ExternalDownloadJpaRepository jpaRepository;
    private final ExternalDownloadJpaMapper mapper;

    public ExternalDownloadPersistenceAdapter(
            ExternalDownloadJpaRepository jpaRepository, ExternalDownloadJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ExternalDownloadId persist(ExternalDownload externalDownload) {
        ExternalDownloadJpaEntity entity = mapper.toEntity(externalDownload);
        ExternalDownloadJpaEntity savedEntity = jpaRepository.save(entity);
        return ExternalDownloadId.of(savedEntity.getId());
    }
}

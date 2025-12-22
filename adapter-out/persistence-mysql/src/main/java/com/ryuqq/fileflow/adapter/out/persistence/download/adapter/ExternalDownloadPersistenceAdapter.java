package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.ExternalDownloadJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.ExternalDownloadJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.command.ExternalDownloadPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/**
 * ExternalDownload Persistence Adapter.
 *
 * <p><strong>@Version 필드 동기화</strong>:
 *
 * <ul>
 *   <li>JPA의 @Version 필드는 트랜잭션 커밋 시점에 자동 증가
 *   <li>save() 직후에는 version이 아직 갱신되지 않음
 *   <li>saveAndFlush() + refresh()로 즉시 version 동기화 필요
 * </ul>
 */
@Repository
public class ExternalDownloadPersistenceAdapter implements ExternalDownloadPersistencePort {

    private final ExternalDownloadJpaRepository jpaRepository;
    private final ExternalDownloadJpaMapper mapper;

    @PersistenceContext private EntityManager entityManager;

    public ExternalDownloadPersistenceAdapter(
            ExternalDownloadJpaRepository jpaRepository, ExternalDownloadJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ExternalDownload persist(ExternalDownload externalDownload) {
        ExternalDownloadJpaEntity entity = mapper.toEntity(externalDownload);
        ExternalDownloadJpaEntity savedEntity = jpaRepository.saveAndFlush(entity);
        // @Version 필드 즉시 동기화 (flush 후 DB에서 갱신된 version 반영)
        entityManager.refresh(savedEntity);
        return mapper.toDomain(savedEntity);
    }
}

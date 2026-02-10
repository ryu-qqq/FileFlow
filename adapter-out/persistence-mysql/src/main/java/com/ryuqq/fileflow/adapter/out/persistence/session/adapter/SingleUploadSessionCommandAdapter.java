package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.SingleUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.SingleUploadSessionJpaRepository;
import com.ryuqq.fileflow.application.session.port.out.command.SingleUploadSessionPersistencePort;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import org.springframework.stereotype.Component;

@Component
public class SingleUploadSessionCommandAdapter implements SingleUploadSessionPersistencePort {

    private final SingleUploadSessionJpaRepository jpaRepository;
    private final SingleUploadSessionJpaMapper mapper;

    public SingleUploadSessionCommandAdapter(
            SingleUploadSessionJpaRepository jpaRepository, SingleUploadSessionJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persist(SingleUploadSession session) {
        SingleUploadSessionJpaEntity entity = mapper.toEntity(session);
        jpaRepository.save(entity);
    }
}

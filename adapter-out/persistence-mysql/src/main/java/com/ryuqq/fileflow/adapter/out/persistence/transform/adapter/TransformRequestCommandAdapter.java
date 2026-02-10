package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformRequestJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformRequestJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformRequestJpaRepository;
import com.ryuqq.fileflow.application.transform.port.out.command.TransformRequestPersistencePort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import org.springframework.stereotype.Component;

@Component
public class TransformRequestCommandAdapter implements TransformRequestPersistencePort {

    private final TransformRequestJpaRepository jpaRepository;
    private final TransformRequestJpaMapper mapper;

    public TransformRequestCommandAdapter(
            TransformRequestJpaRepository jpaRepository, TransformRequestJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persist(TransformRequest transformRequest) {
        TransformRequestJpaEntity entity = mapper.toEntity(transformRequest);
        jpaRepository.save(entity);
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.CompletedPartJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.MultipartUploadSessionJpaRepository;
import com.ryuqq.fileflow.application.session.port.out.command.MultipartUploadSessionPersistencePort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MultipartUploadSessionCommandAdapter implements MultipartUploadSessionPersistencePort {

    private final MultipartUploadSessionJpaRepository jpaRepository;
    private final CompletedPartJpaRepository completedPartJpaRepository;
    private final MultipartUploadSessionJpaMapper mapper;

    public MultipartUploadSessionCommandAdapter(
            MultipartUploadSessionJpaRepository jpaRepository,
            CompletedPartJpaRepository completedPartJpaRepository,
            MultipartUploadSessionJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.completedPartJpaRepository = completedPartJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persist(MultipartUploadSession session) {
        MultipartUploadSessionJpaEntity entity = mapper.toEntity(session);
        jpaRepository.save(entity);

        completedPartJpaRepository.deleteBySessionId(session.idValue());
        List<CompletedPartJpaEntity> partEntities =
                mapper.toPartEntities(session.idValue(), session.completedParts());
        if (!partEntities.isEmpty()) {
            completedPartJpaRepository.saveAll(partEntities);
        }
    }
}

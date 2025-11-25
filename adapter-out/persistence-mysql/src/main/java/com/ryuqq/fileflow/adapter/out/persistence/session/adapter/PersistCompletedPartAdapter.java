package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.CompletedPartJpaRepository;
import com.ryuqq.fileflow.application.session.port.out.command.PersistCompletedPartPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * CompletedPart 영속화 Command Adapter.
 *
 * <p>PersistCompletedPartPort를 구현하여 RDB에 개별 파트를 저장합니다.
 */
@Component
public class PersistCompletedPartAdapter implements PersistCompletedPartPort {

    private final CompletedPartJpaRepository repository;
    private final MultipartUploadSessionJpaMapper mapper;

    public PersistCompletedPartAdapter(
            CompletedPartJpaRepository repository, MultipartUploadSessionJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CompletedPart persist(UploadSessionId sessionId, CompletedPart completedPart) {
        String sessionIdValue = sessionId.getValue();
        CompletedPartJpaEntity entity = mapper.toPartEntity(sessionIdValue, completedPart);
        CompletedPartJpaEntity savedEntity = repository.save(entity);
        return mapper.toCompletedPart(savedEntity);
    }

    @Override
    public void persistAll(List<CompletedPart> completedParts) {
        List<CompletedPartJpaEntity> entities =
                completedParts.stream()
                        .map(
                                part ->
                                        mapper.toPartEntity(
                                                part.getSessionId().value().toString(), part))
                        .toList();
        repository.saveAll(entities);
    }
}

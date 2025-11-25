package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.SingleUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.SingleUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.SingleUploadSessionJpaRepository;
import com.ryuqq.fileflow.application.session.port.out.command.PersistSingleUploadSessionPort;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import org.springframework.stereotype.Component;

/**
 * 단일 업로드 세션 영속화 Command Adapter.
 *
 * <p>PersistSingleUploadSessionPort를 구현하여 RDB에 세션을 저장합니다.
 */
@Component
public class PersistSingleUploadSessionAdapter implements PersistSingleUploadSessionPort {

    private final SingleUploadSessionJpaRepository repository;
    private final SingleUploadSessionJpaMapper mapper;

    public PersistSingleUploadSessionAdapter(
            SingleUploadSessionJpaRepository repository, SingleUploadSessionJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public SingleUploadSession persist(SingleUploadSession session) {
        SingleUploadSessionJpaEntity entity = mapper.toEntity(session);
        SingleUploadSessionJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}

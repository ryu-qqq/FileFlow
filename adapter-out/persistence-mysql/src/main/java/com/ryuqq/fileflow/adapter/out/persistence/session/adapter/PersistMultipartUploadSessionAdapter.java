package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.MultipartUploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.MultipartUploadSessionJpaRepository;
import com.ryuqq.fileflow.application.session.port.out.command.PersistMultipartUploadSessionPort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import org.springframework.stereotype.Component;

/**
 * Multipart 업로드 세션 영속화 Command Adapter.
 *
 * <p>PersistMultipartUploadSessionPort를 구현하여 RDB에 세션을 저장합니다.
 */
@Component
public class PersistMultipartUploadSessionAdapter implements PersistMultipartUploadSessionPort {

    private final MultipartUploadSessionJpaRepository sessionRepository;
    private final MultipartUploadSessionJpaMapper mapper;

    public PersistMultipartUploadSessionAdapter(
            MultipartUploadSessionJpaRepository sessionRepository,
            MultipartUploadSessionJpaMapper mapper) {
        this.sessionRepository = sessionRepository;
        this.mapper = mapper;
    }

    @Override
    public MultipartUploadSession persist(MultipartUploadSession session) {
        MultipartUploadSessionJpaEntity entity = mapper.toEntity(session);
        MultipartUploadSessionJpaEntity savedEntity = sessionRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}

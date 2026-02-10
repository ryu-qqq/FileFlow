package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.session.entity.CompletedPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.MultipartUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.MultipartUploadSessionQueryDslRepository;
import com.ryuqq.fileflow.application.session.port.out.query.MultipartUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MultipartUploadSessionQueryAdapter implements MultipartUploadSessionQueryPort {

    private final MultipartUploadSessionQueryDslRepository queryDslRepository;
    private final MultipartUploadSessionJpaMapper mapper;

    public MultipartUploadSessionQueryAdapter(
            MultipartUploadSessionQueryDslRepository queryDslRepository,
            MultipartUploadSessionJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<MultipartUploadSession> findById(MultipartUploadSessionId id) {
        return queryDslRepository
                .findById(id.value())
                .map(
                        entity -> {
                            List<CompletedPartJpaEntity> parts =
                                    queryDslRepository.findCompletedPartsBySessionId(id.value());
                            return mapper.toDomain(entity, parts);
                        });
    }
}

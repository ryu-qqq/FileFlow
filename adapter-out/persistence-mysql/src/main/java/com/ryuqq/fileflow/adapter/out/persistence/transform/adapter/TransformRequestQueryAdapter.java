package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformRequestJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformRequestQueryDslRepository;
import com.ryuqq.fileflow.application.transform.port.out.query.TransformRequestQueryPort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.id.TransformRequestId;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TransformRequestQueryAdapter implements TransformRequestQueryPort {

    private final TransformRequestQueryDslRepository queryDslRepository;
    private final TransformRequestJpaMapper mapper;

    public TransformRequestQueryAdapter(
            TransformRequestQueryDslRepository queryDslRepository,
            TransformRequestJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<TransformRequest> findById(TransformRequestId id) {
        return queryDslRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<TransformRequest> findByStatusAndCreatedBefore(
            TransformStatus status, Instant createdBefore, int limit) {
        return queryDslRepository
                .findByStatusAndCreatedBefore(status, createdBefore, limit)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

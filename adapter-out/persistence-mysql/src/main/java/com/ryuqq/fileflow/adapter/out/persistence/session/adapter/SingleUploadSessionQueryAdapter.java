package com.ryuqq.fileflow.adapter.out.persistence.session.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.session.mapper.SingleUploadSessionJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.session.repository.SingleUploadSessionQueryDslRepository;
import com.ryuqq.fileflow.application.session.port.out.query.SingleUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SingleUploadSessionQueryAdapter implements SingleUploadSessionQueryPort {

    private final SingleUploadSessionQueryDslRepository queryDslRepository;
    private final SingleUploadSessionJpaMapper mapper;

    public SingleUploadSessionQueryAdapter(
            SingleUploadSessionQueryDslRepository queryDslRepository,
            SingleUploadSessionJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<SingleUploadSession> findById(SingleUploadSessionId id) {
        return queryDslRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<SingleUploadSession> findExpiredSessions(Instant now, int limit) {
        return queryDslRepository.findExpiredSessions(now, limit).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.ExternalDownloadOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.ExternalDownloadOutboxQueryDslRepository;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * ExternalDownloadOutbox Query Adapter.
 *
 * <p>ExternalDownloadOutbox의 조회를 담당합니다.
 */
@Component
public class ExternalDownloadOutboxQueryAdapter implements ExternalDownloadOutboxQueryPort {

    private final ExternalDownloadOutboxQueryDslRepository queryDslRepository;
    private final ExternalDownloadOutboxJpaMapper mapper;

    public ExternalDownloadOutboxQueryAdapter(
            ExternalDownloadOutboxQueryDslRepository queryDslRepository,
            ExternalDownloadOutboxJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ExternalDownloadOutbox> findByExternalDownloadId(
            ExternalDownloadId externalDownloadId) {
        return queryDslRepository
                .findByExternalDownloadId(externalDownloadId.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<ExternalDownloadOutbox> findUnpublished(int limit) {
        return queryDslRepository.findUnpublished(limit).stream().map(mapper::toDomain).toList();
    }
}

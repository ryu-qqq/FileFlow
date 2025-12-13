package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.ExternalDownloadJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.ExternalDownloadQueryDslRepository;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * ExternalDownload Query Adapter.
 *
 * <p>ExternalDownload의 조회를 담당합니다.
 */
@Component
public class ExternalDownloadQueryAdapter implements ExternalDownloadQueryPort {

    private final ExternalDownloadQueryDslRepository queryDslRepository;
    private final ExternalDownloadJpaMapper mapper;

    public ExternalDownloadQueryAdapter(
            ExternalDownloadQueryDslRepository queryDslRepository,
            ExternalDownloadJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ExternalDownload> findById(ExternalDownloadId id) {
        return queryDslRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<ExternalDownload> findByIdAndTenantId(ExternalDownloadId id, String tenantId) {
        return queryDslRepository.findByIdAndTenantId(id.value(), tenantId).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(ExternalDownloadId id) {
        return queryDslRepository.existsById(id.value());
    }
}

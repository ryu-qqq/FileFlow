package com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.mapper.FileProcessingOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.outbox.repository.FileProcessingOutboxQueryDslRepository;
import com.ryuqq.fileflow.application.asset.port.out.query.FileProcessingOutboxQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * FileProcessingOutbox Query Adapter.
 *
 * <p>FileProcessingOutbox의 조회를 담당합니다.
 */
@Component
public class FileProcessingOutboxQueryAdapter implements FileProcessingOutboxQueryPort {

    private final FileProcessingOutboxQueryDslRepository queryDslRepository;
    private final FileProcessingOutboxJpaMapper mapper;

    public FileProcessingOutboxQueryAdapter(
            FileProcessingOutboxQueryDslRepository queryDslRepository,
            FileProcessingOutboxJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<FileProcessingOutbox> findById(FileProcessingOutboxId outboxId) {
        return queryDslRepository.findById(outboxId.value()).map(mapper::toDomain);
    }

    @Override
    public List<FileProcessingOutbox> findPendingEvents(int limit) {
        return queryDslRepository.findPendingEvents(limit).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<FileProcessingOutbox> findRetryableFailedEvents(int limit) {
        return queryDslRepository.findRetryableFailedEvents(limit).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

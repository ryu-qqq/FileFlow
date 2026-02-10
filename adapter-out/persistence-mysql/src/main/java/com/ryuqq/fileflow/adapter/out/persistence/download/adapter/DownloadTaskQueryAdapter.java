package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.DownloadTaskJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadTaskQueryDslRepository;
import com.ryuqq.fileflow.application.download.port.out.query.DownloadTaskQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DownloadTaskQueryAdapter implements DownloadTaskQueryPort {

    private final DownloadTaskQueryDslRepository queryDslRepository;
    private final DownloadTaskJpaMapper mapper;

    public DownloadTaskQueryAdapter(
            DownloadTaskQueryDslRepository queryDslRepository, DownloadTaskJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<DownloadTask> findById(DownloadTaskId id) {
        return queryDslRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<DownloadTask> findByStatusAndCreatedBefore(
            DownloadTaskStatus status, Instant createdBefore, int limit) {
        return queryDslRepository
                .findByStatusAndCreatedBefore(status, createdBefore, limit)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

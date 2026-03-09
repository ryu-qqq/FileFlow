package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.DownloadTaskJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadTaskJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.DownloadTaskQueryDslRepository;
import com.ryuqq.fileflow.application.download.port.out.query.DownloadTaskQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.id.DownloadTaskId;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DownloadTaskQueryAdapter implements DownloadTaskQueryPort {

    private static final Logger log = LoggerFactory.getLogger(DownloadTaskQueryAdapter.class);

    private final DownloadTaskQueryDslRepository queryDslRepository;
    private final DownloadTaskJpaRepository jpaRepository;
    private final DownloadTaskJpaMapper mapper;

    public DownloadTaskQueryAdapter(
            DownloadTaskQueryDslRepository queryDslRepository,
            DownloadTaskJpaRepository jpaRepository,
            DownloadTaskJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<DownloadTask> findById(DownloadTaskId id) {
        return queryDslRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<DownloadTask> findByStatusAndCreatedBefore(
            DownloadTaskStatus status, Instant createdBefore, int limit) {
        List<DownloadTaskJpaEntity> entities =
                queryDslRepository.findByStatusAndCreatedBefore(status, createdBefore, limit);

        List<DownloadTask> result = new ArrayList<>();
        for (DownloadTaskJpaEntity entity : entities) {
            try {
                result.add(mapper.toDomain(entity));
            } catch (Exception e) {
                log.error(
                        "corrupted 데이터 감지, FAILED 처리: taskId={}, error={}",
                        entity.getId(),
                        e.getMessage());
                jpaRepository.updateStatusAndError(
                        entity.getId(),
                        DownloadTaskStatus.FAILED,
                        "corrupted data: " + e.getMessage(),
                        Instant.now());
            }
        }
        return result;
    }
}

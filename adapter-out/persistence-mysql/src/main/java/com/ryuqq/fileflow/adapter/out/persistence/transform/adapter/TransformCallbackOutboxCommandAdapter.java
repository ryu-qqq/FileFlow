package com.ryuqq.fileflow.adapter.out.persistence.transform.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.transform.mapper.TransformCallbackOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.transform.repository.TransformCallbackOutboxJpaRepository;
import com.ryuqq.fileflow.application.transform.port.out.command.TransformCallbackOutboxPersistencePort;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TransformCallbackOutboxCommandAdapter
        implements TransformCallbackOutboxPersistencePort {

    private final TransformCallbackOutboxJpaRepository transformCallbackOutboxJpaRepository;
    private final TransformCallbackOutboxJpaMapper transformCallbackOutboxJpaMapper;

    public TransformCallbackOutboxCommandAdapter(
            TransformCallbackOutboxJpaRepository transformCallbackOutboxJpaRepository,
            TransformCallbackOutboxJpaMapper transformCallbackOutboxJpaMapper) {
        this.transformCallbackOutboxJpaRepository = transformCallbackOutboxJpaRepository;
        this.transformCallbackOutboxJpaMapper = transformCallbackOutboxJpaMapper;
    }

    @Override
    public void persist(TransformCallbackOutbox outbox) {
        transformCallbackOutboxJpaRepository.save(
                transformCallbackOutboxJpaMapper.toEntity(outbox));
    }

    @Override
    public void bulkMarkSent(List<String> ids, Instant now) {
        if (ids.isEmpty()) return;
        transformCallbackOutboxJpaRepository.bulkMarkSent(ids, now);
    }

    @Override
    public void bulkMarkFailed(List<String> ids, Instant now, String lastError) {
        if (ids.isEmpty()) return;
        transformCallbackOutboxJpaRepository.bulkMarkFailed(ids, now, lastError);
    }

    @Override
    public int recoverStuckProcessing(Instant cutoff) {
        return transformCallbackOutboxJpaRepository.recoverStuckProcessing(cutoff);
    }
}

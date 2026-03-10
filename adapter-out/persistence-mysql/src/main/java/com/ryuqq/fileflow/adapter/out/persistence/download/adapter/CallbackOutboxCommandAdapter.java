package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.CallbackOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.CallbackOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.CallbackOutboxJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.command.CallbackOutboxPersistencePort;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CallbackOutboxCommandAdapter implements CallbackOutboxPersistencePort {

    private final CallbackOutboxJpaRepository jpaRepository;
    private final CallbackOutboxJpaMapper mapper;

    public CallbackOutboxCommandAdapter(
            CallbackOutboxJpaRepository jpaRepository, CallbackOutboxJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void persist(CallbackOutbox callbackOutbox) {
        CallbackOutboxJpaEntity entity = mapper.toEntity(callbackOutbox);
        jpaRepository.save(entity);
    }

    @Override
    public void bulkMarkSent(List<String> ids, Instant now) {
        if (ids.isEmpty()) return;
        jpaRepository.bulkMarkSent(ids, now);
    }

    @Override
    public void bulkMarkFailed(List<String> ids, Instant now) {
        if (ids.isEmpty()) return;
        jpaRepository.bulkMarkFailed(ids, now);
    }

    @Override
    public int recoverStuckProcessing(Instant cutoff) {
        return jpaRepository.recoverStuckProcessing(cutoff);
    }
}

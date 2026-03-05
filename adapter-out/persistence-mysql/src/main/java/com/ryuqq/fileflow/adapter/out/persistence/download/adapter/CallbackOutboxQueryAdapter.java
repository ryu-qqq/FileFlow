package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.CallbackOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.CallbackOutboxJpaRepository;
import com.ryuqq.fileflow.application.download.port.out.query.CallbackOutboxQueryPort;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class CallbackOutboxQueryAdapter implements CallbackOutboxQueryPort {

    private final CallbackOutboxJpaRepository jpaRepository;
    private final CallbackOutboxJpaMapper mapper;

    public CallbackOutboxQueryAdapter(
            CallbackOutboxJpaRepository jpaRepository, CallbackOutboxJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<CallbackOutbox> findPendingMessages(int limit) {
        return jpaRepository
                .findByOutboxStatusOrderByCreatedAtAsc(
                        OutboxStatus.PENDING, PageRequest.of(0, limit))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.WebhookOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.WebhookOutboxQueryDslRepository;
import com.ryuqq.fileflow.application.download.port.out.query.WebhookOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.WebhookOutbox;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxId;
import com.ryuqq.fileflow.domain.download.vo.WebhookOutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * WebhookOutbox Query Adapter.
 *
 * <p>WebhookOutbox의 조회를 담당합니다.
 */
@Component
public class WebhookOutboxQueryAdapter implements WebhookOutboxQueryPort {

    private final WebhookOutboxQueryDslRepository queryDslRepository;
    private final WebhookOutboxJpaMapper mapper;

    public WebhookOutboxQueryAdapter(
            WebhookOutboxQueryDslRepository queryDslRepository, WebhookOutboxJpaMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<WebhookOutbox> findById(WebhookOutboxId id) {
        return queryDslRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<WebhookOutbox> findByStatusForRetry(WebhookOutboxStatus status, int limit) {
        return queryDslRepository.findByStatusForRetry(status, limit).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

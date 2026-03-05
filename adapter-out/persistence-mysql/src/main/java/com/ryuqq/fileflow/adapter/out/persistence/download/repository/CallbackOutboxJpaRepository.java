package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.CallbackOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallbackOutboxJpaRepository
        extends JpaRepository<CallbackOutboxJpaEntity, String> {

    List<CallbackOutboxJpaEntity> findByOutboxStatusOrderByCreatedAtAsc(
            OutboxStatus outboxStatus, Pageable pageable);
}

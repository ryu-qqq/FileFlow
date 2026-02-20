package com.ryuqq.fileflow.adapter.out.persistence.transform.repository;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformQueueOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransformQueueOutboxJpaRepository
        extends JpaRepository<TransformQueueOutboxJpaEntity, String> {

    List<TransformQueueOutboxJpaEntity> findByOutboxStatusOrderByCreatedAtAsc(
            OutboxStatus outboxStatus, Pageable pageable);

    @Query(
            "SELECT e.outboxStatus, COUNT(e) FROM TransformQueueOutboxJpaEntity e WHERE"
                + " e.outboxStatus IN (com.ryuqq.fileflow.domain.common.vo.OutboxStatus.PENDING,"
                + " com.ryuqq.fileflow.domain.common.vo.OutboxStatus.FAILED) OR (e.outboxStatus ="
                + " com.ryuqq.fileflow.domain.common.vo.OutboxStatus.SENT AND e.createdAt >="
                + " :startInstant AND e.createdAt <= :endInstant) GROUP BY e.outboxStatus")
    List<Object[]> countGroupByOutboxStatus(
            @Param("startInstant") Instant startInstant, @Param("endInstant") Instant endInstant);
}

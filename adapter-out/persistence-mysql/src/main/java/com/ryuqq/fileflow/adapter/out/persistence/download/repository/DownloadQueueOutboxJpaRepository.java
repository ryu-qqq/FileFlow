package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadQueueOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DownloadQueueOutboxJpaRepository
        extends JpaRepository<DownloadQueueOutboxJpaEntity, String> {

    List<DownloadQueueOutboxJpaEntity> findByOutboxStatusOrderByCreatedAtAsc(
            OutboxStatus outboxStatus, Pageable pageable);

    @Query(
            "SELECT e.outboxStatus, COUNT(e) FROM DownloadQueueOutboxJpaEntity e WHERE"
                + " e.outboxStatus IN (com.ryuqq.fileflow.domain.common.vo.OutboxStatus.PENDING,"
                + " com.ryuqq.fileflow.domain.common.vo.OutboxStatus.FAILED) OR (e.outboxStatus ="
                + " com.ryuqq.fileflow.domain.common.vo.OutboxStatus.SENT AND e.createdAt >="
                + " :startInstant AND e.createdAt <= :endInstant) GROUP BY e.outboxStatus")
    List<Object[]> countGroupByOutboxStatus(
            @Param("startInstant") Instant startInstant, @Param("endInstant") Instant endInstant);
}

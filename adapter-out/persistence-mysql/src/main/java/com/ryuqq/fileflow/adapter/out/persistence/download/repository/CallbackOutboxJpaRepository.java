package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.CallbackOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.vo.OutboxStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface CallbackOutboxJpaRepository
        extends JpaRepository<CallbackOutboxJpaEntity, String> {

    List<CallbackOutboxJpaEntity> findByOutboxStatusOrderByCreatedAtAsc(
            OutboxStatus outboxStatus, Pageable pageable);

    @Modifying
    @Query(
            value =
                    "UPDATE callback_outbox SET outbox_status = 'PROCESSING', processed_at = :now"
                        + " WHERE outbox_status = 'PENDING' ORDER BY created_at ASC LIMIT :limit",
            nativeQuery = true)
    int claimPending(@Param("limit") int limit, @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
    @Query("SELECT e FROM CallbackOutboxJpaEntity e WHERE e.outboxStatus = :status")
    List<CallbackOutboxJpaEntity> findByStatus(@Param("status") OutboxStatus status);

    @Modifying
    @Query(
            "UPDATE CallbackOutboxJpaEntity e SET e.outboxStatus = 'SENT', e.processedAt = :now"
                    + " WHERE e.id IN :ids")
    int bulkMarkSent(@Param("ids") List<String> ids, @Param("now") Instant now);

    @Modifying
    @Query(
            value =
                    "UPDATE callback_outbox SET retry_count = retry_count + 1, processed_at = :now,"
                            + " outbox_status = CASE WHEN retry_count + 1 >= 5 THEN 'FAILED' ELSE"
                            + " 'PENDING' END WHERE id IN (:ids)",
            nativeQuery = true)
    int bulkMarkFailed(@Param("ids") List<String> ids, @Param("now") Instant now);
}

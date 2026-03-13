package com.ryuqq.fileflow.adapter.out.persistence.transform.repository;

import com.ryuqq.fileflow.adapter.out.persistence.transform.entity.TransformQueueOutboxJpaEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransformQueueOutboxJpaRepository
        extends JpaRepository<TransformQueueOutboxJpaEntity, String> {

    @Modifying
    @Query(
            "UPDATE TransformQueueOutboxJpaEntity e SET e.outboxStatus = 'SENT', e.processedAt ="
                    + " :now WHERE e.id IN :ids")
    int bulkMarkSent(@Param("ids") List<String> ids, @Param("now") Instant now);

    @Modifying
    @Query(
            value =
                    "UPDATE transform_queue_outbox SET retry_count = retry_count + 1, processed_at"
                        + " = :now, last_error = :lastError, outbox_status = CASE WHEN retry_count"
                        + " + 1 >= 5 THEN 'FAILED' ELSE 'PENDING' END WHERE id IN (:ids)",
            nativeQuery = true)
    int bulkMarkFailed(
            @Param("ids") List<String> ids,
            @Param("now") Instant now,
            @Param("lastError") String lastError);

    @Modifying
    @Query(
            value =
                    "UPDATE transform_queue_outbox SET outbox_status = 'PENDING', processed_at ="
                            + " NULL WHERE outbox_status = 'PROCESSING' AND processed_at < :cutoff",
            nativeQuery = true)
    int recoverStuckProcessing(@Param("cutoff") Instant cutoff);
}

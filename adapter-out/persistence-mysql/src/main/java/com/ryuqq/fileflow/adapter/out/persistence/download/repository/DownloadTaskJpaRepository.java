package com.ryuqq.fileflow.adapter.out.persistence.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.DownloadTaskJpaEntity;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DownloadTaskJpaRepository extends JpaRepository<DownloadTaskJpaEntity, String> {

    @Modifying
    @Query(
            "UPDATE DownloadTaskJpaEntity e SET e.status = :status, e.lastError = :error,"
                    + " e.completedAt = :failedAt WHERE e.id = :id")
    void updateStatusAndError(
            @Param("id") String id,
            @Param("status") DownloadTaskStatus status,
            @Param("error") String error,
            @Param("failedAt") Instant failedAt);
}

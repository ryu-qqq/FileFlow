package com.ryuqq.adapter.out.persistence.{domain_lower}.repository;

import com.ryuqq.adapter.out.persistence.{domain_lower}.entity.{Domain}WriteAheadLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {Domain} Write-Ahead Log Repository
 *
 * @author {author_name}
 * @since {version}
 */
public interface {Domain}WriteAheadLogRepository extends JpaRepository<{Domain}WriteAheadLogEntity, Long> {

    /**
     * PENDING 상태 WAL 조회 (Finalizer가 처리)
     */
    List<{Domain}WriteAheadLogEntity> findByState(WriteAheadState state);

    /**
     * 오래된 PENDING WAL 조회 (5초 이상 지난 것)
     */
    @Query("SELECT w FROM {Domain}WriteAheadLogEntity w WHERE w.state = :state AND w.createdAt <= :threshold")
    List<{Domain}WriteAheadLogEntity> findStalePendingWal(
        @Param("state") WriteAheadState state,
        @Param("threshold") LocalDateTime threshold
    );

    /**
     * OpId로 WAL 조회
     */
    List<{Domain}WriteAheadLogEntity> findByOpId(String opId);
}

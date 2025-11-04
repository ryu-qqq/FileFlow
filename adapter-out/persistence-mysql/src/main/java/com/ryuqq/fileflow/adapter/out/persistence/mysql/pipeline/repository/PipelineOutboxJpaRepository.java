package com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.entity.PipelineOutboxJpaEntity;
import com.ryuqq.fileflow.domain.download.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Pipeline Outbox JPA Repository
 *
 * <p>Pipeline Outbox의 데이터 접근 레이어입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>PipelineOutbox CRUD 작업</li>
 *   <li>PENDING 상태 Outbox 조회 (Scheduler용)</li>
 *   <li>재시도 대상 FAILED Outbox 조회</li>
 *   <li>Idempotency Key 중복 확인</li>
 * </ul>
 *
 * <p><strong>쿼리 최적화:</strong></p>
 * <ul>
 *   <li>IDX_status_created_at 인덱스 활용 (PENDING 조회)</li>
 *   <li>UK_idempotency_key 인덱스 활용 (중복 방지)</li>
 *   <li>IDX_file_id 인덱스 활용 (FileAsset 기반 조회)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface PipelineOutboxJpaRepository extends JpaRepository<PipelineOutboxJpaEntity, Long> {

    /**
     * PENDING 상태의 Outbox를 생성 시간 오름차순으로 조회
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>PipelineOutboxScheduler에서 처리 대기 중인 메시지 조회</li>
     * </ul>
     *
     * <p><strong>인덱스:</strong> IDX_status_created_at (status, created_at)</p>
     *
     * @return PENDING 상태의 Outbox 목록 (생성 시간 오름차순)
     */
    @Query("""
        SELECT o FROM PipelineOutboxJpaEntity o
        WHERE o.status = :status
        ORDER BY o.createdAt ASC
        """)
    List<PipelineOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(
        @Param("status") OutboxStatus status
    );

    /**
     * FAILED 상태이면서 재시도 횟수가 최대값 미만인 Outbox 조회
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>PipelineOutboxScheduler에서 재시도 대상 메시지 조회</li>
     * </ul>
     *
     * <p><strong>재시도 정책:</strong></p>
     * <ul>
     *   <li>retryCount < maxRetries인 경우만 조회</li>
     *   <li>일정 시간(minIntervalMinutes) 경과 후 재시도</li>
     * </ul>
     *
     * @param status              FAILED 상태
     * @param maxRetries          최대 재시도 횟수
     * @param minIntervalMinutes  최소 재시도 간격 (분)
     * @return 재시도 가능한 FAILED Outbox 목록
     */
    @Query("""
        SELECT o FROM PipelineOutboxJpaEntity o
        WHERE o.status = :status
          AND o.retryCount < :maxRetries
          AND o.updatedAt < :retryAfter
        ORDER BY o.updatedAt ASC
        """)
    List<PipelineOutboxJpaEntity> findRetryableFailedOutboxes(
        @Param("status") OutboxStatus status,
        @Param("maxRetries") Integer maxRetries,
        @Param("retryAfter") LocalDateTime retryAfter
    );

    /**
     * Idempotency Key로 Outbox 조회
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>중복 이벤트 방지</li>
     *   <li>FileCommandManager에서 FileAsset 저장 전 중복 확인</li>
     * </ul>
     *
     * <p><strong>인덱스:</strong> UK_idempotency_key (unique)</p>
     *
     * @param idempotencyKey 멱등성 키
     * @return Outbox (존재하지 않으면 empty)
     */
    Optional<PipelineOutboxJpaEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * FileAsset ID로 Outbox 조회
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>특정 FileAsset의 Pipeline 처리 상태 확인</li>
     *   <li>디버깅 및 모니터링</li>
     * </ul>
     *
     * <p><strong>인덱스:</strong> IDX_file_id</p>
     *
     * @param fileId FileAsset ID
     * @return Outbox 목록 (일반적으로 1개)
     */
    List<PipelineOutboxJpaEntity> findByFileId(Long fileId);

    /**
     * 특정 상태의 Outbox 개수 조회
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>모니터링: PENDING, PROCESSING, FAILED 개수 확인</li>
     *   <li>알림: FAILED 누적 개수 임계값 확인</li>
     * </ul>
     *
     * @param status Outbox 상태
     * @return 해당 상태의 Outbox 개수
     */
    @Query("""
        SELECT COUNT(o) FROM PipelineOutboxJpaEntity o
        WHERE o.status = :status
        """)
    long countByStatus(@Param("status") OutboxStatus status);
}

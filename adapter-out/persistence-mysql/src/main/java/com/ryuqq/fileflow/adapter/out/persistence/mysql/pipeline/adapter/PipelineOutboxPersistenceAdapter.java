package com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.entity.PipelineOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.mapper.PipelineOutboxEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.repository.PipelineOutboxJpaRepository;
import com.ryuqq.fileflow.application.file.port.out.PipelineOutboxPort;
import com.ryuqq.fileflow.application.file.port.out.PipelineOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.OutboxStatus;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pipeline Outbox Persistence Adapter
 *
 * <p>PipelineOutboxPort와 PipelineOutboxQueryPort 구현체로, Pipeline Outbox의 영속성 계층 접근을 담당합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>PipelineOutbox 저장 (Domain → JPA Entity → DB)</li>
 *   <li>PipelineOutbox 조회 (DB → JPA Entity → Domain)</li>
 *   <li>CQRS Query 작업 (상태별, 시간별 조회)</li>
 *   <li>트랜잭션 경계 관리</li>
 *   <li>영속성 계층 예외 처리</li>
 * </ul>
 *
 * <p><strong>헥사고날 아키텍처 위치:</strong></p>
 * <ul>
 *   <li>Adapter Layer (Port Out 구현체)</li>
 *   <li>Application Layer ← PipelineOutboxPort/QueryPort → Persistence Adapter</li>
 * </ul>
 *
 * <p><strong>트랜잭션:</strong></p>
 * <ul>
 *   <li>save(): readOnly=false (쓰기 작업)</li>
 *   <li>findByStatus(), findStaleProcessingMessages(): readOnly=true (읽기 작업)</li>
 * </ul>
 *
 * <p><strong>의존성:</strong></p>
 * <ul>
 *   <li>PipelineOutboxJpaRepository - JPA 데이터 접근</li>
 *   <li>PipelineOutboxEntityMapper - Domain ↔ Entity 매핑</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class PipelineOutboxPersistenceAdapter implements PipelineOutboxPort, PipelineOutboxQueryPort {

    private static final Logger log = LoggerFactory.getLogger(PipelineOutboxPersistenceAdapter.class);

    private final PipelineOutboxJpaRepository repository;
    private final PipelineOutboxEntityMapper mapper;

    /**
     * 생성자
     *
     * @param repository PipelineOutbox JPA Repository
     * @param mapper     PipelineOutbox Entity Mapper
     */
    public PipelineOutboxPersistenceAdapter(
        PipelineOutboxJpaRepository repository,
        PipelineOutboxEntityMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * PipelineOutbox 저장
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>FileCommandManager.save()의 트랜잭션에 참여</li>
     *   <li>FileAsset 저장 실패 시 함께 Rollback</li>
     * </ul>
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Domain Aggregate → JPA Entity 변환</li>
     *   <li>JPA Repository를 통해 DB 저장 (ID 자동 생성)</li>
     *   <li>저장된 Entity → Domain Aggregate 변환</li>
     *   <li>변환된 Domain 반환 (ID 포함)</li>
     * </ol>
     *
     * <p><strong>중복 방지:</strong></p>
     * <ul>
     *   <li>UK_idempotency_key 제약으로 중복 저장 방지</li>
     *   <li>중복 시 DataIntegrityViolationException 발생</li>
     * </ul>
     *
     * @param outbox PipelineOutbox Domain Aggregate
     * @return 저장된 PipelineOutbox (ID 포함)
     * @throws org.springframework.dao.DataIntegrityViolationException 중복 idempotencyKey
     */
    @Override
    @Transactional
    public PipelineOutbox save(PipelineOutbox outbox) {
        log.debug("Saving PipelineOutbox: idempotencyKey={}, fileId={}",
            outbox.getIdempotencyKeyValue(), outbox.getFileIdValue());

        // 1. Domain → Entity 변환
        PipelineOutboxJpaEntity entity = mapper.toEntity(outbox);

        // 2. DB 저장 (ID 자동 생성)
        PipelineOutboxJpaEntity savedEntity = repository.save(entity);

        log.info("PipelineOutbox saved: id={}, idempotencyKey={}, status={}",
            savedEntity.getId(), savedEntity.getIdempotencyKey(), savedEntity.getStatus());

        // 3. Entity → Domain 변환 및 반환
        return mapper.toDomain(savedEntity);
    }

    /**
     * 특정 상태의 PipelineOutbox 조회
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>PipelineOutboxScheduler에서 PENDING 메시지 조회</li>
     * </ul>
     *
     * <p><strong>쿼리 최적화:</strong></p>
     * <ul>
     *   <li>IDX_status_created_at 인덱스 활용</li>
     *   <li>생성 시간 오름차순 정렬 (FIFO)</li>
     *   <li>Pageable로 배치 크기 제한</li>
     * </ul>
     *
     * @param status    조회할 Outbox 상태
     * @param batchSize 조회할 최대 개수
     * @return 해당 상태의 Outbox 목록 (생성 시간 오름차순)
     */
    @Override
    @Transactional(readOnly = true)
    public List<PipelineOutbox> findByStatus(OutboxStatus status, int batchSize) {
        log.debug("Finding PipelineOutbox by status: status={}, batchSize={}", status, batchSize);

        Pageable pageable = PageRequest.of(0, batchSize);

        List<PipelineOutboxJpaEntity> entities =
            repository.findByStatusOrderByCreatedAtAsc(status);

        List<PipelineOutbox> outboxes = entities.stream()
            .limit(batchSize)
            .map(mapper::toDomain)
            .collect(Collectors.toList());

        log.debug("Found {} PipelineOutbox with status={}", outboxes.size(), status);

        return outboxes;
    }

    /**
     * 오래된 PROCESSING 메시지 조회 (장애 복구)
     *
     * <p><strong>장애 시나리오:</strong></p>
     * <ul>
     *   <li>Worker 크래시로 PROCESSING 상태로 남은 메시지</li>
     *   <li>네트워크 단절로 상태 업데이트 실패</li>
     *   <li>예외 발생 후 상태 업데이트 실패</li>
     * </ul>
     *
     * <p><strong>복구 전략:</strong></p>
     * <ul>
     *   <li>일정 시간(staleMinutes) 이상 PROCESSING 상태인 메시지 재처리</li>
     *   <li>PipelineOutboxScheduler가 주기적으로 실행</li>
     * </ul>
     *
     * @param staleThreshold PROCESSING 상태가 이 시간보다 오래된 메시지
     * @param batchSize      조회할 최대 개수
     * @return 오래된 PROCESSING 메시지 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<PipelineOutbox> findStaleProcessingMessages(
        LocalDateTime staleThreshold,
        int batchSize
    ) {
        log.debug("Finding stale PROCESSING messages: staleThreshold={}, batchSize={}",
            staleThreshold, batchSize);

        Pageable pageable = PageRequest.of(0, batchSize);

        List<PipelineOutboxJpaEntity> entities =
            repository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PROCESSING);

        List<PipelineOutbox> staleOutboxes = entities.stream()
            .filter(entity -> entity.getUpdatedAt().isBefore(staleThreshold))
            .limit(batchSize)
            .map(mapper::toDomain)
            .collect(Collectors.toList());

        log.warn("Found {} stale PROCESSING messages (threshold={})",
            staleOutboxes.size(), staleThreshold);

        return staleOutboxes;
    }

    /**
     * 재시도 가능한 FAILED 메시지 조회
     *
     * <p><strong>재시도 조건:</strong></p>
     * <ul>
     *   <li>retryCount < maxRetryCount</li>
     *   <li>updatedAt < retryAfter (지수 백오프 경과)</li>
     * </ul>
     *
     * <p><strong>지수 백오프:</strong></p>
     * <ul>
     *   <li>retryAfter = now - (multiplier^retryCount * baseDelay)</li>
     *   <li>예: 60초 → 120초 → 240초 → ...</li>
     * </ul>
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param retryAfter    이 시간 이전에 실패한 메시지만 재시도
     * @param batchSize     조회할 최대 개수
     * @return 재시도 가능한 메시지 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<PipelineOutbox> findRetryableFailedMessages(
        int maxRetryCount,
        LocalDateTime retryAfter,
        int batchSize
    ) {
        log.debug("Finding retryable FAILED messages: maxRetryCount={}, retryAfter={}, batchSize={}",
            maxRetryCount, retryAfter, batchSize);

        Pageable pageable = PageRequest.of(0, batchSize);

        List<PipelineOutboxJpaEntity> entities =
            repository.findRetryableFailedOutboxes(
                OutboxStatus.FAILED,
                maxRetryCount,
                retryAfter
            );

        List<PipelineOutbox> retryableOutboxes = entities.stream()
            .limit(batchSize)
            .map(mapper::toDomain)
            .collect(Collectors.toList());

        log.info("Found {} retryable FAILED messages (maxRetryCount={}, retryAfter={})",
            retryableOutboxes.size(), maxRetryCount, retryAfter);

        return retryableOutboxes;
    }
}

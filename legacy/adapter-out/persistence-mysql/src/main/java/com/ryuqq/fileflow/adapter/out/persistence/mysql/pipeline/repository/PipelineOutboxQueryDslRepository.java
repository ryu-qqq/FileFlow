package com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.entity.PipelineOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.OutboxStatus;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.entity.QPipelineOutboxJpaEntity.pipelineOutboxJpaEntity;

/**
 * Pipeline Outbox QueryDSL Repository Implementation
 *
 * <p>QueryDSL을 사용한 동적 쿼리 실행 구현체입니다.</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>QueryDSL 기반 동적 쿼리 실행</li>
 *   <li>CQRS Query Side - 조회 전용</li>
 *   <li>성능 최적화된 조회 쿼리 제공</li>
 * </ul>
 *
 * <p><strong>설계 원칙</strong>:</p>
 * <ul>
 *   <li>✅ QueryDSL JPAQueryFactory 사용</li>
 *   <li>✅ 구현체만 존재 (인터페이스 추상화 불필요)</li>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ CQRS Query Side 전용 (조회만)</li>
 * </ul>
 *
 * <p><strong>성능 최적화</strong>:</p>
 * <ul>
 *   <li>인덱스 활용: UK_idempotency_key, IDX_file_id</li>
 *   <li>QueryDSL 타입 안전 쿼리</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Repository
public class PipelineOutboxQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * Constructor - JPAQueryFactory 주입
     *
     * @param queryFactory JPAQueryFactory
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public PipelineOutboxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Idempotency Key로 Outbox 조회
     *
     * <p>중복 이벤트 방지 및 멱등성 보장을 위해 사용합니다.</p>
     *
     * <p><strong>사용 시기</strong>:</p>
     * <ul>
     *   <li>FileCommandManager에서 FileAsset 저장 전 중복 확인</li>
     *   <li>이벤트 재발행 방지</li>
     * </ul>
     *
     * <p><strong>인덱스</strong>: UK_idempotency_key (unique)</p>
     *
     * @param idempotencyKey 멱등성 키
     * @return Outbox Entity (존재하지 않으면 {@code Optional.empty()})
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<PipelineOutboxJpaEntity> findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }

        PipelineOutboxJpaEntity entity = queryFactory
            .selectFrom(pipelineOutboxJpaEntity)
            .where(pipelineOutboxJpaEntity.idempotencyKey.eq(idempotencyKey))
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * FileAsset ID로 Outbox 조회
     *
     * <p>특정 FileAsset의 Pipeline Outbox를 조회합니다.</p>
     *
     * <p><strong>사용 시기</strong>:</p>
     * <ul>
     *   <li>PipelineOutboxEventListener - 이벤트 수신 시 Outbox 조회</li>
     *   <li>특정 FileAsset의 Pipeline 처리 상태 확인</li>
     * </ul>
     *
     * <p><strong>Long FK 전략</strong>: FileAsset PK 타입(Long AUTO_INCREMENT)과 일치</p>
     * <p><strong>인덱스</strong>: IDX_file_id</p>
     *
     * @param fileId FileAsset ID (Long - FileAsset PK 타입과 일치)
     * @return Outbox Entity (존재하지 않으면 {@code Optional.empty()})
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public Optional<PipelineOutboxJpaEntity> findByFileId(Long fileId) {
        if (fileId == null || fileId <= 0) {
            return Optional.empty();
        }

        PipelineOutboxJpaEntity entity = queryFactory
            .selectFrom(pipelineOutboxJpaEntity)
            .where(pipelineOutboxJpaEntity.fileId.eq(fileId))
            .fetchOne();

        return Optional.ofNullable(entity);
    }

    /**
     * 특정 상태의 Outbox 조회 (생성 시간 오름차순)
     *
     * <p><strong>사용 시기</strong>:</p>
     * <ul>
     *   <li>PipelineOutboxScheduler에서 PENDING 메시지 조회</li>
     * </ul>
     *
     * <p><strong>쿼리 최적화</strong>:</p>
     * <ul>
     *   <li>IDX_status_created_at 인덱스 활용</li>
     *   <li>생성 시간 오름차순 정렬 (FIFO)</li>
     *   <li>Pageable로 배치 크기 제한</li>
     * </ul>
     *
     * @param status 조회할 Outbox 상태
     * @param pageable 페이징 정보 (배치 크기 제한)
     * @return 해당 상태의 Outbox 목록 (생성 시간 오름차순)
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<PipelineOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(
        OutboxStatus status,
        Pageable pageable
    ) {
        if (status == null || pageable == null) {
            return List.of();
        }

        return queryFactory
            .selectFrom(pipelineOutboxJpaEntity)
            .where(pipelineOutboxJpaEntity.status.eq(status))
            .orderBy(pipelineOutboxJpaEntity.createdAt.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    /**
     * 오래된 PROCESSING 메시지 조회 (장애 복구)
     *
     * <p><strong>장애 시나리오</strong>:</p>
     * <ul>
     *   <li>Worker 크래시로 PROCESSING 상태로 남은 메시지</li>
     *   <li>네트워크 단절로 상태 업데이트 실패</li>
     *   <li>예외 발생 후 상태 업데이트 실패</li>
     * </ul>
     *
     * <p><strong>복구 전략</strong>:</p>
     * <ul>
     *   <li>일정 시간(staleThreshold) 이상 PROCESSING 상태인 메시지 재처리</li>
     *   <li>PipelineOutboxScheduler가 주기적으로 실행</li>
     * </ul>
     *
     * @param status PROCESSING 상태
     * @param staleThreshold 이 시간보다 오래된 메시지
     * @param pageable 페이징 정보 (배치 크기 제한)
     * @return 오래된 PROCESSING 메시지 목록
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<PipelineOutboxJpaEntity> findStaleProcessingMessages(
        OutboxStatus status,
        LocalDateTime staleThreshold,
        Pageable pageable
    ) {
        if (status == null || staleThreshold == null || pageable == null) {
            return List.of();
        }

        return queryFactory
            .selectFrom(pipelineOutboxJpaEntity)
            .where(
                pipelineOutboxJpaEntity.status.eq(status),
                pipelineOutboxJpaEntity.updatedAt.before(staleThreshold)
            )
            .orderBy(pipelineOutboxJpaEntity.updatedAt.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    /**
     * 재시도 가능한 FAILED 메시지 조회
     *
     * <p><strong>재시도 조건</strong>:</p>
     * <ul>
     *   <li>retryCount < maxRetries</li>
     *   <li>updatedAt < retryAfter (지수 백오프 경과)</li>
     * </ul>
     *
     * <p><strong>지수 백오프</strong>:</p>
     * <ul>
     *   <li>retryAfter = now - (multiplier^retryCount * baseDelay)</li>
     *   <li>예: 60초 → 120초 → 240초 → ...</li>
     * </ul>
     *
     * @param status FAILED 상태
     * @param maxRetries 최대 재시도 횟수
     * @param retryAfter 이 시간 이전에 실패한 메시지만 재시도
     * @param pageable 페이징 정보 (배치 크기 제한)
     * @return 재시도 가능한 메시지 목록
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public List<PipelineOutboxJpaEntity> findRetryableFailedOutboxes(
        OutboxStatus status,
        int maxRetries,
        LocalDateTime retryAfter,
        Pageable pageable
    ) {
        if (status == null || retryAfter == null || pageable == null) {
            return List.of();
        }

        return queryFactory
            .selectFrom(pipelineOutboxJpaEntity)
            .where(
                pipelineOutboxJpaEntity.status.eq(status),
                pipelineOutboxJpaEntity.retryCount.lt(maxRetries),
                pipelineOutboxJpaEntity.updatedAt.before(retryAfter)
            )
            .orderBy(pipelineOutboxJpaEntity.updatedAt.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    /**
     * 특정 상태의 Outbox 개수 조회
     *
     * <p><strong>사용 시기</strong>:</p>
     * <ul>
     *   <li>모니터링: 각 상태별 메시지 개수 추적</li>
     *   <li>경고: FAILED 메시지가 임계치 초과 시 알림</li>
     * </ul>
     *
     * @param status 조회할 Outbox 상태
     * @return 해당 상태의 Outbox 개수
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public long countByStatus(OutboxStatus status) {
        if (status == null) {
            return 0L;
        }

        Long count = queryFactory
            .select(pipelineOutboxJpaEntity.count())
            .from(pipelineOutboxJpaEntity)
            .where(pipelineOutboxJpaEntity.status.eq(status))
            .fetchOne();

        return count != null ? count : 0L;
    }
}

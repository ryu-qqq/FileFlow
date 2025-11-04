package com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.mapper;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.entity.PipelineOutboxJpaEntity;
import com.ryuqq.fileflow.domain.download.IdempotencyKey;
import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutbox;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutboxId;
import org.springframework.stereotype.Component;

/**
 * Pipeline Outbox Entity Mapper
 *
 * <p>Domain Layer (PipelineOutbox) ↔ Persistence Layer (PipelineOutboxJpaEntity) 매핑을 담당합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Domain → JPA Entity 변환 (toEntity)</li>
 *   <li>JPA Entity → Domain 변환 (toDomain)</li>
 *   <li>Value Object 래핑/언래핑</li>
 * </ul>
 *
 * <p><strong>매핑 규칙:</strong></p>
 * <ul>
 *   <li>Domain의 Value Object는 JPA Entity의 Primitive로 변환</li>
 *   <li>JPA Entity의 Primitive는 Domain의 Value Object로 래핑</li>
 *   <li>Null 안전성: Domain은 null 허용 안 함, JPA Entity는 nullable</li>
 * </ul>
 *
 * <p><strong>의존성:</strong></p>
 * <ul>
 *   <li>Domain Layer: PipelineOutbox, PipelineOutboxId, IdempotencyKey, FileId</li>
 *   <li>Persistence Layer: PipelineOutboxJpaEntity</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class PipelineOutboxEntityMapper {

    /**
     * Domain Aggregate → JPA Entity 변환
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>PipelineOutboxPersistenceAdapter.save() - Domain을 DB에 저장</li>
     * </ul>
     *
     * <p><strong>변환 로직:</strong></p>
     * <ul>
     *   <li>PipelineOutboxId → Long id</li>
     *   <li>IdempotencyKey → String idempotencyKey</li>
     *   <li>FileId → Long fileId</li>
     *   <li>OutboxStatus, retryCount는 그대로 유지</li>
     * </ul>
     *
     * @param domain PipelineOutbox Domain Aggregate
     * @return PipelineOutboxJpaEntity
     * @throws IllegalArgumentException domain이 null인 경우
     */
    public PipelineOutboxJpaEntity toEntity(PipelineOutbox domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain Aggregate는 null일 수 없습니다");
        }

        return new PipelineOutboxJpaEntity(
            domain.getIdValue(),                    // Long id (null 가능 - 신규 생성 시)
            domain.getIdempotencyKeyValue(),        // String idempotencyKey
            domain.getFileIdValue(),                // Long fileId
            domain.getStatus(),                     // OutboxStatus
            domain.getRetryCount()                  // Integer retryCount
        );
    }

    /**
     * JPA Entity → Domain Aggregate 변환
     *
     * <p><strong>사용 시기:</strong></p>
     * <ul>
     *   <li>PipelineOutboxPersistenceAdapter.findById() - DB에서 조회한 Entity를 Domain으로 변환</li>
     *   <li>PipelineOutboxPersistenceAdapter.findPendingOutboxes() - Scheduler용 조회</li>
     * </ul>
     *
     * <p><strong>변환 로직:</strong></p>
     * <ul>
     *   <li>Long id → PipelineOutboxId</li>
     *   <li>String idempotencyKey → IdempotencyKey</li>
     *   <li>Long fileId → FileId</li>
     *   <li>BaseEntity의 createdAt, updatedAt 포함</li>
     * </ul>
     *
     * <p><strong>reconstitute() 사용:</strong></p>
     * <ul>
     *   <li>DB에서 조회한 데이터는 ID가 필수 (null이면 예외 발생)</li>
     *   <li>모든 타임스탬프는 DB에서 관리</li>
     * </ul>
     *
     * @param entity PipelineOutboxJpaEntity
     * @return PipelineOutbox Domain Aggregate
     * @throws IllegalArgumentException entity가 null인 경우
     * @throws IllegalArgumentException entity.id가 null인 경우 (reconstitute 제약)
     */
    public PipelineOutbox toDomain(PipelineOutboxJpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("JPA Entity는 null일 수 없습니다");
        }

        return PipelineOutbox.reconstitute(
            PipelineOutboxId.of(entity.getId()),                // PipelineOutboxId (필수)
            IdempotencyKey.of(entity.getIdempotencyKey()),      // IdempotencyKey
            FileId.of(entity.getFileId()),                      // FileId
            entity.getStatus(),                                 // OutboxStatus
            entity.getRetryCount(),                             // Integer retryCount
            entity.getCreatedAt(),                              // LocalDateTime createdAt
            entity.getUpdatedAt()                               // LocalDateTime updatedAt
        );
    }
}

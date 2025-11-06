package com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.common.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Pipeline Outbox JPA Entity
 *
 * <p>Pipeline 처리를 위한 Transactional Outbox Pattern 구현 엔티티입니다.</p>
 *
 * <p><strong>테이블:</strong> pipeline_outbox</p>
 *
 * <p><strong>인덱스:</strong></p>
 * <ul>
 *   <li>UK_idempotency_key: 멱등성 키 (중복 방지)</li>
 *   <li>IDX_status_created_at: 처리 대기 중인 메시지 조회 최적화</li>
 *   <li>IDX_file_id: FileAsset 기반 조회 최적화</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ BaseEntity 상속 (createdAt, updatedAt 자동 관리)</li>
 *   <li>✅ Immutable ID (Long, AUTO_INCREMENT)</li>
 *   <li>✅ Enum은 STRING 타입으로 저장</li>
 *   <li>❌ Domain 의존성 없음 (순수 JPA Entity)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Entity
@Table(
    name = "pipeline_outbox",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_idempotency_key",
            columnNames = {"idempotency_key"}
        )
    },
    indexes = {
        @Index(
            name = "IDX_status_created_at",
            columnList = "status, created_at"
        ),
        @Index(
            name = "IDX_file_id",
            columnList = "file_id"
        )
    }
)
public class PipelineOutboxJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pipeline_outbox_id", nullable = false)
    private Long id;

    /**
     * 멱등성 키 (중복 이벤트 방지)
     *
     * <p>패턴: fileAsset-{fileAssetId}</p>
     */
    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    /**
     * FileAsset ID (FK)
     *
     * <p>Long FK 전략 준수 (JPA 관계 어노테이션 없음)</p>
     */
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    /**
     * Outbox 상태
     *
     * <p>PENDING → PROCESSING → COMPLETED/FAILED</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    /**
     * 재시도 횟수
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    /**
     * JPA 기본 생성자 (protected)
     */
    protected PipelineOutboxJpaEntity() {
    }

    /**
     * 전체 생성자
     *
     * @param id             Pipeline Outbox ID
     * @param idempotencyKey 멱등성 키
     * @param fileId         FileAsset ID
     * @param status         Outbox 상태
     * @param retryCount     재시도 횟수
     */
    public PipelineOutboxJpaEntity(
        Long id,
        String idempotencyKey,
        Long fileId,
        OutboxStatus status,
        Integer retryCount
    ) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.fileId = fileId;
        this.status = status;
        this.retryCount = retryCount;
    }

    /**
     * 신규 Entity 생성 (Static Factory Method)
     *
     * <p>ID = null로 초기화 (DB 저장 시 자동 생성)</p>
     *
     * @param idempotencyKey 멱등성 키
     * @param fileId         FileAsset ID
     * @param status         Outbox 상태
     * @param retryCount     재시도 횟수
     * @return PipelineOutboxJpaEntity
     */
    public static PipelineOutboxJpaEntity forNew(
        String idempotencyKey,
        Long fileId,
        OutboxStatus status,
        Integer retryCount
    ) {
        return new PipelineOutboxJpaEntity(
            null,
            idempotencyKey,
            fileId,
            status,
            retryCount
        );
    }

    // ===== Getter 메서드 =====

    public Long getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getFileId() {
        return fileId;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    // ===== Setter 메서드 (상태 변경용) =====

    public void setStatus(OutboxStatus status) {
        this.status = status;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
}

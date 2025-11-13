package com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.pipeline.entity.PipelineOutboxJpaEntity;
import com.ryuqq.fileflow.domain.common.OutboxStatus;

import java.time.LocalDateTime;

/**
 * PipelineOutboxJpaEntity Test Fixture
 *
 * <p>테스트에서 PipelineOutboxJpaEntity 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class PipelineOutboxJpaEntityFixture {

    private static final Long DEFAULT_FILE_ID = 1L;
    private static final String DEFAULT_IDEMPOTENCY_KEY = "test-idempotency-key-1";
    private static final OutboxStatus DEFAULT_STATUS = OutboxStatus.PENDING;
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private PipelineOutboxJpaEntityFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 PipelineOutboxJpaEntity 생성 (PENDING 상태)
     *
     * @return 새로운 PipelineOutboxJpaEntity
     */
    public static PipelineOutboxJpaEntity create() {
        return PipelineOutboxJpaEntity.forNew(
            DEFAULT_IDEMPOTENCY_KEY,
            DEFAULT_FILE_ID,
            DEFAULT_STATUS,
            0
        );
    }

    /**
     * ID를 포함한 PipelineOutboxJpaEntity 재구성
     *
     * @param id Outbox ID
     * @param fileId File ID
     * @param idempotencyKey Idempotency Key
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @return 재구성된 PipelineOutboxJpaEntity
     */
    public static PipelineOutboxJpaEntity reconstitute(
        Long id,
        Long fileId,
        String idempotencyKey,
        OutboxStatus status,
        int retryCount
    ) {
        return new PipelineOutboxJpaEntity(
            id,
            idempotencyKey,
            fileId,
            status,
            retryCount
        );
    }

    /**
     * PENDING 상태의 PipelineOutboxJpaEntity 생성
     *
     * @param id Outbox ID
     * @return PENDING 상태의 PipelineOutboxJpaEntity
     */
    public static PipelineOutboxJpaEntity createPending(Long id) {
        return reconstitute(id, DEFAULT_FILE_ID, DEFAULT_IDEMPOTENCY_KEY, OutboxStatus.PENDING, 0);
    }

    /**
     * PROCESSING 상태의 PipelineOutboxJpaEntity 생성
     *
     * @param id Outbox ID
     * @return PROCESSING 상태의 PipelineOutboxJpaEntity
     */
    public static PipelineOutboxJpaEntity createProcessing(Long id) {
        return reconstitute(id, DEFAULT_FILE_ID, DEFAULT_IDEMPOTENCY_KEY, OutboxStatus.PROCESSING, 0);
    }

    /**
     * COMPLETED 상태의 PipelineOutboxJpaEntity 생성
     *
     * @param id Outbox ID
     * @return COMPLETED 상태의 PipelineOutboxJpaEntity
     */
    public static PipelineOutboxJpaEntity createCompleted(Long id) {
        return reconstitute(id, DEFAULT_FILE_ID, DEFAULT_IDEMPOTENCY_KEY, OutboxStatus.COMPLETED, 0);
    }

    /**
     * FAILED 상태의 PipelineOutboxJpaEntity 생성
     *
     * @param id Outbox ID
     * @param retryCount 재시도 횟수
     * @return FAILED 상태의 PipelineOutboxJpaEntity
     */
    public static PipelineOutboxJpaEntity createFailed(Long id, int retryCount) {
        return reconstitute(id, DEFAULT_FILE_ID, DEFAULT_IDEMPOTENCY_KEY, OutboxStatus.FAILED, retryCount);
    }

    /**
     * 여러 개의 PipelineOutboxJpaEntity 생성
     *
     * @param count 생성할 개수
     * @param status 상태
     * @return PipelineOutboxJpaEntity 배열
     */
    public static PipelineOutboxJpaEntity[] createMultiple(int count, OutboxStatus status) {
        PipelineOutboxJpaEntity[] entities = new PipelineOutboxJpaEntity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = reconstitute(
                (long) (i + 1),
                (long) (i + 1),
                DEFAULT_IDEMPOTENCY_KEY + "-" + (i + 1),
                status,
                0
            );
        }
        return entities;
    }
}


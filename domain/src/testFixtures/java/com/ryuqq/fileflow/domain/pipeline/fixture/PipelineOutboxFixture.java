package com.ryuqq.fileflow.domain.pipeline.fixture;

import com.ryuqq.fileflow.domain.common.OutboxStatus;
import com.ryuqq.fileflow.domain.download.IdempotencyKey;
import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutbox;
import com.ryuqq.fileflow.domain.pipeline.PipelineOutboxId;

import java.time.LocalDateTime;

/**
 * PipelineOutbox Test Fixture
 *
 * <p>테스트에서 PipelineOutbox 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class PipelineOutboxFixture {

    private static final Long DEFAULT_FILE_ID = 1L;
    private static final String DEFAULT_IDEMPOTENCY_KEY = "test-idempotency-key-1";

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private PipelineOutboxFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * PENDING 상태의 PipelineOutbox 생성
     *
     * @return PENDING 상태의 PipelineOutbox
     */
    public static PipelineOutbox createPending() {
        return PipelineOutbox.forNew(
            IdempotencyKey.of(DEFAULT_IDEMPOTENCY_KEY),
            FileId.of(DEFAULT_FILE_ID)
        );
    }

    /**
     * PENDING 상태의 PipelineOutbox 생성 (커스텀 FileId)
     *
     * @param fileId File ID
     * @return PENDING 상태의 PipelineOutbox
     */
    public static PipelineOutbox createPending(Long fileId) {
        return PipelineOutbox.forNew(
            IdempotencyKey.of(DEFAULT_IDEMPOTENCY_KEY + "-" + fileId),
            FileId.of(fileId)
        );
    }

    /**
     * PROCESSING 상태의 PipelineOutbox 생성
     *
     * @param id Outbox ID
     * @return PROCESSING 상태의 PipelineOutbox
     */
    public static PipelineOutbox createProcessing(Long id) {
        PipelineOutbox outbox = reconstitute(
            id,
            DEFAULT_FILE_ID,
            DEFAULT_IDEMPOTENCY_KEY,
            OutboxStatus.PENDING,
            0
        );
        outbox.startProcessing();
        return outbox;
    }

    /**
     * COMPLETED 상태의 PipelineOutbox 생성
     *
     * @param id Outbox ID
     * @return COMPLETED 상태의 PipelineOutbox
     */
    public static PipelineOutbox createCompleted(Long id) {
        PipelineOutbox outbox = createProcessing(id);
        outbox.complete();
        return outbox;
    }

    /**
     * FAILED 상태의 PipelineOutbox 생성
     *
     * @param id Outbox ID
     * @return FAILED 상태의 PipelineOutbox
     */
    public static PipelineOutbox createFailed(Long id) {
        PipelineOutbox outbox = createProcessing(id);
        outbox.fail();
        return outbox;
    }

    /**
     * FAILED 상태의 PipelineOutbox 생성 (지정된 retryCount)
     *
     * @param id Outbox ID
     * @param retryCount 재시도 횟수
     * @return FAILED 상태의 PipelineOutbox
     */
    public static PipelineOutbox createFailed(Long id, int retryCount) {
        return reconstitute(
            id,
            DEFAULT_FILE_ID,
            DEFAULT_IDEMPOTENCY_KEY,
            OutboxStatus.FAILED,
            retryCount
        );
    }

    /**
     * 재구성된 PipelineOutbox 생성 (모든 필드 지정)
     *
     * @param id Outbox ID
     * @param fileId File ID
     * @param idempotencyKey Idempotency Key
     * @param status 상태
     * @param retryCount 재시도 횟수
     * @return 재구성된 PipelineOutbox
     */
    public static PipelineOutbox reconstitute(
        Long id,
        Long fileId,
        String idempotencyKey,
        OutboxStatus status,
        int retryCount
    ) {
        return PipelineOutbox.reconstitute(
            PipelineOutboxId.of(id),
            IdempotencyKey.of(idempotencyKey),
            FileId.of(fileId),
            status,
            retryCount,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now()
        );
    }
}


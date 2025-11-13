package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.domain.pipeline.PipelineOutbox;

/**
 * Pipeline Outbox Port (Port Out)
 *
 * <p>Pipeline Outbox의 영속성 계층 인터페이스입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>PipelineOutbox 저장</li>
 *   <li>Persistence Layer 추상화</li>
 * </ul>
 *
 * <p><strong>구현체:</strong></p>
 * <ul>
 *   <li>PipelineOutboxPersistenceAdapter (Persistence Layer)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface PipelineOutboxPort {

    /**
     * PipelineOutbox 저장
     *
     * @param outbox PipelineOutbox Domain Aggregate
     * @return 저장된 PipelineOutbox (ID 포함)
     */
    PipelineOutbox save(PipelineOutbox outbox);
}

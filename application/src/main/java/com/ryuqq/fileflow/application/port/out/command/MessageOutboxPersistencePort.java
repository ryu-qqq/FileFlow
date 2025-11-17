package com.ryuqq.fileflow.application.port.out.command;

import com.ryuqq.fileflow.domain.aggregate.MessageOutbox;
import com.ryuqq.fileflow.domain.vo.MessageOutboxId;

/**
 * MessageOutbox Persistence Port (Outbound Port)
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *PersistencePort
 * - 패키지: ..application..port.out.command..
 * - 메서드: persist() 하나만
 * - 반환 타입: MessageOutboxId (Value Object)
 * - 파라미터: MessageOutbox (Domain Aggregate)
 * </p>
 * <p>
 * Application Layer에서 Persistence Layer로 MessageOutbox Aggregate 영속화 요청을 위한 Port입니다.
 * persist() 메서드는 신규 생성과 수정을 모두 처리합니다.
 * </p>
 */
public interface MessageOutboxPersistencePort {

    /**
     * MessageOutbox Aggregate 영속화
     * <p>
     * 신규 생성과 수정을 통합 처리합니다.
     * - 신규: messageOutboxId가 null → 저장 후 생성된 MessageOutboxId 반환
     * - 수정: messageOutboxId가 존재 → 수정 후 동일한 MessageOutboxId 반환
     * </p>
     *
     * @param outbox 영속화할 MessageOutbox Aggregate
     * @return 영속화된 MessageOutbox의 MessageOutboxId (Value Object)
     */
    MessageOutboxId persist(MessageOutbox outbox);
}

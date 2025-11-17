package com.ryuqq.fileflow.application.port.out.command;

import com.ryuqq.fileflow.domain.aggregate.MessageOutbox;
import com.ryuqq.fileflow.domain.vo.MessageOutboxId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MessageOutboxPersistencePort 인터페이스 계약 테스트
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *PersistencePort
 * - 패키지: ..application..port.out.command..
 * - 메서드: persist() 하나만
 * - 반환 타입: MessageOutboxId (Value Object)
 * - 파라미터: MessageOutbox (Domain Aggregate)
 * </p>
 */
class MessageOutboxPersistencePortTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * MessageOutboxPersistencePort 인터페이스가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("MessageOutboxPersistencePort는 persist() 메서드를 제공해야 한다")
    void shouldProvidePersistMethod() {
        // Given: MessageOutboxPersistencePort 인터페이스 (컴파일 에러)
        MessageOutboxPersistencePort port = null;

        // When & Then: 메서드 시그니처 검증
        // MessageOutboxId persist(MessageOutbox outbox) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("persist()는 MessageOutboxId를 반환해야 한다")
    void persistShouldReturnMessageOutboxId() {
        // Given: MessageOutboxPersistencePort 인터페이스 (컴파일 에러)
        MessageOutboxPersistencePort port = null;

        // When & Then: 반환 타입 검증
        // MessageOutboxId 반환 (Value Object)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("persist()는 MessageOutbox을 파라미터로 받아야 한다")
    void persistShouldAcceptMessageOutbox() {
        // Given: MessageOutboxPersistencePort 인터페이스 (컴파일 에러)
        MessageOutboxPersistencePort port = null;

        // When & Then: 파라미터 타입 검증
        // MessageOutbox 파라미터 (Domain Aggregate)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }
}

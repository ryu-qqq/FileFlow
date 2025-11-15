package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.fixture.MessageOutboxFixture;
import com.ryuqq.fileflow.domain.fixture.MessageOutboxIdFixture;
import com.ryuqq.fileflow.domain.fixture.OutboxStatusFixture;
import com.ryuqq.fileflow.domain.vo.MessageOutboxId;
import com.ryuqq.fileflow.domain.vo.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MessageOutbox Aggregate Root 테스트")
class MessageOutboxTest {

    @Test
    @DisplayName("유효한 데이터로 MessageOutbox를 생성할 수 있어야 한다")
    void shouldCreateOutboxWithValidData() {
        // Given & When
        MessageOutbox outbox = MessageOutboxFixture.anOutbox()
                .eventType("FileCreated")
                .aggregateId("file-uuid-v7-123")
                .payload("{\"fileName\":\"test.jpg\",\"fileSize\":1024000}")
                .maxRetryCount(3)
                .build();

        // Then
        assertThat(outbox).isNotNull();
        assertThat(outbox.getId()).isNotNull();
        assertThat(outbox.getId().getValue()).isNotBlank();
        assertThat(outbox.getEventType()).isEqualTo("FileCreated");
        assertThat(outbox.getAggregateId()).isEqualTo("file-uuid-v7-123");
        assertThat(outbox.getPayload()).contains("fileName");
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatusFixture.pending());
        assertThat(outbox.getRetryCount()).isEqualTo(0);
        assertThat(outbox.getMaxRetryCount()).isEqualTo(3);
        assertThat(outbox.getCreatedAt()).isNotNull();
        assertThat(outbox.getProcessedAt()).isNull();
    }

    @Test
    @DisplayName("필수 필드가 올바르게 설정되어야 한다")
    void shouldHaveRequiredFields() {
        // Given & When - MessageOutboxFixture 사용
        MessageOutbox outbox = MessageOutboxFixture.anOutbox().build();

        // Then - 필수 필드 검증
        assertThat(outbox.getId()).isNotNull();
        assertThat(outbox.getId().getValue()).isNotBlank();
        assertThat(outbox.getEventType()).isNotBlank();
        assertThat(outbox.getAggregateId()).isNotBlank();
        assertThat(outbox.getPayload()).isNotBlank();
        assertThat(outbox.getStatus()).isNotNull();
        assertThat(outbox.getMaxRetryCount()).isPositive();
        assertThat(outbox.getCreatedAt()).isNotNull();
    }

    // ===== create() 팩토리 메서드 테스트 =====

    @Test
    @DisplayName("create() 팩토리 메서드로 UUID v7과 PENDING 상태로 메시지를 생성해야 한다")
    void shouldCreateOutboxWithPendingStatus() {
        // Given
        String eventType = "FileCreated";
        String aggregateId = "file-uuid-v7-123";
        String payload = "{\"fileName\":\"test.jpg\",\"fileSize\":1024000}";
        int maxRetryCount = 3;

        // When
        MessageOutbox outbox = MessageOutbox.create(eventType, aggregateId, payload, maxRetryCount);

        // Then
        assertThat(outbox.getId()).isNotNull(); // UUID v7 자동 생성
        assertThat(outbox.getId().getValue()).isNotBlank();
        assertThat(outbox.getId().getValue()).hasSize(36); // UUID 표준 길이
        assertThat(outbox.getEventType()).isEqualTo(eventType);
        assertThat(outbox.getAggregateId()).isEqualTo(aggregateId);
        assertThat(outbox.getPayload()).isEqualTo(payload);
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatusFixture.pending()); // PENDING 상태
        assertThat(outbox.getRetryCount()).isEqualTo(0); // 초기 재시도 횟수 0
        assertThat(outbox.getMaxRetryCount()).isEqualTo(maxRetryCount);
        assertThat(outbox.getCreatedAt()).isNotNull();
        assertThat(outbox.getProcessedAt()).isNull();
    }

    // ===== 상태 전환 메서드 테스트 =====

    @Test
    @DisplayName("메시지를 발송 완료 처리하고 processedAt을 설정할 수 있어야 한다")
    void shouldMarkAsSent() {
        // Given
        MessageOutbox outbox = MessageOutboxFixture.createOutbox(
                "FileCreated",
                "file-uuid-v7-123",
                "{\"fileName\":\"test.jpg\"}",
                3
        );
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatusFixture.pending());

        // When
        MessageOutbox sentOutbox = outbox.markAsSent();

        // Then
        assertThat(sentOutbox.getStatus()).isEqualTo(OutboxStatusFixture.sent());
        assertThat(sentOutbox.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("발송 완료 시 processedAt이 현재 시각으로 설정되어야 한다")
    void shouldMarkAsSentWithProcessedAt() {
        // Given
        MessageOutbox outbox = MessageOutboxFixture.createOutbox(
                "FileCreated",
                "file-uuid-v7-123",
                "{\"fileName\":\"test.jpg\"}",
                3
        );

        // When
        MessageOutbox sentOutbox = outbox.markAsSent();

        // Then
        assertThat(sentOutbox.getProcessedAt()).isNotNull();
        assertThat(sentOutbox.getProcessedAt()).isAfterOrEqualTo(outbox.getCreatedAt());
    }

    @Test
    @DisplayName("메시지를 실패 처리할 수 있어야 한다")
    void shouldMarkAsFailed() {
        // Given
        MessageOutbox outbox = MessageOutboxFixture.createOutbox(
                "FileCreated",
                "file-uuid-v7-123",
                "{\"fileName\":\"test.jpg\"}",
                3
        );

        // When
        MessageOutbox failedOutbox = outbox.markAsFailed();

        // Then
        assertThat(failedOutbox.getStatus()).isEqualTo(OutboxStatusFixture.failed());
        assertThat(failedOutbox.getProcessedAt()).isNotNull();
    }

    // ===== 부가 메서드 테스트 =====

    @Test
    @DisplayName("재시도 횟수를 증가시킬 수 있어야 한다")
    void shouldIncrementRetryCount() {
        // Given
        MessageOutbox outbox = MessageOutboxFixture.createOutbox(
                "FileCreated",
                "file-uuid-v7-123",
                "{\"fileName\":\"test.jpg\"}",
                3
        );
        assertThat(outbox.getRetryCount()).isEqualTo(0);

        // When
        MessageOutbox retriedOutbox = outbox.incrementRetryCount();

        // Then
        assertThat(retriedOutbox.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("재시도 가능 여부를 확인할 수 있어야 한다 - 가능한 경우")
    void shouldReturnTrueWhenCanRetry() {
        // Given
        MessageOutbox outbox = MessageOutboxFixture.anOutbox()
                .retryCount(2)
                .maxRetryCount(3)
                .build();

        // When
        boolean canRetry = outbox.canRetry();

        // Then
        assertThat(canRetry).isTrue();
    }

    @Test
    @DisplayName("재시도 가능 여부를 확인할 수 있어야 한다 - 불가능한 경우")
    void shouldReturnFalseWhenCannotRetry() {
        // Given
        MessageOutbox outbox = MessageOutboxFixture.anOutbox()
                .retryCount(3)
                .maxRetryCount(3)
                .build();

        // When
        boolean canRetry = outbox.canRetry();

        // Then
        assertThat(canRetry).isFalse();
    }

    // ===== TTL 만료 검증 테스트 =====

    @Test
    @DisplayName("SENT 상태 메시지는 7일 후 만료되어야 한다")
    void shouldExpireAfter7DaysWhenSent() {
        // Given - 8일 전에 발송된 메시지
        MessageOutbox outbox = MessageOutboxFixture.anOutbox()
                .status(OutboxStatusFixture.sent())
                .createdAt(LocalDateTime.now().minusDays(8))
                .processedAt(LocalDateTime.now().minusDays(8))
                .build();

        // When
        boolean isExpired = outbox.isExpired();

        // Then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("FAILED 상태 메시지는 30일 후 만료되어야 한다")
    void shouldExpireAfter30DaysWhenFailed() {
        // Given - 31일 전에 실패한 메시지
        MessageOutbox outbox = MessageOutboxFixture.anOutbox()
                .status(OutboxStatusFixture.failed())
                .createdAt(LocalDateTime.now().minusDays(31))
                .processedAt(LocalDateTime.now().minusDays(31))
                .build();

        // When
        boolean isExpired = outbox.isExpired();

        // Then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("TTL 이내 메시지는 만료되지 않아야 한다")
    void shouldNotExpireWhenWithinTTL() {
        // Given - SENT 상태, 6일 전 메시지 (7일 이내)
        MessageOutbox sentOutbox = MessageOutboxFixture.anOutbox()
                .status(OutboxStatusFixture.sent())
                .createdAt(LocalDateTime.now().minusDays(6))
                .processedAt(LocalDateTime.now().minusDays(6))
                .build();

        // Given - FAILED 상태, 29일 전 메시지 (30일 이내)
        MessageOutbox failedOutbox = MessageOutboxFixture.anOutbox()
                .status(OutboxStatusFixture.failed())
                .createdAt(LocalDateTime.now().minusDays(29))
                .processedAt(LocalDateTime.now().minusDays(29))
                .build();

        // When & Then
        assertThat(sentOutbox.isExpired()).isFalse();
        assertThat(failedOutbox.isExpired()).isFalse();
    }

    // ========================================
    // 3종 팩토리 메서드 테스트 (Cycle 2)
    // ========================================

    @Test
    @DisplayName("forNew() 팩토리 메서드로 ID null인 신규 메시지를 생성해야 한다")
    void shouldCreateNewOutboxWithForNew() {
        // Given
        String eventType = "FileCreated";
        String aggregateId = "file-uuid-v7-123";
        String payload = "{\"fileName\":\"test.jpg\"}";
        int maxRetryCount = 3;

        // When
        MessageOutbox outbox = MessageOutbox.forNew(
                eventType,
                aggregateId,
                payload,
                maxRetryCount
        );

        // Then
        assertThat(outbox).isNotNull();
        assertThat(outbox.getId()).isNull(); // forNew()는 ID가 null
        assertThat(outbox.getEventType()).isEqualTo(eventType);
        assertThat(outbox.getAggregateId()).isEqualTo(aggregateId);
        assertThat(outbox.getPayload()).isEqualTo(payload);
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatusFixture.pending());
        assertThat(outbox.getRetryCount()).isEqualTo(0);
        assertThat(outbox.getMaxRetryCount()).isEqualTo(maxRetryCount);
        assertThat(outbox.getCreatedAt()).isNotNull();
        assertThat(outbox.getProcessedAt()).isNull();
    }

    @Test
    @DisplayName("of() 팩토리 메서드로 ID 필수인 메시지를 생성해야 한다")
    void shouldCreateOutboxWithOf() {
        // Given
        MessageOutboxId id = MessageOutboxIdFixture.aMessageOutboxId();
        String eventType = "FileCreated";
        String aggregateId = "file-uuid-v7-123";
        String payload = "{\"fileName\":\"test.jpg\"}";
        OutboxStatus status = OutboxStatusFixture.pending();
        int retryCount = 0;
        int maxRetryCount = 3;
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        MessageOutbox outbox = MessageOutbox.of(
                id,
                eventType,
                aggregateId,
                payload,
                status,
                retryCount,
                maxRetryCount,
                createdAt,
                null
        );

        // Then
        assertThat(outbox).isNotNull();
        assertThat(outbox.getId()).isEqualTo(id);
        assertThat(outbox.getEventType()).isEqualTo(eventType);
        assertThat(outbox.getAggregateId()).isEqualTo(aggregateId);
        assertThat(outbox.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("of() 팩토리 메서드에 null ID를 전달하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenOfWithNullId() {
        // Given
        MessageOutboxId nullId = null;

        // When & Then
        assertThatThrownBy(() -> MessageOutbox.of(
                nullId,
                "FileCreated",
                "file-uuid-v7-123",
                "{}",
                OutboxStatusFixture.pending(),
                0,
                3,
                LocalDateTime.now(),
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID는 null일 수 없습니다");
    }

    @Test
    @DisplayName("reconstitute() 팩토리 메서드로 영속성 복원용 메시지를 생성해야 한다")
    void shouldReconstituteOutbox() {
        // Given
        MessageOutboxId id = MessageOutboxIdFixture.aMessageOutboxId();
        String eventType = "FileCreated";
        String aggregateId = "file-uuid-v7-123";
        String payload = "{\"fileName\":\"test.jpg\"}";
        OutboxStatus status = OutboxStatusFixture.sent();
        int retryCount = 2;
        int maxRetryCount = 3;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime processedAt = LocalDateTime.now();

        // When
        MessageOutbox outbox = MessageOutbox.reconstitute(
                id,
                eventType,
                aggregateId,
                payload,
                status,
                retryCount,
                maxRetryCount,
                createdAt,
                processedAt
        );

        // Then
        assertThat(outbox).isNotNull();
        assertThat(outbox.getId()).isEqualTo(id);
        assertThat(outbox.getEventType()).isEqualTo(eventType);
        assertThat(outbox.getStatus()).isEqualTo(status);
        assertThat(outbox.getRetryCount()).isEqualTo(retryCount);
        assertThat(outbox.getProcessedAt()).isEqualTo(processedAt);
    }

    @Test
    @DisplayName("reconstitute() 팩토리 메서드에 null ID를 전달하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenReconstituteWithNullId() {
        // Given
        MessageOutboxId nullId = null;

        // When & Then
        assertThatThrownBy(() -> MessageOutbox.reconstitute(
                nullId,
                "FileCreated",
                "file-uuid-v7-123",
                "{}",
                OutboxStatusFixture.pending(),
                0,
                3,
                LocalDateTime.now(),
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID는 null일 수 없습니다");
    }
}

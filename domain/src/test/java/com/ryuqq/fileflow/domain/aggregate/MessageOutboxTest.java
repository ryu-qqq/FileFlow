package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.fixture.MessageOutboxFixture;
import com.ryuqq.fileflow.domain.fixture.OutboxStatusFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(outbox.getId()).isNotBlank();
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
        assertThat(outbox.getId()).isNotBlank();
        assertThat(outbox.getEventType()).isNotBlank();
        assertThat(outbox.getAggregateId()).isNotBlank();
        assertThat(outbox.getPayload()).isNotBlank();
        assertThat(outbox.getStatus()).isNotNull();
        assertThat(outbox.getMaxRetryCount()).isPositive();
        assertThat(outbox.getCreatedAt()).isNotNull();
    }
}

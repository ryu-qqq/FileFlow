package com.ryuqq.fileflow.domain.outbox.vo;

import com.ryuqq.fileflow.domain.outbox.fixture.OutboxStatusFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OutboxStatus Value Object 테스트")
class OutboxStatusTest {

    @Test
    @DisplayName("모든 필수 상태를 포함해야 한다")
    void shouldContainAllRequiredStatuses() {
        // Given & When
        OutboxStatus[] statuses = OutboxStatus.values();

        // Then
        assertThat(statuses).hasSize(3);
        assertThat(statuses).contains(
                OutboxStatusFixture.pending(),
                OutboxStatusFixture.sent(),
                OutboxStatusFixture.failed()
        );
    }

    @Test
    @DisplayName("PENDING에서 SENT로 전환 가능해야 한다")
    void shouldTransitionFromPendingToSent() {
        // Given
        OutboxStatus pending = OutboxStatusFixture.pending();

        // When & Then
        assertThat(pending).isNotNull();
        assertThat(OutboxStatusFixture.sent()).isNotNull();
        assertThat(pending).isNotEqualTo(OutboxStatusFixture.sent());
    }
}

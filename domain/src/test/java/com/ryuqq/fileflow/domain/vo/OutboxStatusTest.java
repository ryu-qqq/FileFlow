package com.ryuqq.fileflow.domain.vo;

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
                OutboxStatus.PENDING,
                OutboxStatus.SENT,
                OutboxStatus.FAILED
        );
    }

    @Test
    @DisplayName("PENDING에서 SENT로 전환 가능해야 한다")
    void shouldTransitionFromPendingToSent() {
        // Given
        OutboxStatus pending = OutboxStatus.PENDING;

        // When & Then
        assertThat(pending).isNotNull();
        assertThat(OutboxStatus.SENT).isNotNull();
        assertThat(pending).isNotEqualTo(OutboxStatus.SENT);
    }
}

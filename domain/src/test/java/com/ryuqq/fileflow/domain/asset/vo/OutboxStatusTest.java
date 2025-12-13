package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** OutboxStatus Enum 단위 테스트. */
@DisplayName("OutboxStatus Enum 단위 테스트")
class OutboxStatusTest {

    @Test
    @DisplayName("PENDING 상태가 존재한다")
    void shouldHavePendingStatus() {
        // when
        OutboxStatus status = OutboxStatus.PENDING;

        // then
        assertThat(status).isNotNull();
        assertThat(status.name()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("SENT 상태가 존재한다")
    void shouldHaveSentStatus() {
        // when
        OutboxStatus status = OutboxStatus.SENT;

        // then
        assertThat(status).isNotNull();
        assertThat(status.name()).isEqualTo("SENT");
    }

    @Test
    @DisplayName("FAILED 상태가 존재한다")
    void shouldHaveFailedStatus() {
        // when
        OutboxStatus status = OutboxStatus.FAILED;

        // then
        assertThat(status).isNotNull();
        assertThat(status.name()).isEqualTo("FAILED");
    }

    @Test
    @DisplayName("모든 상태 값이 3개이다")
    void shouldHaveThreeStatuses() {
        // when
        OutboxStatus[] statuses = OutboxStatus.values();

        // then
        assertThat(statuses).hasSize(3);
        assertThat(statuses)
                .containsExactlyInAnyOrder(
                        OutboxStatus.PENDING, OutboxStatus.SENT, OutboxStatus.FAILED);
    }
}

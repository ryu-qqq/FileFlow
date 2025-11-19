package com.ryuqq.fileflow.domain.session.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SessionStatus Enum Tests")
class SessionStatusTest {

    @Test
    @DisplayName("PREPARING → ACTIVE → (COMPLETED|EXPIRED|FAILED) 전환을 허용해야 한다")
    void shouldTransitionCorrectly() {
        assertThat(SessionStatus.PREPARING.canTransitionTo(SessionStatus.ACTIVE)).isTrue();
        assertThat(SessionStatus.ACTIVE.canTransitionTo(SessionStatus.COMPLETED)).isTrue();
        assertThat(SessionStatus.ACTIVE.canTransitionTo(SessionStatus.EXPIRED)).isTrue();
        assertThat(SessionStatus.ACTIVE.canTransitionTo(SessionStatus.FAILED)).isTrue();
    }

    @Test
    @DisplayName("정의되지 않은 전환은 거부해야 한다")
    void shouldRejectInvalidTransitions() {
        assertThat(SessionStatus.PREPARING.canTransitionTo(SessionStatus.COMPLETED)).isFalse();
        assertThat(SessionStatus.ACTIVE.canTransitionTo(SessionStatus.PREPARING)).isFalse();
        assertThat(SessionStatus.COMPLETED.canTransitionTo(SessionStatus.ACTIVE)).isFalse();
        assertThat(SessionStatus.EXPIRED.canTransitionTo(SessionStatus.FAILED)).isFalse();
        assertThat(SessionStatus.FAILED.canTransitionTo(SessionStatus.ACTIVE)).isFalse();
    }
}


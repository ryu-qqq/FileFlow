package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SessionStatus Value Object 테스트
 */
class SessionStatusTest {

    @Test
    @DisplayName("INITIATED 상태는 시작됨을 의미해야 한다")
    void shouldCheckInitiatedStatus() {
        // when
        SessionStatus status = SessionStatus.INITIATED;

        // then
        assertThat(status.isInitiated()).isTrue();
        assertThat(status.isInProgress()).isFalse();
        assertThat(status.isCompleted()).isFalse();
        assertThat(status.isTerminal()).isFalse();
    }

    @Test
    @DisplayName("IN_PROGRESS 상태는 진행 중을 의미해야 한다")
    void shouldCheckInProgressStatus() {
        // when
        SessionStatus status = SessionStatus.IN_PROGRESS;

        // then
        assertThat(status.isInitiated()).isFalse();
        assertThat(status.isInProgress()).isTrue();
        assertThat(status.isCompleted()).isFalse();
        assertThat(status.isTerminal()).isFalse();
    }

    @Test
    @DisplayName("COMPLETED 상태는 완료를 의미하며 종료 상태여야 한다")
    void shouldCheckCompletedStatus() {
        // when
        SessionStatus status = SessionStatus.COMPLETED;

        // then
        assertThat(status.isCompleted()).isTrue();
        assertThat(status.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("EXPIRED 상태는 만료를 의미하며 종료 상태여야 한다")
    void shouldCheckExpiredStatus() {
        // when
        SessionStatus status = SessionStatus.EXPIRED;

        // then
        assertThat(status.isExpired()).isTrue();
        assertThat(status.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("FAILED 상태는 실패를 의미하며 종료 상태여야 한다")
    void shouldCheckFailedStatus() {
        // when
        SessionStatus status = SessionStatus.FAILED;

        // then
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("종료 상태는 COMPLETED, EXPIRED, FAILED만 해당되어야 한다")
    void shouldIdentifyTerminalStatuses() {
        // when & then
        assertThat(SessionStatus.COMPLETED.isTerminal()).isTrue();
        assertThat(SessionStatus.EXPIRED.isTerminal()).isTrue();
        assertThat(SessionStatus.FAILED.isTerminal()).isTrue();

        assertThat(SessionStatus.INITIATED.isTerminal()).isFalse();
        assertThat(SessionStatus.IN_PROGRESS.isTerminal()).isFalse();
    }
}

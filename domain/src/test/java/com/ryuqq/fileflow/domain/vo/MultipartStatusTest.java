package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MultipartStatus Value Object 테스트
 */
class MultipartStatusTest {

    @Test
    @DisplayName("INITIATED 상태는 시작됨을 의미해야 한다")
    void shouldCheckInitiatedStatus() {
        // when
        MultipartStatus status = MultipartStatus.INITIATED;

        // then
        assertThat(status.isInitiated()).isTrue();
        assertThat(status.isInProgress()).isFalse();
        assertThat(status.isCompleted()).isFalse();
        assertThat(status.isAborted()).isFalse();
    }

    @Test
    @DisplayName("IN_PROGRESS 상태는 진행 중을 의미해야 한다")
    void shouldCheckInProgressStatus() {
        // when
        MultipartStatus status = MultipartStatus.IN_PROGRESS;

        // then
        assertThat(status.isInitiated()).isFalse();
        assertThat(status.isInProgress()).isTrue();
        assertThat(status.isCompleted()).isFalse();
        assertThat(status.isAborted()).isFalse();
    }

    @Test
    @DisplayName("COMPLETED 상태는 완료를 의미하며 종료 상태여야 한다")
    void shouldCheckCompletedStatus() {
        // when
        MultipartStatus status = MultipartStatus.COMPLETED;

        // then
        assertThat(status.isCompleted()).isTrue();
        assertThat(status.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("ABORTED 상태는 중단을 의미하며 종료 상태여야 한다")
    void shouldCheckAbortedStatus() {
        // when
        MultipartStatus status = MultipartStatus.ABORTED;

        // then
        assertThat(status.isAborted()).isTrue();
        assertThat(status.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("종료 상태는 COMPLETED, ABORTED만 해당되어야 한다")
    void shouldIdentifyTerminalStatuses() {
        // when & then
        assertThat(MultipartStatus.COMPLETED.isTerminal()).isTrue();
        assertThat(MultipartStatus.ABORTED.isTerminal()).isTrue();

        assertThat(MultipartStatus.INITIATED.isTerminal()).isFalse();
        assertThat(MultipartStatus.IN_PROGRESS.isTerminal()).isFalse();
    }
}

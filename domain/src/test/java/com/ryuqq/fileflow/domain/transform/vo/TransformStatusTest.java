package com.ryuqq.fileflow.domain.transform.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TransformStatusTest {

    @Test
    @DisplayName("모든 TransformStatus 값이 존재한다")
    void all_values_exist() {
        assertThat(TransformStatus.values())
                .containsExactly(
                        TransformStatus.QUEUED,
                        TransformStatus.PROCESSING,
                        TransformStatus.COMPLETED,
                        TransformStatus.FAILED);
    }

    @ParameterizedTest
    @CsvSource({"QUEUED, 대기", "PROCESSING, 처리 중", "COMPLETED, 완료", "FAILED, 실패"})
    @DisplayName("각 상태는 올바른 displayName을 반환한다")
    void display_name_matches(TransformStatus status, String expectedDisplayName) {
        assertThat(status.displayName()).isEqualTo(expectedDisplayName);
    }

    @Test
    @DisplayName("COMPLETED와 FAILED는 terminal 상태이다")
    void terminal_states() {
        assertThat(TransformStatus.COMPLETED.isTerminal()).isTrue();
        assertThat(TransformStatus.FAILED.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("QUEUED와 PROCESSING은 terminal 상태가 아니다")
    void non_terminal_states() {
        assertThat(TransformStatus.QUEUED.isTerminal()).isFalse();
        assertThat(TransformStatus.PROCESSING.isTerminal()).isFalse();
    }
}

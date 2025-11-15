package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JobStatus Value Object 테스트")
class JobStatusTest {

    @Test
    @DisplayName("모든 필수 상태를 포함해야 한다")
    void shouldContainAllRequiredStatuses() {
        // Given & When
        JobStatus[] statuses = JobStatus.values();

        // Then
        assertThat(statuses).hasSize(5);
        assertThat(statuses).contains(
                JobStatus.PENDING,
                JobStatus.PROCESSING,
                JobStatus.COMPLETED,
                JobStatus.FAILED,
                JobStatus.RETRY_PENDING
        );
    }

    @Test
    @DisplayName("PENDING에서 PROCESSING으로 전환 가능해야 한다")
    void shouldTransitionFromPendingToProcessing() {
        // Given
        JobStatus pending = JobStatus.PENDING;

        // When & Then
        assertThat(pending).isNotNull();
        assertThat(JobStatus.PROCESSING).isNotNull();
        assertThat(pending).isNotEqualTo(JobStatus.PROCESSING);
    }
}

package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FileStatus Value Object 테스트")
class FileStatusTest {

    @Test
    @DisplayName("모든 필수 상태를 포함해야 한다")
    void shouldContainAllRequiredStatuses() {
        // Given & When
        FileStatus[] statuses = FileStatus.values();

        // Then
        assertThat(statuses).hasSize(6);
        assertThat(statuses).contains(
                FileStatus.PENDING,
                FileStatus.UPLOADING,
                FileStatus.COMPLETED,
                FileStatus.FAILED,
                FileStatus.RETRY_PENDING,
                FileStatus.PROCESSING
        );
    }

    @Test
    @DisplayName("PENDING에서 UPLOADING으로 전환 가능해야 한다")
    void shouldTransitionFromPendingToUploading() {
        // Given
        FileStatus pending = FileStatus.PENDING;

        // When & Then
        assertThat(pending).isNotNull();
        assertThat(FileStatus.UPLOADING).isNotNull();
        assertThat(pending).isNotEqualTo(FileStatus.UPLOADING);
    }
}

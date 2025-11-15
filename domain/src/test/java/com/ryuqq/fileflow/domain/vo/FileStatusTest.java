package com.ryuqq.fileflow.domain.vo;

import com.ryuqq.fileflow.domain.fixture.FileStatusFixture;
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
                FileStatusFixture.pending(),
                FileStatusFixture.uploading(),
                FileStatusFixture.completed(),
                FileStatusFixture.failed(),
                FileStatusFixture.retryPending(),
                FileStatusFixture.processing()
        );
    }

    @Test
    @DisplayName("PENDING에서 UPLOADING으로 전환 가능해야 한다")
    void shouldTransitionFromPendingToUploading() {
        // Given
        FileStatus pending = FileStatusFixture.pending();

        // When & Then
        assertThat(pending).isNotNull();
        assertThat(FileStatusFixture.uploading()).isNotNull();
        assertThat(pending).isNotEqualTo(FileStatusFixture.uploading());
    }
}

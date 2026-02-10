package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DownloadTaskStatus")
class DownloadTaskStatusTest {

    @Test
    @DisplayName("모든 enum 값이 존재한다")
    void allValuesExist() {
        assertThat(DownloadTaskStatus.values())
                .containsExactly(
                        DownloadTaskStatus.QUEUED,
                        DownloadTaskStatus.DOWNLOADING,
                        DownloadTaskStatus.COMPLETED,
                        DownloadTaskStatus.FAILED);
    }

    @Test
    @DisplayName("QUEUED의 displayName은 '대기 중'이다")
    void queuedDisplayName() {
        assertThat(DownloadTaskStatus.QUEUED.displayName()).isEqualTo("대기 중");
    }

    @Test
    @DisplayName("DOWNLOADING의 displayName은 '다운로드 중'이다")
    void downloadingDisplayName() {
        assertThat(DownloadTaskStatus.DOWNLOADING.displayName()).isEqualTo("다운로드 중");
    }

    @Test
    @DisplayName("COMPLETED의 displayName은 '완료'이다")
    void completedDisplayName() {
        assertThat(DownloadTaskStatus.COMPLETED.displayName()).isEqualTo("완료");
    }

    @Test
    @DisplayName("FAILED의 displayName은 '실패'이다")
    void failedDisplayName() {
        assertThat(DownloadTaskStatus.FAILED.displayName()).isEqualTo("실패");
    }
}

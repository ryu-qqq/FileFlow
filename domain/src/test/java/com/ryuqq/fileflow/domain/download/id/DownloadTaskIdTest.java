package com.ryuqq.fileflow.domain.download.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DownloadTaskId")
class DownloadTaskIdTest {

    @Test
    @DisplayName("of 팩토리로 생성하면 value가 올바르게 설정된다")
    void ofFactoryCreatesWithValue() {
        DownloadTaskId id = DownloadTaskId.of("download-001");

        assertThat(id.value()).isEqualTo("download-001");
    }

    @Test
    @DisplayName("같은 value를 가진 DownloadTaskId는 동일하다")
    void equalWhenSameValue() {
        DownloadTaskId id1 = DownloadTaskId.of("download-001");
        DownloadTaskId id2 = DownloadTaskId.of("download-001");

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("다른 value를 가진 DownloadTaskId는 다르다")
    void notEqualWhenDifferentValue() {
        DownloadTaskId id1 = DownloadTaskId.of("download-001");
        DownloadTaskId id2 = DownloadTaskId.of("download-002");

        assertThat(id1).isNotEqualTo(id2);
    }
}

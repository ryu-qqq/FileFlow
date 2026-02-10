package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("MultipartSessionStatus 단위 테스트")
class MultipartSessionStatusTest {

    @Test
    @DisplayName("INITIATED의 displayName은 '초기화됨'이다")
    void initiatedDisplayName() {
        assertThat(MultipartSessionStatus.INITIATED.displayName()).isEqualTo("초기화됨");
    }

    @Test
    @DisplayName("UPLOADING의 displayName은 '업로드 중'이다")
    void uploadingDisplayName() {
        assertThat(MultipartSessionStatus.UPLOADING.displayName()).isEqualTo("업로드 중");
    }

    @Test
    @DisplayName("COMPLETED의 displayName은 '완료'이다")
    void completedDisplayName() {
        assertThat(MultipartSessionStatus.COMPLETED.displayName()).isEqualTo("완료");
    }

    @Test
    @DisplayName("ABORTED의 displayName은 '중단됨'이다")
    void abortedDisplayName() {
        assertThat(MultipartSessionStatus.ABORTED.displayName()).isEqualTo("중단됨");
    }

    @Test
    @DisplayName("EXPIRED의 displayName은 '만료'이다")
    void expiredDisplayName() {
        assertThat(MultipartSessionStatus.EXPIRED.displayName()).isEqualTo("만료");
    }

    @Test
    @DisplayName("MultipartSessionStatus는 5개의 값을 가진다")
    void hasFiveValues() {
        assertThat(MultipartSessionStatus.values()).hasSize(5);
    }
}

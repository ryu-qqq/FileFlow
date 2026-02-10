package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SingleSessionStatus 단위 테스트")
class SingleSessionStatusTest {

    @Test
    @DisplayName("CREATED의 displayName은 '생성됨'이다")
    void createdDisplayName() {
        assertThat(SingleSessionStatus.CREATED.displayName()).isEqualTo("생성됨");
    }

    @Test
    @DisplayName("COMPLETED의 displayName은 '완료'이다")
    void completedDisplayName() {
        assertThat(SingleSessionStatus.COMPLETED.displayName()).isEqualTo("완료");
    }

    @Test
    @DisplayName("EXPIRED의 displayName은 '만료'이다")
    void expiredDisplayName() {
        assertThat(SingleSessionStatus.EXPIRED.displayName()).isEqualTo("만료");
    }

    @Test
    @DisplayName("SingleSessionStatus는 3개의 값을 가진다")
    void hasThreeValues() {
        assertThat(SingleSessionStatus.values()).hasSize(3);
    }
}

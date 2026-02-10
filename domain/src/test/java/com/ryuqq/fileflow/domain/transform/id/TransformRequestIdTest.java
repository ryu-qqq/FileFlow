package com.ryuqq.fileflow.domain.transform.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransformRequestIdTest {

    @Test
    @DisplayName("유효한 값으로 생성 시 정상적으로 생성된다")
    void creates_with_valid_value() {
        TransformRequestId id = TransformRequestId.of("transform-001");

        assertThat(id.value()).isEqualTo("transform-001");
    }

    @Test
    @DisplayName("null 값으로 생성 시 NullPointerException이 발생한다")
    void null_value_throws_npe() {
        assertThatThrownBy(() -> new TransformRequestId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("blank 값으로 생성 시 IllegalArgumentException이 발생한다")
    void blank_value_throws_exception() {
        assertThatThrownBy(() -> new TransformRequestId("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }
}

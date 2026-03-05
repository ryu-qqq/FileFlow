package com.ryuqq.fileflow.domain.transform.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransformCallbackOutboxIdTest {

    @Test
    @DisplayName("of 팩토리 메서드로 생성한 ID의 value를 반환한다")
    void of_returnsIdWithValue() {
        TransformCallbackOutboxId id = TransformCallbackOutboxId.of("outbox-001");

        assertThat(id.value()).isEqualTo("outbox-001");
    }

    @Test
    @DisplayName("같은 value를 가진 ID는 동등하다")
    void sameValue_areEqual() {
        TransformCallbackOutboxId id1 = TransformCallbackOutboxId.of("outbox-001");
        TransformCallbackOutboxId id2 = TransformCallbackOutboxId.of("outbox-001");

        assertThat(id1).isEqualTo(id2);
    }
}

package com.ryuqq.fileflow.domain.transform.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("TransformQueueOutboxId 단위 테스트")
class TransformQueueOutboxIdTest {

    @Test
    @DisplayName("유효한 값으로 생성 시 정상적으로 생성된다")
    void creates_with_valid_value() {
        TransformQueueOutboxId id = TransformQueueOutboxId.of("outbox-001");

        assertThat(id.value()).isEqualTo("outbox-001");
    }

    @Test
    @DisplayName("of 팩토리 메서드와 생성자가 동일한 결과를 반환한다")
    void of_returns_same_as_constructor() {
        TransformQueueOutboxId fromOf = TransformQueueOutboxId.of("outbox-001");
        TransformQueueOutboxId fromConstructor = new TransformQueueOutboxId("outbox-001");

        assertThat(fromOf).isEqualTo(fromConstructor);
    }
}

package com.ryuqq.fileflow.domain.session.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SingleUploadSessionId 단위 테스트")
class SingleUploadSessionIdTest {

    @Test
    @DisplayName("of 팩토리 메서드로 ID를 생성할 수 있다")
    void createsWithOfFactory() {
        SingleUploadSessionId id = SingleUploadSessionId.of("session-001");

        assertThat(id.value()).isEqualTo("session-001");
    }

    @Test
    @DisplayName("동일한 value를 가진 ID는 동등하다")
    void equalsByValue() {
        SingleUploadSessionId id1 = SingleUploadSessionId.of("session-001");
        SingleUploadSessionId id2 = SingleUploadSessionId.of("session-001");

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    @DisplayName("다른 value를 가진 ID는 동등하지 않다")
    void notEqualWithDifferentValue() {
        SingleUploadSessionId id1 = SingleUploadSessionId.of("session-001");
        SingleUploadSessionId id2 = SingleUploadSessionId.of("session-002");

        assertThat(id1).isNotEqualTo(id2);
    }
}

package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("CompletedPart Value Object 단위 테스트")
class CompletedPartTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("of - 생성")
    class Of {

        @Test
        @DisplayName("유효한 값으로 CompletedPart를 생성할 수 있다")
        void createsWithValidValues() {
            CompletedPart part = CompletedPart.of(1, "etag-123", 5_242_880L, NOW);

            assertThat(part.partNumber()).isEqualTo(1);
            assertThat(part.etag()).isEqualTo("etag-123");
            assertThat(part.size()).isEqualTo(5_242_880L);
            assertThat(part.createdAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class Validation {

        @Test
        @DisplayName("partNumber가 0이면 IllegalArgumentException이 발생한다")
        void throwsWhenPartNumberIsZero() {
            assertThatThrownBy(() -> CompletedPart.of(0, "etag-123", 1024L, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("partNumber must be >= 1");
        }

        @Test
        @DisplayName("partNumber가 음수면 IllegalArgumentException이 발생한다")
        void throwsWhenPartNumberIsNegative() {
            assertThatThrownBy(() -> CompletedPart.of(-1, "etag-123", 1024L, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("partNumber must be >= 1");
        }

        @Test
        @DisplayName("etag가 null이면 NullPointerException이 발생한다")
        void throwsWhenEtagIsNull() {
            assertThatThrownBy(() -> CompletedPart.of(1, null, 1024L, NOW))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("etag must not be null");
        }

        @Test
        @DisplayName("size가 0이면 IllegalArgumentException이 발생한다")
        void throwsWhenSizeIsZero() {
            assertThatThrownBy(() -> CompletedPart.of(1, "etag-123", 0L, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size must be > 0");
        }

        @Test
        @DisplayName("size가 음수면 IllegalArgumentException이 발생한다")
        void throwsWhenSizeIsNegative() {
            assertThatThrownBy(() -> CompletedPart.of(1, "etag-123", -1L, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size must be > 0");
        }

        @Test
        @DisplayName("createdAt가 null이면 NullPointerException이 발생한다")
        void throwsWhenCreatedAtIsNull() {
            assertThatThrownBy(() -> CompletedPart.of(1, "etag-123", 1024L, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("createdAt must not be null");
        }
    }
}

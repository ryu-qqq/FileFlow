package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("WebhookOutboxId 단위 테스트")
class WebhookOutboxIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 ID를 생성할 수 있다")
        void forNew_ShouldCreateNewId() {
            // given & when
            WebhookOutboxId id = WebhookOutboxId.forNew();

            // then
            assertThat(id.value()).isNotNull();
            assertThat(id.getValue()).isNotBlank();
        }

        @Test
        @DisplayName("of(UUID)로 기존 ID를 재구성할 수 있다")
        void of_WithUuid_ShouldReconstituteId() {
            // given
            UUID value = UUID.fromString("00000000-0000-0000-0000-000000000100");

            // when
            WebhookOutboxId id = WebhookOutboxId.of(value);

            // then
            assertThat(id.value()).isEqualTo(value);
        }

        @Test
        @DisplayName("of(String)로 기존 ID를 재구성할 수 있다")
        void of_WithString_ShouldReconstituteId() {
            // given
            String value = "00000000-0000-0000-0000-000000000100";

            // when
            WebhookOutboxId id = WebhookOutboxId.of(value);

            // then
            assertThat(id.value().toString()).isEqualTo(value);
            assertThat(id.getValue()).isEqualTo(value);
        }

        @Test
        @DisplayName("of(UUID)에 null을 전달하면 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given
            UUID nullValue = null;

            // when & then
            assertThatThrownBy(() -> WebhookOutboxId.of(nullValue))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("WebhookOutboxId");
        }

        @Test
        @DisplayName("of(String)에 잘못된 UUID 형식을 전달하면 예외가 발생한다")
        void of_WithInvalidFormat_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> WebhookOutboxId.of("invalid-uuid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값의 WebhookOutboxId는 동등하다")
        void sameValue_ShouldBeEqual() {
            // given
            WebhookOutboxId id1 = WebhookOutboxId.of("00000000-0000-0000-0000-000000000001");
            WebhookOutboxId id2 = WebhookOutboxId.of("00000000-0000-0000-0000-000000000001");

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 WebhookOutboxId는 동등하지 않다")
        void differentValue_ShouldNotBeEqual() {
            // given
            WebhookOutboxId id1 = WebhookOutboxId.of("00000000-0000-0000-0000-000000000001");
            WebhookOutboxId id2 = WebhookOutboxId.of("00000000-0000-0000-0000-000000000002");

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }
    }
}

package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadOutboxId 단위 테스트")
class ExternalDownloadOutboxIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 ID를 생성할 수 있다")
        void forNew_ShouldCreateNewId() {
            // given & when
            ExternalDownloadOutboxId id = ExternalDownloadOutboxId.forNew();

            // then
            assertThat(id.value()).isNull();
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("of()로 기존 ID를 재구성할 수 있다")
        void of_ShouldReconstituteId() {
            // given
            Long value = 1L;

            // when
            ExternalDownloadOutboxId id = ExternalDownloadOutboxId.of(value);

            // then
            assertThat(id.value()).isEqualTo(1L);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("of()에 null을 전달하면 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> ExternalDownloadOutboxId.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("ExternalDownloadOutboxId");
        }

        @Test
        @DisplayName("of()에 0 이하의 값을 전달하면 예외가 발생한다")
        void of_WithZeroOrNegative_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> ExternalDownloadOutboxId.of(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");

            assertThatThrownBy(() -> ExternalDownloadOutboxId.of(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값의 ExternalDownloadOutboxId는 동등하다")
        void sameValue_ShouldBeEqual() {
            // given
            ExternalDownloadOutboxId id1 = ExternalDownloadOutboxId.of(1L);
            ExternalDownloadOutboxId id2 = ExternalDownloadOutboxId.of(1L);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 ExternalDownloadOutboxId는 동등하지 않다")
        void differentValue_ShouldNotBeEqual() {
            // given
            ExternalDownloadOutboxId id1 = ExternalDownloadOutboxId.of(1L);
            ExternalDownloadOutboxId id2 = ExternalDownloadOutboxId.of(2L);

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }
    }
}

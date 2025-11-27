package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadId 단위 테스트")
class ExternalDownloadIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 ID(null)를 생성할 수 있다")
        void forNew_ShouldCreateNullId() {
            // given & when
            ExternalDownloadId id = ExternalDownloadId.forNew();

            // then
            assertThat(id).isNotNull();
            assertThat(id.value()).isNull();
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("of(Long)로 특정 값을 가진 ID를 생성할 수 있다")
        void of_WithLong_ShouldCreateIdWithSpecificValue() {
            // given
            Long value = 100L;

            // when
            ExternalDownloadId id = ExternalDownloadId.of(value);

            // then
            assertThat(id.value()).isEqualTo(value);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("of(Long)로 null 값을 전달하면 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given
            Long nullValue = null;

            // when & then
            assertThatThrownBy(() -> ExternalDownloadId.of(nullValue))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("ExternalDownloadId");
        }

        @Test
        @DisplayName("of(Long)로 0 이하의 값을 전달하면 예외가 발생한다")
        void of_WithZeroOrNegative_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> ExternalDownloadId.of(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");

            assertThatThrownBy(() -> ExternalDownloadId.of(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 ID는 동등하다")
        void equals_WithSameValue_ShouldBeEqual() {
            // given
            ExternalDownloadId id1 = ExternalDownloadId.of(100L);
            ExternalDownloadId id2 = ExternalDownloadId.of(100L);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ID는 동등하지 않다")
        void equals_WithDifferentValue_ShouldNotBeEqual() {
            // given
            ExternalDownloadId id1 = ExternalDownloadId.of(100L);
            ExternalDownloadId id2 = ExternalDownloadId.of(200L);

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("forNew()로 생성된 ID들은 모두 동등하다 (둘 다 null)")
        void equals_ForNewIds_ShouldBeEqual() {
            // given
            ExternalDownloadId id1 = ExternalDownloadId.forNew();
            ExternalDownloadId id2 = ExternalDownloadId.forNew();

            // when & then
            assertThat(id1).isEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("isNew 테스트")
    class IsNewTest {

        @Test
        @DisplayName("forNew()로 생성된 ID는 isNew()가 true를 반환한다")
        void isNew_ForNewId_ShouldReturnTrue() {
            // given
            ExternalDownloadId id = ExternalDownloadId.forNew();

            // when & then
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("of(Long)로 생성된 ID는 isNew()가 false를 반환한다")
        void isNew_ForExistingId_ShouldReturnFalse() {
            // given
            ExternalDownloadId id = ExternalDownloadId.of(100L);

            // when & then
            assertThat(id.isNew()).isFalse();
        }
    }
}

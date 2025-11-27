package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.fixture.ETagFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ETag 단위 테스트")
class ETagTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 ETag 값으로 생성할 수 있다")
        void of_WithValidValue_ShouldCreateETag() {
            // given
            String validETag = "d41d8cd98f00b204e9800998ecf8427e";

            // when
            ETag etag = ETag.of(validETag);

            // then
            assertThat(etag.value()).isEqualTo(validETag);
            assertThat(etag.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("빈 ETag를 생성할 수 있다")
        void empty_ShouldCreateEmptyETag() {
            // given & when
            ETag emptyETag = ETag.empty();

            // then
            assertThat(emptyETag.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("null 값으로 생성 시 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given
            String nullValue = null;

            // when & then
            assertThatThrownBy(() -> ETag.of(nullValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ETag는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 ETag는 동등하다")
        void equals_WithSameValue_ShouldBeEqual() {
            // given
            String value = "d41d8cd98f00b204e9800998ecf8427e";
            ETag etag1 = ETag.of(value);
            ETag etag2 = ETag.of(value);

            // when & then
            assertThat(etag1).isEqualTo(etag2);
            assertThat(etag1.hashCode()).isEqualTo(etag2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ETag는 동등하지 않다")
        void equals_WithDifferentValue_ShouldNotBeEqual() {
            // given
            ETag etag1 = ETag.of("d41d8cd98f00b204e9800998ecf8427e");
            ETag etag2 = ETag.of("e41d8cd98f00b204e9800998ecf8427f");

            // when & then
            assertThat(etag1).isNotEqualTo(etag2);
        }

        @Test
        @DisplayName("빈 ETag들은 서로 동등하다")
        void equals_WithEmptyETags_ShouldBeEqual() {
            // given
            ETag emptyETag1 = ETag.empty();
            ETag emptyETag2 = ETag.empty();

            // when & then
            assertThat(emptyETag1).isEqualTo(emptyETag2);
            assertThat(emptyETag1.hashCode()).isEqualTo(emptyETag2.hashCode());
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 ETag가 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            ETag defaultETag = ETagFixture.defaultETag();
            ETag emptyETag = ETagFixture.emptyETag();
            ETag multipartETag = ETagFixture.multipartETag();

            // then
            assertThat(defaultETag.value()).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
            assertThat(emptyETag.isEmpty()).isTrue();
            assertThat(multipartETag.value()).isEqualTo("d41d8cd98f00b204e9800998ecf8427e-5");
        }
    }
}

package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.fixture.IdempotencyKeyFixture;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("IdempotencyKey 단위 테스트")
class IdempotencyKeyTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 키를 생성할 수 있다")
        void forNew_ShouldCreateNewKey() {
            // given & when
            IdempotencyKey key1 = IdempotencyKey.forNew();
            IdempotencyKey key2 = IdempotencyKey.forNew();

            // then
            assertThat(key1).isNotNull();
            assertThat(key2).isNotNull();
            assertThat(key1).isNotEqualTo(key2); // 매번 다른 UUID 생성
            assertThat(key1.value()).isNotNull();
            assertThat(key2.value()).isNotNull();
        }

        @Test
        @DisplayName("of(UUID)로 특정 UUID를 가진 키를 생성할 수 있다")
        void of_WithUUID_ShouldCreateKeyWithSpecificValue() {
            // given
            UUID uuid = UUID.randomUUID();

            // when
            IdempotencyKey key = IdempotencyKey.of(uuid);

            // then
            assertThat(key.value()).isEqualTo(uuid);
        }

        @Test
        @DisplayName("null UUID로 생성 시 예외가 발생한다")
        void of_WithNullUUID_ShouldThrowException() {
            // given
            UUID nullUuid = null;

            // when & then
            assertThatThrownBy(() -> IdempotencyKey.of(nullUuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("IdempotencyKey는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 UUID를 가진 키는 동등하다")
        void equals_WithSameUUID_ShouldBeEqual() {
            // given
            UUID uuid = UUID.randomUUID();
            IdempotencyKey key1 = IdempotencyKey.of(uuid);
            IdempotencyKey key2 = IdempotencyKey.of(uuid);

            // when & then
            assertThat(key1).isEqualTo(key2);
            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        }

        @Test
        @DisplayName("다른 UUID를 가진 키는 동등하지 않다")
        void equals_WithDifferentUUID_ShouldNotBeEqual() {
            // given
            IdempotencyKey key1 = IdempotencyKey.forNew();
            IdempotencyKey key2 = IdempotencyKey.forNew();

            // when & then
            assertThat(key1).isNotEqualTo(key2);
        }
    }

    @Nested
    @DisplayName("fromString 테스트")
    class FromStringTest {

        @Test
        @DisplayName("유효한 UUID 문자열로 생성할 수 있다")
        void fromString_WithValidUuidString_ShouldCreate() {
            // given
            String uuidString = "550e8400-e29b-41d4-a716-446655440000";

            // when
            IdempotencyKey key = IdempotencyKey.fromString(uuidString);

            // then
            assertThat(key.value().toString()).isEqualTo(uuidString);
        }

        @Test
        @DisplayName("null 문자열로 생성 시 예외가 발생한다")
        void fromString_WithNull_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> IdempotencyKey.fromString(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 예외가 발생한다")
        void fromString_WithBlank_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> IdempotencyKey.fromString(""))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> IdempotencyKey.fromString("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("유효하지 않은 UUID 형식으로 생성 시 예외가 발생한다")
        void fromString_WithInvalidFormat_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> IdempotencyKey.fromString("not-a-uuid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UUID");
        }
    }

    @Nested
    @DisplayName("getValue 테스트")
    class GetValueTest {

        @Test
        @DisplayName("getValue()는 UUID의 문자열 표현을 반환한다")
        void getValue_ShouldReturnUuidString() {
            // given
            UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            IdempotencyKey key = IdempotencyKey.of(uuid);

            // when
            String result = key.getValue();

            // then
            assertThat(result).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 키가 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            IdempotencyKey defaultKey = IdempotencyKeyFixture.defaultIdempotencyKey();
            IdempotencyKey fixedKey = IdempotencyKeyFixture.fixedIdempotencyKey();
            IdempotencyKey customKey =
                    IdempotencyKeyFixture.customIdempotencyKey(UUID.randomUUID());

            // then
            assertThat(defaultKey).isNotNull();
            assertThat(fixedKey).isNotNull();
            assertThat(customKey).isNotNull();
            assertThat(fixedKey.value().toString())
                    .isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        }
    }
}

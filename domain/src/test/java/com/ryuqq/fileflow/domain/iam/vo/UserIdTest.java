package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.common.util.UuidV7Generator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserId 테스트")
class UserIdTest {

    @Nested
    @DisplayName("생성자")
    class ConstructorTest {

        @Test
        @DisplayName("유효한 UUIDv7로 생성할 수 있다")
        void shouldCreateWithValidUuidV7() {
            // given
            String validUuid = UuidV7Generator.generate();

            // when
            UserId userId = new UserId(validUuid);

            // then
            assertThat(userId.value()).isEqualTo(validUuid);
        }

        @Test
        @DisplayName("null 값은 예외를 던진다")
        void shouldThrowForNullValue() {
            assertThatThrownBy(() -> new UserId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UserId")
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("빈 문자열은 예외를 던진다")
        void shouldThrowForEmptyString() {
            assertThatThrownBy(() -> new UserId(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UserId");
        }

        @Test
        @DisplayName("잘못된 UUID 형식은 예외를 던진다")
        void shouldThrowForInvalidUuidFormat() {
            assertThatThrownBy(() -> new UserId("invalid-uuid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UserId")
                    .hasMessageContaining("UUIDv7");
        }

        @Test
        @DisplayName("UUIDv4는 예외를 던진다")
        void shouldThrowForUuidV4() {
            // given - UUIDv4
            String uuidV4 = "550e8400-e29b-41d4-a716-446655440000";

            // when & then
            assertThatThrownBy(() -> new UserId(uuidV4))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UUIDv7");
        }
    }

    @Nested
    @DisplayName("of() 정적 팩토리")
    class OfTest {

        @Test
        @DisplayName("유효한 값으로 생성할 수 있다")
        void shouldCreateWithValidValue() {
            // given
            String validUuid = UuidV7Generator.generate();

            // when
            UserId userId = UserId.of(validUuid);

            // then
            assertThat(userId.value()).isEqualTo(validUuid);
        }
    }

    @Nested
    @DisplayName("generate() 정적 팩토리")
    class GenerateTest {

        @Test
        @DisplayName("새로운 UUIDv7 기반 UserId를 생성한다")
        void shouldGenerateNewUserId() {
            // when
            UserId userId = UserId.generate();

            // then
            assertThat(userId).isNotNull();
            assertThat(userId.value()).hasSize(36);
            assertThat(UuidV7Generator.isValid(userId.value())).isTrue();
        }

        @Test
        @DisplayName("생성된 UserId는 고유하다")
        void shouldGenerateUniqueUserIds() {
            // when
            UserId userId1 = UserId.generate();
            UserId userId2 = UserId.generate();

            // then
            assertThat(userId1).isNotEqualTo(userId2);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 값을 가진 UserId는 동등하다")
        void shouldBeEqualForSameValue() {
            // given
            String uuid = UuidV7Generator.generate();

            // when
            UserId userId1 = UserId.of(uuid);
            UserId userId2 = UserId.of(uuid);

            // then
            assertThat(userId1).isEqualTo(userId2);
            assertThat(userId1.hashCode()).isEqualTo(userId2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 UserId는 동등하지 않다")
        void shouldNotBeEqualForDifferentValue() {
            // given
            UserId userId1 = UserId.generate();
            UserId userId2 = UserId.generate();

            // then
            assertThat(userId1).isNotEqualTo(userId2);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("toString()은 value를 반환한다")
        void shouldReturnValue() {
            // given
            String uuid = UuidV7Generator.generate();
            UserId userId = UserId.of(uuid);

            // when & then
            assertThat(userId.toString()).isEqualTo(uuid);
        }
    }
}

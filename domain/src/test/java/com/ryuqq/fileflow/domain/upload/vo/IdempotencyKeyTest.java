package com.ryuqq.fileflow.domain.upload.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IdempotencyKey Value Object 테스트")
class IdempotencyKeyTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("generate()로 새로운 멱등성 키를 생성할 수 있다")
        void generate() {
            // when
            IdempotencyKey key = IdempotencyKey.generate();

            // then
            assertThat(key).isNotNull();
            assertThat(key.value()).isNotNull();
            assertThat(key.value()).hasSize(36); // UUID 표준 길이
        }

        @Test
        @DisplayName("유효한 UUID 문자열로 IdempotencyKey를 생성할 수 있다")
        void createWithValidUuid() {
            // given
            String validUuid = "550e8400-e29b-41d4-a716-446655440000";

            // when
            IdempotencyKey key = IdempotencyKey.of(validUuid);

            // then
            assertThat(key).isNotNull();
            assertThat(key.value()).isEqualTo(validUuid);
        }

        @Test
        @DisplayName("UUID 객체로부터 IdempotencyKey를 생성할 수 있다")
        void createFromUuid() {
            // given
            UUID uuid = UUID.randomUUID();

            // when
            IdempotencyKey key = IdempotencyKey.from(uuid);

            // then
            assertThat(key).isNotNull();
            assertThat(key.value()).isEqualTo(uuid.toString());
        }

        @Test
        @DisplayName("IdempotencyKey를 UUID 객체로 변환할 수 있다")
        void toUuid() {
            // given
            UUID originalUuid = UUID.randomUUID();
            IdempotencyKey key = IdempotencyKey.from(originalUuid);

            // when
            UUID convertedUuid = key.toUuid();

            // then
            assertThat(convertedUuid).isEqualTo(originalUuid);
        }
    }

    @Nested
    @DisplayName("검증 실패 테스트")
    class ValidationTest {

        @Test
        @DisplayName("null 값으로 IdempotencyKey를 생성할 수 없다")
        void createWithNullValue() {
            assertThatThrownBy(() -> IdempotencyKey.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("IdempotencyKey value cannot be null or empty");
        }

        @Test
        @DisplayName("빈 문자열로 IdempotencyKey를 생성할 수 없다")
        void createWithEmptyValue() {
            assertThatThrownBy(() -> IdempotencyKey.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("IdempotencyKey value cannot be null or empty");
        }

        @Test
        @DisplayName("공백만 있는 문자열로 IdempotencyKey를 생성할 수 없다")
        void createWithBlankValue() {
            assertThatThrownBy(() -> IdempotencyKey.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("IdempotencyKey value cannot be null or empty");
        }

        @Test
        @DisplayName("잘못된 UUID 형식으로 IdempotencyKey를 생성할 수 없다")
        void createWithInvalidUuidFormat() {
            // given
            String invalidUuid = "invalid-uuid-format-string-x";

            // when & then
            assertThatThrownBy(() -> IdempotencyKey.of(invalidUuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageMatching(".*36 characters.*|.*valid UUID format.*");
        }

        @Test
        @DisplayName("UUID 길이가 36자가 아니면 IdempotencyKey를 생성할 수 없다")
        void createWithInvalidLength() {
            // given
            String shortString = "550e8400-e29b-41d4";

            // when & then
            assertThatThrownBy(() -> IdempotencyKey.of(shortString))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("36 characters");
        }

        @Test
        @DisplayName("null UUID 객체로 IdempotencyKey를 생성할 수 없다")
        void createFromNullUuid() {
            assertThatThrownBy(() -> IdempotencyKey.from(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UUID cannot be null");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("동일한 UUID 값을 가진 IdempotencyKey는 같다")
        void equalKeys() {
            // given
            String uuidString = "550e8400-e29b-41d4-a716-446655440000";
            IdempotencyKey key1 = IdempotencyKey.of(uuidString);
            IdempotencyKey key2 = IdempotencyKey.of(uuidString);

            // when & then
            assertThat(key1).isEqualTo(key2);
            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        }

        @Test
        @DisplayName("다른 UUID 값을 가진 IdempotencyKey는 다르다")
        void notEqualKeys() {
            // given
            IdempotencyKey key1 = IdempotencyKey.generate();
            IdempotencyKey key2 = IdempotencyKey.generate();

            // when & then
            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("UUID 객체로 생성한 키와 문자열로 생성한 키가 같은 값이면 같다")
        void equalFromDifferentFactories() {
            // given
            UUID uuid = UUID.randomUUID();
            IdempotencyKey key1 = IdempotencyKey.from(uuid);
            IdempotencyKey key2 = IdempotencyKey.of(uuid.toString());

            // when & then
            assertThat(key1).isEqualTo(key2);
            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        }
    }

    @Nested
    @DisplayName("유일성 테스트")
    class UniquenessTest {

        @Test
        @DisplayName("generate()로 생성된 키들은 모두 유일하다")
        void generateUniqueKeys() {
            // given
            Set<String> generatedKeys = new HashSet<>();
            int numberOfKeys = 1000;

            // when
            for (int i = 0; i < numberOfKeys; i++) {
                IdempotencyKey key = IdempotencyKey.generate();
                generatedKeys.add(key.value());
            }

            // then
            assertThat(generatedKeys).hasSize(numberOfKeys);
        }

        @Test
        @DisplayName("연속으로 생성된 키들도 유일하다")
        void consecutiveKeysAreUnique() {
            // when
            IdempotencyKey key1 = IdempotencyKey.generate();
            IdempotencyKey key2 = IdempotencyKey.generate();
            IdempotencyKey key3 = IdempotencyKey.generate();

            // then
            assertThat(key1.value())
                    .isNotEqualTo(key2.value())
                    .isNotEqualTo(key3.value());
            assertThat(key2.value()).isNotEqualTo(key3.value());
        }
    }

    @Nested
    @DisplayName("UUID 형식 테스트")
    class UuidFormatTest {

        @Test
        @DisplayName("생성된 키는 표준 UUID 형식을 따른다 (8-4-4-4-12)")
        void standardUuidFormat() {
            // when
            IdempotencyKey key = IdempotencyKey.generate();

            // then
            assertThat(key.value()).matches(
                    "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
            );
        }

        @Test
        @DisplayName("UUID 버전 4(랜덤) 형식을 따른다")
        void uuidVersion4() {
            // when
            IdempotencyKey key = IdempotencyKey.generate();
            UUID uuid = key.toUuid();

            // then
            assertThat(uuid.version()).isEqualTo(4); // UUID v4는 랜덤 생성
        }

        @Test
        @DisplayName("대문자 UUID도 허용된다")
        void acceptsUppercaseUuid() {
            // given
            String uppercaseUuid = "550E8400-E29B-41D4-A716-446655440000";

            // when
            IdempotencyKey key = IdempotencyKey.of(uppercaseUuid);

            // then
            assertThat(key).isNotNull();
            assertThat(key.value()).isEqualTo(uppercaseUuid);
        }

        @Test
        @DisplayName("대소문자 혼합 UUID도 허용된다")
        void acceptsMixedCaseUuid() {
            // given
            String mixedCaseUuid = "550e8400-E29B-41d4-A716-446655440000";

            // when
            IdempotencyKey key = IdempotencyKey.of(mixedCaseUuid);

            // then
            assertThat(key).isNotNull();
            assertThat(key.value()).isEqualTo(mixedCaseUuid);
        }
    }
}

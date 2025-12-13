package com.ryuqq.fileflow.domain.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UuidV7Generator 테스트")
class UuidV7GeneratorTest {

    @Nested
    @DisplayName("generate() 메서드")
    class GenerateTest {

        @Test
        @DisplayName("UUIDv7 형식의 문자열을 생성한다")
        void shouldGenerateValidUuidV7Format() {
            // when
            String uuid = UuidV7Generator.generate();

            // then
            assertThat(uuid).hasSize(36);
            assertThat(uuid)
                    .matches("[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("생성된 UUID는 버전 7이다")
        void shouldHaveVersion7() {
            // when
            String uuid = UuidV7Generator.generate();

            // then
            char versionChar = uuid.charAt(14); // xxxxxxxx-xxxx-Vxxx-xxxx-xxxxxxxxxxxx
            assertThat(versionChar).isEqualTo('7');
        }

        @Test
        @DisplayName("연속 생성된 UUID는 시간순 정렬이 가능하다")
        void shouldBeSortableByTime() throws InterruptedException {
            // given
            String uuid1 = UuidV7Generator.generate();
            Thread.sleep(2); // 밀리초 단위 차이 보장
            String uuid2 = UuidV7Generator.generate();

            // then
            assertThat(uuid1.compareTo(uuid2)).isLessThan(0);
        }

        @Test
        @DisplayName("다수의 UUID 생성 시 중복이 없다")
        void shouldGenerateUniqueUuids() {
            // given
            Set<String> uuids = new HashSet<>();
            int count = 10000;

            // when
            for (int i = 0; i < count; i++) {
                uuids.add(UuidV7Generator.generate());
            }

            // then
            assertThat(uuids).hasSize(count);
        }

        @Test
        @DisplayName("지정된 시간 기반으로 UUID를 생성한다")
        void shouldGenerateUuidWithSpecificTimestamp() {
            // given
            Instant timestamp = Instant.parse("2025-01-01T00:00:00Z");

            // when
            String uuid = UuidV7Generator.generate(timestamp);

            // then
            assertThat(UuidV7Generator.isValid(uuid)).isTrue();

            Instant extracted = UuidV7Generator.extractTimestamp(uuid);
            assertThat(extracted).isEqualTo(timestamp);
        }
    }

    @Nested
    @DisplayName("isValid() 메서드")
    class IsValidTest {

        @Test
        @DisplayName("유효한 UUIDv7은 true를 반환한다")
        void shouldReturnTrueForValidUuidV7() {
            // given
            String validUuid = UuidV7Generator.generate();

            // when & then
            assertThat(UuidV7Generator.isValid(validUuid)).isTrue();
        }

        @Test
        @DisplayName("null은 false를 반환한다")
        void shouldReturnFalseForNull() {
            assertThat(UuidV7Generator.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("빈 문자열은 false를 반환한다")
        void shouldReturnFalseForEmptyString() {
            assertThat(UuidV7Generator.isValid("")).isFalse();
        }

        @Test
        @DisplayName("UUIDv4는 false를 반환한다")
        void shouldReturnFalseForUuidV4() {
            // given - UUIDv4 (버전 4)
            String uuidV4 = "550e8400-e29b-41d4-a716-446655440000";

            // when & then
            assertThat(UuidV7Generator.isValid(uuidV4)).isFalse();
        }

        @Test
        @DisplayName("잘못된 형식은 false를 반환한다")
        void shouldReturnFalseForInvalidFormat() {
            assertThat(UuidV7Generator.isValid("not-a-uuid")).isFalse();
            assertThat(UuidV7Generator.isValid("12345678-1234-7234-8234-123456789012extra"))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("validate() 메서드")
    class ValidateTest {

        @Test
        @DisplayName("유효한 UUIDv7은 예외를 던지지 않는다")
        void shouldNotThrowForValidUuidV7() {
            // given
            String validUuid = UuidV7Generator.generate();

            // when & then
            UuidV7Generator.validate(validUuid, "testField");
            // 예외 없이 통과
        }

        @Test
        @DisplayName("null은 IllegalArgumentException을 던진다")
        void shouldThrowForNull() {
            assertThatThrownBy(() -> UuidV7Generator.validate(null, "testField"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("testField")
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("빈 문자열은 IllegalArgumentException을 던진다")
        void shouldThrowForEmptyString() {
            assertThatThrownBy(() -> UuidV7Generator.validate("", "testField"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("testField");
        }

        @Test
        @DisplayName("잘못된 형식은 IllegalArgumentException을 던진다")
        void shouldThrowForInvalidFormat() {
            assertThatThrownBy(() -> UuidV7Generator.validate("invalid-uuid", "testField"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("testField")
                    .hasMessageContaining("UUIDv7");
        }
    }

    @Nested
    @DisplayName("extractTimestamp() 메서드")
    class ExtractTimestampTest {

        @Test
        @DisplayName("UUIDv7에서 생성 시간을 추출한다")
        void shouldExtractTimestamp() {
            // given
            Instant beforeGeneration = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            String uuid = UuidV7Generator.generate();
            Instant afterGeneration = Instant.now().truncatedTo(ChronoUnit.MILLIS);

            // when
            Instant extracted = UuidV7Generator.extractTimestamp(uuid);

            // then
            assertThat(extracted).isAfterOrEqualTo(beforeGeneration);
            assertThat(extracted).isBeforeOrEqualTo(afterGeneration.plusMillis(1));
        }

        @Test
        @DisplayName("잘못된 UUIDv7은 예외를 던진다")
        void shouldThrowForInvalidUuid() {
            assertThatThrownBy(() -> UuidV7Generator.extractTimestamp("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}

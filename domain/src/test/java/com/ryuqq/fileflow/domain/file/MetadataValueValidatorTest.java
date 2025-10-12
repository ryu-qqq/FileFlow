package com.ryuqq.fileflow.domain.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MetadataValueValidator Domain Utility 테스트
 *
 * 목표: 38% → 70%+ 커버리지 달성
 *
 * 테스트 시나리오:
 * - isNumeric() edge cases
 * - isBoolean() edge cases
 * - isJson() edge cases
 * - Utility class 인스턴스화 방지 테스트
 *
 * @author sangwon-ryu
 */
@DisplayName("MetadataValueValidator Domain Utility 테스트")
class MetadataValueValidatorTest {

    @Nested
    @DisplayName("isNumeric() 테스트")
    class IsNumericTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "0",
                "1",
                "123",
                "456.78",
                "-123",
                "-456.78",
                "0.0",
                "1e10",
                "1.23E-4",
                "9999999999",
                "0.000001"
        })
        @DisplayName("유효한 숫자 문자열에 대해 true를 반환한다")
        void returnsTrue_forValidNumbers(String value) {
            // when & then
            assertThat(MetadataValueValidator.isNumeric(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "not a number",
                "12.34.56",
                "123abc",
                "abc123",
                "12 34",
                ".",
                "e10",
                "1e",
                ""
        })
        @DisplayName("잘못된 숫자 문자열에 대해 false를 반환한다")
        void returnsFalse_forInvalidNumbers(String value) {
            // when & then
            assertThat(MetadataValueValidator.isNumeric(value)).isFalse();
        }

        @Test
        @DisplayName("NaN, Infinity는 Double.parseDouble에서 유효하므로 true를 반환한다")
        void returnsTrue_forSpecialDoubleValues() {
            // when & then - Java의 Double.parseDouble은 이 값들을 유효하게 처리함
            assertThat(MetadataValueValidator.isNumeric("NaN")).isTrue();
            assertThat(MetadataValueValidator.isNumeric("Infinity")).isTrue();
            assertThat(MetadataValueValidator.isNumeric("-Infinity")).isTrue();
        }

        @Test
        @DisplayName("양수 부호가 있는 숫자는 유효하다")
        void returnsTrue_forPositiveSignNumbers() {
            // when & then
            assertThat(MetadataValueValidator.isNumeric("+123")).isTrue();
            assertThat(MetadataValueValidator.isNumeric("+45.67")).isTrue();
        }

        @Test
        @DisplayName("null에 대해 NullPointerException이 발생한다")
        void throwsException_forNull() {
            // when & then - Double.parseDouble(null)은 NullPointerException을 던짐
            assertThatThrownBy(() -> MetadataValueValidator.isNumeric(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("매우 큰 숫자도 처리할 수 있다")
        void handlesVeryLargeNumbers() {
            // given
            String largeNumber = "999999999999999999999999.999999999999999";

            // when & then
            assertThat(MetadataValueValidator.isNumeric(largeNumber)).isTrue();
        }

        @Test
        @DisplayName("매우 작은 숫자도 처리할 수 있다")
        void handlesVerySmallNumbers() {
            // given
            String smallNumber = "0.000000000000000000000001";

            // when & then
            assertThat(MetadataValueValidator.isNumeric(smallNumber)).isTrue();
        }

        @Test
        @DisplayName("과학적 표기법을 지원한다")
        void supportsScientificNotation() {
            // when & then
            assertThat(MetadataValueValidator.isNumeric("1.23e10")).isTrue();
            assertThat(MetadataValueValidator.isNumeric("4.56E-7")).isTrue();
            assertThat(MetadataValueValidator.isNumeric("7.89E+3")).isTrue();
        }
    }

    @Nested
    @DisplayName("isBoolean() 테스트")
    class IsBooleanTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "true",
                "false",
                "TRUE",
                "FALSE",
                "True",
                "False",
                "TrUe",
                "FaLsE",
                "tRuE",
                "fAlSe"
        })
        @DisplayName("'true' 또는 'false' 문자열(대소문자 무시)에 대해 true를 반환한다")
        void returnsTrue_forTrueOrFalse(String value) {
            // when & then
            assertThat(MetadataValueValidator.isBoolean(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "yes",
                "no",
                "1",
                "0",
                "T",
                "F",
                "t",
                "f",
                "truee",
                "falsee",
                " true",
                "false ",
                " false ",
                "true\n",
                "not boolean"
        })
        @DisplayName("'true'/'false'가 아닌 문자열에 대해 false를 반환한다")
        void returnsFalse_forNonBooleanStrings(String value) {
            // when & then
            assertThat(MetadataValueValidator.isBoolean(value)).isFalse();
        }

        @Test
        @DisplayName("null에 대해 NullPointerException이 발생한다")
        void throwsException_forNull() {
            // when & then
            assertThatThrownBy(() -> MetadataValueValidator.isBoolean(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("빈 문자열에 대해 false를 반환한다")
        void returnsFalse_forEmptyString() {
            // when & then
            assertThat(MetadataValueValidator.isBoolean("")).isFalse();
        }

        @Test
        @DisplayName("공백만 있는 문자열에 대해 false를 반환한다")
        void returnsFalse_forWhitespace() {
            // when & then
            assertThat(MetadataValueValidator.isBoolean("   ")).isFalse();
        }
    }

    @Nested
    @DisplayName("isJson() 테스트")
    class IsJsonTest {

        @Nested
        @DisplayName("유효한 JSON 객체")
        class ValidJsonObjects {

            @ParameterizedTest
            @ValueSource(strings = {
                    "{}",
                    "{\"key\": \"value\"}",
                    "{\"name\":\"test\",\"age\":30}",
                    "{\"nested\": {\"key\": \"value\"}}",
                    "{\"array\": [1, 2, 3]}",
                    "{\"bool\": true, \"null\": null}",
                    "{ \"key\" : \"value\" }"  // 공백 포함
            })
            @DisplayName("유효한 JSON 객체에 대해 true를 반환한다")
            void returnsTrue_forValidJsonObjects(String value) {
                // when & then
                assertThat(MetadataValueValidator.isJson(value)).isTrue();
            }
        }

        @Nested
        @DisplayName("유효한 JSON 배열")
        class ValidJsonArrays {

            @ParameterizedTest
            @ValueSource(strings = {
                    "[]",
                    "[1, 2, 3]",
                    "[\"item1\", \"item2\"]",
                    "[{\"id\": 1}, {\"id\": 2}]",
                    "[true, false, null]",
                    "[ 1 , 2 , 3 ]"  // 공백 포함
            })
            @DisplayName("유효한 JSON 배열에 대해 true를 반환한다")
            void returnsTrue_forValidJsonArrays(String value) {
                // when & then
                assertThat(MetadataValueValidator.isJson(value)).isTrue();
            }
        }

        @Nested
        @DisplayName("Escape 처리")
        class EscapeHandling {

            @Test
            @DisplayName("Escaped 따옴표가 있는 JSON은 유효하다")
            void returnsTrue_forEscapedQuotes() {
                // given
                String jsonWithEscapedQuotes = "{\"key\": \"value with \\\"quotes\\\"\"}";

                // when & then
                assertThat(MetadataValueValidator.isJson(jsonWithEscapedQuotes)).isTrue();
            }

            @Test
            @DisplayName("Escaped 백슬래시가 있는 JSON은 유효하다")
            void returnsTrue_forEscapedBackslash() {
                // given
                String jsonWithEscapedBackslash = "{\"path\": \"C:\\\\Users\\\\test\"}";

                // when & then
                assertThat(MetadataValueValidator.isJson(jsonWithEscapedBackslash)).isTrue();
            }

            @Test
            @DisplayName("Escaped 개행 문자가 있는 JSON은 유효하다")
            void returnsTrue_forEscapedNewline() {
                // given
                String jsonWithEscapedNewline = "{\"text\": \"line1\\nline2\"}";

                // when & then
                assertThat(MetadataValueValidator.isJson(jsonWithEscapedNewline)).isTrue();
            }
        }

        @Nested
        @DisplayName("잘못된 JSON")
        class InvalidJson {

            @ParameterizedTest
            @ValueSource(strings = {
                    "{",
                    "}",
                    "[",
                    "]",
                    "{invalid json",
                    "not json at all",
                    "123",
                    "\"just a string\"",  // JSON string은 지원하지 않음
                    "null",
                    "true",
                    "{\"key\": \"unmatched quote}",  // 홀수개의 따옴표
                    "",
                    "   ",
                    "}{",
                    "][",
                    "{]",
                    "[}"
            })
            @DisplayName("잘못된 JSON에 대해 false를 반환한다")
            void returnsFalse_forInvalidJson(String value) {
                // when & then
                assertThat(MetadataValueValidator.isJson(value)).isFalse();
            }

            @Test
            @DisplayName("키에 따옴표가 없는 JSON은 짝수 따옴표를 가질 수 있어 true로 처리될 수 있다")
            void jsonWithoutQuotedKeysHasEvenQuotes() {
                // {key: "value"} has 2 quotes (even), so it passes quote validation
                // {"key" "value"} has 4 quotes (even), so it also passes quote validation
                // Note: This is a limitation of basic JSON validation in domain layer
                assertThat(MetadataValueValidator.isJson("{key: \"value\"}")).isTrue();
                assertThat(MetadataValueValidator.isJson("{\"key\" \"value\"}")).isTrue();
            }
        }

        @Nested
        @DisplayName("Edge Cases")
        class EdgeCases {

            @Test
            @DisplayName("null에 대해 NullPointerException이 발생한다")
            void throwsException_forNull() {
                // when & then
                assertThatThrownBy(() -> MetadataValueValidator.isJson(null))
                        .isInstanceOf(NullPointerException.class);
            }

            @Test
            @DisplayName("빈 객체는 유효한 JSON이다")
            void returnsTrue_forEmptyObject() {
                // when & then
                assertThat(MetadataValueValidator.isJson("{}")).isTrue();
            }

            @Test
            @DisplayName("빈 배열은 유효한 JSON이다")
            void returnsTrue_forEmptyArray() {
                // when & then
                assertThat(MetadataValueValidator.isJson("[]")).isTrue();
            }

            @Test
            @DisplayName("앞뒤 공백이 있는 JSON은 유효하다")
            void returnsTrue_forJsonWithWhitespace() {
                // when & then
                assertThat(MetadataValueValidator.isJson("  {}  ")).isTrue();
                assertThat(MetadataValueValidator.isJson("\n[]\n")).isTrue();
                assertThat(MetadataValueValidator.isJson("\t{\"key\": \"value\"}\t")).isTrue();
            }

            @Test
            @DisplayName("중첩된 JSON 구조를 처리할 수 있다")
            void handlesNestedJson() {
                // given
                String nestedJson = """
                    {
                        "level1": {
                            "level2": {
                                "level3": {
                                    "key": "value"
                                }
                            }
                        }
                    }
                    """;

                // when & then
                assertThat(MetadataValueValidator.isJson(nestedJson)).isTrue();
            }

            @Test
            @DisplayName("복잡한 JSON 배열을 처리할 수 있다")
            void handlesComplexJsonArray() {
                // given
                String complexArray = """
                    [
                        {"id": 1, "name": "Item 1"},
                        {"id": 2, "name": "Item 2"},
                        {"id": 3, "name": "Item 3"}
                    ]
                    """;

                // when & then
                assertThat(MetadataValueValidator.isJson(complexArray)).isTrue();
            }
        }

        @Nested
        @DisplayName("따옴표 검증 로직")
        class QuoteValidation {

            @Test
            @DisplayName("짝수개의 따옴표가 있으면 유효하다")
            void validWithEvenNumberOfQuotes() {
                // when & then
                assertThat(MetadataValueValidator.isJson("{\"key\": \"value\"}")).isTrue();  // 4개
                assertThat(MetadataValueValidator.isJson("{}")).isTrue();  // 0개
            }

            @Test
            @DisplayName("홀수개의 따옴표가 있으면 무효하다")
            void invalidWithOddNumberOfQuotes() {
                // when & then
                assertThat(MetadataValueValidator.isJson("{\"key: \"value\"}")).isFalse();  // 3개
                assertThat(MetadataValueValidator.isJson("{\"key\": \"value}")).isFalse();  // 3개
            }

            @Test
            @DisplayName("Escaped 따옴표는 카운트하지 않는다")
            void doesNotCountEscapedQuotes() {
                // given
                String jsonWithEscapedQuotes = "{\"key\": \"value with \\\"quotes\\\"\"}";
                // 실제 따옴표: " key " value with \" quotes \" " = 4개 (escaped는 제외)

                // when & then
                assertThat(MetadataValueValidator.isJson(jsonWithEscapedQuotes)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Utility Class 특성 테스트")
    class UtilityClassTest {

        @Test
        @DisplayName("private 생성자로 인스턴스화를 방지한다")
        void preventInstantiation() throws NoSuchMethodException {
            // given
            Constructor<MetadataValueValidator> constructor =
                    MetadataValueValidator.class.getDeclaredConstructor();

            // when & then
            assertThat(constructor.canAccess(null)).isFalse();

            constructor.setAccessible(true);
            assertThatThrownBy(constructor::newInstance)
                    .isInstanceOf(InvocationTargetException.class)
                    .hasCauseInstanceOf(AssertionError.class)
                    .cause()
                    .hasMessageContaining("Cannot instantiate utility class");
        }

        @Test
        @DisplayName("모든 메서드는 static이다")
        void allMethodsAreStatic() throws NoSuchMethodException {
            // when & then
            assertThat(MetadataValueValidator.class.getMethod("isNumeric", String.class))
                    .matches(method -> java.lang.reflect.Modifier.isStatic(method.getModifiers()));
            assertThat(MetadataValueValidator.class.getMethod("isBoolean", String.class))
                    .matches(method -> java.lang.reflect.Modifier.isStatic(method.getModifiers()));
            assertThat(MetadataValueValidator.class.getMethod("isJson", String.class))
                    .matches(method -> java.lang.reflect.Modifier.isStatic(method.getModifiers()));
        }

        @Test
        @DisplayName("클래스는 final이다")
        void classIsFinal() {
            // when & then
            assertThat(MetadataValueValidator.class)
                    .matches(clazz -> java.lang.reflect.Modifier.isFinal(clazz.getModifiers()));
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class RealWorldScenarioTest {

        @Test
        @DisplayName("이미지 메타데이터 검증 - 숫자 타입")
        void validateImageNumericMetadata() {
            // given
            String width = "1920";
            String height = "1080";
            String fileSize = "2048576";

            // when & then
            assertThat(MetadataValueValidator.isNumeric(width)).isTrue();
            assertThat(MetadataValueValidator.isNumeric(height)).isTrue();
            assertThat(MetadataValueValidator.isNumeric(fileSize)).isTrue();
        }

        @Test
        @DisplayName("이미지 메타데이터 검증 - 불리언 타입")
        void validateImageBooleanMetadata() {
            // given
            String hasAlpha = "true";
            String isProgressive = "false";

            // when & then
            assertThat(MetadataValueValidator.isBoolean(hasAlpha)).isTrue();
            assertThat(MetadataValueValidator.isBoolean(isProgressive)).isTrue();
        }

        @Test
        @DisplayName("EXIF 데이터 검증 - JSON 타입")
        void validateExifJsonMetadata() {
            // given
            String exifData = "{\"camera\": \"Canon EOS\", \"iso\": 400, \"exposure\": \"1/500\"}";

            // when & then
            assertThat(MetadataValueValidator.isJson(exifData)).isTrue();
        }

        @Test
        @DisplayName("비디오 메타데이터 검증 - 실수 타입")
        void validateVideoFloatMetadata() {
            // given
            String duration = "120.5";
            String frameRate = "29.97";

            // when & then
            assertThat(MetadataValueValidator.isNumeric(duration)).isTrue();
            assertThat(MetadataValueValidator.isNumeric(frameRate)).isTrue();
        }

        @Test
        @DisplayName("문서 메타데이터 검증 - 복합 타입")
        void validateDocumentMetadata() {
            // given
            String pageCount = "42";
            String isEncrypted = "false";
            String metadata = "{\"author\": \"John Doe\", \"title\": \"Test Document\"}";

            // when & then
            assertThat(MetadataValueValidator.isNumeric(pageCount)).isTrue();
            assertThat(MetadataValueValidator.isBoolean(isEncrypted)).isTrue();
            assertThat(MetadataValueValidator.isJson(metadata)).isTrue();
        }
    }
}

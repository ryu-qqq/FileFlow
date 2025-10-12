package com.ryuqq.fileflow.domain.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MetadataType Domain Enum 테스트
 *
 * 목표: 38% → 70%+ 커버리지 달성
 *
 * 테스트 시나리오:
 * - 각 타입별 isCompatible() 검증
 * - getTypeName() 메서드 검증
 * - Edge cases (null, empty, whitespace)
 * - 다양한 타입 조합
 *
 * @author sangwon-ryu
 */
@DisplayName("MetadataType Domain Enum 테스트")
class MetadataTypeTest {

    @Nested
    @DisplayName("STRING 타입 테스트")
    class StringTypeTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "simple text",
                "JPEG",
                "author name",
                "",  // 빈 문자열도 STRING 타입과 호환
                "   ",  // 공백만 있는 문자열
                "123",  // 숫자처럼 보이는 문자열
                "true",  // 불리언처럼 보이는 문자열
                "{invalid json",  // 잘못된 JSON 형식
        })
        @DisplayName("모든 문자열 값은 STRING 타입과 호환된다")
        void stringTypeIsCompatibleWithAllStrings(String value) {
            // when & then
            assertThat(MetadataType.STRING.isCompatible(value)).isTrue();
        }

        @Test
        @DisplayName("null은 STRING 타입과 호환되지 않는다")
        void stringTypeIsNotCompatibleWithNull() {
            // when & then
            assertThat(MetadataType.STRING.isCompatible(null)).isFalse();
        }

        @Test
        @DisplayName("getTypeName()은 'string'을 반환한다")
        void getTypeNameReturnsString() {
            // when & then
            assertThat(MetadataType.STRING.getTypeName()).isEqualTo("string");
        }
    }

    @Nested
    @DisplayName("NUMBER 타입 테스트")
    class NumberTypeTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "123",
                "456.78",
                "0",
                "-123",
                "-456.78",
                "0.0",
                "1e10",
                "1.23E-4"
        })
        @DisplayName("유효한 숫자 문자열은 NUMBER 타입과 호환된다")
        void numberTypeIsCompatibleWithValidNumbers(String value) {
            // when & then
            assertThat(MetadataType.NUMBER.isCompatible(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "not a number",
                "12.34.56",
                "123abc",
                "",  // 빈 문자열
                "   ",  // 공백만 있는 문자열
                "true"
        })
        @DisplayName("잘못된 숫자 문자열은 NUMBER 타입과 호환되지 않는다")
        void numberTypeIsNotCompatibleWithInvalidNumbers(String value) {
            // when & then
            assertThat(MetadataType.NUMBER.isCompatible(value)).isFalse();
        }

        @Test
        @DisplayName("NaN과 Infinity는 Double.parseDouble에서 유효하므로 NUMBER 타입과 호환된다")
        void numberTypeIsCompatibleWithSpecialDoubleValues() {
            // when & then - Java의 Double.parseDouble은 이 값들을 유효하게 처리함
            assertThat(MetadataType.NUMBER.isCompatible("NaN")).isTrue();
            assertThat(MetadataType.NUMBER.isCompatible("Infinity")).isTrue();
            assertThat(MetadataType.NUMBER.isCompatible("-Infinity")).isTrue();
        }

        @Test
        @DisplayName("null은 NUMBER 타입과 호환되지 않는다")
        void numberTypeIsNotCompatibleWithNull() {
            // when & then
            assertThat(MetadataType.NUMBER.isCompatible(null)).isFalse();
        }

        @Test
        @DisplayName("getTypeName()은 'number'를 반환한다")
        void getTypeNameReturnsNumber() {
            // when & then
            assertThat(MetadataType.NUMBER.getTypeName()).isEqualTo("number");
        }
    }

    @Nested
    @DisplayName("BOOLEAN 타입 테스트")
    class BooleanTypeTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "true",
                "false",
                "TRUE",
                "FALSE",
                "True",
                "False",
                "TrUe",
                "FaLsE"
        })
        @DisplayName("'true' 또는 'false' 문자열은 BOOLEAN 타입과 호환된다")
        void booleanTypeIsCompatibleWithTrueOrFalse(String value) {
            // when & then
            assertThat(MetadataType.BOOLEAN.isCompatible(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "yes",
                "no",
                "1",
                "0",
                "T",
                "F",
                "truee",
                "falsee",
                "",  // 빈 문자열
                "   ",  // 공백만 있는 문자열
                "not boolean"
        })
        @DisplayName("'true'/'false'가 아닌 문자열은 BOOLEAN 타입과 호환되지 않는다")
        void booleanTypeIsNotCompatibleWithOtherStrings(String value) {
            // when & then
            assertThat(MetadataType.BOOLEAN.isCompatible(value)).isFalse();
        }

        @Test
        @DisplayName("null은 BOOLEAN 타입과 호환되지 않는다")
        void booleanTypeIsNotCompatibleWithNull() {
            // when & then
            assertThat(MetadataType.BOOLEAN.isCompatible(null)).isFalse();
        }

        @Test
        @DisplayName("getTypeName()은 'boolean'을 반환한다")
        void getTypeNameReturnsBoolean() {
            // when & then
            assertThat(MetadataType.BOOLEAN.getTypeName()).isEqualTo("boolean");
        }
    }

    @Nested
    @DisplayName("JSON 타입 테스트")
    class JsonTypeTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "{}",
                "[]",
                "{\"key\": \"value\"}",
                "[1, 2, 3]",
                "{\"nested\": {\"key\": \"value\"}}",
                "[{\"id\": 1}, {\"id\": 2}]",
                "{\"name\":\"test\",\"age\":30}",
                "{\"key\": \"value with \\\"quotes\\\"\"}",  // escaped quotes
                "[\"item1\", \"item2\"]"
        })
        @DisplayName("유효한 JSON 형식은 JSON 타입과 호환된다")
        void jsonTypeIsCompatibleWithValidJson(String value) {
            // when & then
            assertThat(MetadataType.JSON.isCompatible(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "{",
                "}",
                "[",
                "]",
                "{invalid json",
                "not json at all",
                "123",
                "\"just a string\"",  // JSON string은 지원하지 않음 (객체나 배열만)
                "",  // 빈 문자열
                "   ",  // 공백만 있는 문자열
                "{\"key\": \"unmatched quote}",  // 홀수개의 따옴표
                "{\"key: \"value\"}",  // 따옴표 누락
        })
        @DisplayName("잘못된 JSON 형식은 JSON 타입과 호환되지 않는다")
        void jsonTypeIsNotCompatibleWithInvalidJson(String value) {
            // when & then
            assertThat(MetadataType.JSON.isCompatible(value)).isFalse();
        }

        @Test
        @DisplayName("null은 JSON 타입과 호환되지 않는다")
        void jsonTypeIsNotCompatibleWithNull() {
            // when & then
            assertThat(MetadataType.JSON.isCompatible(null)).isFalse();
        }

        @Test
        @DisplayName("getTypeName()은 'json'을 반환한다")
        void getTypeNameReturnsJson() {
            // when & then
            assertThat(MetadataType.JSON.getTypeName()).isEqualTo("json");
        }
    }

    @Nested
    @DisplayName("타입 간 호환성 테스트")
    class TypeCompatibilityTest {

        @Test
        @DisplayName("숫자 문자열은 STRING과는 호환되지만 BOOLEAN과는 호환되지 않는다")
        void numericStringCompatibility() {
            // given
            String value = "123";

            // when & then
            assertThat(MetadataType.STRING.isCompatible(value)).isTrue();
            assertThat(MetadataType.NUMBER.isCompatible(value)).isTrue();
            assertThat(MetadataType.BOOLEAN.isCompatible(value)).isFalse();
            assertThat(MetadataType.JSON.isCompatible(value)).isFalse();
        }

        @Test
        @DisplayName("'true' 문자열은 STRING, BOOLEAN과는 호환되지만 NUMBER와는 호환되지 않는다")
        void trueStringCompatibility() {
            // given
            String value = "true";

            // when & then
            assertThat(MetadataType.STRING.isCompatible(value)).isTrue();
            assertThat(MetadataType.NUMBER.isCompatible(value)).isFalse();
            assertThat(MetadataType.BOOLEAN.isCompatible(value)).isTrue();
            assertThat(MetadataType.JSON.isCompatible(value)).isFalse();
        }

        @Test
        @DisplayName("JSON 객체는 STRING과는 호환되지만 NUMBER, BOOLEAN과는 호환되지 않는다")
        void jsonObjectCompatibility() {
            // given
            String value = "{\"key\": \"value\"}";

            // when & then
            assertThat(MetadataType.STRING.isCompatible(value)).isTrue();
            assertThat(MetadataType.NUMBER.isCompatible(value)).isFalse();
            assertThat(MetadataType.BOOLEAN.isCompatible(value)).isFalse();
            assertThat(MetadataType.JSON.isCompatible(value)).isTrue();
        }

        @Test
        @DisplayName("빈 문자열은 STRING과만 호환된다")
        void emptyStringCompatibility() {
            // given
            String value = "";

            // when & then
            assertThat(MetadataType.STRING.isCompatible(value)).isTrue();
            assertThat(MetadataType.NUMBER.isCompatible(value)).isFalse();
            assertThat(MetadataType.BOOLEAN.isCompatible(value)).isFalse();
            assertThat(MetadataType.JSON.isCompatible(value)).isFalse();
        }

        @Test
        @DisplayName("공백 문자열은 STRING과만 호환된다")
        void whitespaceStringCompatibility() {
            // given
            String value = "   ";

            // when & then
            assertThat(MetadataType.STRING.isCompatible(value)).isTrue();
            assertThat(MetadataType.NUMBER.isCompatible(value)).isFalse();
            assertThat(MetadataType.BOOLEAN.isCompatible(value)).isFalse();
            assertThat(MetadataType.JSON.isCompatible(value)).isFalse();
        }
    }

    @Nested
    @DisplayName("Enum 특성 테스트")
    class EnumPropertiesTest {

        @Test
        @DisplayName("모든 MetadataType 값을 조회할 수 있다")
        void getAllValues() {
            // when
            MetadataType[] values = MetadataType.values();

            // then
            assertThat(values).hasSize(4);
            assertThat(values).containsExactly(
                    MetadataType.STRING,
                    MetadataType.NUMBER,
                    MetadataType.BOOLEAN,
                    MetadataType.JSON
            );
        }

        @Test
        @DisplayName("문자열로부터 MetadataType을 조회할 수 있다")
        void valueOfFromString() {
            // when & then
            assertThat(MetadataType.valueOf("STRING")).isEqualTo(MetadataType.STRING);
            assertThat(MetadataType.valueOf("NUMBER")).isEqualTo(MetadataType.NUMBER);
            assertThat(MetadataType.valueOf("BOOLEAN")).isEqualTo(MetadataType.BOOLEAN);
            assertThat(MetadataType.valueOf("JSON")).isEqualTo(MetadataType.JSON);
        }

        @Test
        @DisplayName("각 타입의 name()은 대문자 형식이다")
        void nameIsUpperCase() {
            // when & then
            assertThat(MetadataType.STRING.name()).isEqualTo("STRING");
            assertThat(MetadataType.NUMBER.name()).isEqualTo("NUMBER");
            assertThat(MetadataType.BOOLEAN.name()).isEqualTo("BOOLEAN");
            assertThat(MetadataType.JSON.name()).isEqualTo("JSON");
        }

        @Test
        @DisplayName("각 타입의 getTypeName()은 소문자 형식이다")
        void getTypeNameIsLowerCase() {
            // when & then
            assertThat(MetadataType.STRING.getTypeName()).isEqualTo("string");
            assertThat(MetadataType.NUMBER.getTypeName()).isEqualTo("number");
            assertThat(MetadataType.BOOLEAN.getTypeName()).isEqualTo("boolean");
            assertThat(MetadataType.JSON.getTypeName()).isEqualTo("json");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class RealWorldScenarioTest {

        @Test
        @DisplayName("이미지 메타데이터 - width와 height는 NUMBER 타입")
        void imageMetadata() {
            // when & then
            assertThat(MetadataType.NUMBER.isCompatible("1920")).isTrue();
            assertThat(MetadataType.NUMBER.isCompatible("1080")).isTrue();
        }

        @Test
        @DisplayName("이미지 메타데이터 - format은 STRING 타입")
        void imageFormatMetadata() {
            // when & then
            assertThat(MetadataType.STRING.isCompatible("JPEG")).isTrue();
            assertThat(MetadataType.STRING.isCompatible("PNG")).isTrue();
        }

        @Test
        @DisplayName("이미지 메타데이터 - has_alpha는 BOOLEAN 타입")
        void imageHasAlphaMetadata() {
            // when & then
            assertThat(MetadataType.BOOLEAN.isCompatible("true")).isTrue();
            assertThat(MetadataType.BOOLEAN.isCompatible("false")).isTrue();
        }

        @Test
        @DisplayName("비디오 메타데이터 - duration은 NUMBER 타입")
        void videoDurationMetadata() {
            // when & then
            assertThat(MetadataType.NUMBER.isCompatible("120.5")).isTrue();
        }

        @Test
        @DisplayName("문서 메타데이터 - page_count는 NUMBER 타입")
        void documentPageCountMetadata() {
            // when & then
            assertThat(MetadataType.NUMBER.isCompatible("42")).isTrue();
        }

        @Test
        @DisplayName("EXIF 데이터는 JSON 타입")
        void exifDataMetadata() {
            // given
            String exifData = "{\"camera\": \"Canon EOS\", \"iso\": 400, \"exposure\": \"1/500\"}";

            // when & then
            assertThat(MetadataType.JSON.isCompatible(exifData)).isTrue();
        }

        @Test
        @DisplayName("위치 정보는 JSON 타입")
        void locationMetadata() {
            // given
            String location = "{\"lat\": 37.5, \"lng\": 127.0}";

            // when & then
            assertThat(MetadataType.JSON.isCompatible(location)).isTrue();
        }
    }
}

package com.ryuqq.fileflow.domain.iam.organization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OrgCode 유효성 검증 테스트
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("OrgCode 테스트")
class OrgCodeTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 조직 코드로 OrgCode를 생성할 수 있다")
        void createWithValidCode() {
            // given
            String validCode = "SALES-KR";

            // when
            OrgCode orgCode = new OrgCode(validCode);

            // then
            assertThat(orgCode.getValue()).isEqualTo("SALES-KR");
        }

        @Test
        @DisplayName("소문자 입력 시 자동으로 대문자로 변환된다")
        void createWithLowercase() {
            // given
            String lowercaseCode = "sales-kr";

            // when
            OrgCode orgCode = new OrgCode(lowercaseCode);

            // then
            assertThat(orgCode.getValue()).isEqualTo("SALES-KR");
        }

        @Test
        @DisplayName("대소문자 혼합 입력 시 자동으로 대문자로 변환된다")
        void createWithMixedCase() {
            // given
            String mixedCaseCode = "Sales-Kr";

            // when
            OrgCode orgCode = new OrgCode(mixedCaseCode);

            // then
            assertThat(orgCode.getValue()).isEqualTo("SALES-KR");
        }

        @Test
        @DisplayName("앞뒤 공백이 제거된다")
        void createWithWhitespace() {
            // given
            String codeWithWhitespace = "  SALES-KR  ";

            // when
            OrgCode orgCode = new OrgCode(codeWithWhitespace);

            // then
            assertThat(orgCode.getValue()).isEqualTo("SALES-KR");
        }

        @Test
        @DisplayName("영문, 숫자, 하이픈, 언더스코어를 포함한 코드를 생성할 수 있다")
        void createWithAllowedCharacters() {
            // given & when & then
            assertThat(new OrgCode("SALES").getValue()).isEqualTo("SALES");
            assertThat(new OrgCode("SALES123").getValue()).isEqualTo("SALES123");
            assertThat(new OrgCode("SALES-KR").getValue()).isEqualTo("SALES-KR");
            assertThat(new OrgCode("SALES_KR").getValue()).isEqualTo("SALES_KR");
            assertThat(new OrgCode("SALES-KR_01").getValue()).isEqualTo("SALES-KR_01");
        }

        @Test
        @DisplayName("최소 길이(2자)로 생성할 수 있다")
        void createWithMinLength() {
            // given
            String minLengthCode = "AB";

            // when
            OrgCode orgCode = new OrgCode(minLengthCode);

            // then
            assertThat(orgCode.getValue()).isEqualTo("AB");
        }

        @Test
        @DisplayName("최대 길이(20자)로 생성할 수 있다")
        void createWithMaxLength() {
            // given
            String maxLengthCode = "A".repeat(20);

            // when
            OrgCode orgCode = new OrgCode(maxLengthCode);

            // then
            assertThat(orgCode.getValue()).isEqualTo(maxLengthCode);
        }

        @Test
        @DisplayName("null 값으로 생성하면 예외가 발생한다")
        void createWithNullValue() {
            // given
            String nullValue = null;

            // when & then
            assertThatThrownBy(() -> new OrgCode(nullValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 코드는 필수입니다");
        }

        @Test
        @DisplayName("빈 문자열로 생성하면 예외가 발생한다")
        void createWithEmptyValue() {
            // given
            String emptyValue = "";

            // when & then
            assertThatThrownBy(() -> new OrgCode(emptyValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 코드는 필수입니다");
        }

        @Test
        @DisplayName("공백 문자열로 생성하면 예외가 발생한다")
        void createWithBlankValue() {
            // given
            String blankValue = "   ";

            // when & then
            assertThatThrownBy(() -> new OrgCode(blankValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 코드는 필수입니다");
        }

        @Test
        @DisplayName("최소 길이(2자) 미만이면 예외가 발생한다")
        void createWithLessThanMinLength() {
            // given
            String tooShortCode = "A";

            // when & then
            assertThatThrownBy(() -> new OrgCode(tooShortCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 코드는 최소 2자 이상이어야 합니다");
        }

        @Test
        @DisplayName("최대 길이(20자) 초과하면 예외가 발생한다")
        void createWithMoreThanMaxLength() {
            // given
            String tooLongCode = "A".repeat(21);

            // when & then
            assertThatThrownBy(() -> new OrgCode(tooLongCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 코드는 최대 20자까지 허용됩니다");
        }

        @Test
        @DisplayName("허용되지 않는 문자(특수문자)가 포함되면 예외가 발생한다")
        void createWithInvalidCharacters() {
            // when & then
            assertThatThrownBy(() -> new OrgCode("SALES@KR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 코드는 영문 대문자, 숫자, 하이픈(-), 언더스코어(_)만 허용됩니다");

            assertThatThrownBy(() -> new OrgCode("SALES#KR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 코드는 영문 대문자, 숫자, 하이픈(-), 언더스코어(_)만 허용됩니다");

            assertThatThrownBy(() -> new OrgCode("SALES KR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("조직 코드는 영문 대문자, 숫자, 하이픈(-), 언더스코어(_)만 허용됩니다");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 OrgCode는 동등하다")
        void equalityWithSameValue() {
            // given
            OrgCode code1 = new OrgCode("SALES-KR");
            OrgCode code2 = new OrgCode("SALES-KR");

            // when & then
            assertThat(code1).isEqualTo(code2);
            assertThat(code1.hashCode()).isEqualTo(code2.hashCode());
        }

        @Test
        @DisplayName("정규화 후 같은 값이면 동등하다")
        void equalityAfterNormalization() {
            // given
            OrgCode code1 = new OrgCode("sales-kr");
            OrgCode code2 = new OrgCode("SALES-KR");
            OrgCode code3 = new OrgCode("  SALES-KR  ");

            // when & then
            assertThat(code1).isEqualTo(code2);
            assertThat(code2).isEqualTo(code3);
            assertThat(code1.hashCode()).isEqualTo(code2.hashCode());
            assertThat(code2.hashCode()).isEqualTo(code3.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 OrgCode는 동등하지 않다")
        void inequalityWithDifferentValue() {
            // given
            OrgCode code1 = new OrgCode("SALES-KR");
            OrgCode code2 = new OrgCode("SALES-JP");

            // when & then
            assertThat(code1).isNotEqualTo(code2);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString은 내부 값을 반환한다")
        void toStringReturnsValue() {
            // given
            OrgCode orgCode = new OrgCode("SALES-KR");

            // when
            String result = orgCode.toString();

            // then
            assertThat(result).isEqualTo("SALES-KR");
        }
    }
}

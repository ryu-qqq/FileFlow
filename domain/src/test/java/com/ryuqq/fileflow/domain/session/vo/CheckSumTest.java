package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CheckSum 단위 테스트")
class CheckSumTest {

    private static final String VALID_MD5 = "d41d8cd98f00b204e9800998ecf8427e";
    private static final String VALID_SHA256 =
            "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("MD5 형식의 문자열로 생성할 수 있다")
        void of_WithMD5_ShouldCreateCheckSum() {
            // given & when
            CheckSum checkSum = CheckSum.of(VALID_MD5);

            // then
            assertThat(checkSum.key()).isEqualTo(VALID_MD5);
            assertThat(checkSum.isMD5()).isTrue();
            assertThat(checkSum.getAlgorithm()).isEqualTo("MD5");
        }

        @Test
        @DisplayName("SHA256 형식의 문자열로 생성할 수 있다")
        void of_WithSHA256_ShouldCreateCheckSum() {
            // given & when
            CheckSum checkSum = CheckSum.of(VALID_SHA256);

            // then
            assertThat(checkSum.key()).isEqualTo(VALID_SHA256);
            assertThat(checkSum.isSHA256()).isTrue();
            assertThat(checkSum.getAlgorithm()).isEqualTo("SHA256");
        }

        @Test
        @DisplayName("null 또는 빈 문자열이면 예외가 발생한다")
        void of_WithNullOrBlank_ShouldThrowException() {
            // given
            String nullValue = null;
            String blankValue = "   ";

            // when & then
            assertThatThrownBy(() -> CheckSum.of(nullValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("체크섬은 null이거나 빈 문자열일 수 없습니다.");
            assertThatThrownBy(() -> CheckSum.of(blankValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("체크섬은 null이거나 빈 문자열일 수 없습니다.");
        }

        @Test
        @DisplayName("MD5/SHA256 형식이 아니면 예외가 발생한다")
        void of_WithInvalidFormat_ShouldThrowException() {
            // given
            String invalid = "ZZZ123";

            // when & then
            assertThatThrownBy(() -> CheckSum.of(invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("체크섬은 MD5(32자) 또는 SHA256(64자) 형식이어야 합니다");
        }
    }

    @Nested
    @DisplayName("일치 여부 테스트")
    class MatchTest {

        @Test
        @DisplayName("동일한 체크섬 객체끼리는 일치한다")
        void matches_WithSameCheckSum_ShouldReturnTrue() {
            // given
            CheckSum source = CheckSum.of(VALID_SHA256);
            CheckSum target =
                    CheckSum.of("9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08");

            // when & then
            assertThat(source.matches(target)).isTrue();
        }

        @Test
        @DisplayName("서로 다른 체크섬 객체는 일치하지 않는다")
        void matches_WithDifferentCheckSum_ShouldReturnFalse() {
            // given
            CheckSum source = CheckSum.of(VALID_MD5);
            CheckSum target = CheckSum.of(VALID_SHA256);

            // when & then
            assertThat(source.matches(target)).isFalse();
        }

        @Test
        @DisplayName("null 비교 대상은 일치하지 않는다")
        void matches_WithNull_ShouldReturnFalse() {
            // given
            CheckSum source = CheckSum.of(VALID_MD5);

            // when & then
            assertThat(source.matches(null)).isFalse();
        }

        @Test
        @DisplayName("문자열 비교에서 대소문자를 무시하고 일치 여부를 반환한다")
        void matchesKey_WithCaseInsensitive_ShouldReturnTrue() {
            // given
            CheckSum checkSum = CheckSum.of(VALID_SHA256);
            String upperCase = VALID_SHA256.toUpperCase();

            // when & then
            assertThat(checkSum.matchesKey(upperCase)).isTrue();
        }

        @Test
        @DisplayName("null 또는 빈 문자열과는 일치하지 않는다")
        void matchesKey_WithNullOrBlank_ShouldReturnFalse() {
            // given
            CheckSum checkSum = CheckSum.of(VALID_SHA256);

            // when & then
            assertThat(checkSum.matchesKey(null)).isFalse();
            assertThat(checkSum.matchesKey("   ")).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가지면 동등하다")
        void equals_WithSameValue_ShouldBeEqual() {
            // given
            CheckSum checkSum1 = CheckSum.of(VALID_MD5);
            CheckSum checkSum2 = CheckSum.of(VALID_MD5);

            // when & then
            assertThat(checkSum1).isEqualTo(checkSum2);
            assertThat(checkSum1.hashCode()).isEqualTo(checkSum2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가지면 동등하지 않다")
        void equals_WithDifferentValue_ShouldNotBeEqual() {
            // given
            CheckSum checkSum1 = CheckSum.of(VALID_MD5);
            CheckSum checkSum2 = CheckSum.of(VALID_SHA256);

            // when & then
            assertThat(checkSum1).isNotEqualTo(checkSum2);
        }
    }
}

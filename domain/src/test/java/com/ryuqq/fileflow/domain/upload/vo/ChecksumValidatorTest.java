package com.ryuqq.fileflow.domain.upload.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ChecksumValidator 테스트")
class ChecksumValidatorTest {
    // ChecksumValidator는 static 유틸리티 클래스이므로 인스턴스 생성 불필요

    @Nested
    @DisplayName("기본 검증 테스트")
    class BasicValidationTest {

        @Test
        @DisplayName("동일한 체크섬은 검증에 성공한다")
        void validateSameChecksum() {
            // given
            CheckSum checksum1 = CheckSum.sha256("a".repeat(64));
            CheckSum checksum2 = CheckSum.sha256("a".repeat(64));

            // when
            boolean result = ChecksumValidator.validate(checksum1, checksum2);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 체크섬은 검증에 실패한다")
        void validateDifferentChecksum() {
            // given
            CheckSum checksum1 = CheckSum.sha256("a".repeat(64));
            CheckSum checksum2 = CheckSum.sha256("b".repeat(64));

            // when
            boolean result = ChecksumValidator.validate(checksum1, checksum2);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("대소문자가 다른 체크섬도 검증에 성공한다")
        void validateCaseInsensitiveChecksum() {
            // given
            CheckSum lowerCase = CheckSum.sha256("a".repeat(64));
            CheckSum upperCase = CheckSum.sha256("A".repeat(64));

            // when
            boolean result = ChecksumValidator.validate(lowerCase, upperCase);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("예상 체크섬이 null이면 예외가 발생한다")
        void validateWithNullExpected() {
            // given
            CheckSum actual = CheckSum.sha256("a".repeat(64));

            // when & then
            assertThatThrownBy(() ->
                    ChecksumValidator.validate(null, actual)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Expected checksum cannot be null");
        }

        @Test
        @DisplayName("실제 체크섬이 null이면 예외가 발생한다")
        void validateWithNullActual() {
            // given
            CheckSum expected = CheckSum.sha256("a".repeat(64));

            // when & then
            assertThatThrownBy(() ->
                    ChecksumValidator.validate(expected, null)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Actual checksum cannot be null");
        }
    }

    @Nested
    @DisplayName("예외 발생 검증 테스트")
    class ThrowingValidationTest {

        @Test
        @DisplayName("일치하는 체크섬은 예외를 발생시키지 않는다")
        void validateOrThrow_Success() {
            // given
            CheckSum checksum1 = CheckSum.sha256("a".repeat(64));
            CheckSum checksum2 = CheckSum.sha256("a".repeat(64));

            // when & then
            assertThatNoException().isThrownBy(() ->
                    ChecksumValidator.validateOrThrow(checksum1, checksum2)
            );
        }

        @Test
        @DisplayName("일치하지 않는 체크섬은 ChecksumMismatchException을 발생시킨다")
        void validateOrThrow_Failure() {
            // given
            CheckSum expected = CheckSum.sha256("a".repeat(64));
            CheckSum actual = CheckSum.sha256("b".repeat(64));

            // when & then
            assertThatThrownBy(() ->
                    ChecksumValidator.validateOrThrow(expected, actual)
            ).isInstanceOf(ChecksumValidator.ChecksumMismatchException.class)
             .hasMessageContaining("Checksum mismatch")
             .hasMessageContaining("SHA-256");
        }

        @Test
        @DisplayName("다른 알고리즘의 체크섬은 ChecksumMismatchException을 발생시킨다")
        void validateOrThrow_DifferentAlgorithm() {
            // given
            CheckSum sha256 = CheckSum.sha256("a".repeat(64));
            CheckSum md5 = CheckSum.md5("b".repeat(32));

            // when & then
            assertThatThrownBy(() ->
                    ChecksumValidator.validateOrThrow(sha256, md5)
            ).isInstanceOf(ChecksumValidator.ChecksumMismatchException.class);
        }
    }

    @Nested
    @DisplayName("다중 체크섬 검증 테스트")
    class MultipleChecksumValidationTest {

        @Test
        @DisplayName("여러 체크섬 중 하나라도 일치하면 true를 반환한다")
        void validateAny_Success() {
            // given
            CheckSum[] expected = {
                    CheckSum.sha256("a".repeat(64)),
                    CheckSum.sha256("b".repeat(64)),
                    CheckSum.sha256("c".repeat(64))
            };
            CheckSum actual = CheckSum.sha256("b".repeat(64));

            // when
            boolean result = ChecksumValidator.validateAny(expected, actual);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("모든 체크섬이 일치하지 않으면 false를 반환한다")
        void validateAny_Failure() {
            // given
            CheckSum[] expected = {
                    CheckSum.sha256("a".repeat(64)),
                    CheckSum.sha256("b".repeat(64)),
                    CheckSum.sha256("c".repeat(64))
            };
            CheckSum actual = CheckSum.sha256("d".repeat(64));

            // when
            boolean result = ChecksumValidator.validateAny(expected, actual);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("예상 체크섬 배열이 null이면 예외가 발생한다")
        void validateAny_NullExpectedArray() {
            // given
            CheckSum actual = CheckSum.sha256("a".repeat(64));

            // when & then
            assertThatThrownBy(() ->
                    ChecksumValidator.validateAny(null, actual)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Expected checksums cannot be null or empty");
        }

        @Test
        @DisplayName("예상 체크섬 배열이 비어있으면 예외가 발생한다")
        void validateAny_EmptyExpectedArray() {
            // given
            CheckSum[] expected = new CheckSum[0];
            CheckSum actual = CheckSum.sha256("a".repeat(64));

            // when & then
            assertThatThrownBy(() ->
                    ChecksumValidator.validateAny(expected, actual)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Expected checksums cannot be null or empty");
        }

        @Test
        @DisplayName("배열에 null이 포함되어 있어도 정상 동작한다")
        void validateAny_WithNullElement() {
            // given
            CheckSum[] expected = {
                    CheckSum.sha256("a".repeat(64)),
                    null,
                    CheckSum.sha256("b".repeat(64))
            };
            CheckSum actual = CheckSum.sha256("b".repeat(64));

            // when
            boolean result = ChecksumValidator.validateAny(expected, actual);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("알고리즘 검증 테스트")
    class AlgorithmValidationTest {

        @Test
        @DisplayName("동일한 알고리즘을 사용하는 체크섬은 true를 반환한다")
        void hasSameAlgorithm_Success() {
            // given
            CheckSum checksum1 = CheckSum.sha256("a".repeat(64));
            CheckSum checksum2 = CheckSum.sha256("b".repeat(64));

            // when
            boolean result = ChecksumValidator.hasSameAlgorithm(checksum1, checksum2);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 알고리즘을 사용하는 체크섬은 false를 반환한다")
        void hasSameAlgorithm_Failure() {
            // given
            CheckSum sha256 = CheckSum.sha256("a".repeat(64));
            CheckSum md5 = CheckSum.md5("b".repeat(32));

            // when
            boolean result = ChecksumValidator.hasSameAlgorithm(sha256, md5);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("SHA-256은 권장 알고리즘이다")
        void isRecommendedAlgorithm_SHA256() {
            // given
            CheckSum checksum = CheckSum.sha256("a".repeat(64));

            // when
            boolean result = ChecksumValidator.isRecommendedAlgorithm(checksum);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("SHA-512는 권장 알고리즘이다")
        void isRecommendedAlgorithm_SHA512() {
            // given
            CheckSum checksum = CheckSum.sha512("a".repeat(128));

            // when
            boolean result = ChecksumValidator.isRecommendedAlgorithm(checksum);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("MD5는 권장하지 않는 알고리즘이다")
        void isRecommendedAlgorithm_MD5() {
            // given
            CheckSum checksum = CheckSum.md5("a".repeat(32));

            // when
            boolean result = ChecksumValidator.isRecommendedAlgorithm(checksum);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("알고리즘 검증 시 체크섬이 null이면 예외가 발생한다")
        void hasSameAlgorithm_NullChecksum() {
            // given
            CheckSum checksum = CheckSum.sha256("a".repeat(64));

            // when & then
            assertThatThrownBy(() ->
                    ChecksumValidator.hasSameAlgorithm(checksum, null)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("cannot be null");
        }
    }
}

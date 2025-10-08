package com.ryuqq.fileflow.domain.upload.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CheckSum Value Object 테스트")
class CheckSumTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("SHA-256 해시로 CheckSum을 생성할 수 있다")
        void createWithSha256() {
            // given
            String validSha256 = "a".repeat(64);

            // when
            CheckSum checksum = CheckSum.sha256(validSha256);

            // then
            assertThat(checksum).isNotNull();
            assertThat(checksum.value()).isEqualTo(validSha256);
            assertThat(checksum.algorithm()).isEqualTo("SHA-256");
        }

        @Test
        @DisplayName("SHA-512 해시로 CheckSum을 생성할 수 있다")
        void createWithSha512() {
            // given
            String validSha512 = "a".repeat(128);

            // when
            CheckSum checksum = CheckSum.sha512(validSha512);

            // then
            assertThat(checksum).isNotNull();
            assertThat(checksum.value()).isEqualTo(validSha512);
            assertThat(checksum.algorithm()).isEqualTo("SHA-512");
        }

        @Test
        @DisplayName("MD5 해시로 CheckSum을 생성할 수 있다")
        void createWithMd5() {
            // given
            String validMd5 = "a".repeat(32);

            // when
            CheckSum checksum = CheckSum.md5(validMd5);

            // then
            assertThat(checksum).isNotNull();
            assertThat(checksum.value()).isEqualTo(validMd5);
            assertThat(checksum.algorithm()).isEqualTo("MD5");
        }

        @Test
        @DisplayName("대문자 16진수 해시 값도 허용된다")
        void createWithUppercaseHex() {
            // given
            String uppercaseHex = "A".repeat(64);

            // when
            CheckSum checksum = CheckSum.sha256(uppercaseHex);

            // then
            assertThat(checksum).isNotNull();
            assertThat(checksum.value()).isEqualTo(uppercaseHex);
        }

        @Test
        @DisplayName("대소문자 혼합 16진수 해시 값도 허용된다")
        void createWithMixedCaseHex() {
            // given - 정확히 64자 길이의 16진수 문자열
            String mixedCaseHex = "aAbBcCdDeEfF0123456789aAbBcCdDeEfF0123456789aAbBcCdDeEfF01234567";

            // when
            CheckSum checksum = CheckSum.sha256(mixedCaseHex);

            // then
            assertThat(checksum).isNotNull();
            assertThat(checksum.value()).hasSize(64);
        }
    }

    @Nested
    @DisplayName("검증 실패 테스트")
    class ValidationTest {

        @Test
        @DisplayName("null 값으로 CheckSum을 생성할 수 없다")
        void createWithNullValue() {
            assertThatThrownBy(() -> CheckSum.sha256(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CheckSum value cannot be null or empty");
        }

        @Test
        @DisplayName("빈 문자열로 CheckSum을 생성할 수 없다")
        void createWithEmptyValue() {
            assertThatThrownBy(() -> CheckSum.sha256(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CheckSum value cannot be null or empty");
        }

        @Test
        @DisplayName("공백만 있는 문자열로 CheckSum을 생성할 수 없다")
        void createWithBlankValue() {
            assertThatThrownBy(() -> CheckSum.sha256("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CheckSum value cannot be null or empty");
        }

        @Test
        @DisplayName("16진수가 아닌 문자가 포함된 값으로 CheckSum을 생성할 수 없다")
        void createWithNonHexCharacters() {
            // given
            String invalidHex = "g".repeat(64); // 'g'는 16진수가 아님

            // when & then
            assertThatThrownBy(() -> CheckSum.sha256(invalidHex))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("hexadecimal characters");
        }

        @Test
        @DisplayName("SHA-256 해시 길이가 64자가 아니면 CheckSum을 생성할 수 없다")
        void createWithInvalidSha256Length() {
            // given
            String shortHash = "a".repeat(63);

            // when & then
            assertThatThrownBy(() -> CheckSum.sha256(shortHash))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid SHA-256 hash length");
        }

        @Test
        @DisplayName("SHA-512 해시 길이가 128자가 아니면 CheckSum을 생성할 수 없다")
        void createWithInvalidSha512Length() {
            // given
            String shortHash = "a".repeat(127);

            // when & then
            assertThatThrownBy(() -> CheckSum.sha512(shortHash))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid SHA-512 hash length");
        }

        @Test
        @DisplayName("MD5 해시 길이가 32자가 아니면 CheckSum을 생성할 수 없다")
        void createWithInvalidMd5Length() {
            // given
            String shortHash = "a".repeat(31);

            // when & then
            assertThatThrownBy(() -> CheckSum.md5(shortHash))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid MD5 hash length");
        }

        @Test
        @DisplayName("지원하지 않는 알고리즘으로 CheckSum을 생성할 수 없다")
        void createWithUnsupportedAlgorithm() {
            // given
            String validHash = "a".repeat(64);

            // when & then
            assertThatThrownBy(() -> new CheckSum(validHash, "SHA-1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported algorithm");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("동일한 값과 알고리즘을 가진 CheckSum은 같다")
        void equalChecksums() {
            // given
            String hash = "a".repeat(64);
            CheckSum checksum1 = CheckSum.sha256(hash);
            CheckSum checksum2 = CheckSum.sha256(hash);

            // when & then
            assertThat(checksum1).isEqualTo(checksum2);
            assertThat(checksum1.hashCode()).isEqualTo(checksum2.hashCode());
        }

        @Test
        @DisplayName("대소문자가 다른 같은 해시 값은 같다")
        void equalWithDifferentCase() {
            // given
            String lowerCaseHash = "a".repeat(64);
            String upperCaseHash = "A".repeat(64);
            CheckSum checksum1 = CheckSum.sha256(lowerCaseHash);
            CheckSum checksum2 = CheckSum.sha256(upperCaseHash);

            // when & then
            assertThat(checksum1).isEqualTo(checksum2);
            assertThat(checksum1.hashCode()).isEqualTo(checksum2.hashCode());
        }

        @Test
        @DisplayName("다른 해시 값을 가진 CheckSum은 다르다")
        void notEqualDifferentHash() {
            // given
            CheckSum checksum1 = CheckSum.sha256("a".repeat(64));
            CheckSum checksum2 = CheckSum.sha256("b".repeat(64));

            // when & then
            assertThat(checksum1).isNotEqualTo(checksum2);
        }

        @Test
        @DisplayName("다른 알고리즘을 가진 CheckSum은 다르다")
        void notEqualDifferentAlgorithm() {
            // given
            String hash = "a".repeat(32);
            CheckSum md5 = CheckSum.md5(hash);
            CheckSum sha256 = CheckSum.sha256(hash + hash);

            // when & then
            assertThat(md5).isNotEqualTo(sha256);
        }
    }

    @Nested
    @DisplayName("매칭 테스트")
    class MatchingTest {

        @Test
        @DisplayName("동일한 CheckSum은 매칭된다")
        void matchesIdenticalChecksum() {
            // given
            String hash = "a".repeat(64);
            CheckSum checksum1 = CheckSum.sha256(hash);
            CheckSum checksum2 = CheckSum.sha256(hash);

            // when & then
            assertThat(checksum1.matches(checksum2)).isTrue();
        }

        @Test
        @DisplayName("대소문자가 다른 같은 해시는 매칭된다")
        void matchesCaseInsensitive() {
            // given
            CheckSum lowerCase = CheckSum.sha256("a".repeat(64));
            CheckSum upperCase = CheckSum.sha256("A".repeat(64));

            // when & then
            assertThat(lowerCase.matches(upperCase)).isTrue();
        }

        @Test
        @DisplayName("다른 해시 값은 매칭되지 않는다")
        void doesNotMatchDifferentHash() {
            // given
            CheckSum checksum1 = CheckSum.sha256("a".repeat(64));
            CheckSum checksum2 = CheckSum.sha256("b".repeat(64));

            // when & then
            assertThat(checksum1.matches(checksum2)).isFalse();
        }

        @Test
        @DisplayName("다른 알고리즘은 매칭되지 않는다")
        void doesNotMatchDifferentAlgorithm() {
            // given
            CheckSum md5 = CheckSum.md5("a".repeat(32));
            CheckSum sha256 = CheckSum.sha256("a".repeat(64));

            // when & then
            assertThat(md5.matches(sha256)).isFalse();
        }

        @Test
        @DisplayName("null과는 매칭되지 않는다")
        void doesNotMatchNull() {
            // given
            CheckSum checksum = CheckSum.sha256("a".repeat(64));

            // when & then
            assertThat(checksum.matches(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("정규화 테스트")
    class NormalizationTest {

        @Test
        @DisplayName("normalizedValue()는 소문자로 정규화된 해시 값을 반환한다")
        void normalizedValue() {
            // given
            String upperCaseHash = "ABCDEF0123456789".repeat(4);
            CheckSum checksum = CheckSum.sha256(upperCaseHash);

            // when
            String normalized = checksum.normalizedValue();

            // then
            assertThat(normalized).isEqualTo(upperCaseHash.toLowerCase());
        }

        @Test
        @DisplayName("이미 소문자인 경우 동일한 값을 반환한다")
        void normalizedValueAlreadyLowercase() {
            // given
            String lowerCaseHash = "abcdef0123456789".repeat(4);
            CheckSum checksum = CheckSum.sha256(lowerCaseHash);

            // when
            String normalized = checksum.normalizedValue();

            // then
            assertThat(normalized).isEqualTo(lowerCaseHash);
        }
    }
}

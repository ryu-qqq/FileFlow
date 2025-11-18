package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Checksum Value Object 테스트
 */
class ChecksumTest {

    @Test
    @DisplayName("SHA-256 체크섬으로 Checksum을 생성해야 한다")
    void shouldCreateChecksumWithSHA256() {
        // given
        String validSha256 = "a".repeat(64); // 64자 hex string

        // when
        Checksum checksum = Checksum.sha256(validSha256);

        // then
        assertThat(checksum).isNotNull();
        assertThat(checksum.algorithm()).isEqualTo("SHA-256");
        assertThat(checksum.value()).isEqualTo(validSha256);
    }

    @Test
    @DisplayName("MD5 체크섬으로 Checksum을 생성해야 한다")
    void shouldCreateChecksumWithMD5() {
        // given
        String validMd5 = "b".repeat(32); // 32자 hex string

        // when
        Checksum checksum = Checksum.md5(validMd5);

        // then
        assertThat(checksum).isNotNull();
        assertThat(checksum.algorithm()).isEqualTo("MD5");
        assertThat(checksum.value()).isEqualTo(validMd5);
    }

    @Test
    @DisplayName("SHA-256 체크섬이 64자가 아니면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenSHA256LengthIsNot64() {
        // given
        String invalidSha256 = "a".repeat(63);

        // when & then
        assertThatThrownBy(() -> Checksum.sha256(invalidSha256))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SHA-256 체크섬은 64자의 16진수 문자열이어야 합니다");
    }

    @Test
    @DisplayName("MD5 체크섬이 32자가 아니면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenMD5LengthIsNot32() {
        // given
        String invalidMd5 = "b".repeat(31);

        // when & then
        assertThatThrownBy(() -> Checksum.md5(invalidMd5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MD5 체크섬은 32자의 16진수 문자열이어야 합니다");
    }

    @Test
    @DisplayName("16진수가 아닌 문자가 포함되면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenContainsNonHexCharacters() {
        // given
        String invalidHex = "g".repeat(64); // 'g'는 16진수가 아님

        // when & then
        assertThatThrownBy(() -> Checksum.sha256(invalidHex))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("체크섬은 16진수 문자열이어야 합니다");
    }

    @Test
    @DisplayName("같은 값을 가진 Checksum은 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String sha256Value = "a".repeat(64);
        Checksum checksum1 = Checksum.sha256(sha256Value);
        Checksum checksum2 = Checksum.sha256(sha256Value);

        // when & then
        assertThat(checksum1).isEqualTo(checksum2);
    }
}

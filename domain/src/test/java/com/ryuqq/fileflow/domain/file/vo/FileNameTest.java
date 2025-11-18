package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileName Value Object 테스트
 */
class FileNameTest {

    @Test
    @DisplayName("유효한 파일명으로 FileName을 생성해야 한다")
    void shouldCreateValidFileName() {
        // given
        String validName = "example.jpg";

        // when
        FileName fileName = FileName.of(validName);

        // then
        assertThat(fileName).isNotNull();
        assertThat(fileName.getValue()).isEqualTo(validName);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("null 또는 빈 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNullOrEmpty(String invalidName) {
        // when & then
        assertThatThrownBy(() -> FileName.of(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일명은 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("255자를 초과하는 파일명은 예외가 발생해야 한다")
    void shouldThrowExceptionWhenLengthExceeds255() {
        // given
        String tooLongName = "a".repeat(256);

        // when & then
        assertThatThrownBy(() -> FileName.of(tooLongName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일명은 1자 이상 255자 이하여야 합니다");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "\\", "<", ">", ":", "\"", "|", "?", "*"})
    @DisplayName("금지 문자가 포함된 파일명은 예외가 발생해야 한다")
    void shouldThrowExceptionWhenContainsForbiddenCharacters(String forbiddenChar) {
        // given
        String invalidName = "test" + forbiddenChar + "file.jpg";

        // when & then
        assertThatThrownBy(() -> FileName.of(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일명에 사용할 수 없는 문자가 포함되어 있습니다");
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환해야 한다")
    void shouldReturnSameValueFromGetValue() {
        // given
        String expectedValue = "document.pdf";
        FileName fileName = FileName.of(expectedValue);

        // when
        String actualValue = fileName.getValue();

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("같은 값을 가진 FileName은 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String name = "image.png";
        FileName fileName1 = FileName.of(name);
        FileName fileName2 = FileName.of(name);

        // when & then
        assertThat(fileName1).isEqualTo(fileName2);
    }

    @Test
    @DisplayName("같은 값을 가진 FileName은 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenValueIsSame() {
        // given
        String name = "video.mp4";
        FileName fileName1 = FileName.of(name);
        FileName fileName2 = FileName.of(name);

        // when & then
        assertThat(fileName1.hashCode()).isEqualTo(fileName2.hashCode());
    }

    @Test
    @DisplayName("255자의 파일명은 정상적으로 생성되어야 한다")
    void shouldCreateFileNameWith255Characters() {
        // given
        String maxLengthName = "a".repeat(255);

        // when
        FileName fileName = FileName.of(maxLengthName);

        // then
        assertThat(fileName).isNotNull();
        assertThat(fileName.getValue()).hasSize(255);
    }

    @Test
    @DisplayName("1자의 파일명은 정상적으로 생성되어야 한다")
    void shouldCreateFileNameWith1Character() {
        // given
        String singleCharName = "a";

        // when
        FileName fileName = FileName.of(singleCharName);

        // then
        assertThat(fileName).isNotNull();
        assertThat(fileName.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("한글 파일명은 정상적으로 생성되어야 한다")
    void shouldCreateFileNameWithKoreanCharacters() {
        // given
        String koreanName = "한글파일명.txt";

        // when
        FileName fileName = FileName.of(koreanName);

        // then
        assertThat(fileName).isNotNull();
        assertThat(fileName.getValue()).isEqualTo(koreanName);
    }

    @Test
    @DisplayName("공백이 포함된 파일명은 정상적으로 생성되어야 한다")
    void shouldCreateFileNameWithSpaces() {
        // given
        String nameWithSpaces = "my document file.pdf";

        // when
        FileName fileName = FileName.of(nameWithSpaces);

        // then
        assertThat(fileName).isNotNull();
        assertThat(fileName.getValue()).isEqualTo(nameWithSpaces);
    }
}

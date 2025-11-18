package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.htmlPageName;
import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.imageJpegName;
import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.nameWithoutExtension;
import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.overMaxLengthName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileName VO Tests")
class FileNameTest {

    @Test
    @DisplayName("확장자를 포함한 파일 이름을 생성할 수 있어야 한다")
    void shouldCreateFileNameWithExtension() {
        // given
        String fileNameValue = imageJpegName();

        // when
        FileName fileName = FileName.from(fileNameValue);

        // then
        assertThat(fileName.value()).isEqualTo(fileNameValue);
        assertThat(fileName.extension()).isEqualTo(".jpg");
    }

    @Test
    @DisplayName("확장자를 정확하게 추출해야 한다")
    void shouldExtractExtensionCorrectly() {
        // given
        String fileNameValue = htmlPageName();

        // when
        FileName fileName = FileName.from(fileNameValue);

        // then
        assertThat(fileName.extension()).isEqualTo(".html");
    }

    @Test
    @DisplayName("withoutExtension()은 확장자를 제외한 값을 반환해야 한다")
    void shouldReturnWithoutExtension() {
        // given
        String fileNameValue = imageJpegName();
        FileName fileName = FileName.from(fileNameValue);

        // when
        String withoutExtension = fileName.withoutExtension();

        // then
        assertThat(withoutExtension).isEqualTo(nameWithoutExtension());
    }

    @Test
    @DisplayName("null 값으로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenNull() {
        // expect
        assertThatThrownBy(() -> FileName.from(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("255자를 초과하는 파일 이름은 허용되지 않아야 한다")
    void shouldThrowExceptionWhenTooLong() {
        // given
        String overlyLongName = overMaxLengthName();

        // expect
        assertThatThrownBy(() -> FileName.from(overlyLongName))
            .isInstanceOf(IllegalArgumentException.class);
    }
}


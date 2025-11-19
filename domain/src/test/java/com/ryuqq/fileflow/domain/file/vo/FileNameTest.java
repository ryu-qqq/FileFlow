package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.HTML_PAGE_NAME;
import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.IMAGE_JPEG_NAME;
import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.WITHOUT_EXTENSION_NAME;
import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.from;
import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.htmlPage;
import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.imageJpeg;
import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.overMaxLengthName;
import static com.ryuqq.fileflow.domain.file.fixture.FileNameFixture.withoutExtensionFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileName VO Tests")
class FileNameTest {

    @Test
    @DisplayName("확장자를 포함한 파일 이름을 생성할 수 있어야 한다")
    void shouldCreateFileNameWithExtension() {
        // when
        FileName fileName = imageJpeg();

        // then
        assertThat(fileName.value()).isEqualTo(IMAGE_JPEG_NAME);
        assertThat(fileName.extension()).isEqualTo(".jpg");
    }

    @Test
    @DisplayName("확장자를 정확하게 추출해야 한다")
    void shouldExtractExtensionCorrectly() {
        // when
        FileName fileName = htmlPage();

        // then
        assertThat(fileName.extension()).isEqualTo(".html");
    }

    @Test
    @DisplayName("withoutExtension()은 확장자를 제외한 값을 반환해야 한다")
    void shouldReturnWithoutExtension() {
        // given
        FileName fileName = withoutExtensionFile();

        // when
        String withoutExtension = fileName.withoutExtension();

        // then
        assertThat(withoutExtension).isEqualTo(WITHOUT_EXTENSION_NAME);
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


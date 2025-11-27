package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.fixture.FileNameFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileName 단위 테스트")
class FileNameTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 파일명으로 생성할 수 있다")
        void of_WithValidName_ShouldCreateFileName() {
            // given
            String validName = "test-file.jpg";

            // when
            FileName fileName = FileName.of(validName);

            // then
            assertThat(fileName.name()).isEqualTo(validName);
        }

        @Test
        @DisplayName("확장자가 있는 파일명으로 생성할 수 있다")
        void of_WithExtension_ShouldCreateFileName() {
            // given
            String nameWithExtension = "document.pdf";

            // when
            FileName fileName = FileName.of(nameWithExtension);

            // then
            assertThat(fileName.name()).isEqualTo(nameWithExtension);
        }

        @Test
        @DisplayName("특수문자가 포함된 파일명으로 생성할 수 있다")
        void of_WithSpecialCharacters_ShouldCreateFileName() {
            // given
            String nameWithSpecialChars = "test_file-v1.2.jpg";

            // when
            FileName fileName = FileName.of(nameWithSpecialChars);

            // then
            assertThat(fileName.name()).isEqualTo(nameWithSpecialChars);
        }

        @Test
        @DisplayName("null 파일명으로 생성 시 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given
            String nullName = null;

            // when & then
            assertThatThrownBy(() -> FileName.of(nullName))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 문자열 파일명으로 생성 시 예외가 발생한다")
        void of_WithEmptyString_ShouldThrowException() {
            // given
            String emptyName = "";

            // when & then
            assertThatThrownBy(() -> FileName.of(emptyName))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("공백만 있는 파일명으로 생성 시 예외가 발생한다")
        void of_WithBlankString_ShouldThrowException() {
            // given
            String blankName = "   ";

            // when & then
            assertThatThrownBy(() -> FileName.of(blankName))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("유효성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("파일명 길이 제한을 검증한다")
        void validation_ShouldCheckNameLength() {
            // given
            String tooLongName = "a".repeat(256); // 255자 초과

            // when & then
            assertThatThrownBy(() -> FileName.of(tooLongName))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("제어 문자가 포함된 파일명은 거부된다")
        void validation_ShouldRejectControlCharacters() {
            // given
            String[] controlCharNames = {
                "file\0name.txt", "file\nname.txt", "file\rname.txt", "file\tname.txt"
            };

            // when & then
            for (String controlCharName : controlCharNames) {
                assertThatThrownBy(() -> FileName.of(controlCharName))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("제어 문자");
            }
        }

        @Test
        @DisplayName("경로 순회 패턴이 포함된 파일명은 거부된다")
        void validation_ShouldRejectPathTraversalPatterns() {
            // given
            String[] pathTraversalNames = {"../file.txt", "..\\file.txt", "file/../name.txt"};

            // when & then
            for (String pathTraversalName : pathTraversalNames) {
                assertThatThrownBy(() -> FileName.of(pathTraversalName))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("경로 순회");
            }
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 이름을 가진 FileName은 동등하다")
        void equals_WithSameName_ShouldBeEqual() {
            // given
            String name = "test-file.jpg";
            FileName fileName1 = FileName.of(name);
            FileName fileName2 = FileName.of(name);

            // when & then
            assertThat(fileName1).isEqualTo(fileName2);
            assertThat(fileName1.hashCode()).isEqualTo(fileName2.hashCode());
        }

        @Test
        @DisplayName("다른 이름을 가진 FileName은 동등하지 않다")
        void equals_WithDifferentName_ShouldNotBeEqual() {
            // given
            FileName fileName1 = FileName.of("file1.jpg");
            FileName fileName2 = FileName.of("file2.jpg");

            // when & then
            assertThat(fileName1).isNotEqualTo(fileName2);
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 FileName이 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            FileName defaultName = FileNameFixture.defaultFileName();
            FileName imageName = FileNameFixture.imageFileName();
            FileName videoName = FileNameFixture.videoFileName();
            FileName pdfName = FileNameFixture.pdfFileName();

            // then
            assertThat(defaultName.name()).isEqualTo("test-file.jpg");
            assertThat(imageName.name()).isEqualTo("image-sample.png");
            assertThat(videoName.name()).isEqualTo("video-sample.mp4");
            assertThat(pdfName.name()).isEqualTo("document.pdf");
        }
    }

    @Nested
    @DisplayName("파일 확장자 테스트")
    class FileExtensionTest {

        @Test
        @DisplayName("파일 확장자를 추출할 수 있다")
        void getExtension_ShouldReturnCorrectExtension() {
            // given
            FileName fileName = FileName.of("test-file.jpg");

            // when
            String extension = fileName.getExtension();

            // then
            assertThat(extension).isEqualTo("jpg");
        }

        @Test
        @DisplayName("확장자가 없는 파일은 빈 문자열을 반환한다")
        void getExtension_WithoutExtension_ShouldReturnEmptyString() {
            // given
            FileName fileName = FileName.of("filename");

            // when
            String extension = fileName.getExtension();

            // then
            assertThat(extension).isEmpty();
        }

        @Test
        @DisplayName("파일명만 추출할 수 있다")
        void withoutExtension_ShouldReturnNameOnly() {
            // given
            FileName fileName = FileName.of("test-file.jpg");

            // when
            String nameOnly = fileName.withoutExtension();

            // then
            assertThat(nameOnly).isEqualTo("test-file");
        }

        @Test
        @DisplayName("특정 확장자와 일치하는지 확인할 수 있다")
        void hasExtension_ShouldCheckExtensionMatch() {
            // given
            FileName fileName = FileName.of("test-file.JPG");

            // when & then
            assertThat(fileName.hasExtension("jpg")).isTrue();
            assertThat(fileName.hasExtension("JPG")).isTrue();
            assertThat(fileName.hasExtension("png")).isFalse();
        }

        @Test
        @DisplayName("파일명이 안전한지 확인할 수 있다")
        void isSecure_ShouldCheckSafety() {
            // given
            FileName safeName = FileName.of("safe-file.jpg");

            // when & then
            assertThat(safeName.isSecure()).isTrue();
            // 경로 순회 패턴은 생성 시점에서 예외 발생하므로 별도 테스트 불가
        }
    }
}

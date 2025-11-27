package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.exception.FileSizeExceededException;
import com.ryuqq.fileflow.domain.session.fixture.FileSizeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileSize 단위 테스트")
class FileSizeTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 크기로 생성할 수 있다")
        void of_WithValidSize_ShouldCreateFileSize() {
            // given
            long validSize = 1024L; // 1KB

            // when
            FileSize fileSize = FileSize.of(validSize);

            // then
            assertThat(fileSize.size()).isEqualTo(validSize);
        }

        @Test
        @DisplayName("0 이하 크기로 생성 시 예외가 발생한다")
        void of_WithZeroOrNegativeSize_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> FileSize.of(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일 크기는 0보다 커야 합니다");

            assertThatThrownBy(() -> FileSize.of(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일 크기는 0보다 커야 합니다");
        }

        @Test
        @DisplayName("최대 크기 초과 시 예외가 발생한다")
        void of_WithOversizedFile_ShouldThrowException() {
            // given
            long oversizedFile = 6L * 1024 * 1024 * 1024 * 1024; // 6TB (5TB 초과)

            // when & then
            assertThatThrownBy(() -> FileSize.of(oversizedFile))
                    .isInstanceOf(FileSizeExceededException.class);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 크기를 가진 FileSize는 동등하다")
        void equals_WithSameSize_ShouldBeEqual() {
            // given
            long size = 1024L;
            FileSize fileSize1 = FileSize.of(size);
            FileSize fileSize2 = FileSize.of(size);

            // when & then
            assertThat(fileSize1).isEqualTo(fileSize2);
            assertThat(fileSize1.hashCode()).isEqualTo(fileSize2.hashCode());
        }

        @Test
        @DisplayName("다른 크기를 가진 FileSize는 동등하지 않다")
        void equals_WithDifferentSize_ShouldNotBeEqual() {
            // given
            FileSize fileSize1 = FileSize.of(1024L);
            FileSize fileSize2 = FileSize.of(2048L);

            // when & then
            assertThat(fileSize1).isNotEqualTo(fileSize2);
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 FileSize가 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            FileSize defaultSize = FileSizeFixture.defaultFileSize();
            FileSize smallSize = FileSizeFixture.smallFileSize();
            FileSize mediumSize = FileSizeFixture.mediumFileSize();
            FileSize largeSize = FileSizeFixture.largeFileSize();

            // then
            assertThat(defaultSize.size()).isEqualTo(10 * 1024 * 1024L); // 10MB
            assertThat(smallSize.size()).isEqualTo(1024 * 1024L); // 1MB
            assertThat(mediumSize.size()).isEqualTo(100 * 1024 * 1024L); // 100MB
            assertThat(largeSize.size()).isEqualTo(1024 * 1024 * 1024L); // 1GB
        }
    }
}

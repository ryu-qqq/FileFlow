package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileInfo VO")
class FileInfoTest {

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("정상적인 값으로 생성된다")
        void shouldCreateWithValidValues() {
            // when
            FileInfo info = FileInfo.of("test.jpg", 1024L, "image/jpeg", "etag-123", "jpg");

            // then
            assertThat(info.fileName()).isEqualTo("test.jpg");
            assertThat(info.fileSize()).isEqualTo(1024L);
            assertThat(info.contentType()).isEqualTo("image/jpeg");
            assertThat(info.etag()).isEqualTo("etag-123");
            assertThat(info.extension()).isEqualTo("jpg");
        }

        @Test
        @DisplayName("fileName이 null이면 NullPointerException이 발생한다")
        void shouldThrowOnNullFileName() {
            assertThatThrownBy(() -> FileInfo.of(null, 1024L, "image/jpeg", "etag", "jpg"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("fileName must not be null");
        }

        @Test
        @DisplayName("contentType이 null이면 NullPointerException이 발생한다")
        void shouldThrowOnNullContentType() {
            assertThatThrownBy(() -> FileInfo.of("test.jpg", 1024L, null, "etag", "jpg"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("contentType must not be null");
        }

        @Test
        @DisplayName("etag이 null이면 NullPointerException이 발생한다")
        void shouldThrowOnNullEtag() {
            assertThatThrownBy(() -> FileInfo.of("test.jpg", 1024L, "image/jpeg", null, "jpg"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("etag must not be null");
        }

        @Test
        @DisplayName("extension이 null이면 NullPointerException이 발생한다")
        void shouldThrowOnNullExtension() {
            assertThatThrownBy(() -> FileInfo.of("test.jpg", 1024L, "image/jpeg", "etag", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("extension must not be null");
        }

        @Test
        @DisplayName("fileSize가 0이면 IllegalArgumentException이 발생한다")
        void shouldThrowOnZeroFileSize() {
            assertThatThrownBy(() -> FileInfo.of("test.jpg", 0L, "image/jpeg", "etag", "jpg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("fileSize must be > 0");
        }

        @Test
        @DisplayName("fileSize가 음수면 IllegalArgumentException이 발생한다")
        void shouldThrowOnNegativeFileSize() {
            assertThatThrownBy(() -> FileInfo.of("test.jpg", -1L, "image/jpeg", "etag", "jpg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("fileSize must be > 0");
        }
    }

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("같은 값이면 동일하다 (record)")
        void shouldBeEqualWithSameValues() {
            // given
            FileInfo info1 = FileInfo.of("test.jpg", 1024L, "image/jpeg", "etag-123", "jpg");
            FileInfo info2 = FileInfo.of("test.jpg", 1024L, "image/jpeg", "etag-123", "jpg");

            // then
            assertThat(info1).isEqualTo(info2);
        }
    }
}

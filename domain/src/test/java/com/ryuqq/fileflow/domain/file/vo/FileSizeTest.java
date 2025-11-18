package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileSize Value Object 테스트
 */
class FileSizeTest {

    private static final long ONE_GB_IN_BYTES = 1_073_741_824L; // 1GB
    private static final long ONE_BYTE = 1L;

    @Test
    @DisplayName("유효한 파일 크기로 FileSize를 생성해야 한다")
    void shouldCreateValidFileSize() {
        // given
        long validSize = 1048576L; // 1MB

        // when
        FileSize fileSize = FileSize.of(validSize);

        // then
        assertThat(fileSize).isNotNull();
        assertThat(fileSize.getValue()).isEqualTo(validSize);
    }

    @Test
    @DisplayName("1 바이트 파일 크기를 생성해야 한다")
    void shouldCreateFileSizeWithOneByte() {
        // given
        long oneByte = ONE_BYTE;

        // when
        FileSize fileSize = FileSize.of(oneByte);

        // then
        assertThat(fileSize).isNotNull();
        assertThat(fileSize.getValue()).isEqualTo(oneByte);
    }

    @Test
    @DisplayName("1GB (최대 크기) 파일 크기를 생성해야 한다")
    void shouldCreateFileSizeWithMaxSize() {
        // given
        long maxSize = ONE_GB_IN_BYTES;

        // when
        FileSize fileSize = FileSize.of(maxSize);

        // then
        assertThat(fileSize).isNotNull();
        assertThat(fileSize.getValue()).isEqualTo(maxSize);
    }

    @Test
    @DisplayName("0 이하의 파일 크기는 예외가 발생해야 한다")
    void shouldThrowExceptionWhenSizeIsZeroOrNegative() {
        // given
        long zero = 0L;
        long negative = -1L;

        // when & then
        assertThatThrownBy(() -> FileSize.of(zero))
                .hasMessageContaining("파일 크기는 0 이상이어야 합니다");

        assertThatThrownBy(() -> FileSize.of(negative))
                .hasMessageContaining("파일 크기는 0 이상이어야 합니다");
    }

    @Test
    @DisplayName("1GB를 초과하는 파일 크기는 예외가 발생해야 한다")
    void shouldThrowExceptionWhenSizeExceedsOneGB() {
        // given
        long overMaxSize = ONE_GB_IN_BYTES + 1;

        // when & then
        assertThatThrownBy(() -> FileSize.of(overMaxSize))
                .hasMessageContaining("파일 크기 제한을 초과했습니다");
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환해야 한다")
    void shouldReturnSameValueFromGetValue() {
        // given
        long expectedValue = 524288L; // 512KB
        FileSize fileSize = FileSize.of(expectedValue);

        // when
        long actualValue = fileSize.getValue();

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("같은 값을 가진 FileSize는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        long size = 1048576L; // 1MB
        FileSize fileSize1 = FileSize.of(size);
        FileSize fileSize2 = FileSize.of(size);

        // when & then
        assertThat(fileSize1).isEqualTo(fileSize2);
    }

    @Test
    @DisplayName("같은 값을 가진 FileSize는 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenValueIsSame() {
        // given
        long size = 2097152L; // 2MB
        FileSize fileSize1 = FileSize.of(size);
        FileSize fileSize2 = FileSize.of(size);

        // when & then
        assertThat(fileSize1.hashCode()).isEqualTo(fileSize2.hashCode());
    }

    @Test
    @DisplayName("isSingleUpload()는 100MB 미만일 때 true를 반환해야 한다")
    void shouldReturnTrueForSingleUploadWhenSizeIsLessThan100MB() {
        // given
        long lessThan100MB = 100 * 1024 * 1024 - 1; // 100MB - 1 byte
        FileSize fileSize = FileSize.of(lessThan100MB);

        // when & then
        assertThat(fileSize.isSingleUpload()).isTrue();
    }

    @Test
    @DisplayName("isSingleUpload()는 100MB 이상일 때 false를 반환해야 한다")
    void shouldReturnFalseForSingleUploadWhenSizeIs100MBOrMore() {
        // given
        long exactly100MB = 100 * 1024 * 1024; // 100MB
        FileSize fileSize = FileSize.of(exactly100MB);

        // when & then
        assertThat(fileSize.isSingleUpload()).isFalse();
    }

    @Test
    @DisplayName("isMultipartUpload()는 100MB 이상일 때 true를 반환해야 한다")
    void shouldReturnTrueForMultipartUploadWhenSizeIs100MBOrMore() {
        // given
        long exactly100MB = 100 * 1024 * 1024; // 100MB
        FileSize fileSize = FileSize.of(exactly100MB);

        // when & then
        assertThat(fileSize.isMultipartUpload()).isTrue();
    }

    @Test
    @DisplayName("isMultipartUpload()는 100MB 미만일 때 false를 반환해야 한다")
    void shouldReturnFalseForMultipartUploadWhenSizeIsLessThan100MB() {
        // given
        long lessThan100MB = 100 * 1024 * 1024 - 1; // 100MB - 1 byte
        FileSize fileSize = FileSize.of(lessThan100MB);

        // when & then
        assertThat(fileSize.isMultipartUpload()).isFalse();
    }
}

package com.ryuqq.fileflow.domain.upload.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileSize 테스트")
class FileSizeTest {

    @Test
    @DisplayName("바이트 단위로 FileSize를 생성한다")
    void createFileSizeWithBytes() {
        // given
        long bytes = 1024L;

        // when
        FileSize fileSize = FileSize.ofBytes(bytes);

        // then
        assertThat(fileSize.bytes()).isEqualTo(1024L);
    }

    @Test
    @DisplayName("KB 단위로 FileSize를 생성한다")
    void createFileSizeWithKilobytes() {
        // given
        long kilobytes = 10L;

        // when
        FileSize fileSize = FileSize.ofKilobytes(kilobytes);

        // then
        assertThat(fileSize.bytes()).isEqualTo(10 * 1024L);
    }

    @Test
    @DisplayName("MB 단위로 FileSize를 생성한다")
    void createFileSizeWithMegabytes() {
        // given
        long megabytes = 5L;

        // when
        FileSize fileSize = FileSize.ofMegabytes(megabytes);

        // then
        assertThat(fileSize.bytes()).isEqualTo(5 * 1024L * 1024L);
    }

    @Test
    @DisplayName("GB 단위로 FileSize를 생성한다")
    void createFileSizeWithGigabytes() {
        // given
        long gigabytes = 2L;

        // when
        FileSize fileSize = FileSize.ofGigabytes(gigabytes);

        // then
        assertThat(fileSize.bytes()).isEqualTo(2L * 1024L * 1024L * 1024L);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, -100, -1000, Long.MIN_VALUE})
    @DisplayName("음수 바이트로 생성 시 예외가 발생한다")
    void throwsExceptionWhenBytesIsNegative(long negativeBytes) {
        // when & then
        assertThatThrownBy(() -> FileSize.ofBytes(negativeBytes))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File size cannot be negative");
    }

    @Test
    @DisplayName("0 바이트로 생성 시 예외가 발생한다")
    void throwsExceptionWhenBytesIsZero() {
        // when & then
        assertThatThrownBy(() -> FileSize.ofBytes(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File size cannot be zero");
    }

    @Test
    @DisplayName("최소값(1 byte)으로 FileSize를 생성한다")
    void createFileSizeWithMinimumValue() {
        // when
        FileSize fileSize = FileSize.ofBytes(1);

        // then
        assertThat(fileSize.bytes()).isEqualTo(1L);
    }

    @Test
    @DisplayName("최대값(Long.MAX_VALUE)으로 FileSize를 생성한다")
    void createFileSizeWithMaximumValue() {
        // when
        FileSize fileSize = FileSize.ofBytes(Long.MAX_VALUE);

        // then
        assertThat(fileSize.bytes()).isEqualTo(Long.MAX_VALUE);
    }

    @ParameterizedTest
    @CsvSource({
        "500, '500 bytes'",
        "1024, '1.00 KB'",
        "1536, '1.50 KB'",
        "1048576, '1.00 MB'",
        "10485760, '10.00 MB'",
        "1073741824, '1.00 GB'",
        "5368709120, '5.00 GB'"
    })
    @DisplayName("사람이 읽기 쉬운 형식으로 변환한다")
    void convertToHumanReadableFormat(long bytes, String expected) {
        // given
        FileSize fileSize = FileSize.ofBytes(bytes);

        // when
        String humanReadable = fileSize.toHumanReadable();

        // then
        assertThat(humanReadable).isEqualTo(expected);
    }

    @Test
    @DisplayName("isGreaterThan 비교가 올바르게 동작한다")
    void isGreaterThanWorksCorrectly() {
        // given
        FileSize smaller = FileSize.ofBytes(100);
        FileSize larger = FileSize.ofBytes(200);

        // when & then
        assertThat(larger.isGreaterThan(smaller)).isTrue();
        assertThat(smaller.isGreaterThan(larger)).isFalse();
        assertThat(larger.isGreaterThan(larger)).isFalse();
    }

    @Test
    @DisplayName("isLessThan 비교가 올바르게 동작한다")
    void isLessThanWorksCorrectly() {
        // given
        FileSize smaller = FileSize.ofBytes(100);
        FileSize larger = FileSize.ofBytes(200);

        // when & then
        assertThat(smaller.isLessThan(larger)).isTrue();
        assertThat(larger.isLessThan(smaller)).isFalse();
        assertThat(smaller.isLessThan(smaller)).isFalse();
    }

    @Test
    @DisplayName("동일한 크기의 FileSize는 같다")
    void equalFileSizesAreEqual() {
        // given
        FileSize fileSize1 = FileSize.ofBytes(1024);
        FileSize fileSize2 = FileSize.ofBytes(1024);

        // when & then
        assertThat(fileSize1).isEqualTo(fileSize2);
        assertThat(fileSize1.hashCode()).isEqualTo(fileSize2.hashCode());
    }

    @Test
    @DisplayName("다른 크기의 FileSize는 다르다")
    void differentFileSizesAreNotEqual() {
        // given
        FileSize fileSize1 = FileSize.ofBytes(1024);
        FileSize fileSize2 = FileSize.ofBytes(2048);

        // when & then
        assertThat(fileSize1).isNotEqualTo(fileSize2);
    }

    @Test
    @DisplayName("단위 변환이 정확하다 - KB")
    void unitConversionIsAccurateForKB() {
        // given
        FileSize fromBytes = FileSize.ofBytes(10240);
        FileSize fromKB = FileSize.ofKilobytes(10);

        // when & then
        assertThat(fromBytes).isEqualTo(fromKB);
    }

    @Test
    @DisplayName("단위 변환이 정확하다 - MB")
    void unitConversionIsAccurateForMB() {
        // given
        FileSize fromBytes = FileSize.ofBytes(10485760);
        FileSize fromMB = FileSize.ofMegabytes(10);

        // when & then
        assertThat(fromBytes).isEqualTo(fromMB);
    }

    @Test
    @DisplayName("단위 변환이 정확하다 - GB")
    void unitConversionIsAccurateForGB() {
        // given
        FileSize fromBytes = FileSize.ofBytes(10737418240L);
        FileSize fromGB = FileSize.ofGigabytes(10);

        // when & then
        assertThat(fromBytes).isEqualTo(fromGB);
    }

    @Test
    @DisplayName("매우 큰 파일 크기를 처리한다")
    void handlesVeryLargeFileSizes() {
        // given
        long largeSize = 1099511627776L; // 1TB in bytes

        // when
        FileSize fileSize = FileSize.ofBytes(largeSize);

        // then
        assertThat(fileSize.bytes()).isEqualTo(largeSize);
        assertThat(fileSize.toHumanReadable()).contains("GB");
    }

    @Test
    @DisplayName("1 바이트의 사람이 읽기 쉬운 형식은 'bytes'이다")
    void singleByteShowsAsBytesFormat() {
        // given
        FileSize fileSize = FileSize.ofBytes(1);

        // when
        String humanReadable = fileSize.toHumanReadable();

        // then
        assertThat(humanReadable).isEqualTo("1 bytes");
    }

    @Test
    @DisplayName("복수 바이트(1000 미만)의 형식이 올바르다")
    void multipleBytesShowCorrectFormat() {
        // given
        FileSize fileSize = FileSize.ofBytes(999);

        // when
        String humanReadable = fileSize.toHumanReadable();

        // then
        assertThat(humanReadable).isEqualTo("999 bytes");
    }

    @Test
    @DisplayName("경계값 테스트 - 1KB 미만")
    void boundaryTestJustBelowKB() {
        // given
        FileSize fileSize = FileSize.ofBytes(1023);

        // when
        String humanReadable = fileSize.toHumanReadable();

        // then
        assertThat(humanReadable).isEqualTo("1023 bytes");
    }

    @Test
    @DisplayName("경계값 테스트 - 정확히 1KB")
    void boundaryTestExactlyKB() {
        // given
        FileSize fileSize = FileSize.ofBytes(1024);

        // when
        String humanReadable = fileSize.toHumanReadable();

        // then
        assertThat(humanReadable).isEqualTo("1.00 KB");
    }

    @Test
    @DisplayName("경계값 테스트 - 1MB 미만")
    void boundaryTestJustBelowMB() {
        // given
        FileSize fileSize = FileSize.ofBytes(1048575);

        // when
        String humanReadable = fileSize.toHumanReadable();

        // then
        assertThat(humanReadable).contains("KB");
    }

    @Test
    @DisplayName("경계값 테스트 - 정확히 1MB")
    void boundaryTestExactlyMB() {
        // given
        FileSize fileSize = FileSize.ofBytes(1048576);

        // when
        String humanReadable = fileSize.toHumanReadable();

        // then
        assertThat(humanReadable).isEqualTo("1.00 MB");
    }

    @Test
    @DisplayName("경계값 테스트 - 1GB 미만")
    void boundaryTestJustBelowGB() {
        // given
        FileSize fileSize = FileSize.ofBytes(1073741823);

        // when
        String humanReadable = fileSize.toHumanReadable();

        // then
        assertThat(humanReadable).contains("MB");
    }

    @Test
    @DisplayName("경계값 테스트 - 정확히 1GB")
    void boundaryTestExactlyGB() {
        // given
        FileSize fileSize = FileSize.ofBytes(1073741824);

        // when
        String humanReadable = fileSize.toHumanReadable();

        // then
        assertThat(humanReadable).isEqualTo("1.00 GB");
    }

    @Test
    @DisplayName("여러 FileSize 간의 비교가 올바르게 동작한다")
    void multipleFileSizeComparisonsWorkCorrectly() {
        // given
        FileSize size1 = FileSize.ofBytes(100);
        FileSize size2 = FileSize.ofKilobytes(1);
        FileSize size3 = FileSize.ofMegabytes(1);
        FileSize size4 = FileSize.ofGigabytes(1);

        // when & then
        assertThat(size1.isLessThan(size2)).isTrue();
        assertThat(size2.isLessThan(size3)).isTrue();
        assertThat(size3.isLessThan(size4)).isTrue();
        assertThat(size4.isGreaterThan(size3)).isTrue();
        assertThat(size3.isGreaterThan(size2)).isTrue();
        assertThat(size2.isGreaterThan(size1)).isTrue();
    }
}

package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.fileflow.domain.file.fixture.FileSizeFixture;

import static com.ryuqq.fileflow.domain.file.fixture.FileSizeFixture.ONE_GB_IN_BYTES;
import static com.ryuqq.fileflow.domain.file.fixture.FileSizeFixture.ONE_MB_IN_BYTES;
import static com.ryuqq.fileflow.domain.file.fixture.FileSizeFixture.exceedingMultipartLimit;
import static com.ryuqq.fileflow.domain.file.fixture.FileSizeFixture.exceedingSingleLimit;
import static com.ryuqq.fileflow.domain.file.fixture.FileSizeFixture.fiveGigabytes;
import static com.ryuqq.fileflow.domain.file.fixture.FileSizeFixture.fiveTerabytes;
import static com.ryuqq.fileflow.domain.file.fixture.FileSizeFixture.oneGigabyte;
import static com.ryuqq.fileflow.domain.file.fixture.FileSizeFixture.oneMegabyte;
import static com.ryuqq.fileflow.domain.file.fixture.FileSizeFixture.tenMegabytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileSize VO Tests")
class FileSizeTest {

    @Test
    @DisplayName("정상적인 파일 크기를 생성할 수 있어야 한다")
    void shouldCreateFileSize() {
        // when
        FileSize fileSize = oneMegabyte();

        // then
        assertThat(fileSize.bytes()).isEqualTo(ONE_MB_IN_BYTES);
    }

    @Test
    @DisplayName("0 또는 음수 크기는 허용되지 않는다")
    void shouldThrowExceptionWhenZeroOrNegative() {
        // expect
        assertThatThrownBy(() -> FileSize.of(0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FileSize.of(-10))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("업로드 타입별 최대 크기를 검증해야 한다")
    void shouldValidateForUploadType() {
        // given
        FileSize singleLimit = fiveGigabytes();
        FileSize multipartLimit = fiveTerabytes();

        // expect
        assertThatCode(() -> singleLimit.validateForUploadType(UploadType.SINGLE))
            .doesNotThrowAnyException();
        assertThatCode(() -> multipartLimit.validateForUploadType(UploadType.MULTIPART))
            .doesNotThrowAnyException();

        assertThatThrownBy(() -> exceedingSingleLimit()
            .validateForUploadType(UploadType.SINGLE))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> exceedingMultipartLimit()
            .validateForUploadType(UploadType.MULTIPART))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("isLargerThan 메서드는 기준값과 비교할 수 있어야 한다")
    void shouldCompareSizeCorrectly() {
        // given
        FileSize fileSize = tenMegabytes();

        // expect
        assertThat(fileSize.isLargerThan(5 * ONE_MB_IN_BYTES)).isTrue();
        assertThat(fileSize.isLargerThan(15 * ONE_MB_IN_BYTES)).isFalse();
    }

    @Test
    @DisplayName("파일 크기를 MB와 GB 단위로 변환할 수 있어야 한다")
    void shouldConvertToMBAndGB() {
        // given
        FileSize oneMb = oneMegabyte();
        FileSize oneGb = oneGigabyte();

        // expect
        assertThat(oneMb.toMB()).isEqualTo(1.0);
        assertThat(oneGb.toGB()).isEqualTo(1.0);
        assertThat(oneGb.toMB()).isEqualTo((double) ONE_GB_IN_BYTES / ONE_MB_IN_BYTES);
    }
}


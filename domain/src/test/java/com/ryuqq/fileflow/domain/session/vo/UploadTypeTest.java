package com.ryuqq.fileflow.domain.session.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.ryuqq.fileflow.domain.file.support.FileSizeUnit;
import com.ryuqq.fileflow.domain.file.vo.UploadType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UploadType Enum Tests")
class UploadTypeTest {

    @ParameterizedTest(name = "{0} should allow {1} bytes")
    @CsvSource({
        "SINGLE, 5, GB",
        "MULTIPART, 5, TB"
    })
    @DisplayName("업로드 타입별 최대 허용 크기를 반환해야 한다")
    void shouldReturnCorrectMaxSize(UploadType type, long size, String unit) {
        long expected = unit.equals("GB")
            ? FileSizeUnit.gigabytes(size)
            : FileSizeUnit.terabytes(size);

        assertThat(type.getMaxSize()).isEqualTo(expected);
        assertThat(type.getMaxSizeInBytes()).isEqualTo(expected);
    }
}


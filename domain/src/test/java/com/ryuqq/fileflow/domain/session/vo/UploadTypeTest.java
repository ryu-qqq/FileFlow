package com.ryuqq.fileflow.domain.session.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.fileflow.domain.file.support.FileSizeUnit;
import com.ryuqq.fileflow.domain.file.vo.UploadType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UploadType Enum Tests")
class UploadTypeTest {

    @Test
    @DisplayName("업로드 타입별 최대 허용 크기를 반환해야 한다")
    void shouldReturnCorrectMaxSize() {
        assertThat(UploadType.SINGLE.getMaxSize()).isEqualTo(FileSizeUnit.gigabytes(5));
        assertThat(UploadType.SINGLE.getMaxSizeInBytes()).isEqualTo(FileSizeUnit.gigabytes(5));

        assertThat(UploadType.MULTIPART.getMaxSize()).isEqualTo(FileSizeUnit.terabytes(5));
        assertThat(UploadType.MULTIPART.getMaxSizeInBytes()).isEqualTo(FileSizeUnit.terabytes(5));
    }
}


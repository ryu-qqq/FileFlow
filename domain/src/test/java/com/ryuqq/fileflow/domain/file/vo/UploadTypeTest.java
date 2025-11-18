package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UploadType Value Object 테스트
 */
class UploadTypeTest {

    @Test
    @DisplayName("100MB 미만 파일은 SINGLE 업로드 타입이어야 한다")
    void shouldBeSingleUploadWhenFileSizeLessThan100MB() {
        // given
        FileSize smallFile = FileSize.of(50 * 1024 * 1024L); // 50MB

        // when
        UploadType uploadType = UploadType.determineBySize(smallFile);

        // then
        assertThat(uploadType).isEqualTo(UploadType.SINGLE);
    }

    @Test
    @DisplayName("100MB 이상 파일은 MULTIPART 업로드 타입이어야 한다")
    void shouldBeMultipartUploadWhenFileSizeGreaterThanOrEqualTo100MB() {
        // given
        FileSize largeFile = FileSize.of(100 * 1024 * 1024L); // 100MB

        // when
        UploadType uploadType = UploadType.determineBySize(largeFile);

        // then
        assertThat(uploadType).isEqualTo(UploadType.MULTIPART);
    }

    @Test
    @DisplayName("경계값 99MB는 SINGLE 업로드 타입이어야 한다")
    void shouldBeSingleUploadWhenFileSizeIs99MB() {
        // given
        FileSize borderlineFile = FileSize.of(99 * 1024 * 1024L); // 99MB

        // when
        UploadType uploadType = UploadType.determineBySize(borderlineFile);

        // then
        assertThat(uploadType).isEqualTo(UploadType.SINGLE);
    }

    @Test
    @DisplayName("경계값 100MB는 MULTIPART 업로드 타입이어야 한다")
    void shouldBeMultipartUploadWhenFileSizeIs100MB() {
        // given
        FileSize exactThreshold = FileSize.of(100 * 1024 * 1024L); // 100MB

        // when
        UploadType uploadType = UploadType.determineBySize(exactThreshold);

        // then
        assertThat(uploadType).isEqualTo(UploadType.MULTIPART);
    }

    @Test
    @DisplayName("SINGLE 타입은 단일 업로드인지 확인할 수 있어야 한다")
    void shouldCheckIfSingleUpload() {
        // when & then
        assertThat(UploadType.SINGLE.isSingleUpload()).isTrue();
        assertThat(UploadType.MULTIPART.isSingleUpload()).isFalse();
    }

    @Test
    @DisplayName("MULTIPART 타입은 멀티파트 업로드인지 확인할 수 있어야 한다")
    void shouldCheckIfMultipartUpload() {
        // when & then
        assertThat(UploadType.MULTIPART.isMultipartUpload()).isTrue();
        assertThat(UploadType.SINGLE.isMultipartUpload()).isFalse();
    }
}

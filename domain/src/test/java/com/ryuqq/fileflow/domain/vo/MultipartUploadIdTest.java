package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MultipartUploadId Value Object 테스트
 */
class MultipartUploadIdTest {

    @Test
    @DisplayName("유효한 S3 Upload ID로 MultipartUploadId를 생성해야 한다")
    void shouldCreateValidMultipartUploadId() {
        // given
        String validUploadId = "examplevQpHp7eHc_J5s9U.kzM3GAHeOJh1P8wVTmRqEVojwiwu3wPX6fWYzADNtOHklJI6W";

        // when
        MultipartUploadId uploadId = MultipartUploadId.of(validUploadId);

        // then
        assertThat(uploadId).isNotNull();
        assertThat(uploadId.value()).isEqualTo(validUploadId);
    }

    @Test
    @DisplayName("null 또는 빈 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNullOrEmpty() {
        // when & then
        assertThatThrownBy(() -> MultipartUploadId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Multipart Upload ID는 null이거나 빈 값일 수 없습니다");

        assertThatThrownBy(() -> MultipartUploadId.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Multipart Upload ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("같은 값을 가진 MultipartUploadId는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String uploadIdValue = "examplevQpHp7eHc_J5s9U.kzM3GAHeOJh1P8wVTmRqEVojwiwu3wPX6fWYzADNtOHklJI6W";
        MultipartUploadId uploadId1 = MultipartUploadId.of(uploadIdValue);
        MultipartUploadId uploadId2 = MultipartUploadId.of(uploadIdValue);

        // when & then
        assertThat(uploadId1).isEqualTo(uploadId2);
    }
}

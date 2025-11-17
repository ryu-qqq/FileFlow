package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UploadedPart Value Object 테스트
 */
class UploadedPartTest {

    @Test
    @DisplayName("유효한 값으로 UploadedPart를 생성해야 한다")
    void shouldCreateValidUploadedPart() {
        // given
        int partNumber = 1;
        ETag etag = ETag.of("d41d8cd98f00b204e9800998ecf8427e");
        long size = 5 * 1024 * 1024L; // 5MB

        // when
        UploadedPart part = UploadedPart.of(partNumber, etag, size);

        // then
        assertThat(part).isNotNull();
        assertThat(part.partNumber()).isEqualTo(partNumber);
        assertThat(part.etag()).isEqualTo(etag);
        assertThat(part.size()).isEqualTo(size);
    }

    @Test
    @DisplayName("partNumber가 1 미만이면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenPartNumberLessThan1() {
        // given
        int invalidPartNumber = 0;
        ETag etag = ETag.of("d41d8cd98f00b204e9800998ecf8427e");
        long size = 5242880L;

        // when & then
        assertThatThrownBy(() -> UploadedPart.of(invalidPartNumber, etag, size))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파트 번호는 1 이상이어야 합니다");
    }

    @Test
    @DisplayName("size가 0 이하이면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenSizeIsZeroOrNegative() {
        // given
        int partNumber = 1;
        ETag etag = ETag.of("d41d8cd98f00b204e9800998ecf8427e");
        long invalidSize = 0L;

        // when & then
        assertThatThrownBy(() -> UploadedPart.of(partNumber, etag, invalidSize))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파트 크기는 0보다 커야 합니다");
    }

    @Test
    @DisplayName("etag가 null이면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenEtagIsNull() {
        // given
        int partNumber = 1;
        ETag nullEtag = null;
        long size = 5242880L;

        // when & then
        assertThatThrownBy(() -> UploadedPart.of(partNumber, nullEtag, size))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ETag는 null일 수 없습니다");
    }

    @Test
    @DisplayName("같은 값을 가진 UploadedPart는 동등해야 한다")
    void shouldBeEqualWhenValuesAreSame() {
        // given
        int partNumber = 1;
        ETag etag = ETag.of("d41d8cd98f00b204e9800998ecf8427e");
        long size = 5242880L;

        UploadedPart part1 = UploadedPart.of(partNumber, etag, size);
        UploadedPart part2 = UploadedPart.of(partNumber, etag, size);

        // when & then
        assertThat(part1).isEqualTo(part2);
    }
}

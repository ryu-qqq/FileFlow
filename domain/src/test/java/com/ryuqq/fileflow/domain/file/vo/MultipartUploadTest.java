package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MultipartUpload Value Object 테스트
 */
class MultipartUploadTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2025-01-15T12:00:00Z"),
            ZoneId.systemDefault()
    );

    @Test
    @DisplayName("새로운 MultipartUpload를 생성해야 한다")
    void shouldCreateNewMultipartUpload() {
        // given
        MultipartUploadId uploadId = MultipartUploadId.of("test-upload-id");
        int totalParts = 5;

        // when
        MultipartUpload upload = MultipartUpload.forNew(uploadId, totalParts, FIXED_CLOCK);

        // then
        assertThat(upload).isNotNull();
        assertThat(upload.uploadId()).isEqualTo(uploadId);
        assertThat(upload.totalParts()).isEqualTo(totalParts);
        assertThat(upload.status()).isEqualTo(MultipartStatus.INITIATED);
        assertThat(upload.uploadedParts()).isEmpty();
        assertThat(upload.initiatedAt()).isNotNull();
        assertThat(upload.completedAt()).isNull();
        assertThat(upload.abortedAt()).isNull();
    }

    @Test
    @DisplayName("파트를 추가하면 상태가 IN_PROGRESS로 변경되어야 한다")
    void shouldAddPartAndChangeStatusToInProgress() {
        // given
        MultipartUploadId uploadId = MultipartUploadId.of("test-upload-id");
        MultipartUpload upload = MultipartUpload.forNew(uploadId, 3, FIXED_CLOCK);
        UploadedPart part1 = UploadedPart.of(1, ETag.of("etag1"), 5242880L);

        // when
        MultipartUpload updated = upload.withAddedPart(part1);

        // then
        assertThat(updated.status()).isEqualTo(MultipartStatus.IN_PROGRESS);
        assertThat(updated.uploadedParts()).hasSize(1);
        assertThat(updated.uploadedParts().get(0)).isEqualTo(part1);
    }

    @Test
    @DisplayName("모든 파트가 업로드되었는지 확인할 수 있어야 한다")
    void shouldCheckIfAllPartsUploaded() {
        // given
        MultipartUploadId uploadId = MultipartUploadId.of("test-upload-id");
        MultipartUpload upload = MultipartUpload.forNew(uploadId, 2, FIXED_CLOCK);

        UploadedPart part1 = UploadedPart.of(1, ETag.of("etag1"), 5242880L);
        UploadedPart part2 = UploadedPart.of(2, ETag.of("etag2"), 5242880L);

        // when
        MultipartUpload withPart1 = upload.withAddedPart(part1);
        MultipartUpload withAllParts = withPart1.withAddedPart(part2);

        // then
        assertThat(withPart1.isAllPartsUploaded()).isFalse();
        assertThat(withAllParts.isAllPartsUploaded()).isTrue();
    }

    @Test
    @DisplayName("완료 표시 시 COMPLETED 상태가 되고 completedAt이 설정되어야 한다")
    void shouldMarkAsCompleted() {
        // given
        MultipartUploadId uploadId = MultipartUploadId.of("test-upload-id");
        MultipartUpload upload = MultipartUpload.forNew(uploadId, 1, FIXED_CLOCK)
                .withAddedPart(UploadedPart.of(1, ETag.of("etag1"), 5242880L));

        // when
        MultipartUpload completed = upload.markAsCompleted(FIXED_CLOCK);

        // then
        assertThat(completed.status()).isEqualTo(MultipartStatus.COMPLETED);
        assertThat(completed.completedAt()).isNotNull();
        assertThat(completed.abortedAt()).isNull();
    }

    @Test
    @DisplayName("중단 표시 시 ABORTED 상태가 되고 abortedAt이 설정되어야 한다")
    void shouldMarkAsAborted() {
        // given
        MultipartUploadId uploadId = MultipartUploadId.of("test-upload-id");
        MultipartUpload upload = MultipartUpload.forNew(uploadId, 3, FIXED_CLOCK);

        // when
        MultipartUpload aborted = upload.markAsAborted(FIXED_CLOCK);

        // then
        assertThat(aborted.status()).isEqualTo(MultipartStatus.ABORTED);
        assertThat(aborted.abortedAt()).isNotNull();
        assertThat(aborted.completedAt()).isNull();
    }

    @Test
    @DisplayName("totalParts가 1 미만이면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenTotalPartsLessThan1() {
        // given
        MultipartUploadId uploadId = MultipartUploadId.of("test-upload-id");
        int invalidTotalParts = 0;

        // when & then
        assertThatThrownBy(() -> MultipartUpload.forNew(uploadId, invalidTotalParts, FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("전체 파트 수는 1 이상이어야 합니다");
    }

    @Test
    @DisplayName("동일한 파트 번호를 중복 추가하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenAddingDuplicatePartNumber() {
        // given
        MultipartUploadId uploadId = MultipartUploadId.of("test-upload-id");
        MultipartUpload upload = MultipartUpload.forNew(uploadId, 2, FIXED_CLOCK);
        UploadedPart part1 = UploadedPart.of(1, ETag.of("etag1"), 5242880L);
        UploadedPart duplicatePart1 = UploadedPart.of(1, ETag.of("etag2"), 5242880L);

        // when
        MultipartUpload withPart = upload.withAddedPart(part1);

        // then
        assertThatThrownBy(() -> withPart.withAddedPart(duplicatePart1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 업로드된 파트 번호입니다");
    }

    @Test
    @DisplayName("totalParts를 초과하는 파트를 추가하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenExceedingTotalParts() {
        // given
        MultipartUploadId uploadId = MultipartUploadId.of("test-upload-id");
        MultipartUpload upload = MultipartUpload.forNew(uploadId, 1, FIXED_CLOCK)
                .withAddedPart(UploadedPart.of(1, ETag.of("etag1"), 5242880L));

        UploadedPart extraPart = UploadedPart.of(2, ETag.of("etag2"), 5242880L);

        // when & then
        assertThatThrownBy(() -> upload.withAddedPart(extraPart))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("전체 파트 수를 초과했습니다");
    }
}

package com.ryuqq.fileflow.domain.image.model;

import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ImageAsset 테스트")
class ImageAssetTest {

    private static final String FILE_ID = "test-file-id";
    private static final String S3_URI = "s3://bucket/test-image.jpg";
    private static final ImageFormat FORMAT = ImageFormat.JPEG;
    private static final ImageDimension DIMENSION = ImageDimension.of(1920, 1080);
    private static final long FILE_SIZE = 1024L * 1024L; // 1MB

    @Test
    @DisplayName("ImageAsset을 생성할 수 있다")
    void create_Success() {
        // given & when
        ImageAsset imageAsset = ImageAsset.create(
                FILE_ID,
                S3_URI,
                FORMAT,
                DIMENSION,
                FILE_SIZE
        );

        // then
        assertThat(imageAsset.getImageId()).isNotNull();
        assertThat(imageAsset.getFileId()).isEqualTo(FILE_ID);
        assertThat(imageAsset.getS3Uri()).isEqualTo(S3_URI);
        assertThat(imageAsset.getFormat()).isEqualTo(FORMAT);
        assertThat(imageAsset.getDimension()).isEqualTo(DIMENSION);
        assertThat(imageAsset.getFileSizeBytes()).isEqualTo(FILE_SIZE);
        assertThat(imageAsset.getOptimizationStatus()).isEqualTo(ImageOptimizationStatus.PENDING);
        assertThat(imageAsset.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("fileId가 null이면 예외가 발생한다")
    void create_NullFileId_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> ImageAsset.create(
                null,
                S3_URI,
                FORMAT,
                DIMENSION,
                FILE_SIZE
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileId cannot be null");
    }

    @Test
    @DisplayName("s3Uri가 잘못된 형식이면 예외가 발생한다")
    void create_InvalidS3Uri_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> ImageAsset.create(
                FILE_ID,
                "http://bucket/test-image.jpg",
                FORMAT,
                DIMENSION,
                FILE_SIZE
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must start with 's3://'");
    }

    @Test
    @DisplayName("파일 크기가 0 이하이면 예외가 발생한다")
    void create_InvalidFileSize_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> ImageAsset.create(
                FILE_ID,
                S3_URI,
                FORMAT,
                DIMENSION,
                0L
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size must be positive");
    }

    @Test
    @DisplayName("최적화를 시작할 수 있다")
    void startOptimization_Success() {
        // given
        ImageAsset imageAsset = ImageAsset.create(
                FILE_ID, S3_URI, FORMAT, DIMENSION, FILE_SIZE
        );

        // when
        imageAsset.startOptimization();

        // then
        assertThat(imageAsset.getOptimizationStatus()).isEqualTo(ImageOptimizationStatus.IN_PROGRESS);
        assertThat(imageAsset.isOptimizing()).isTrue();
    }

    @Test
    @DisplayName("이미 최적화가 완료된 경우 시작할 수 없다")
    void startOptimization_AlreadyCompleted_ThrowsException() {
        // given
        Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        ImageAsset imageAsset = ImageAsset.create(
                FILE_ID, S3_URI, FORMAT, DIMENSION, FILE_SIZE, fixedClock
        );
        imageAsset.startOptimization();
        imageAsset.completeOptimization("s3://bucket/optimized.webp", fixedClock);

        // when & then
        assertThatThrownBy(imageAsset::startOptimization)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already optimized");
    }

    @Test
    @DisplayName("최적화를 완료할 수 있다")
    void completeOptimization_Success() {
        // given
        Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        ImageAsset imageAsset = ImageAsset.create(
                FILE_ID, S3_URI, FORMAT, DIMENSION, FILE_SIZE, fixedClock
        );
        imageAsset.startOptimization();
        String optimizedUri = "s3://bucket/optimized.webp";

        // when
        imageAsset.completeOptimization(optimizedUri, fixedClock);

        // then
        assertThat(imageAsset.getOptimizationStatus()).isEqualTo(ImageOptimizationStatus.COMPLETED);
        assertThat(imageAsset.getOptimizedS3Uri()).isEqualTo(optimizedUri);
        assertThat(imageAsset.getOptimizedAt()).isNotNull();
        assertThat(imageAsset.isOptimized()).isTrue();
    }

    @Test
    @DisplayName("최적화 진행 중이 아니면 완료할 수 없다")
    void completeOptimization_NotInProgress_ThrowsException() {
        // given
        Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        ImageAsset imageAsset = ImageAsset.create(
                FILE_ID, S3_URI, FORMAT, DIMENSION, FILE_SIZE, fixedClock
        );

        // when & then
        assertThatThrownBy(() -> imageAsset.completeOptimization("s3://bucket/optimized.webp", fixedClock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in progress");
    }

    @Test
    @DisplayName("최적화를 실패 처리할 수 있다")
    void failOptimization_Success() {
        // given
        ImageAsset imageAsset = ImageAsset.create(
                FILE_ID, S3_URI, FORMAT, DIMENSION, FILE_SIZE
        );
        imageAsset.startOptimization();

        // when
        imageAsset.failOptimization();

        // then
        assertThat(imageAsset.getOptimizationStatus()).isEqualTo(ImageOptimizationStatus.FAILED);
    }

    @Test
    @DisplayName("최적화가 필요한지 확인할 수 있다")
    void needsOptimization() {
        // given
        Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        ImageAsset pending = ImageAsset.create(
                FILE_ID, S3_URI, FORMAT, DIMENSION, FILE_SIZE, fixedClock
        );
        ImageAsset completed = ImageAsset.create(
                FILE_ID, S3_URI, FORMAT, DIMENSION, FILE_SIZE, fixedClock
        );
        completed.startOptimization();
        completed.completeOptimization("s3://bucket/optimized.webp", fixedClock);

        // when & then
        assertThat(pending.needsOptimization()).isTrue();
        assertThat(completed.needsOptimization()).isFalse();
    }

    @Test
    @DisplayName("WebP 포맷인지 확인할 수 있다")
    void isWebP() {
        // given
        ImageAsset jpegImage = ImageAsset.create(
                FILE_ID, "s3://bucket/test.jpg", ImageFormat.JPEG, DIMENSION, FILE_SIZE
        );
        ImageAsset webpImage = ImageAsset.create(
                FILE_ID, "s3://bucket/test.webp", ImageFormat.WEBP, DIMENSION, FILE_SIZE
        );

        // when & then
        assertThat(jpegImage.isWebP()).isFalse();
        assertThat(webpImage.isWebP()).isTrue();
    }

    @Test
    @DisplayName("WebP로 변환 가능한지 확인할 수 있다")
    void isConvertibleToWebP() {
        // given
        ImageAsset jpegImage = ImageAsset.create(
                FILE_ID, "s3://bucket/test.jpg", ImageFormat.JPEG, DIMENSION, FILE_SIZE
        );
        ImageAsset gifImage = ImageAsset.create(
                FILE_ID, "s3://bucket/test.gif", ImageFormat.GIF, DIMENSION, FILE_SIZE
        );

        // when & then
        assertThat(jpegImage.isConvertibleToWebP()).isTrue();
        assertThat(gifImage.isConvertibleToWebP()).isFalse();
    }

    @Test
    @DisplayName("크기 제한을 초과하는지 확인할 수 있다")
    void exceedsSize() {
        // given
        ImageAsset imageAsset = ImageAsset.create(
                FILE_ID, S3_URI, FORMAT, DIMENSION, FILE_SIZE
        );

        // when & then
        assertThat(imageAsset.exceedsSize(512L * 1024L)).isTrue();
        assertThat(imageAsset.exceedsSize(2L * 1024L * 1024L)).isFalse();
    }

    @Test
    @DisplayName("사람이 읽기 쉬운 파일 크기를 반환한다")
    void getHumanReadableSize() {
        // given
        ImageAsset imageAsset = ImageAsset.create(
                FILE_ID, S3_URI, FORMAT, DIMENSION, FILE_SIZE
        );

        // when
        String humanReadableSize = imageAsset.getHumanReadableSize();

        // then
        assertThat(humanReadableSize).contains("MB");
    }

    @Test
    @DisplayName("동일한 imageId를 가진 ImageAsset은 동등하다")
    void equals_SameImageId_ReturnsTrue() {
        // given
        ImageAsset asset1 = ImageAsset.reconstitute(
                "same-id", FILE_ID, S3_URI, FORMAT, DIMENSION, FILE_SIZE,
                LocalDateTime.now(), ImageOptimizationStatus.PENDING, null, null
        );
        ImageAsset asset2 = ImageAsset.reconstitute(
                "same-id", "different-file-id", S3_URI, FORMAT, DIMENSION, FILE_SIZE,
                LocalDateTime.now(), ImageOptimizationStatus.PENDING, null, null
        );

        // when & then
        assertThat(asset1).isEqualTo(asset2);
        assertThat(asset1.hashCode()).isEqualTo(asset2.hashCode());
    }
}

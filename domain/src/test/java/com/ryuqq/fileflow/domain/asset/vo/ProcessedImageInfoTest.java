package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ProcessedImageInfo 단위 테스트")
class ProcessedImageInfoTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 ProcessedImageInfo를 생성할 수 있다")
        void shouldCreateProcessedImageInfoWithValidData() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);
            long fileSize = 524288L;

            // when
            ProcessedImageInfo info = ProcessedImageInfo.of(spec, fileSize);

            // then
            assertThat(info.spec()).isEqualTo(spec);
            assertThat(info.fileSize()).isEqualTo(fileSize);
        }

        @Test
        @DisplayName("spec이 null이면 예외가 발생한다")
        void shouldThrowWhenSpecIsNull() {
            // given
            ImageResizingSpec nullSpec = null;
            long fileSize = 524288L;

            // when & then
            assertThatThrownBy(() -> ProcessedImageInfo.of(nullSpec, fileSize))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("fileSize가 0이면 예외가 발생한다")
        void shouldThrowWhenFileSizeIsZero() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);
            long invalidFileSize = 0L;

            // when & then
            assertThatThrownBy(() -> ProcessedImageInfo.of(spec, invalidFileSize))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일 크기");
        }

        @Test
        @DisplayName("fileSize가 음수이면 예외가 발생한다")
        void shouldThrowWhenFileSizeIsNegative() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);
            long negativeFileSize = -1000L;

            // when & then
            assertThatThrownBy(() -> ProcessedImageInfo.of(spec, negativeFileSize))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("파일 크기");
        }
    }

    @Nested
    @DisplayName("infoId 테스트")
    class InfoIdTest {

        @Test
        @DisplayName("infoId는 spec의 specId를 반환한다")
        void shouldReturnSpecIdAsInfoId() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);
            ProcessedImageInfo info = ProcessedImageInfo.of(spec, 524288L);

            // when
            String infoId = info.infoId();

            // then
            assertThat(infoId).isEqualTo("LARGE_WEBP");
            assertThat(infoId).isEqualTo(spec.specId());
        }

        @Test
        @DisplayName("THUMBNAIL + JPEG 조합의 infoId를 반환한다")
        void shouldReturnInfoIdForThumbnailJpeg() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.THUMBNAIL, ImageFormat.JPEG);
            ProcessedImageInfo info = ProcessedImageInfo.of(spec, 10240L);

            // when
            String infoId = info.infoId();

            // then
            assertThat(infoId).isEqualTo("THUMBNAIL_JPEG");
        }
    }

    @Nested
    @DisplayName("위임 메서드 테스트")
    class DelegationTest {

        @Test
        @DisplayName("variant()는 spec의 variant를 반환한다")
        void shouldReturnVariantFromSpec() {
            // given
            ImageVariant expectedVariant = ImageVariant.MEDIUM;
            ImageResizingSpec spec = ImageResizingSpec.of(expectedVariant, ImageFormat.WEBP);
            ProcessedImageInfo info = ProcessedImageInfo.of(spec, 262144L);

            // when
            ImageVariant variant = info.variant();

            // then
            assertThat(variant).isEqualTo(expectedVariant);
        }

        @Test
        @DisplayName("format()은 spec의 format을 반환한다")
        void shouldReturnFormatFromSpec() {
            // given
            ImageFormat expectedFormat = ImageFormat.JPEG;
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, expectedFormat);
            ProcessedImageInfo info = ProcessedImageInfo.of(spec, 524288L);

            // when
            ImageFormat format = info.format();

            // then
            assertThat(format).isEqualTo(expectedFormat);
        }

        @Test
        @DisplayName("fileSizeKb()는 파일 크기를 KB로 반환한다")
        void shouldReturnFileSizeInKb() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);
            long fileSize = 1024L * 100; // 100KB
            ProcessedImageInfo info = ProcessedImageInfo.of(spec, fileSize);

            // when
            double fileSizeKb = info.fileSizeKb();

            // then
            assertThat(fileSizeKb).isEqualTo(100.0);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 spec, fileSize를 가진 info는 동등하다")
        void shouldBeEqualWithSameValues() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);
            ProcessedImageInfo info1 = ProcessedImageInfo.of(spec, 524288L);
            ProcessedImageInfo info2 = ProcessedImageInfo.of(spec, 524288L);

            // when & then
            assertThat(info1).isEqualTo(info2);
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("다른 spec을 가진 info는 동등하지 않다")
        void shouldNotBeEqualWithDifferentSpec() {
            // given
            ImageResizingSpec spec1 = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);
            ImageResizingSpec spec2 = ImageResizingSpec.of(ImageVariant.MEDIUM, ImageFormat.WEBP);
            ProcessedImageInfo info1 = ProcessedImageInfo.of(spec1, 524288L);
            ProcessedImageInfo info2 = ProcessedImageInfo.of(spec2, 524288L);

            // when & then
            assertThat(info1).isNotEqualTo(info2);
        }

        @Test
        @DisplayName("다른 fileSize를 가진 info는 동등하지 않다")
        void shouldNotBeEqualWithDifferentFileSize() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.LARGE, ImageFormat.WEBP);
            ProcessedImageInfo info1 = ProcessedImageInfo.of(spec, 524288L);
            ProcessedImageInfo info2 = ProcessedImageInfo.of(spec, 1048576L);

            // when & then
            assertThat(info1).isNotEqualTo(info2);
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTest {

        @Test
        @DisplayName("최소 유효 값으로 생성할 수 있다")
        void shouldCreateWithMinimumValidValues() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.THUMBNAIL, ImageFormat.JPEG);
            long minFileSize = 1L;

            // when
            ProcessedImageInfo info = ProcessedImageInfo.of(spec, minFileSize);

            // then
            assertThat(info.fileSize()).isEqualTo(minFileSize);
        }

        @Test
        @DisplayName("큰 파일 크기로 생성할 수 있다")
        void shouldCreateWithLargeFileSize() {
            // given
            ImageResizingSpec spec = ImageResizingSpec.of(ImageVariant.ORIGINAL, ImageFormat.WEBP);
            long largeFileSize = 10L * 1024 * 1024 * 1024; // 10GB

            // when
            ProcessedImageInfo info = ProcessedImageInfo.of(spec, largeFileSize);

            // then
            assertThat(info.fileSize()).isEqualTo(largeFileSize);
        }
    }
}

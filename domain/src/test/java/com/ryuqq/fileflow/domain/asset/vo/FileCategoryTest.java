package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileCategory 단위 테스트")
class FileCategoryTest {

    @Nested
    @DisplayName("Enum 기본 테스트")
    class EnumBasicTest {

        @Test
        @DisplayName("모든 카테고리가 정의되어 있다")
        void values_ShouldContainAllCategories() {
            // when
            FileCategory[] values = FileCategory.values();

            // then
            assertThat(values).hasSize(5);
            assertThat(values)
                    .containsExactly(
                            FileCategory.IMAGE,
                            FileCategory.VIDEO,
                            FileCategory.DOCUMENT,
                            FileCategory.AUDIO,
                            FileCategory.OTHER);
        }

        @Test
        @DisplayName("문자열로 카테고리를 찾을 수 있다")
        void valueOf_WithValidName_ShouldReturnCategory() {
            // when & then
            assertThat(FileCategory.valueOf("IMAGE")).isEqualTo(FileCategory.IMAGE);
            assertThat(FileCategory.valueOf("VIDEO")).isEqualTo(FileCategory.VIDEO);
            assertThat(FileCategory.valueOf("DOCUMENT")).isEqualTo(FileCategory.DOCUMENT);
            assertThat(FileCategory.valueOf("AUDIO")).isEqualTo(FileCategory.AUDIO);
            assertThat(FileCategory.valueOf("OTHER")).isEqualTo(FileCategory.OTHER);
        }
    }

    @Nested
    @DisplayName("카테고리 의미 테스트")
    class CategoryMeaningTest {

        @Test
        @DisplayName("IMAGE는 이미지 파일 카테고리이다")
        void image_ShouldRepresentImageFiles() {
            // given
            FileCategory category = FileCategory.IMAGE;

            // then
            assertThat(category.name()).isEqualTo("IMAGE");
            assertThat(category.ordinal()).isEqualTo(0);
        }

        @Test
        @DisplayName("VIDEO는 비디오 파일 카테고리이다")
        void video_ShouldRepresentVideoFiles() {
            // given
            FileCategory category = FileCategory.VIDEO;

            // then
            assertThat(category.name()).isEqualTo("VIDEO");
            assertThat(category.ordinal()).isEqualTo(1);
        }

        @Test
        @DisplayName("DOCUMENT는 문서 파일 카테고리이다")
        void document_ShouldRepresentDocumentFiles() {
            // given
            FileCategory category = FileCategory.DOCUMENT;

            // then
            assertThat(category.name()).isEqualTo("DOCUMENT");
            assertThat(category.ordinal()).isEqualTo(2);
        }

        @Test
        @DisplayName("AUDIO는 오디오 파일 카테고리이다")
        void audio_ShouldRepresentAudioFiles() {
            // given
            FileCategory category = FileCategory.AUDIO;

            // then
            assertThat(category.name()).isEqualTo("AUDIO");
            assertThat(category.ordinal()).isEqualTo(3);
        }

        @Test
        @DisplayName("OTHER는 기타 파일 카테고리이다")
        void other_ShouldRepresentOtherFiles() {
            // given
            FileCategory category = FileCategory.OTHER;

            // then
            assertThat(category.name()).isEqualTo("OTHER");
            assertThat(category.ordinal()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("fromMimeType 테스트")
    class FromMimeTypeTest {

        @Test
        @DisplayName("이미지 MIME 타입은 IMAGE로 분류된다")
        void fromMimeType_WithImageType_ShouldReturnImage() {
            // given & when & then
            assertThat(FileCategory.fromMimeType("image/jpeg")).isEqualTo(FileCategory.IMAGE);
            assertThat(FileCategory.fromMimeType("image/png")).isEqualTo(FileCategory.IMAGE);
            assertThat(FileCategory.fromMimeType("image/gif")).isEqualTo(FileCategory.IMAGE);
            assertThat(FileCategory.fromMimeType("image/webp")).isEqualTo(FileCategory.IMAGE);
            assertThat(FileCategory.fromMimeType("IMAGE/JPEG")).isEqualTo(FileCategory.IMAGE);
        }

        @Test
        @DisplayName("비디오 MIME 타입은 VIDEO로 분류된다")
        void fromMimeType_WithVideoType_ShouldReturnVideo() {
            // given & when & then
            assertThat(FileCategory.fromMimeType("video/mp4")).isEqualTo(FileCategory.VIDEO);
            assertThat(FileCategory.fromMimeType("video/mpeg")).isEqualTo(FileCategory.VIDEO);
            assertThat(FileCategory.fromMimeType("video/quicktime")).isEqualTo(FileCategory.VIDEO);
            assertThat(FileCategory.fromMimeType("VIDEO/MP4")).isEqualTo(FileCategory.VIDEO);
        }

        @Test
        @DisplayName("오디오 MIME 타입은 AUDIO로 분류된다")
        void fromMimeType_WithAudioType_ShouldReturnAudio() {
            // given & when & then
            assertThat(FileCategory.fromMimeType("audio/mpeg")).isEqualTo(FileCategory.AUDIO);
            assertThat(FileCategory.fromMimeType("audio/wav")).isEqualTo(FileCategory.AUDIO);
            assertThat(FileCategory.fromMimeType("audio/ogg")).isEqualTo(FileCategory.AUDIO);
            assertThat(FileCategory.fromMimeType("AUDIO/MPEG")).isEqualTo(FileCategory.AUDIO);
        }

        @Test
        @DisplayName("문서 MIME 타입은 DOCUMENT로 분류된다")
        void fromMimeType_WithDocumentType_ShouldReturnDocument() {
            // given & when & then
            assertThat(FileCategory.fromMimeType("application/pdf"))
                    .isEqualTo(FileCategory.DOCUMENT);
            assertThat(FileCategory.fromMimeType("application/msword"))
                    .isEqualTo(FileCategory.DOCUMENT);
            assertThat(
                            FileCategory.fromMimeType(
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .isEqualTo(FileCategory.DOCUMENT);
            assertThat(FileCategory.fromMimeType("application/vnd.ms-excel"))
                    .isEqualTo(FileCategory.DOCUMENT);
            assertThat(FileCategory.fromMimeType("application/vnd.ms-powerpoint"))
                    .isEqualTo(FileCategory.DOCUMENT);
            assertThat(FileCategory.fromMimeType("text/plain")).isEqualTo(FileCategory.DOCUMENT);
            assertThat(FileCategory.fromMimeType("text/csv")).isEqualTo(FileCategory.DOCUMENT);
        }

        @Test
        @DisplayName("알 수 없는 MIME 타입은 OTHER로 분류된다")
        void fromMimeType_WithUnknownType_ShouldReturnOther() {
            // given & when & then
            assertThat(FileCategory.fromMimeType("application/octet-stream"))
                    .isEqualTo(FileCategory.OTHER);
            assertThat(FileCategory.fromMimeType("application/zip")).isEqualTo(FileCategory.OTHER);
            assertThat(FileCategory.fromMimeType("unknown/type")).isEqualTo(FileCategory.OTHER);
        }

        @Test
        @DisplayName("null MIME 타입은 OTHER로 분류된다")
        void fromMimeType_WithNull_ShouldReturnOther() {
            // given & when & then
            assertThat(FileCategory.fromMimeType(null)).isEqualTo(FileCategory.OTHER);
        }

        @Test
        @DisplayName("빈 문자열 MIME 타입은 OTHER로 분류된다")
        void fromMimeType_WithBlank_ShouldReturnOther() {
            // given & when & then
            assertThat(FileCategory.fromMimeType("")).isEqualTo(FileCategory.OTHER);
            assertThat(FileCategory.fromMimeType("   ")).isEqualTo(FileCategory.OTHER);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 카테고리는 동등하다")
        void equals_WithSameCategory_ShouldBeEqual() {
            // given
            FileCategory category1 = FileCategory.IMAGE;
            FileCategory category2 = FileCategory.IMAGE;

            // when & then
            assertThat(category1).isEqualTo(category2);
            assertThat(category1 == category2).isTrue();
        }

        @Test
        @DisplayName("다른 카테고리는 동등하지 않다")
        void equals_WithDifferentCategory_ShouldNotBeEqual() {
            // given
            FileCategory category1 = FileCategory.IMAGE;
            FileCategory category2 = FileCategory.VIDEO;

            // when & then
            assertThat(category1).isNotEqualTo(category2);
        }
    }
}

package com.ryuqq.fileflow.domain.asset.service;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormatType;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariantType;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.UploadCategory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ImageProcessingPolicy Domain Service 단위 테스트.
 *
 * <p>Cycle 7: shouldProcess 메서드 테스트
 */
@DisplayName("ImageProcessingPolicy Domain Service 단위 테스트")
class ImageProcessingPolicyTest {

    private final ImageProcessingPolicy policy = new ImageProcessingPolicy();

    @Nested
    @DisplayName("shouldProcess(ContentType) 테스트")
    class ShouldProcessByContentTypeTest {

        @Test
        @DisplayName("이미지 Content-Type이면 true를 반환한다")
        void shouldReturnTrueForImageContentType() {
            // given
            ContentType jpegType = ContentType.of("image/jpeg");
            ContentType pngType = ContentType.of("image/png");
            ContentType webpType = ContentType.of("image/webp");

            // when & then
            assertThat(policy.shouldProcess(jpegType)).isTrue();
            assertThat(policy.shouldProcess(pngType)).isTrue();
            assertThat(policy.shouldProcess(webpType)).isTrue();
        }

        @Test
        @DisplayName("비이미지 Content-Type이면 false를 반환한다")
        void shouldReturnFalseForNonImageContentType() {
            // given
            ContentType pdfType = ContentType.of("application/pdf");
            ContentType videoType = ContentType.of("video/mp4");

            // when & then
            assertThat(policy.shouldProcess(pdfType)).isFalse();
            assertThat(policy.shouldProcess(videoType)).isFalse();
        }
    }

    @Nested
    @DisplayName("shouldProcess(UploadCategory) 테스트")
    class ShouldProcessByUploadCategoryTest {

        @Test
        @DisplayName("BANNER 카테고리면 true를 반환한다")
        void shouldReturnTrueForBannerCategory() {
            // given
            UploadCategory category = UploadCategory.BANNER;

            // when & then
            assertThat(policy.shouldProcess(category)).isTrue();
        }

        @Test
        @DisplayName("PRODUCT_IMAGE 카테고리면 true를 반환한다")
        void shouldReturnTrueForProductImageCategory() {
            // given
            UploadCategory category = UploadCategory.PRODUCT_IMAGE;

            // when & then
            assertThat(policy.shouldProcess(category)).isTrue();
        }

        @Test
        @DisplayName("HTML 카테고리면 true를 반환한다")
        void shouldReturnTrueForHtmlCategory() {
            // given
            UploadCategory category = UploadCategory.HTML;

            // when & then
            assertThat(policy.shouldProcess(category)).isTrue();
        }

        @Test
        @DisplayName("EXCEL 카테고리면 false를 반환한다")
        void shouldReturnFalseForExcelCategory() {
            // given
            UploadCategory category = UploadCategory.EXCEL;

            // when & then
            assertThat(policy.shouldProcess(category)).isFalse();
        }

        @Test
        @DisplayName("DOCUMENT 카테고리면 false를 반환한다")
        void shouldReturnFalseForDocumentCategory() {
            // given
            UploadCategory category = UploadCategory.DOCUMENT;

            // when & then
            assertThat(policy.shouldProcess(category)).isFalse();
        }

        @Test
        @DisplayName("SALES_MATERIAL 카테고리면 false를 반환한다")
        void shouldReturnFalseForSalesMaterialCategory() {
            // given
            UploadCategory category = UploadCategory.SALES_MATERIAL;

            // when & then
            assertThat(policy.shouldProcess(category)).isFalse();
        }
    }

    @Nested
    @DisplayName("shouldProcess(ContentType, UploadCategory) 테스트")
    class ShouldProcessByBothTest {

        @Test
        @DisplayName("이미지 타입이고 처리 필요 카테고리면 true를 반환한다")
        void shouldReturnTrueWhenBothConditionsMet() {
            // given
            ContentType jpegType = ContentType.of("image/jpeg");
            UploadCategory banner = UploadCategory.BANNER;

            // when & then
            assertThat(policy.shouldProcess(jpegType, banner)).isTrue();
        }

        @Test
        @DisplayName("이미지 타입이지만 처리 불필요 카테고리면 false를 반환한다")
        void shouldReturnFalseWhenCategoryDoesNotRequireProcessing() {
            // given
            ContentType jpegType = ContentType.of("image/jpeg");
            UploadCategory excel = UploadCategory.EXCEL;

            // when & then
            assertThat(policy.shouldProcess(jpegType, excel)).isFalse();
        }

        @Test
        @DisplayName("비이미지 타입이면 카테고리와 무관하게 false를 반환한다")
        void shouldReturnFalseWhenNotImageType() {
            // given
            ContentType pdfType = ContentType.of("application/pdf");
            UploadCategory banner = UploadCategory.BANNER;

            // when & then
            assertThat(policy.shouldProcess(pdfType, banner)).isFalse();
        }
    }

    @Nested
    @DisplayName("getVariantsToGenerate() 테스트")
    class GetVariantsToGenerateTest {

        @Test
        @DisplayName("LARGE, MEDIUM, THUMBNAIL 변형을 반환한다")
        void shouldReturnLargeMediumThumbnailVariants() {
            // when
            List<ImageVariant> variants = policy.getVariantsToGenerate();

            // then
            assertThat(variants).hasSize(3);
            assertThat(variants)
                    .extracting(ImageVariant::type)
                    .containsExactlyInAnyOrder(
                            ImageVariantType.LARGE,
                            ImageVariantType.MEDIUM,
                            ImageVariantType.THUMBNAIL);
        }

        @Test
        @DisplayName("ORIGINAL은 변형 목록에 포함되지 않는다")
        void shouldNotIncludeOriginalInVariants() {
            // when
            List<ImageVariant> variants = policy.getVariantsToGenerate();

            // then
            assertThat(variants)
                    .extracting(ImageVariant::type)
                    .doesNotContain(ImageVariantType.ORIGINAL);
        }
    }

    @Nested
    @DisplayName("getFormatsToGenerate(String) 테스트")
    class GetFormatsToGenerateTest {

        @Test
        @DisplayName("JPG 확장자면 WEBP와 JPEG 포맷을 반환한다")
        void shouldReturnWebpAndJpegForJpgExtension() {
            // when
            List<ImageFormat> formats = policy.getFormatsToGenerate("jpg");

            // then
            assertThat(formats).hasSize(2);
            assertThat(formats)
                    .extracting(ImageFormat::type)
                    .containsExactly(ImageFormatType.WEBP, ImageFormatType.JPEG);
        }

        @Test
        @DisplayName("PNG 확장자면 WEBP와 PNG 포맷을 반환한다")
        void shouldReturnWebpAndPngForPngExtension() {
            // when
            List<ImageFormat> formats = policy.getFormatsToGenerate("png");

            // then
            assertThat(formats).hasSize(2);
            assertThat(formats)
                    .extracting(ImageFormat::type)
                    .containsExactly(ImageFormatType.WEBP, ImageFormatType.PNG);
        }

        @Test
        @DisplayName("WEBP는 항상 첫 번째로 포함된다")
        void shouldAlwaysIncludeWebpAsFirstFormat() {
            // when
            List<ImageFormat> jpgFormats = policy.getFormatsToGenerate("jpg");
            List<ImageFormat> pngFormats = policy.getFormatsToGenerate("png");
            List<ImageFormat> jpegFormats = policy.getFormatsToGenerate("jpeg");

            // then
            assertThat(jpgFormats.get(0).type()).isEqualTo(ImageFormatType.WEBP);
            assertThat(pngFormats.get(0).type()).isEqualTo(ImageFormatType.WEBP);
            assertThat(jpegFormats.get(0).type()).isEqualTo(ImageFormatType.WEBP);
        }
    }
}

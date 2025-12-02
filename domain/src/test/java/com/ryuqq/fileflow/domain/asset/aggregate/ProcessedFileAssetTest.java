package com.ryuqq.fileflow.domain.asset.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormatType;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariantType;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedFileAssetId;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ProcessedFileAsset 단위 테스트")
class ProcessedFileAssetTest {

    @Nested
    @DisplayName("forNew 테스트")
    class ForNewTest {

        @Test
        @DisplayName("유효한 데이터로 ProcessedFileAsset을 생성할 수 있다")
        void shouldCreateProcessedFileAssetWithForNew() {
            // given
            FileAssetId originalAssetId = FileAssetId.forNew();
            ImageVariant variant = ImageVariant.LARGE;
            ImageFormat format = ImageFormat.WEBP;
            FileName fileName = FileName.of("product_large.webp");
            FileSize fileSize = FileSize.of(50000L);
            Integer width = 1200;
            Integer height = 800;
            S3Bucket bucket = S3Bucket.of("test-bucket");
            S3Key s3Key = S3Key.of("processed/product_large.webp");
            Long userId = 1L;
            Long organizationId = 100L;
            Long tenantId = 1000L;

            // when
            ProcessedFileAsset asset = ProcessedFileAsset.forNew(
                    originalAssetId,
                    variant,
                    format,
                    fileName,
                    fileSize,
                    width,
                    height,
                    bucket,
                    s3Key,
                    userId,
                    organizationId,
                    tenantId);

            // then
            assertThat(asset.getId()).isNotNull();
            assertThat(asset.getOriginalAssetId()).isEqualTo(originalAssetId);
            assertThat(asset.getParentAssetId()).isNull();
            assertThat(asset.getVariant()).isEqualTo(variant);
            assertThat(asset.getFormat()).isEqualTo(format);
            assertThat(asset.getFileName()).isEqualTo(fileName);
            assertThat(asset.getFileSize()).isEqualTo(fileSize);
            assertThat(asset.getWidth()).isEqualTo(width);
            assertThat(asset.getHeight()).isEqualTo(height);
            assertThat(asset.getBucket()).isEqualTo(bucket);
            assertThat(asset.getS3Key()).isEqualTo(s3Key);
            assertThat(asset.getUserId()).isEqualTo(userId);
            assertThat(asset.getOrganizationId()).isEqualTo(organizationId);
            assertThat(asset.getTenantId()).isEqualTo(tenantId);
            assertThat(asset.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("forHtmlExtractedImage 테스트")
    class ForHtmlExtractedImageTest {

        @Test
        @DisplayName("HTML에서 추출된 이미지용 ProcessedFileAsset을 생성할 수 있다")
        void shouldCreateHtmlExtractedImageWithForHtmlExtractedImage() {
            // given
            FileAssetId parentAssetId = FileAssetId.forNew();
            FileAssetId originalAssetId = FileAssetId.forNew();
            ImageVariant variant = ImageVariant.MEDIUM;
            ImageFormat format = ImageFormat.JPEG;
            FileName fileName = FileName.of("html_image_medium.jpg");
            FileSize fileSize = FileSize.of(30000L);
            Integer width = 600;
            Integer height = 400;
            S3Bucket bucket = S3Bucket.of("test-bucket");
            S3Key s3Key = S3Key.of("processed/html_image_medium.jpg");
            Long userId = 1L;
            Long organizationId = 100L;
            Long tenantId = 1000L;

            // when
            ProcessedFileAsset asset = ProcessedFileAsset.forHtmlExtractedImage(
                    parentAssetId,
                    originalAssetId,
                    variant,
                    format,
                    fileName,
                    fileSize,
                    width,
                    height,
                    bucket,
                    s3Key,
                    userId,
                    organizationId,
                    tenantId);

            // then
            assertThat(asset.getId()).isNotNull();
            assertThat(asset.getOriginalAssetId()).isEqualTo(originalAssetId);
            assertThat(asset.getParentAssetId()).isEqualTo(parentAssetId);
            assertThat(asset.getVariant()).isEqualTo(variant);
            assertThat(asset.getFormat()).isEqualTo(format);
        }
    }

    @Nested
    @DisplayName("reconstitute 테스트")
    class ReconstituteTest {

        @Test
        @DisplayName("영속성에서 ProcessedFileAsset을 복원할 수 있다")
        void shouldReconstitute() {
            // given
            ProcessedFileAssetId id = ProcessedFileAssetId.forNew();
            FileAssetId originalAssetId = FileAssetId.forNew();
            FileAssetId parentAssetId = FileAssetId.forNew();
            ImageVariant variant = ImageVariant.THUMBNAIL;
            ImageFormat format = ImageFormat.PNG;
            FileName fileName = FileName.of("product_thumb.png");
            FileSize fileSize = FileSize.of(10000L);
            Integer width = 200;
            Integer height = 150;
            S3Bucket bucket = S3Bucket.of("test-bucket");
            S3Key s3Key = S3Key.of("processed/product_thumb.png");
            Long userId = 1L;
            Long organizationId = 100L;
            Long tenantId = 1000L;
            LocalDateTime createdAt = LocalDateTime.of(2025, 12, 1, 10, 0);

            // when
            ProcessedFileAsset asset = ProcessedFileAsset.reconstitute(
                    id,
                    originalAssetId,
                    parentAssetId,
                    variant,
                    format,
                    fileName,
                    fileSize,
                    width,
                    height,
                    bucket,
                    s3Key,
                    userId,
                    organizationId,
                    tenantId,
                    createdAt);

            // then
            assertThat(asset.getId()).isEqualTo(id);
            assertThat(asset.getOriginalAssetId()).isEqualTo(originalAssetId);
            assertThat(asset.getParentAssetId()).isEqualTo(parentAssetId);
            assertThat(asset.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("hasParentAsset 테스트")
    class HasParentAssetTest {

        @Test
        @DisplayName("부모 에셋이 있으면 true를 반환한다")
        void shouldReturnTrueForHasParentAssetWhenParentExists() {
            // given
            FileAssetId parentAssetId = FileAssetId.forNew();
            FileAssetId originalAssetId = FileAssetId.forNew();
            ProcessedFileAsset asset = ProcessedFileAsset.forHtmlExtractedImage(
                    parentAssetId,
                    originalAssetId,
                    ImageVariant.LARGE,
                    ImageFormat.WEBP,
                    FileName.of("test.webp"),
                    FileSize.of(1000L),
                    100,
                    100,
                    S3Bucket.of("bucket"),
                    S3Key.of("key"),
                    1L,
                    1L,
                    1L);

            // when & then
            assertThat(asset.hasParentAsset()).isTrue();
        }

        @Test
        @DisplayName("부모 에셋이 없으면 false를 반환한다")
        void shouldReturnFalseForHasParentAssetWhenNoParent() {
            // given
            ProcessedFileAsset asset = ProcessedFileAsset.forNew(
                    FileAssetId.forNew(),
                    ImageVariant.LARGE,
                    ImageFormat.WEBP,
                    FileName.of("test.webp"),
                    FileSize.of(1000L),
                    100,
                    100,
                    S3Bucket.of("bucket"),
                    S3Key.of("key"),
                    1L,
                    1L,
                    1L);

            // when & then
            assertThat(asset.hasParentAsset()).isFalse();
        }
    }

    @Nested
    @DisplayName("isOriginalVariant 테스트")
    class IsOriginalVariantTest {

        @Test
        @DisplayName("ORIGINAL 버전이면 true를 반환한다")
        void shouldReturnTrueForIsOriginalVariantWhenOriginal() {
            // given
            ProcessedFileAsset asset = ProcessedFileAsset.forNew(
                    FileAssetId.forNew(),
                    ImageVariant.ORIGINAL,
                    ImageFormat.JPEG,
                    FileName.of("test.jpg"),
                    FileSize.of(1000L),
                    100,
                    100,
                    S3Bucket.of("bucket"),
                    S3Key.of("key"),
                    1L,
                    1L,
                    1L);

            // when & then
            assertThat(asset.isOriginalVariant()).isTrue();
        }

        @Test
        @DisplayName("ORIGINAL이 아닌 버전이면 false를 반환한다")
        void shouldReturnFalseForIsOriginalVariantWhenNotOriginal() {
            // given
            ProcessedFileAsset asset = ProcessedFileAsset.forNew(
                    FileAssetId.forNew(),
                    ImageVariant.LARGE,
                    ImageFormat.JPEG,
                    FileName.of("test_large.jpg"),
                    FileSize.of(1000L),
                    100,
                    100,
                    S3Bucket.of("bucket"),
                    S3Key.of("key"),
                    1L,
                    1L,
                    1L);

            // when & then
            assertThat(asset.isOriginalVariant()).isFalse();
        }
    }

    @Nested
    @DisplayName("isWebpFormat 테스트")
    class IsWebpFormatTest {

        @Test
        @DisplayName("WebP 포맷이면 true를 반환한다")
        void shouldReturnTrueForIsWebpFormatWhenWebp() {
            // given
            ProcessedFileAsset asset = ProcessedFileAsset.forNew(
                    FileAssetId.forNew(),
                    ImageVariant.LARGE,
                    ImageFormat.WEBP,
                    FileName.of("test.webp"),
                    FileSize.of(1000L),
                    100,
                    100,
                    S3Bucket.of("bucket"),
                    S3Key.of("key"),
                    1L,
                    1L,
                    1L);

            // when & then
            assertThat(asset.isWebpFormat()).isTrue();
        }

        @Test
        @DisplayName("WebP가 아닌 포맷이면 false를 반환한다")
        void shouldReturnFalseForIsWebpFormatWhenNotWebp() {
            // given
            ProcessedFileAsset asset = ProcessedFileAsset.forNew(
                    FileAssetId.forNew(),
                    ImageVariant.LARGE,
                    ImageFormat.JPEG,
                    FileName.of("test.jpg"),
                    FileSize.of(1000L),
                    100,
                    100,
                    S3Bucket.of("bucket"),
                    S3Key.of("key"),
                    1L,
                    1L,
                    1L);

            // when & then
            assertThat(asset.isWebpFormat()).isFalse();
        }
    }
}

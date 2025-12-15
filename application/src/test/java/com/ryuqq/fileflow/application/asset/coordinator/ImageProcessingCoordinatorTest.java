package com.ryuqq.fileflow.application.asset.coordinator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.asset.component.ImageDownloader;
import com.ryuqq.fileflow.application.asset.component.ImageMetadataExtractor;
import com.ryuqq.fileflow.application.asset.dto.processor.UploadedImage;
import com.ryuqq.fileflow.application.asset.processor.ImageResizingProcessor;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.fixture.FileAssetFixture;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageResizingSpec;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedImageInfo;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageProcessingCoordinator 테스트")
class ImageProcessingCoordinatorTest {

    @Mock private ImageDownloader imageDownloader;
    @Mock private ImageMetadataExtractor metadataExtractor;
    @Mock private ImageResizingProcessor resizingProcessor;

    private ImageProcessingCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator =
                new ImageProcessingCoordinator(imageDownloader, metadataExtractor, resizingProcessor);
    }

    @Nested
    @DisplayName("process 메서드")
    class ProcessTest {

        @Test
        @DisplayName("정상적으로 이미지를 처리하고 결과를 반환한다")
        void shouldProcessImageSuccessfully() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            byte[] imageData = "test image data".getBytes();
            List<UploadedImage> expectedResults = List.of(createUploadedImage());

            given(imageDownloader.download(fileAsset)).willReturn(imageData);
            doNothing().when(metadataExtractor).extractAndUpdateDimension(fileAsset, imageData);
            given(resizingProcessor.processAndUpload(fileAsset, imageData)).willReturn(expectedResults);

            // when
            List<UploadedImage> results = coordinator.process(fileAsset);

            // then
            assertThat(results).isNotNull();
            assertThat(results).hasSize(1);
            verify(imageDownloader).download(fileAsset);
            verify(metadataExtractor).extractAndUpdateDimension(fileAsset, imageData);
            verify(resizingProcessor).processAndUpload(fileAsset, imageData);
        }

        @Test
        @DisplayName("처리 순서가 다운로드 → 메타데이터 → 리사이징 순서로 수행된다")
        void shouldProcessInCorrectOrder() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            byte[] imageData = "test image data".getBytes();
            List<UploadedImage> expectedResults = Collections.emptyList();

            given(imageDownloader.download(fileAsset)).willReturn(imageData);
            doNothing().when(metadataExtractor).extractAndUpdateDimension(fileAsset, imageData);
            given(resizingProcessor.processAndUpload(fileAsset, imageData)).willReturn(expectedResults);

            // when
            coordinator.process(fileAsset);

            // then
            InOrder inOrder = inOrder(imageDownloader, metadataExtractor, resizingProcessor);
            inOrder.verify(imageDownloader).download(fileAsset);
            inOrder.verify(metadataExtractor).extractAndUpdateDimension(fileAsset, imageData);
            inOrder.verify(resizingProcessor).processAndUpload(fileAsset, imageData);
        }

        @Test
        @DisplayName("다운로드 실패 시 예외를 던진다")
        void shouldThrowExceptionWhenDownloadFails() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            RuntimeException downloadException = new RuntimeException("S3 다운로드 실패");

            given(imageDownloader.download(fileAsset)).willThrow(downloadException);

            // when & then
            assertThatThrownBy(() -> coordinator.process(fileAsset))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("S3 다운로드 실패");

            verify(imageDownloader).download(fileAsset);
            verify(metadataExtractor, never()).extractAndUpdateDimension(any(), any());
            verify(resizingProcessor, never()).processAndUpload(any(), any());
        }

        @Test
        @DisplayName("메타데이터 추출 실패 시 예외를 던진다")
        void shouldThrowExceptionWhenMetadataExtractionFails() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            byte[] imageData = "test image data".getBytes();
            RuntimeException extractionException = new RuntimeException("메타데이터 추출 실패");

            given(imageDownloader.download(fileAsset)).willReturn(imageData);
            doThrow(extractionException)
                    .when(metadataExtractor)
                    .extractAndUpdateDimension(fileAsset, imageData);

            // when & then
            assertThatThrownBy(() -> coordinator.process(fileAsset))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("메타데이터 추출 실패");

            verify(imageDownloader).download(fileAsset);
            verify(metadataExtractor).extractAndUpdateDimension(fileAsset, imageData);
            verify(resizingProcessor, never()).processAndUpload(any(), any());
        }

        @Test
        @DisplayName("리사이징 실패 시 예외를 던진다")
        void shouldThrowExceptionWhenResizingFails() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            byte[] imageData = "test image data".getBytes();
            RuntimeException resizingException = new RuntimeException("리사이징 실패");

            given(imageDownloader.download(fileAsset)).willReturn(imageData);
            doNothing().when(metadataExtractor).extractAndUpdateDimension(fileAsset, imageData);
            given(resizingProcessor.processAndUpload(fileAsset, imageData)).willThrow(resizingException);

            // when & then
            assertThatThrownBy(() -> coordinator.process(fileAsset))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("리사이징 실패");

            verify(imageDownloader).download(fileAsset);
            verify(metadataExtractor).extractAndUpdateDimension(fileAsset, imageData);
            verify(resizingProcessor).processAndUpload(fileAsset, imageData);
        }

        @Test
        @DisplayName("여러 리사이징 결과를 반환할 수 있다")
        void shouldReturnMultipleResults() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            byte[] imageData = "test image data".getBytes();
            List<UploadedImage> expectedResults =
                    List.of(
                            createUploadedImage(ImageVariant.ORIGINAL),
                            createUploadedImage(ImageVariant.LARGE),
                            createUploadedImage(ImageVariant.MEDIUM),
                            createUploadedImage(ImageVariant.THUMBNAIL));

            given(imageDownloader.download(fileAsset)).willReturn(imageData);
            doNothing().when(metadataExtractor).extractAndUpdateDimension(fileAsset, imageData);
            given(resizingProcessor.processAndUpload(fileAsset, imageData)).willReturn(expectedResults);

            // when
            List<UploadedImage> results = coordinator.process(fileAsset);

            // then
            assertThat(results).hasSize(4);
        }

        @Test
        @DisplayName("빈 결과를 반환할 수 있다")
        void shouldHandleEmptyResults() {
            // given
            FileAsset fileAsset = FileAssetFixture.defaultFileAsset();
            byte[] imageData = "test image data".getBytes();
            List<UploadedImage> emptyResults = Collections.emptyList();

            given(imageDownloader.download(fileAsset)).willReturn(imageData);
            doNothing().when(metadataExtractor).extractAndUpdateDimension(fileAsset, imageData);
            given(resizingProcessor.processAndUpload(fileAsset, imageData)).willReturn(emptyResults);

            // when
            List<UploadedImage> results = coordinator.process(fileAsset);

            // then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("PROCESSING 상태의 FileAsset도 처리할 수 있다")
        void shouldProcessProcessingFileAsset() {
            // given
            FileAsset fileAsset = FileAssetFixture.processingFileAsset();
            byte[] imageData = "test image data".getBytes();
            List<UploadedImage> expectedResults = List.of(createUploadedImage());

            given(imageDownloader.download(fileAsset)).willReturn(imageData);
            doNothing().when(metadataExtractor).extractAndUpdateDimension(fileAsset, imageData);
            given(resizingProcessor.processAndUpload(fileAsset, imageData)).willReturn(expectedResults);

            // when
            List<UploadedImage> results = coordinator.process(fileAsset);

            // then
            assertThat(results).hasSize(1);
        }
    }

    // ==================== Helper Methods ====================

    private UploadedImage createUploadedImage() {
        return createUploadedImage(ImageVariant.ORIGINAL);
    }

    private UploadedImage createUploadedImage(ImageVariant variant) {
        ImageResizingSpec spec = new ImageResizingSpec(variant, ImageFormat.WEBP);
        ProcessedImageInfo imageInfo = ProcessedImageInfo.of(spec, 1024L);

        return UploadedImage.of(
                imageInfo,
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/image.webp"),
                new ETag("test-etag"),
                null);
    }
}

package com.ryuqq.fileflow.application.transform.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.dto.result.ImageProcessingResult;
import com.ryuqq.fileflow.application.transform.dto.result.ImageTransformResult;
import com.ryuqq.fileflow.application.transform.manager.client.FileStorageDownloadManager;
import com.ryuqq.fileflow.application.transform.manager.client.FileStorageUploadManager;
import com.ryuqq.fileflow.application.transform.manager.client.ImageProcessingManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageTransformFacade 단위 테스트")
class ImageTransformFacadeTest {

    @InjectMocks private ImageTransformFacade sut;
    @Mock private FileStorageDownloadManager fileStorageDownloadManager;
    @Mock private ImageProcessingManager imageProcessingManager;
    @Mock private FileStorageUploadManager fileStorageUploadManager;

    @Nested
    @DisplayName("transform 메서드")
    class TransformTest {

        @Test
        @DisplayName("성공: S3 다운로드 → 이미지 처리 → S3 업로드 후 성공 결과를 반환한다")
        void transform_Success_ReturnsSuccessResult() {
            // given
            Asset sourceAsset = AssetFixture.anAsset();
            TransformRequest request = TransformRequestFixture.aResizeRequest();

            byte[] sourceBytes = "source-image-bytes".getBytes();
            given(fileStorageDownloadManager.download(sourceAsset.bucket(), sourceAsset.s3Key()))
                    .willReturn(sourceBytes);

            byte[] processedBytes = "processed-image-bytes".getBytes();
            ImageProcessingResult processed =
                    new ImageProcessingResult(processedBytes, 800, 600, "image/png", "png");
            given(imageProcessingManager.process(sourceBytes, request.type(), request.params()))
                    .willReturn(processed);

            String expectedEtag = "\"etag-result\"";
            given(
                            fileStorageUploadManager.upload(
                                    eq(sourceAsset.bucket()),
                                    anyString(),
                                    eq(processedBytes),
                                    eq("image/png")))
                    .willReturn(expectedEtag);

            // when
            ImageTransformResult result = sut.transform(sourceAsset, request);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.s3Key()).startsWith("transformed/resize/");
            assertThat(result.s3Key()).endsWith(".png");
            assertThat(result.bucket()).isEqualTo(sourceAsset.bucket());
            assertThat(result.fileInfo().contentType()).isEqualTo("image/png");
            assertThat(result.fileInfo().etag()).isEqualTo(expectedEtag);
            assertThat(result.dimension().width()).isEqualTo(800);
            assertThat(result.dimension().height()).isEqualTo(600);
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("실패: S3 다운로드 실패 시 실패 결과를 반환한다")
        void transform_DownloadFails_ReturnsFailureResult() {
            // given
            Asset sourceAsset = AssetFixture.anAsset();
            TransformRequest request = TransformRequestFixture.aResizeRequest();

            given(fileStorageDownloadManager.download(sourceAsset.bucket(), sourceAsset.s3Key()))
                    .willThrow(new RuntimeException("S3 download failed"));

            // when
            ImageTransformResult result = sut.transform(sourceAsset, request);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("S3 download failed");
            then(imageProcessingManager).shouldHaveNoInteractions();
            then(fileStorageUploadManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패: 이미지 처리 실패 시 실패 결과를 반환한다")
        void transform_ProcessingFails_ReturnsFailureResult() {
            // given
            Asset sourceAsset = AssetFixture.anAsset();
            TransformRequest request = TransformRequestFixture.aResizeRequest();

            byte[] sourceBytes = "source-image-bytes".getBytes();
            given(fileStorageDownloadManager.download(sourceAsset.bucket(), sourceAsset.s3Key()))
                    .willReturn(sourceBytes);
            given(imageProcessingManager.process(sourceBytes, request.type(), request.params()))
                    .willThrow(new RuntimeException("Image processing failed"));

            // when
            ImageTransformResult result = sut.transform(sourceAsset, request);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("Image processing failed");
            then(fileStorageUploadManager).shouldHaveNoInteractions();
        }
    }
}

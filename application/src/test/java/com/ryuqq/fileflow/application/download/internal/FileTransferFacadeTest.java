package com.ryuqq.fileflow.application.download.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.dto.response.FileDownloadResult;
import com.ryuqq.fileflow.application.download.dto.response.RawDownloadedFile;
import com.ryuqq.fileflow.application.download.manager.client.FileDownloadManager;
import com.ryuqq.fileflow.application.download.manager.client.FileStorageUploadManager;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
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
@DisplayName("FileTransferFacade 단위 테스트")
class FileTransferFacadeTest {

    @InjectMocks private FileTransferFacade sut;
    @Mock private FileDownloadManager fileDownloadManager;
    @Mock private FileStorageUploadManager fileStorageUploadManager;

    @Nested
    @DisplayName("transfer 메서드")
    class TransferTest {

        @Test
        @DisplayName("성공: 다운로드 후 업로드하여 성공 결과를 반환한다")
        void transfer_Success_ReturnsSuccessResult() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();
            byte[] data = "fake-image-data".getBytes();
            RawDownloadedFile rawFile = RawDownloadedFile.of("image.jpg", "image/jpeg", data);
            String expectedEtag = "\"abc123\"";

            given(fileDownloadManager.download(downloadTask.sourceUrlValue())).willReturn(rawFile);
            given(
                            fileStorageUploadManager.upload(
                                    downloadTask.bucket(),
                                    downloadTask.s3Key(),
                                    data,
                                    "image/jpeg"))
                    .willReturn(expectedEtag);

            // when
            FileDownloadResult result = sut.transfer(downloadTask);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.fileName()).isEqualTo("image.jpg");
            assertThat(result.contentType()).isEqualTo("image/jpeg");
            assertThat(result.fileSize()).isEqualTo(data.length);
            assertThat(result.etag()).isEqualTo(expectedEtag);
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("실패: 다운로드 실패 시 실패 결과를 반환한다")
        void transfer_DownloadFails_ReturnsFailureResult() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();

            given(fileDownloadManager.download(downloadTask.sourceUrlValue()))
                    .willThrow(new RuntimeException("Connection timeout"));

            // when
            FileDownloadResult result = sut.transfer(downloadTask);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("Connection timeout");
            then(fileStorageUploadManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패: 업로드 실패 시 실패 결과를 반환한다")
        void transfer_UploadFails_ReturnsFailureResult() {
            // given
            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();
            byte[] data = "fake-image-data".getBytes();
            RawDownloadedFile rawFile = RawDownloadedFile.of("image.jpg", "image/jpeg", data);

            given(fileDownloadManager.download(downloadTask.sourceUrlValue())).willReturn(rawFile);
            given(
                            fileStorageUploadManager.upload(
                                    downloadTask.bucket(),
                                    downloadTask.s3Key(),
                                    data,
                                    "image/jpeg"))
                    .willThrow(new RuntimeException("S3 upload failed"));

            // when
            FileDownloadResult result = sut.transfer(downloadTask);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("S3 upload failed");
        }
    }
}

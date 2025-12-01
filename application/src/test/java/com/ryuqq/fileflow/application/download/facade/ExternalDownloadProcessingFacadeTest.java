package com.ryuqq.fileflow.application.download.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.download.assembler.ExternalDownloadAssembler;
import com.ryuqq.fileflow.application.download.dto.DownloadResult;
import com.ryuqq.fileflow.application.download.dto.response.S3UploadResponse;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadManager;
import com.ryuqq.fileflow.application.download.port.out.client.HttpDownloadPort;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadFileCreatedEvent;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadFixture;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalDownloadProcessingFacade 테스트")
class ExternalDownloadProcessingFacadeTest {

    @Mock private ExternalDownloadQueryPort queryPort;

    @Mock private ExternalDownloadManager externalDownloadManager;

    @Mock private ExternalDownloadAssembler assembler;

    @Mock private HttpDownloadPort httpDownloadPort;

    @Mock private S3ClientPort s3ClientPort;

    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private ClockHolder clockHolder;

    @InjectMocks private ExternalDownloadProcessingFacade facade;

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-11-26T12:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        given(clockHolder.getClock()).willReturn(FIXED_CLOCK);
    }

    @Nested
    @DisplayName("process 메서드")
    class ProcessTest {

        @Test
        @DisplayName("외부 다운로드 전체 프로세스가 정상적으로 실행된다")
        void shouldProcessSuccessfully() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            ExternalDownload download = ExternalDownloadFixture.withId(downloadId).build();

            byte[] content = "test-image-content".getBytes();
            DownloadResult downloadResult =
                    new DownloadResult(content, "image/jpeg", content.length);

            S3Key s3Key = S3Key.of("customer/test-file.jpg");
            S3UploadResponse uploadResponse =
                    new S3UploadResponse(
                            s3Key,
                            FileName.of("test-file.jpg"),
                            ContentType.of("image/jpeg"),
                            content);

            ETag etag = ETag.of("abc123etag");

            given(queryPort.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));
            given(httpDownloadPort.download(download.getSourceUrl())).willReturn(downloadResult);
            given(assembler.toS3UploadResponse(download, downloadResult))
                    .willReturn(uploadResponse);
            given(
                            s3ClientPort.putObject(
                                    download.getS3Bucket(),
                                    uploadResponse.s3Key(),
                                    uploadResponse.contentType(),
                                    uploadResponse.content()))
                    .willReturn(etag);

            // when
            facade.process(downloadId);

            // then
            verify(queryPort).findById(ExternalDownloadId.of(downloadId));
            verify(httpDownloadPort).download(download.getSourceUrl());
            verify(assembler).toS3UploadResponse(download, downloadResult);
            verify(s3ClientPort)
                    .putObject(
                            download.getS3Bucket(),
                            uploadResponse.s3Key(),
                            uploadResponse.contentType(),
                            uploadResponse.content());
            verify(externalDownloadManager, org.mockito.Mockito.times(2)).save(download);
            verify(eventPublisher).publishEvent(any(ExternalDownloadFileCreatedEvent.class));
        }

        @Test
        @DisplayName("1단계: ExternalDownload를 조회하고 PROCESSING 상태로 전환한다")
        void shouldStartProcessing() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            ExternalDownload download =
                    ExternalDownloadFixture.withId(downloadId)
                            .status(ExternalDownloadStatus.PENDING)
                            .build();

            byte[] content = "test-content".getBytes();
            DownloadResult downloadResult =
                    new DownloadResult(content, "image/png", content.length);

            S3UploadResponse uploadResponse =
                    new S3UploadResponse(
                            S3Key.of("customer/test.png"),
                            FileName.of("test.png"),
                            ContentType.of("image/png"),
                            content);

            ETag etag = ETag.of("etag123");

            given(queryPort.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));
            given(httpDownloadPort.download(any())).willReturn(downloadResult);
            given(assembler.toS3UploadResponse(any(), any())).willReturn(uploadResponse);
            given(s3ClientPort.putObject(any(), any(), any(), any())).willReturn(etag);

            // when
            facade.process(downloadId);

            // then
            ArgumentCaptor<ExternalDownload> captor =
                    ArgumentCaptor.forClass(ExternalDownload.class);
            verify(externalDownloadManager, org.mockito.Mockito.times(2)).save(captor.capture());

            ExternalDownload savedDownload = captor.getAllValues().get(1);
            assertThat(savedDownload.getStatus()).isEqualTo(ExternalDownloadStatus.COMPLETED);
        }

        @Test
        @DisplayName("존재하지 않는 ExternalDownload 조회 시 예외가 발생한다")
        void shouldThrowExceptionWhenDownloadNotFound() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000999";
            given(queryPort.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> facade.process(downloadId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ExternalDownload not found");

            verify(httpDownloadPort, never()).download(any());
            verify(s3ClientPort, never()).putObject(any(), any(), any(), any());
        }

        @Test
        @DisplayName("5단계: 완료 처리 시 도메인 이벤트가 발행되고 클리어된다")
        void shouldPublishEventAndClearAfterCompletion() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            ExternalDownload download = ExternalDownloadFixture.withId(downloadId).build();

            byte[] content = "image-data".getBytes();
            DownloadResult downloadResult =
                    new DownloadResult(content, "image/gif", content.length);

            S3UploadResponse uploadResponse =
                    new S3UploadResponse(
                            S3Key.of("customer/test.gif"),
                            FileName.of("test.gif"),
                            ContentType.of("image/gif"),
                            content);

            ETag etag = ETag.of("etag-gif");

            given(queryPort.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));
            given(httpDownloadPort.download(download.getSourceUrl())).willReturn(downloadResult);
            given(assembler.toS3UploadResponse(download, downloadResult))
                    .willReturn(uploadResponse);
            given(s3ClientPort.putObject(any(), any(), any(), any())).willReturn(etag);

            // when
            facade.process(downloadId);

            // then
            verify(eventPublisher).publishEvent(any(ExternalDownloadFileCreatedEvent.class));
            assertThat(download.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("2-4단계: HTTP 다운로드 및 S3 업로드가 순차적으로 실행된다")
        void shouldExecuteDownloadAndUploadSequentially() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            ExternalDownload download = ExternalDownloadFixture.withId(downloadId).build();

            byte[] content = "test-bytes".getBytes();
            DownloadResult downloadResult =
                    new DownloadResult(content, "image/webp", content.length);

            S3Key s3Key = S3Key.of("customer/image.webp");
            S3UploadResponse uploadResponse =
                    new S3UploadResponse(
                            s3Key,
                            FileName.of("image.webp"),
                            ContentType.of("image/webp"),
                            content);

            ETag etag = ETag.of("webp-etag");

            given(queryPort.findById(eq(ExternalDownloadId.of(downloadId))))
                    .willReturn(Optional.of(download));
            given(httpDownloadPort.download(download.getSourceUrl())).willReturn(downloadResult);
            given(assembler.toS3UploadResponse(download, downloadResult))
                    .willReturn(uploadResponse);
            given(
                            s3ClientPort.putObject(
                                    download.getS3Bucket(),
                                    s3Key,
                                    uploadResponse.contentType(),
                                    content))
                    .willReturn(etag);

            // when
            facade.process(downloadId);

            // then
            verify(httpDownloadPort).download(download.getSourceUrl());
            verify(assembler).toS3UploadResponse(download, downloadResult);
            verify(s3ClientPort)
                    .putObject(
                            download.getS3Bucket(), s3Key, uploadResponse.contentType(), content);
        }
    }
}

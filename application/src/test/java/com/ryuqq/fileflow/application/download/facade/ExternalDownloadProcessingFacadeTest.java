package com.ryuqq.fileflow.application.download.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.common.config.TransactionEventRegistry;
import com.ryuqq.fileflow.application.download.dto.DownloadResult;
import com.ryuqq.fileflow.application.download.dto.response.S3UploadResponse;
import com.ryuqq.fileflow.application.download.factory.command.ExternalDownloadCommandFactory;
import com.ryuqq.fileflow.application.download.manager.command.ExternalDownloadTransactionManager;
import com.ryuqq.fileflow.application.download.manager.query.ExternalDownloadReadManager;
import com.ryuqq.fileflow.application.download.port.out.client.DownloadS3ClientPort;
import com.ryuqq.fileflow.application.download.port.out.client.HttpDownloadPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalDownloadProcessingFacade 테스트")
class ExternalDownloadProcessingFacadeTest {

    @Mock private ExternalDownloadReadManager externalDownloadReadManager;

    @Mock private ExternalDownloadTransactionManager externalDownloadTransactionManager;

    @Mock private HttpDownloadPort httpDownloadPort;

    @Mock private DownloadS3ClientPort downloadS3ClientPort;

    @Mock private TransactionEventRegistry transactionEventRegistry;

    @Mock private ExternalDownloadCommandFactory commandFactory;

    private ExternalDownloadProcessingFacade facade;

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2025-11-26T12:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        facade =
                new ExternalDownloadProcessingFacade(
                        externalDownloadReadManager,
                        externalDownloadTransactionManager,
                        httpDownloadPort,
                        downloadS3ClientPort,
                        transactionEventRegistry,
                        commandFactory);
        given(commandFactory.getClock()).willReturn(FIXED_CLOCK);
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

            given(externalDownloadReadManager.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));
            given(externalDownloadTransactionManager.persist(any(ExternalDownload.class)))
                    .willReturn(download);
            given(
                            externalDownloadTransactionManager.persistWithEvents(
                                    any(ExternalDownload.class),
                                    any(TransactionEventRegistry.class)))
                    .willReturn(download);
            given(httpDownloadPort.download(download.getSourceUrl())).willReturn(downloadResult);
            given(commandFactory.createS3UploadResponse(any(), eq(downloadResult)))
                    .willReturn(uploadResponse);
            given(
                            downloadS3ClientPort.putObject(
                                    any(),
                                    eq(uploadResponse.s3Key()),
                                    eq(uploadResponse.contentType()),
                                    eq(uploadResponse.content())))
                    .willReturn(etag);

            // when
            facade.process(downloadId);

            // then
            verify(externalDownloadReadManager).findById(ExternalDownloadId.of(downloadId));
            verify(httpDownloadPort).download(any());
            verify(commandFactory).createS3UploadResponse(any(), eq(downloadResult));
            verify(downloadS3ClientPort)
                    .putObject(
                            any(),
                            eq(uploadResponse.s3Key()),
                            eq(uploadResponse.contentType()),
                            eq(uploadResponse.content()));
            // startProcessing에서 persist 1회, completeProcessing에서 persistWithEvents 1회
            verify(externalDownloadTransactionManager).persist(any(ExternalDownload.class));
            verify(externalDownloadTransactionManager)
                    .persistWithEvents(any(ExternalDownload.class), eq(transactionEventRegistry));
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

            given(externalDownloadReadManager.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));
            given(externalDownloadTransactionManager.persist(any(ExternalDownload.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(
                            externalDownloadTransactionManager.persistWithEvents(
                                    any(ExternalDownload.class),
                                    any(TransactionEventRegistry.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(httpDownloadPort.download(any())).willReturn(downloadResult);
            given(commandFactory.createS3UploadResponse(any(), any())).willReturn(uploadResponse);
            given(downloadS3ClientPort.putObject(any(), any(), any(), any())).willReturn(etag);

            // when
            facade.process(downloadId);

            // then
            ArgumentCaptor<ExternalDownload> captor =
                    ArgumentCaptor.forClass(ExternalDownload.class);
            // startProcessing에서 persist, completeProcessing에서 persistWithEvents
            verify(externalDownloadTransactionManager).persist(captor.capture());
            verify(externalDownloadTransactionManager)
                    .persistWithEvents(captor.capture(), eq(transactionEventRegistry));

            // 두 번째 캡처가 완료 상태
            ExternalDownload savedDownload = captor.getAllValues().get(1);
            assertThat(savedDownload.getStatus()).isEqualTo(ExternalDownloadStatus.COMPLETED);
        }

        @Test
        @DisplayName("존재하지 않는 ExternalDownload 조회 시 예외가 발생한다")
        void shouldThrowExceptionWhenDownloadNotFound() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000999";
            given(externalDownloadReadManager.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> facade.process(downloadId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ExternalDownload not found");

            verify(httpDownloadPort, never()).download(any());
            verify(downloadS3ClientPort, never()).putObject(any(), any(), any(), any());
        }

        @Test
        @DisplayName("5단계: 완료 처리 시 persistWithEvents를 통해 이벤트가 등록된다")
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

            given(externalDownloadReadManager.findById(ExternalDownloadId.of(downloadId)))
                    .willReturn(Optional.of(download));
            given(externalDownloadTransactionManager.persist(any(ExternalDownload.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(
                            externalDownloadTransactionManager.persistWithEvents(
                                    any(ExternalDownload.class),
                                    any(TransactionEventRegistry.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(httpDownloadPort.download(any())).willReturn(downloadResult);
            given(commandFactory.createS3UploadResponse(any(), any())).willReturn(uploadResponse);
            given(downloadS3ClientPort.putObject(any(), any(), any(), any())).willReturn(etag);

            // when
            facade.process(downloadId);

            // then
            // persistWithEvents 호출 시 내부에서 이벤트 등록 + 클리어가 수행됨
            verify(externalDownloadTransactionManager)
                    .persistWithEvents(any(ExternalDownload.class), eq(transactionEventRegistry));
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

            given(externalDownloadReadManager.findById(eq(ExternalDownloadId.of(downloadId))))
                    .willReturn(Optional.of(download));
            given(externalDownloadTransactionManager.persist(any(ExternalDownload.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(
                            externalDownloadTransactionManager.persistWithEvents(
                                    any(ExternalDownload.class),
                                    any(TransactionEventRegistry.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(httpDownloadPort.download(any())).willReturn(downloadResult);
            given(commandFactory.createS3UploadResponse(any(), any())).willReturn(uploadResponse);
            given(downloadS3ClientPort.putObject(any(), any(), any(), any())).willReturn(etag);

            // when
            facade.process(downloadId);

            // then
            verify(httpDownloadPort).download(any());
            verify(commandFactory).createS3UploadResponse(any(), any());
            verify(downloadS3ClientPort).putObject(any(), eq(s3Key), any(), any());
        }
    }
}

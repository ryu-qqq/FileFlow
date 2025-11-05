package com.ryuqq.fileflow.application.download.service;

import com.ryuqq.fileflow.application.download.dto.command.StartExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.fixture.ExternalDownloadResponseFixture;
import com.ryuqq.fileflow.application.download.fixture.StartExternalDownloadCommandFixture;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadCommandPort;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxCommandPort;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.application.upload.manager.UploadSessionManager;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.IdempotencyKey;
import com.ryuqq.fileflow.domain.download.exception.DownloadNotFoundException;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadFixture;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadOutboxFixture;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.fixture.UploadSessionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * StartExternalDownloadService 단위 테스트
 *
 * <p>외부 URL 다운로드 시작 UseCase를 검증합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ Port Mock을 활용한 단위 테스트</li>
 *   <li>✅ TestFixture 사용 (CommandFixture, DomainFixture)</li>
 *   <li>✅ BDD 스타일(Given-When-Then) 테스트</li>
 *   <li>✅ CQRS 분리 검증 (CommandPort, QueryPort)</li>
 *   <li>✅ 멱등성 검증</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StartExternalDownloadService 단위 테스트")
class StartExternalDownloadServiceTest {

    @Mock
    private UploadSessionManager uploadSessionManager;

    @Mock
    private ExternalDownloadCommandPort downloadCommandPort;

    @Mock
    private ExternalDownloadQueryPort downloadQueryPort;

    @Mock
    private ExternalDownloadOutboxCommandPort outboxCommandPort;

    @Mock
    private ExternalDownloadOutboxQueryPort outboxQueryPort;

    @InjectMocks
    private StartExternalDownloadService service;

    @Nested
    @DisplayName("Happy Path 테스트")
    class HappyPathTests {

        @Test
        @DisplayName("execute_Success - 정상 다운로드 시작")
        void execute_Success() {
            // Given
            StartExternalDownloadCommand command = StartExternalDownloadCommandFixture.create();
            UploadSession session = UploadSessionFixture.createSingle();
            UploadSession savedSession = UploadSessionFixture.reconstitute(
                session.getIdValue(),
                session.getSessionKey(),
                session.getTenantId(),
                session.getFileName(),
                session.getFileSize(),
                session.getUploadType(),
                session.getStorageKey(),
                session.getStatus(),
                null,
                null,
                session.getCreatedAt(),
                session.getUpdatedAt(),
                null,
                null
            );

            ExternalDownload download = ExternalDownloadFixture.createNew();
            ExternalDownload savedDownload = ExternalDownloadFixture.reconstituteDefault(
                download.getIdValue() != null ? download.getIdValue() : 67890L
            );

            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.create();

            given(outboxQueryPort.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.empty());
            given(uploadSessionManager.save(any(UploadSession.class)))
                .willReturn(savedSession);
            given(downloadCommandPort.save(any(ExternalDownload.class)))
                .willReturn(savedDownload);
            given(outboxCommandPort.save(any(ExternalDownloadOutbox.class)))
                .willReturn(outbox);

            // When
            ExternalDownloadResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.downloadId()).isNotNull();
            assertThat(response.uploadSessionId()).isEqualTo(savedSession.getIdValue());
            assertThat(response.status()).isEqualTo("INIT");

            verify(outboxQueryPort).findByIdempotencyKey(command.idempotencyKey());
            verify(uploadSessionManager).save(any(UploadSession.class));
            verify(downloadCommandPort).save(any(ExternalDownload.class));
            verify(outboxCommandPort).save(any(ExternalDownloadOutbox.class));
        }

        @Test
        @DisplayName("execute_Success_WithIdempotencyKey - 멱등키로 기존 요청 반환")
        void execute_Success_WithIdempotencyKey() {
            // Given
            StartExternalDownloadCommand command = StartExternalDownloadCommandFixture.create();
            ExternalDownloadOutbox existingOutbox = ExternalDownloadOutboxFixture.create();
            ExternalDownload existingDownload = ExternalDownloadFixture.reconstituteDefault(67890L);
            UploadSession existingSession = UploadSessionFixture.reconstitute(
                existingOutbox.getUploadSessionIdValue(),
                UploadSessionFixture.createSingle().getSessionKey(),
                UploadSessionFixture.createSingle().getTenantId(),
                UploadSessionFixture.createSingle().getFileName(),
                UploadSessionFixture.createSingle().getFileSize(),
                UploadSessionFixture.createSingle().getUploadType(),
                UploadSessionFixture.createSingle().getStorageKey(),
                UploadSessionFixture.createSingle().getStatus(),
                null,
                null,
                UploadSessionFixture.createSingle().getCreatedAt(),
                UploadSessionFixture.createSingle().getUpdatedAt(),
                null,
                null
            );

            given(outboxQueryPort.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.of(existingOutbox));
            given(downloadQueryPort.findById(existingOutbox.getDownloadIdValue()))
                .willReturn(Optional.of(existingDownload));
            given(uploadSessionManager.findById(existingOutbox.getUploadSessionIdValue()))
                .willReturn(Optional.of(existingSession));

            // When
            ExternalDownloadResponse response = service.execute(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.downloadId()).isEqualTo(existingDownload.getIdValue());

            verify(outboxQueryPort).findByIdempotencyKey(command.idempotencyKey());
            verify(downloadQueryPort).findById(existingOutbox.getDownloadIdValue());
            verify(uploadSessionManager).findById(existingOutbox.getUploadSessionIdValue());
            verify(downloadCommandPort, never()).save(any(ExternalDownload.class));
            verify(outboxCommandPort, never()).save(any(ExternalDownloadOutbox.class));
        }
    }

    @Nested
    @DisplayName("Exception Cases 테스트")
    class ExceptionCasesTests {

        @Test
        @DisplayName("execute_ThrowsException_WhenDownloadNotFound - 멱등키로 찾은 Outbox의 Download가 없을 때")
        void execute_ThrowsException_WhenDownloadNotFound() {
            // Given
            StartExternalDownloadCommand command = StartExternalDownloadCommandFixture.create();
            ExternalDownloadOutbox existingOutbox = ExternalDownloadOutboxFixture.create();

            given(outboxQueryPort.findByIdempotencyKey(command.idempotencyKey()))
                .willReturn(Optional.of(existingOutbox));
            given(downloadQueryPort.findById(existingOutbox.getDownloadIdValue()))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Download not found for outbox");

            verify(downloadQueryPort).findById(existingOutbox.getDownloadIdValue());
            verify(downloadCommandPort, never()).save(any(ExternalDownload.class));
        }
    }

    @Nested
    @DisplayName("CQRS 분리 검증")
    class CQRSValidationTests {

        @Test
        @DisplayName("CommandPort는 쓰기만 사용 - save() 호출 확인")
        void commandPortShouldOnlyUseWriteMethods() {
            // Given
            StartExternalDownloadCommand command = StartExternalDownloadCommandFixture.create();
            UploadSession session = UploadSessionFixture.createSingle();
            ExternalDownload download = ExternalDownloadFixture.reconstituteDefault(67890L);
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.create();

            given(outboxQueryPort.findByIdempotencyKey(any()))
                .willReturn(Optional.empty());
            given(uploadSessionManager.save(any()))
                .willReturn(session);
            given(downloadCommandPort.save(any()))
                .willReturn(download);
            given(outboxCommandPort.save(any()))
                .willReturn(outbox);

            // When
            service.execute(command);

            // Then - CommandPort는 save()만 호출
            verify(downloadCommandPort).save(any(ExternalDownload.class));
            verify(downloadCommandPort, never()).delete(any());
            verify(downloadQueryPort, never()).findById(any()); // QueryPort는 조회만
        }

        @Test
        @DisplayName("QueryPort는 읽기만 사용 - findByIdempotencyKey() 호출 확인")
        void queryPortShouldOnlyUseReadMethods() {
            // Given
            StartExternalDownloadCommand command = StartExternalDownloadCommandFixture.create();
            UploadSession session = UploadSessionFixture.createSingle();
            ExternalDownload download = ExternalDownloadFixture.reconstituteDefault(67890L);
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.create();

            given(outboxQueryPort.findByIdempotencyKey(any()))
                .willReturn(Optional.empty());
            given(uploadSessionManager.save(any()))
                .willReturn(session);
            given(downloadCommandPort.save(any()))
                .willReturn(download);
            given(outboxCommandPort.save(any()))
                .willReturn(outbox);

            // When
            service.execute(command);

            // Then - QueryPort는 조회만 호출
            verify(outboxQueryPort).findByIdempotencyKey(any());
            verify(outboxQueryPort, never()).findById(any()); // QueryPort는 조회만
        }
    }
}


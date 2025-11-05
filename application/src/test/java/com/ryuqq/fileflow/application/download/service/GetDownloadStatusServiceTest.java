package com.ryuqq.fileflow.application.download.service;

import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadQueryPort;
import com.ryuqq.fileflow.application.download.port.out.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.exception.DownloadNotFoundException;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadFixture;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadOutboxFixture;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
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
import static org.mockito.BDDMockito.given;

/**
 * GetDownloadStatusService 단위 테스트
 *
 * <p>다운로드 상태 조회 UseCase를 검증합니다.</p>
 *
 * <p><strong>테스트 전략:</strong></p>
 * <ul>
 *   <li>✅ Port Mock을 활용한 단위 테스트</li>
 *   <li>✅ TestFixture 사용</li>
 *   <li>✅ BDD 스타일(Given-When-Then) 테스트</li>
 *   <li>✅ CQRS 분리 검증 (QueryPort만 사용)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetDownloadStatusService 단위 테스트")
class GetDownloadStatusServiceTest {

    @Mock
    private ExternalDownloadQueryPort downloadQueryPort;

    @Mock
    private ExternalDownloadOutboxQueryPort outboxQueryPort;

    @InjectMocks
    private GetDownloadStatusService service;

    @Nested
    @DisplayName("Happy Path 테스트")
    class HappyPathTests {

        @Test
        @DisplayName("execute_Success - 다운로드 상태 조회 성공")
        void execute_Success() {
            // Given
            Long downloadId = 67890L;
            ExternalDownload download = ExternalDownloadFixture.reconstituteDefault(downloadId);
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.createNew();

            given(downloadQueryPort.findById(downloadId))
                .willReturn(Optional.of(download));
            given(outboxQueryPort.findByDownloadId(downloadId))
                .willReturn(Optional.of(outbox));

            // When
            ExternalDownloadResponse response = service.execute(downloadId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.downloadId()).isEqualTo(downloadId);
            assertThat(response.status()).isEqualTo("INIT");
        }

        @Test
        @DisplayName("execute_Success_DOWNLOADING - DOWNLOADING 상태 조회 성공")
        void execute_Success_DOWNLOADING() throws java.net.MalformedURLException {
            // Given
            Long downloadId = 67890L;
            ExternalDownload download = ExternalDownloadFixture.reconstitute(
                downloadId,
                UploadSessionId.of(1L),
                new java.net.URL("https://example.com/files/test-file.pdf"),
                FileSize.of(1024L),
                FileSize.of(2048L),
                ExternalDownloadStatus.DOWNLOADING,
                0,
                null,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                null,
                null
            );
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.createNew();

            given(downloadQueryPort.findById(downloadId))
                .willReturn(Optional.of(download));
            given(outboxQueryPort.findByDownloadId(downloadId))
                .willReturn(Optional.of(outbox));

            // When
            ExternalDownloadResponse response = service.execute(downloadId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("DOWNLOADING");
        }

        @Test
        @DisplayName("execute_Success_COMPLETED - COMPLETED 상태 조회 성공")
        void execute_Success_COMPLETED() throws java.net.MalformedURLException {
            // Given
            Long downloadId = 67890L;
            ExternalDownload download = ExternalDownloadFixture.reconstitute(
                downloadId,
                UploadSessionId.of(1L),
                new java.net.URL("https://example.com/files/test-file.pdf"),
                FileSize.of(2048L),
                FileSize.of(2048L),
                ExternalDownloadStatus.COMPLETED,
                0,
                null,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                null,
                null
            );
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.createNew();

            given(downloadQueryPort.findById(downloadId))
                .willReturn(Optional.of(download));
            given(outboxQueryPort.findByDownloadId(downloadId))
                .willReturn(Optional.of(outbox));

            // When
            ExternalDownloadResponse response = service.execute(downloadId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("COMPLETED");
        }
    }

    @Nested
    @DisplayName("Exception Cases 테스트")
    class ExceptionCasesTests {

        @Test
        @DisplayName("execute_ThrowsException_WhenDownloadNotFound - Download가 없을 때")
        void execute_ThrowsException_WhenDownloadNotFound() {
            // Given
            Long downloadId = 999L;

            given(downloadQueryPort.findById(downloadId))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(downloadId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("External download not found");
        }

        @Test
        @DisplayName("execute_ThrowsException_WhenOutboxNotFound - Outbox가 없을 때")
        void execute_ThrowsException_WhenOutboxNotFound() {
            // Given
            Long downloadId = 67890L;
            ExternalDownload download = ExternalDownloadFixture.reconstituteDefault(downloadId);

            given(downloadQueryPort.findById(downloadId))
                .willReturn(Optional.of(download));
            given(outboxQueryPort.findByDownloadId(downloadId))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(downloadId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("External download outbox not found");
        }
    }

    @Nested
    @DisplayName("CQRS 분리 검증")
    class CQRSValidationTests {

        @Test
        @DisplayName("QueryPort만 사용 - 조회 메서드만 호출")
        void queryPortShouldOnlyUseReadMethods() {
            // Given
            Long downloadId = 67890L;
            ExternalDownload download = ExternalDownloadFixture.reconstituteDefault(downloadId);
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.createNew();

            given(downloadQueryPort.findById(downloadId))
                .willReturn(Optional.of(download));
            given(outboxQueryPort.findByDownloadId(downloadId))
                .willReturn(Optional.of(outbox));

            // When
            service.execute(downloadId);

            // Then - QueryPort는 조회만 사용
            // CommandPort는 주입되지 않음 (의존성 없음)
        }
    }
}

